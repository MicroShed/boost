/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.liberty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.*;

import io.openliberty.boost.BoostException;
import io.openliberty.boost.BoosterPackConfigurator;
import io.openliberty.boost.BoosterPacksParent;
import io.openliberty.boost.utils.BoostLogger;
import io.openliberty.boost.utils.BoostUtil;
import io.openliberty.boost.utils.LibertyServerConfigGenerator;
import io.openliberty.boost.utils.MavenProjectUtil;
import io.openliberty.boost.utils.SpringBootUtil;
import io.openliberty.boost.utils.ConfigConstants;
import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Packages an existing application into a Liberty executable jar so that the
 * application can be run from the command line using java -jar. (This is for
 * the 'jar' packaging type).
 *
 */
@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LibertyPackageMojo extends AbstractLibertyMojo {

    String springBootVersion = null;

    BoosterPacksParent boosterParent;
    List<BoosterPackConfigurator> boosterFeatures = null;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        springBootVersion = MavenProjectUtil.findSpringBootVersion(project);

        boosterParent = new BoosterPacksParent();

        createServer();

        /**
         * Whether the packaged Liberty Uber JAR will be the project artifact. This
         * should be the case in Spring Boot scenarios since Spring Boot developers
         * expect a runnable JAR.
         */
        boolean attach;

        /**
         * Use the classifier to determine whether we need to set the Liberty Uber JAR
         * as the project artifact, and add Spring-Boot-Version to the manifest
         */
        String springBootClassifier = null;

        if (MavenProjectUtil.isNotNullOrEmpty(springBootVersion)) { // Dealing with a spring boot app
            springBootClassifier = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil
                    .getSpringBootMavenPluginClassifier(project, getLog());

            // Check if we need to attach based on the classifier configuration
            if (MavenProjectUtil.isNotNullOrEmpty(springBootClassifier)) {
                attach = false;
            } else {
                attach = true;
            }

            File springBootUberJar = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil.getSpringBootUberJAR(project,
                    getLog());
            validateSpringBootUberJAR(springBootUberJar);
            copySpringBootUberJar(springBootUberJar, attach); // Only copy back if we need to overwrite the project
                                                              // artifact
            generateServerConfigSpringBoot();
            installMissingFeatures();
            installApp("spring-boot-project");

            if (springBootUberJar != null) {
                // Create the Liberty Uber JAR from the Spring Boot Uber JAR in place
                createUberJar(springBootUberJar.getAbsolutePath(), attach);
            } else {
                // The Spring Boot Uber JAR was already replaced with the Liberty Uber JAR (this
                // is a re-execution in the non-classifier scenario)
                createUberJar(null, attach);
            }

            if (!MavenProjectUtil.isNotNullOrEmpty(springBootClassifier)) {
                // If necessary, add the manifest to prevent Spring Boot from repackaging again
                addSpringBootVersionToManifest(springBootVersion);
            }
        } else { // Dealing with an EE based app
            attach = false;
            boosterFeatures = getBoosterConfigsFromDependencies(project);
            generateServerXMLJ2EE(boosterFeatures);
            installMissingFeatures();
            installApp("project");

            createUberJar(null, attach);
        }
    }

    /**
     * Manipulate the manifest so that Spring Boot will not attempt to repackage a
     * Liberty Uber JAR.
     * 
     * @param springBootVersion
     * @throws MojoExecutionException
     */
    private void addSpringBootVersionToManifest(String springBootVersion) throws MojoExecutionException {
        File artifact = project.getArtifact().getFile();
        if (BoostUtil.isLibertyJar(artifact, BoostLogger.getInstance())) {
            try {
                SpringBootUtil.addSpringBootVersionToManifest(artifact, springBootVersion, BoostLogger.getInstance());
            } catch (BoostException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            getLog().debug("Added Spring Boot Version to manifest to prevent repackaging of Liberty Uber JAR.");
        } else {
            throw new MojoExecutionException("Project artifact is not a Liberty Uber JAR. This should never happen.");
        }
    }

    /**
     * Check that we either have a Liberty Uber JAR (in which case this is a
     * re-execution) or a Spring Boot Uber JAR (from which we will create a Liberty
     * Uber JAR) when we begin the packaging for Spring Boot projects.
     * 
     * @throws MojoExecutionException
     */
    private void validateSpringBootUberJAR(File springBootUberJar) throws MojoExecutionException {
        if (!BoostUtil.isLibertyJar(project.getArtifact().getFile(), BoostLogger.getInstance())
                && springBootUberJar == null) {
            throw new MojoExecutionException(
                    "A valid Spring Boot Uber JAR was not found. Run spring-boot:repackage and try again.");
        }
    }

    /**
     * Copy the Spring Boot uber JAR back as the project artifact, only if Spring
     * Boot didn't create it already
     * 
     * @throws MojoExecutionException
     */
    private void copySpringBootUberJar(File springBootUberJar, boolean attach) throws MojoExecutionException {
        try {
            File springBootUberJarCopy = null;
            if (springBootUberJar != null) { // Only copy the Uber JAR if it is valid
                springBootUberJarCopy = SpringBootUtil.copySpringBootUberJar(springBootUberJar,
                        BoostLogger.getInstance());
            }

            if (springBootUberJarCopy == null) { // Copy didn't happen
                if (attach) { // If we are replacing the project artifact, then copy back the Spring Boot Uber
                              // JAR so we can thin and package it again
                    File springJar = new File(
                            SpringBootUtil.getBoostedSpringBootUberJarPath(project.getArtifact().getFile()));
                    if (net.wasdev.wlp.common.plugins.util.SpringBootUtil.isSpringBootUberJar(springJar)) {
                        getLog().info("Copying back Spring Boot Uber JAR as project artifact.");
                        FileUtils.copyFile(springJar, project.getArtifact().getFile());
                    }
                }
            } else {
                getLog().info("Copied Spring Boot Uber JAR to " + springBootUberJarCopy.getCanonicalPath());
            }
        } catch (PluginExecutionException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void generateServerConfigSpringBoot() throws MojoExecutionException {
        
        // Get Spring Boot server properties
        Properties springBootAppProps = new Properties();

        try {
            springBootAppProps = SpringBootUtil.getSpringBootServerProperties(projectBuildDir);

        } catch (IOException e) {
            
            throw new MojoExecutionException("Unable to read properties from Spring Boot application", e);
        }
        
        try {
            // Generate Liberty configuration
            LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);
        
        
            // Find and add appropriate springBoot features
            List<String> featuresNeededForSpringBootApp = SpringBootUtil.getLibertyFeaturesForSpringBoot(springBootVersion,
                    getSpringBootStarters(), BoostLogger.getInstance());
            serverConfig.addFeatures(featuresNeededForSpringBootApp);
    
            // Configure SSL and endpoints
            if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE)) {
                
                Map<String, String> keystoreProperties = new HashMap<String, String>();
                Map<String, String> keyProperties = new HashMap<String, String>();
                
                // For each Spring Boot keystore property, add an entry to the keystore map which maps our 
                // Liberty keystore attribute to a bootstrap variable with the same name as the Spring Boot property.
                // The Spring Boot properties will then be added to the server's bootstrap.properties file. 
                keystoreProperties.put(ConfigConstants.KEYSTORE_LOCATION, "${" + SpringBootUtil.SERVER_SSL_KEYSTORE + "}");
                
                if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_PASSWORD)) {
                    keystoreProperties.put(ConfigConstants.KEYSTORE_PASSWORD, "${" + SpringBootUtil.SERVER_SSL_KEYSTORE_PASSWORD + "}");
                }
                if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_TYPE)) {
                    keystoreProperties.put(ConfigConstants.KEYSTORE_TYPE, "${" + SpringBootUtil.SERVER_SSL_KEYSTORE_TYPE + "}");
                }
                if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_PROVIDER)) {
                    keystoreProperties.put(ConfigConstants.KEYSTORE_PROVIDER, "${" + SpringBootUtil.SERVER_SSL_KEYSTORE_PROVIDER + "}");
                }
                
                // Add any key properties to the separate key map.
                if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEY_PASSWORD)) {
                    keyProperties.put(ConfigConstants.KEY_PASSWORD, "${" + SpringBootUtil.SERVER_SSL_KEY_PASSWORD + "}");
                } 
                if (springBootAppProps.containsKey(SpringBootUtil.SERVER_SSL_KEY_ALIAS)) {
                    keyProperties.put(ConfigConstants.KEY_NAME, "${" + SpringBootUtil.SERVER_SSL_KEY_ALIAS + "}");
                }
               
                // Create keystore element in server.xml and endpoint with http disabled. 
                serverConfig.addKeystore(keystoreProperties, keyProperties);
                serverConfig.addHttpEndpoint("${" + SpringBootUtil.SERVER_ADDRESS + "}", "-1", "${" + SpringBootUtil.SERVER_PORT + "}"); 
                serverConfig.addFeature(ConfigConstants.TRANSPORT_SECURITY_10);
                
                // Since the keystore for the Spring Boot app is created manually and already exists, 
                // if it is specified on the classpath, we need to copy it to the Liberty server. Otherwise, 
                // we can just reference the external location without needing to copy the file.
                String keystoreFile = springBootAppProps.getProperty(SpringBootUtil.SERVER_SSL_KEYSTORE);
                      
                if (keystoreFile.contains("classpath:")){
                    
                    // Keystore is in resources directory of spring boot application
                    keystoreFile = keystoreFile.replace("classpath:", "");
                    springBootAppProps.put(SpringBootUtil.SERVER_SSL_KEYSTORE, keystoreFile);
                    
                    // Copy keystore to Liberty
                    Path springBootKeystorePath = Paths.get(projectBuildDir + "/classes/" + keystoreFile);
                    Path libertyKeystorePath = Paths.get(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName + "/resources/security/" + keystoreFile);
                    Path libertySecurityPath = Paths.get(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName + "/resources/security");
                    
                    Files.createDirectories(libertySecurityPath);
                    Files.copy(springBootKeystorePath, libertyKeystorePath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                } 
                
            } else {
                serverConfig.addHttpEndpoint("${" + SpringBootUtil.SERVER_ADDRESS + "}", "${" + SpringBootUtil.SERVER_PORT + "}", null);
    
            }
            
            // Add properties to bootstrap properties
            serverConfig.addBootstrapProperties(springBootAppProps);
            
            serverConfig.writeToServer();
            
        } catch (TransformerException | IOException | ParserConfigurationException e) {
            
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private List<BoosterPackConfigurator> getBoosterConfigsFromDependencies(MavenProject proj) {

        List<String> listOfDependencies = new ArrayList<String>();
        getLog().debug("getBoostCfg: first lets see what dependencies we find");

        for (Artifact artifact : project.getArtifacts()) {
            getLog().debug("getBoostCfg: found this, adding as a string -> " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId());
            listOfDependencies.add(artifact.getGroupId() + ":" + artifact.getArtifactId());
        }

        return boosterParent.mapDependenciesToFeatureList(listOfDependencies);
    }

    /**
     * Generate a server.xml based on the found EE dependencies
     * 
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    private void generateServerXMLJ2EE(List<BoosterPackConfigurator> boosterConfigurators) throws MojoExecutionException {

        try {
            LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);

            // Add any other Liberty features needed depending on the spring boot
            // starters defined
            List<String> boosterFeatureNames = getBoosterFeatureNames(boosterConfigurators);
            serverConfig.addFeatures(boosterFeatureNames);
            serverConfig.addConfigForFeatures(boosterConfigurators);
        
            // Write server.xml to Liberty server config directory
            serverConfig.writeToServer();
            
        } catch (TransformerException | IOException | ParserConfigurationException e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }

    }

    /**
     * 
     * @param boosterConfigurators
     * @return
     */
    private List<String> getBoosterFeatureNames(List<BoosterPackConfigurator> boosterConfigurators) {
        List<String> featureStrings = new ArrayList<String>();
        for (BoosterPackConfigurator bpconfig : boosterConfigurators) {
            featureStrings.add(bpconfig.getFeatureString());
        }

        return featureStrings;
    }

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createServer() throws MojoExecutionException {

        executeMojo(getPlugin(), goal("create-server"),
                configuration(element(name("serverName"), libertyServerName), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-app goal.
     */
    private void installApp(String installAppPackagesVal) throws MojoExecutionException {
        executeMojo(getPlugin(), goal("install-apps"),
                configuration(element(name("installAppPackages"), installAppPackagesVal),
                        element(name("serverName"), libertyServerName), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-feature goal.
     *
     * This will install any missing features defined in the server.xml or
     * configDropins.
     *
     */
    private void installMissingFeatures() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("install-feature"), configuration(element(name("serverName"), libertyServerName),
                element(name("features"), element(name("acceptLicense"), "false"))), getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable Liberty
     * JAR
     * 
     * @param packageFilePath
     *            the Spring Boot Uber JAR file path, whose contents will be
     *            replaced by the Liberty Uber JAR
     * @param attach
     *            whether or not to make the packaged server the project artifact
     * @throws MojoExecutionException
     */
    private void createUberJar(String packageFilePath, boolean attach) throws MojoExecutionException {
        if (packageFilePath == null) {
            packageFilePath = "";
        }
        executeMojo(getPlugin(), goal("package-server"),
                configuration(element(name("isInstall"), "false"), element(name("include"), "minify,runnable"),
                        element(name("attach"), Boolean.toString(attach)),
                        element(name("packageFile"), packageFilePath), element(name("serverName"), libertyServerName)),
                getExecutionEnvironment());
    }

    /**
     * Get all dependencies with "spring-boot-starter-*" as the artifactId. These
     * dependencies will be used to determine which additional Liberty features need
     * to be enabled.
     * 
     */
    private List<String> getSpringBootStarters() {

        List<String> springBootStarters = new ArrayList<String>();

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            if (art.getArtifactId().contains("spring-boot-starter")) {
                springBootStarters.add(art.getArtifactId());
            }
        }

        return springBootStarters;
    }

}
