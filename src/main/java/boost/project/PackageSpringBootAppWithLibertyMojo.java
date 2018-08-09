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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.*;

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Package a Spring Boot application with Liberty
 *
 */
@Mojo( name = "package-app", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageSpringBootAppWithLibertyMojo extends AbstractMojo implements ConfigConstants
{
    String libertyServerName = "BoostServer";

    String libertyMavenPluginGroupId = "net.wasdev.wlp.maven.plugins";
    String libertyMavenPluginArtifactId = "liberty-maven-plugin";
    String libertyMavenPluginVersion = "2.5.1-SNAPSHOT";
    
    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;
    
    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}")
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;
    
    @Parameter
    private ArtifactItem runtimeArtifact;

    public void execute() throws MojoExecutionException
    {                
        createDefaultRuntimeArtifactIfNeeded();
        
        installOpenLiberty();
        
        createServer();
        
        thinSpringBootApp();
               
        installApp();
        
        try {
            generateServerXML();
        } catch ( TransformerException e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server", e);
        } catch ( ParserConfigurationException e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server", e);
        }        
        
        installMissingFeatures();
    
        createUberJar();
        
    }
    
    /**
     * Create default runtime artifact, if one has not been provided by the user
     */
    private void createDefaultRuntimeArtifactIfNeeded() {
        if(runtimeArtifact == null) {
            runtimeArtifact = new ArtifactItem();
            runtimeArtifact.setGroupId("io.openliberty");
            runtimeArtifact.setArtifactId("openliberty-runtime");
            runtimeArtifact.setVersion("RELEASE");
            runtimeArtifact.setType("zip");
        }
    }
    
    /**
     * Generate a server.xml based on the Spring version and dependencies
     * @throws TransformerException 
     * @throws ParserConfigurationException 
     */
    private void generateServerXML() throws TransformerException, ParserConfigurationException {
        
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator();
        
        // Add appropriate springBoot feature 
        String springBootVersion = findSpringBootVersion(mavenProject);

        if (springBootVersion != null) {
            
            String springBootFeature = null;
            
            if (springBootVersion.startsWith("1.")) {
                springBootFeature = SPRING_BOOT_15;
            } else if (springBootVersion.startsWith("2.")) {
                springBootFeature = SPRING_BOOT_20;
            } else {
                // log error for unsupported version
                getLog().error("No supporting feature available in Open Liberty for org.springframework.boot dependency with version " + springBootVersion);
            }

            if (springBootFeature != null) {
                getLog().info("Adding the "+springBootFeature+" feature to the Open Liberty server configuration.");
                serverConfig.addFeature(springBootFeature);
            }
            
        } else {
            getLog().info("The springBoot feature was not added to the Open Liberty server because no spring-boot-starter dependencies were found.");
        }
        
        // Add any other Liberty features needed depending on the spring boot starters defined
        List<String> springBootStarters = getSpringBootStarters(mavenProject);
        
        for (String springBootStarter : springBootStarters) {
            
            if (springBootStarter.equals("spring-boot-starter-web")) {
                // Add the servlet-4.0 feature
                serverConfig.addFeature(SERVLET_40);
            }
            
            // TODO: Add more dependency mappings if needed. 
        }
        
        // Write server.xml to Liberty server config directory
        serverConfig.writeToServer(projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName);
        
    }
    
    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(
            groupId(libertyMavenPluginGroupId),
            artifactId(libertyMavenPluginArtifactId),
            version(libertyMavenPluginVersion)
        );
    }
    
    private Element getRuntimeArtifactElement() {
        return element(name("assemblyArtifact"),
            element(name("groupId"), runtimeArtifact.getGroupId()),
            element(name("artifactId"), runtimeArtifact.getArtifactId()),
            element(name("version"), runtimeArtifact.getVersion()),
            element(name("type"), runtimeArtifact.getType())
        );
    }
    
    private ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment(
            mavenProject,
            mavenSession,
            pluginManager
        );
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the install-server goal
     *
     */
    private void installOpenLiberty() throws MojoExecutionException {
    
        executeMojo(
            getPlugin(),
            goal("install-server"),
            configuration(
                element(name("serverName"), libertyServerName),
                getRuntimeArtifactElement()
            ),
            getExecutionEnvironment()
        );
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     *
     */
    private void createServer() throws MojoExecutionException {
    
        executeMojo(
            getPlugin(),
            goal("create-server"),
            configuration(
                element(name("serverName"), libertyServerName),
                element(name("bootstrapProperties"),
                        element(name("server.liberty.use-default-host"), "false")
                ),
                getRuntimeArtifactElement()
            ),
            getExecutionEnvironment()
        );
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the thin goal
     *
     */
    private void thinSpringBootApp() throws MojoExecutionException { 
        executeMojo(
            getPlugin(),
            goal("thin"),
            configuration(
            ),
            getExecutionEnvironment()
        );
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the install-app goal.
     *
     *
     */
    private void installApp() throws MojoExecutionException {
    
        executeMojo(
            getPlugin(),
            goal("install-apps"),
            configuration(
                element(name("installAppPackages"), "thin-project"),
                element(name("serverName"), libertyServerName),
                getRuntimeArtifactElement()
            ),
            getExecutionEnvironment()
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
            getPlugin(),
            goal("install-feature"),
            configuration(
                element(name("serverName"), libertyServerName),
                element(name("features"),
                    element(name("acceptLicense"), "true")

                )
            ),
            getExecutionEnvironment()
        );
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the package-server goal
     *
     */
    private void createUberJar() throws MojoExecutionException {
    
        // Package server into runnable jar
        executeMojo(
            getPlugin(),
            goal("package-server"),
            configuration(
                element(name("include"), "minify,runnable"),
                element(name("serverName"), libertyServerName)
            ),
            getExecutionEnvironment()
        );
    }

    /**
    * Detect spring boot version dependency
    */
    private String findSpringBootVersion(MavenProject project) {
            String version = null;

            Set<Artifact> artifacts = project.getArtifacts();
            for(Artifact art: artifacts) {
                if("org.springframework.boot".equals(art.getGroupId())  && "spring-boot".equals(art.getArtifactId())) {
                    version = art.getVersion();
                    break;
                }
            }

            return version;
    }
    
    /**
     * Get all dependencies with "spring-boot-starter-*" as the artifactId. These
     * dependencies will be used to determine which additional Liberty features 
     * need to be enabled.
     * 
     */
    private List<String> getSpringBootStarters(MavenProject project) {
        
        List<String> springBootStarters = new ArrayList<String>();
        
        Set<Artifact> artifacts = project.getArtifacts();
        for(Artifact art: artifacts) {
            if(art.getArtifactId().contains("spring-boot-starter")) {
                springBootStarters.add(art.getArtifactId());
            }
        }
        
        return springBootStarters;
    }
}
