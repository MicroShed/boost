/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.runtimes.openliberty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

import groovy.lang.Closure;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.config.BoosterConfigurator;
import boost.common.config.ConfigConstants;
import boost.gradle.runtimes.GradleRuntimeI;
import boost.gradle.utils.BoostLogger;
import boost.gradle.utils.GradleProjectUtil;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;

import net.wasdev.wlp.gradle.plugins.extensions.LibertyExtension;

public class LibertyGradleRuntime implements GradleRuntimeI {

    private final String serverName = "BoostServer";
    private final String OPEN_LIBERTY_VERSION = "19.0.0.3";

    private String projectBuildDir;
    private String libertyServerPath;

    public LibertyGradleRuntime() {}

    public void configureRuntimePlugin(Project project) throws GradleException {
        project.getPluginManager().apply("net.wasdev.wlp.gradle.plugins.Liberty");

        project.getTasks().getByName("libertyPackage").dependsOn("installApps", "installFeature");
        project.getTasks().getByName("installApps").mustRunAfter("installFeature");
        
        project.getDependencies().add("libertyRuntime", "io.openliberty:openliberty-runtime:" + OPEN_LIBERTY_VERSION);
        //The rest of this method is used to set the server name in the ServerExtension.

        //Creating a closure to call on the ServerExtension in the LibertyExtension
        //Other properties aside from the server name can be configured here as well
        Closure serverClosure = new Closure(this) {
            @Override
            public Object call() {
                try {
                    Field nameField = getDelegate().getClass().getDeclaredField("name");
                    nameField.setAccessible(true);
                    nameField.set(getDelegate(), serverName);
                } catch (Exception e) {
                    throw new GradleException("Error configuring Liberty Gradle Plugin.", e);
                }
                
                return this.getDelegate();
            }
        };
        //Configuring the LibertyExtension's server ServerExtension
        project.getExtensions().configure("liberty", new Action<LibertyExtension>() {
            @Override
            public void execute(LibertyExtension liberty) {
                liberty.server(serverClosure);
            }
        });

        this.projectBuildDir = project.getBuildDir().toString();
        this.libertyServerPath = projectBuildDir + "/wlp/usr/servers/" + serverName;
    }

