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
package io.openliberty.boost.maven.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractLibertyMojo extends MojoSupport {

    protected String libertyServerName = "BoostServer";

    protected String libertyMavenPluginGroupId = "net.wasdev.wlp.maven.plugins";
    protected String libertyMavenPluginArtifactId = "liberty-maven-plugin";

    @Parameter(defaultValue = "2.6.2", readonly = true)
    protected String libertyMavenPluginVersion;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected String projectBuildDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Component
    protected BuildPluginManager pluginManager;

    @Parameter
    protected ArtifactItem runtimeArtifact;
    
    protected static String SERVER_PORT = "server.port";
    
    protected static String SERVER_PORT_ORIG = "server.port.orig";
    
    protected static String savedPort = null;


    protected Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(libertyMavenPluginGroupId), artifactId(libertyMavenPluginArtifactId),
                version(libertyMavenPluginVersion));
    }

    protected Element getRuntimeArtifactElement() {
        return element(name("assemblyArtifact"), element(name("groupId"), runtimeArtifact.getGroupId()),
                element(name("artifactId"), runtimeArtifact.getArtifactId()),
                element(name("version"), runtimeArtifact.getVersion()),
                element(name("type"), runtimeArtifact.getType()));
    }

    protected ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment(project, session, pluginManager);
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

    @Override
    public void execute() throws MojoExecutionException {
        createDefaultRuntimeArtifactIfNeeded();
    }
    /*
     * Check for environment variable being set for server port. 
     * if set update bootstrap.properties to add the server port
     */
    protected void setServerPort() {
        Properties sysProps = session.getSystemProperties();
        String serverPort = sysProps.getProperty(SERVER_PORT);
        Properties bootStrap = getBootStrapProperties();
        // was a special server port was requested?
        if(serverPort != null) {
        	    getLog().info("Using custom server http port = " + serverPort);
            	savedPort = bootStrap.getProperty(SERVER_PORT);
            	// Save port for later unless original already set
            	if(bootStrap.getProperty(SERVER_PORT_ORIG) == null)
            	    bootStrap.setProperty(SERVER_PORT_ORIG, savedPort);
            	// Set new port
            	bootStrap.setProperty(SERVER_PORT, serverPort);
            	// Update bootstrap.properties
            	setBootStrapProperties(bootStrap);
        // Check to see if we used custom port last time 
        // defined by the presence of the server.port.default property
        } else {            
        	    resetServerPort(bootStrap);
        }

    }
    
    protected void resetServerPort() {
    	      Properties bootStrap = getBootStrapProperties();
    	      resetServerPort(bootStrap);
    }
    
    protected void resetServerPort(Properties bootStrap) {
        if(!bootStrap.isEmpty() ) {
   	       String defaultPort = bootStrap.getProperty(SERVER_PORT_ORIG);
   	       if(defaultPort != null) {
   	    	        bootStrap.setProperty(SERVER_PORT, defaultPort);
   	    	        bootStrap.remove(SERVER_PORT_ORIG);
   	    	        setBootStrapProperties(bootStrap);
   	       }
        }  //TODO can we delete bootstrap.properties file if the 
           // properties object is empty??
    }
    protected Properties getBootStrapProperties() {
    	       Properties bootStrap = new Properties();
    	       String libertyBootStrapProps = projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName + "/bootstrap.properties";
           try {
           	   FileInputStream in = new FileInputStream(libertyBootStrapProps);
               bootStrap.load(in);
               in.close();
           } catch(IOException ioe ) {
           	   getLog().error("Error opening bootstrap.properties: " + ioe.toString());
           }
           return bootStrap;
    }
    
    protected void setBootStrapProperties(Properties bootStrap) {
	       String libertyBootStrapProps = projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName + "/bootstrap.properties";
           try {
   	           FileOutputStream out = new FileOutputStream(libertyBootStrapProps);
   	           bootStrap.store(out, null);
   	           out.close();
            } catch(IOException ioe) {
   	           getLog().error("Error restoring bootstrap.properties: " + ioe.toString());
            }            	    	      
    }


}
