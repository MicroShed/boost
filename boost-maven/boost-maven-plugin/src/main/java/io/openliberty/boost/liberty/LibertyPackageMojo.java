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
import java.util.ArrayList;
import java.util.List;
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

        try {
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
                generateServerXML();
                generateBootstrapProps();
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
        } catch (TransformerException | ParserConfigurationException e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
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

    private void generateBootstrapProps() throws MojoExecutionException {

        Properties springBootProps = new Properties();

        try {
            springBootProps = SpringBootUtil.getSpringBootServerProperties(projectBuildDir);

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read properties from Spring Boot application", e);
        }

        // Write properties to bootstrap.properties
        OutputStream output = null;

        try {
            output = new FileOutputStream(
                    projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName + "/bootstrap.properties");
            springBootProps.store(output, null);

        } catch (IOException io) {
            throw new MojoExecutionException("Unable to write properties to bootstrap.properties file", io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException io_finally) {
                    throw new MojoExecutionException("Unable to write properties to bootstrap.properties file",
                            io_finally);
                }
            }
        }
    }

    /**
     * Generate a server.xml based on the Spring version and dependencies
     * 
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    private void generateServerXML() throws TransformerException, ParserConfigurationException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator();

        // Find and add appropriate springBoot features
        List<String> featuresNeededForSpringBootApp = SpringBootUtil.getLibertyFeaturesForSpringBoot(springBootVersion,
                getSpringBootStarters(), BoostLogger.getInstance());
        serverConfig.addFeatures(featuresNeededForSpringBootApp);

        serverConfig.addHttpEndpoint("${server.address}", "${server.port}", null);

        // Write server.xml to Liberty server config directory
        serverConfig.writeToServer(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);
    }

    private List<BoosterPackConfigurator> getBoosterConfigsFromDependencies(MavenProject proj) {

        List<String> listOfDependencies = new ArrayList<String>();
        getLog().debug("Processing project for dependencies.");

        for (Artifact artifact : project.getArtifacts()) {
            getLog().debug("Found dependency while processing project: " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId() + ":" + artifact.getVersion());
            listOfDependencies.add(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion());
        }

        return boosterParent.mapDependenciesToFeatureList(listOfDependencies);
    }

    /**
     * Generate a server.xml based on the found EE dependencies
     * 
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    private void generateServerXMLJ2EE(List<BoosterPackConfigurator> boosterConfigurators)
            throws TransformerException, ParserConfigurationException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator();

        // Add any other Liberty features needed depending on the spring boot
        // starters defined
        List<String> boosterFeatureNames = getBoosterFeatureNames(boosterConfigurators);
        serverConfig.addFeatures(boosterFeatureNames);
        serverConfig.addConfigForFeatures(boosterConfigurators);

        // Write server.xml to Liberty server config directory
        serverConfig.writeToServer(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);

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
