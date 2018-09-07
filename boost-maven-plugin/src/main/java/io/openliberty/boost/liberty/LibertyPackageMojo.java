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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.*;

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import io.openliberty.boost.BoostException;
import io.openliberty.boost.BoosterPackConfigurator;
import io.openliberty.boost.BoosterPacksParent;
import io.openliberty.boost.utils.LibertyServerConfigGenerator;
import io.openliberty.boost.utils.SpringBootProjectUtils;
import io.openliberty.boost.utils.SpringBootUtil;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Packages an existing application into a Liberty executable jar so that the
 * application can be run from the command line using java -jar.  (This is for the 'jar' packaging type).
 *
 */
@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LibertyPackageMojo extends AbstractLibertyMojo {

	BoosterPacksParent boosterParent;
	List<BoosterPackConfigurator> boosterFeatures = null;
    
    SpringBootUtil springBootUtil = new SpringBootUtil();

    public void execute() throws MojoExecutionException {        
        String springBootVersion = SpringBootProjectUtils.findSpringBootVersion(project);

        boosterParent = new BoosterPacksParent();    	
        
        createDefaultRuntimeArtifactIfNeeded();

        try {
            // Copy the Spring Boot uber JAR back as the project artifact, only if Spring Boot didn't create it already
            if(!springBootUtil.copySpringBootUberJar(project.getArtifact().getFile())) {
                File springJar = new File(springBootUtil.getSpringBootUberJarPath(project.getArtifact().getFile()));
                if(springJar.exists()) {
                    getLog().debug("Copying back Spring Boot Uber JAR as project artifact.");
                    FileUtils.copyFile(springJar, project.getArtifact().getFile());
                }
            }
        } catch (BoostException | IOException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        }

        createServer();
        
		try {
			if (springBootVersion != null) {
				// dealing with a spring boot app
				installApp("spring-boot-project");
				generateServerXML();
				generateBootstrapProps();
			} else {
				// dealing with an EE based app
				installApp("project");
				boosterFeatures = getBoosterConfigsFromDependencies(project);
				generateServerXMLJ2EE(boosterFeatures);
			}
        } catch ( TransformerException | ParserConfigurationException e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server", e);
        }

        installMissingFeatures();
        
        createUberJar();
        
        // Add the manifest to prevent Spring Boot from repackaging again
        try {
            springBootUtil.addSpringBootVersionToManifest(project.getArtifact().getFile(), springBootVersion);
        } catch (BoostException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Create default runtime artifact, if one has not been provided by the user
     */
    private void createDefaultRuntimeArtifactIfNeeded() {
        if (runtimeArtifact == null) {
            runtimeArtifact = new ArtifactItem();
            runtimeArtifact.setGroupId("io.openliberty");
            runtimeArtifact.setArtifactId("openliberty-runtime");
            runtimeArtifact.setVersion("RELEASE");
            runtimeArtifact.setType("zip");
        }
    }

    private void generateBootstrapProps() throws MojoExecutionException {

        Properties springBootProps = new Properties();

        try {
            springBootProps = SpringBootProjectUtils.getSpringBootServerProperties(projectBuildDir);

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
        List<String> featuresNeededForSpringBootApp = SpringBootProjectUtils.getLibertyFeaturesNeeded(project, getLog());
        serverConfig.addFeatures(featuresNeededForSpringBootApp);

        serverConfig.addHttpEndpoint(null, "${server.port}", null);
        
        // Write server.xml to Liberty server config directory
        serverConfig.writeToServer(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);
    }

	private List<BoosterPackConfigurator> getBoosterConfigsFromDependencies(MavenProject proj) {
		
    	List<String> listOfDependencies = new ArrayList<String>();
		getLog().debug("getBoostCfg: first lets see what dependencies we find");

		for (Artifact artifact : project.getArtifacts()) {
			getLog().debug("getBoostCfg: found this, adding as a string -> " + artifact.getGroupId() + ":" + artifact.getArtifactId());
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
		for (BoosterPackConfigurator bpconfig : boosterConfigurators){
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
    
        executeMojo(
            getPlugin(),
            goal("install-apps"),
            configuration(
                element(name("installAppPackages"), installAppPackagesVal),
                element(name("serverName"), libertyServerName),
                getRuntimeArtifactElement()
            ),
            getExecutionEnvironment()
        );
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
                element(name("features"), element(name("acceptLicense"), "true")
        )), getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to run the package-server goal
     *
     */
    private void createUberJar() throws MojoExecutionException {
        // Package server into runnable jar
        executeMojo(getPlugin(),
            goal("package-server"), configuration(element(name("isInstall"), "false"),
                    element(name("include"), "minify,runnable"),
                    element(name("attach"), "true"),
                    element(name("serverName"), libertyServerName)
            ),
        getExecutionEnvironment());
    }

}