    public void doPackage(List<AbstractBoosterConfig> boosterConfigs, Object project, Object pluginTask) throws BoostException {
        Project gradleProject = (Project)project;
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, gradleProject.findProperty("targetCompatibility").toString());
        try {
            packageLiberty(boosterConfigs, gradleProject);
        } catch (GradleException e) {
            throw new BoostException("Error packaging Liberty server", e);
        }
    }

    private void packageLiberty(List<AbstractBoosterConfig> boosterConfigs, Project project) throws BoostException {
        try {
            runTask("installLiberty", project);
            runTask("libertyCreate", project);
            
            // targeting a liberty install
            copyBoosterDependencies(boosterConfigs, project);

            generateServerConfig(boosterConfigs, project);

            installMissingFeatures(project);

            // Create the Liberty runnable jar
            createUberJar(project);
        } catch(Exception e) {
            throw new BoostException("Error packaging Liberty server", e);
        }

    }

    /**
     * Get all booster dependencies and copy them to the Liberty server.
     * 
     * @throws GradleException
     *
     */
    private void copyBoosterDependencies(List<AbstractBoosterConfig> boosterConfigs, Project project) throws IOException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs,
                BoostLogger.getInstance());

        project.getConfigurations().maybeCreate("boosterDependencies");
        for (String dep : dependenciesToCopy) {
            project.getDependencies().add("boosterDependencies", dep);
        }

        File resouresDir = new File(libertyServerPath, "resources");

        for (File dep : project.getConfigurations().getByName("boosterDependencies").resolve()) {
            FileUtils.copyFileToDirectory(dep, resouresDir);
        }
    }

    /**
     * Generate config for the Liberty server based on the Gradle project.
     * 
     * @throws GradleException
     */
    private void generateServerConfig(List<AbstractBoosterConfig> boosterConfigs, Project project) throws GradleException {

        try {
            // Generate server config
            generateLibertyServerConfig(boosterConfigs, project);

        } catch (Exception e) {
            throw new GradleException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private List<String> getWarNames(Project project) {
        String warName = null;

        if (project.getPlugins().hasPlugin("war")) {
            WarPlugin warPlugin = (WarPlugin)(project.getPlugins().findPlugin("war"));
            War warTask = (War)(project.getTasks().findByPath(warPlugin.WAR_TASK_NAME));
            warName = warTask.getArchiveFileName().get().substring(0, warTask.getArchiveFileName().get().length() - 4);
        }

        return Arrays.asList(warName);
    }

    /**
     * Configure the Liberty runtime
     * 
     * @param boosterConfigurators
     * @throws Exception
     */
    private void generateLibertyServerConfig(List<AbstractBoosterConfig> boosterConfigurators, Project project) throws Exception {

        List<String> warNames = getWarNames(project);
        LibertyServerConfigGenerator libertyConfig = new LibertyServerConfigGenerator(libertyServerPath,
                BoostLogger.getInstance());

        // Add default http endpoint configuration
        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(BoostLogger.getInstance());

        String host = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "*");
        libertyConfig.addHostname(host);

        String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "9080");
        libertyConfig.addHttpPort(httpPort);

        String httpsPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTPS_PORT, "9443");
        libertyConfig.addHttpsPort(httpsPort);

        // Add war configuration if necessary
        if (!warNames.isEmpty()) {
            for (String warName : warNames) {
                libertyConfig.addApplication(warName);
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
        }

        // Loop through configuration objects and add config and
        // the corresponding Liberty feature
        for (AbstractBoosterConfig configurator : boosterConfigurators) {
            if (configurator instanceof LibertyBoosterI) {
                ((LibertyBoosterI) configurator).addServerConfig(libertyConfig);
                libertyConfig.addFeature(((LibertyBoosterI) configurator).getFeature());
            }
        }

        libertyConfig.writeToServer();
    }

    //Liberty Gradle Plugin Execution

    /**
     * Invoke the liberty-gradle-plugin to run the install-feature goal.
     *
     * This will install any missing features defined in the server.xml or
     * configDropins.
     *
     */
    private void installMissingFeatures(Project project) throws GradleException {
        runTask("installFeature", project);
    }

    /**
     * Invoke the liberty-gradle-plugin to package the server into a runnable Liberty
     * JAR
     */
    private void createUberJar(Project project) throws BoostException {
        try {
            runTask("libertyPackage", project);
        } catch (GradleException e) {
            throw new BoostException("Error creating Liberty uber jar", e);
        }
    }

    public void doDebug(Object project, Object pluginTask) throws BoostException {
        try {
            runTask("libertyDebug", (Project)project);
        } catch (GradleException e) {
            throw new BoostException("Error debugging Liberty server", e);
        }
    }

    public void doRun(Object project, Object pluginTask) throws BoostException {
        try {
            runTask("LibertyRun", (Project)project);
        } catch (GradleException e) {
            throw new BoostException("Error running Liberty server", e);
        }
    }

    public void doStart(Object project, Object pluginTask) throws BoostException {
        try {
            runTask("libertyStart", (Project)project);
        } catch (GradleException e) {
            throw new BoostException("Error starting Liberty server", e);
        }
    }

    public void doStop(Object project, Object pluginTask) throws BoostException {
        try {
            runTask("libertyStop", (Project)project);
        } catch (GradleException e) {
            throw new BoostException("Error stopping Liberty server", e);
        }
    }

    private void runTask(String taskName, Project project) throws GradleException {
        //String command = project.getProjectDir().toString() + "/gradlew";
        try {
            ProcessBuilder pb = new ProcessBuilder("gradle", taskName, "-i", "-s");
            System.out.println("Executing task " + pb.command().get(1));
            pb.directory(project.getProjectDir());
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new GradleException("Unable to execute the " + taskName + " task.", e);
        }
    }

}
