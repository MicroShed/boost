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
package boost.project;
 
import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Package a Spring Boot application with Liberty
 *
 */
@Mojo( name = "package-app", defaultPhase = LifecyclePhase.PACKAGE )
public class PackageSpringBootAppWithLibertyMojo extends AbstractMojo
{

    String libertyServerName = "BoostServer";

	@Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;
    
	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException
    {
    
    	installOpenLiberty();
    	
    	createServer();
    	
    	thinSpringBootApp();
               
        installApp();
		
		// Add springBoot-1.5 feature to server configDropins
		LibertyFeatureConfigGenerator featureConfig = new LibertyFeatureConfigGenerator();
		featureConfig.addFeature("springBoot-1.5");
		featureConfig.writeToServer(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);
		
		installMissingFeatures();
		
		createUberJar();
		
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the install-server goal
     *
     */
    private void installOpenLiberty() throws MojoExecutionException {
    
    	executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("install-server"),
    		configuration(
        		element(name("serverName"), libertyServerName),
        		element(name("assemblyArtifact"),
        			element(name("groupId"), "io.openliberty"),
        			element(name("artifactId"), "openliberty-runtime"),
        			element(name("version"), "RELEASE"),
        			element(name("type"), "zip")
        		)
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
	
	/**
	 * Invoke the liberty-maven-plugin to run the create-server goal
	 *
	 */
	private void createServer() throws MojoExecutionException {
	
		executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("create-server"),
    		configuration(
        		element(name("serverName"), libertyServerName),
        		element(name("assemblyArtifact"),
        			element(name("groupId"), "io.openliberty"),
        			element(name("artifactId"), "openliberty-runtime"),
        			element(name("version"), "RELEASE"),
        			element(name("type"), "zip")
        		)
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
	
	/**
	 * Invoke the liberty-maven-plugin to run the thin goal
	 *
	 */
	private void thinSpringBootApp() throws MojoExecutionException { 
    	executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("thin"),
    		configuration(
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
	
	/**
	 * Invoke the liberty-maven-plugin to run the install-app goal.
	 *
	 *
	 */
	private void installApp() throws MojoExecutionException {
	
        executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("install-apps"),
    		configuration(
    		    element(name("installAppPackages"), "thin-project"),
        		element(name("serverName"), libertyServerName),
        		element(name("assemblyArtifact"),
        			element(name("groupId"), "io.openliberty"),
        			element(name("artifactId"), "openliberty-runtime"),
        			element(name("version"), "RELEASE"),
        			element(name("type"), "zip")
        		)
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
	
	/**
	 * Invoke the liberty-maven-plugin to run the install-feature goal.
	 *
	 * This will install any missing features defined in the server.xml 
	 * or configDropins.
	 *
	 */
	private void installMissingFeatures() throws MojoExecutionException {
	
		executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("install-feature"),
    		configuration(
    		    element(name("serverName"), libertyServerName)
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
	
	/**
	 * Invoke the liberty-maven-plugin to run the package-server goal
	 *
	 */
	private void createUberJar() throws MojoExecutionException {
	
		// Package server into runnable jar
		executeMojo(
    		plugin(
        		groupId("net.wasdev.wlp.maven.plugins"),
        		artifactId("liberty-maven-plugin"),
        		version("2.6-SNAPSHOT")
    		),
    		goal("package-server"),
    		configuration(
    		    element(name("include"), "runnable"),
    		    element(name("serverName"), libertyServerName)
    		),
    		executionEnvironment(
        		mavenProject,
        		mavenSession,
        		pluginManager
    		)
		);
	}
}
