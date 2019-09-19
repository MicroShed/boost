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
package org.microshed.boost.runtimes.openliberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.boosters.AbstractBoosterConfig;
import org.microshed.boost.common.config.BoostProperties;
import org.microshed.boost.common.config.BoosterConfigurator;
import org.microshed.boost.common.config.ConfigConstants;
import org.microshed.boost.common.runtimes.RuntimeI;
import org.microshed.boost.maven.runtimes.RuntimeParams;
import org.microshed.boost.maven.utils.BoostLogger;
import org.microshed.boost.maven.utils.MavenProjectUtil;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

public class LibertyRuntime implements RuntimeI {
    private final List<AbstractBoosterConfig> boosterConfigs;
    private final Properties boostProperties;
    private final ExecutionEnvironment env;
    private final MavenProject project;
    private final Plugin mavenDepPlugin;

    private final String serverName = "defaultServer";
    private final String projectBuildDir;
    private final String libertyServerPath;

    private final String runtimeGroupId = "io.openliberty";
    private final String runtimeArtifactId = "openliberty-runtime";
    private final String defaultRuntimeVersion = "19.0.0.9";
    private String runtimeVersion;

    private String libertyMavenPluginGroupId = "io.openliberty.tools";
    private String libertyMavenPluginArtifactId = "liberty-maven-plugin";
    private String libertyMavenPluginVersion = "3.0.1";

    public LibertyRuntime() {
        this.boosterConfigs = null;
        this.boostProperties = null;
        this.env = null;
        this.project = null;
        this.projectBuildDir = null;
        this.libertyServerPath = null;
        this.mavenDepPlugin = null;
        this.runtimeVersion = defaultRuntimeVersion;
    }

    public LibertyRuntime(RuntimeParams runtimeParams) {
        this.boosterConfigs = runtimeParams.getBoosterConfigs();
        this.boostProperties = runtimeParams.getBoostProperties();
        this.env = runtimeParams.getEnv();
        this.project = runtimeParams.getProject();
        this.projectBuildDir = project.getBuild().getDirectory();
        this.libertyServerPath = projectBuildDir + "/liberty/wlp/usr/servers/" + serverName;
        this.mavenDepPlugin = runtimeParams.getMavenDepPlugin();
        this.runtimeVersion = boostProperties.getProperty("boost_runtime_version", defaultRuntimeVersion);
    }

    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(libertyMavenPluginGroupId), artifactId(libertyMavenPluginArtifactId),
                version(libertyMavenPluginVersion));
    }

    @Override
    public void doPackage() throws BoostException {
        String javaCompilerTargetVersion = MavenProjectUtil.getJavaCompilerTargetVersion(project);
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, javaCompilerTargetVersion);
        try {
            packageLiberty(boosterConfigs);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error packaging Liberty server", e);
        }
    }

    private void packageLiberty(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        createLibertyServer();

        // targeting a liberty install
        copyBoosterDependencies(boosterConfigs);

        generateServerConfig(boosterConfigs);

        installMissingFeatures();
        // we install the app now, after server.xml is configured. This is
        // so that we can specify a custom config-root in server.xml ("/").
        // If we installed the app prior to server.xml configuration, then
        // the LMP would write out a webapp stanza into config dropins that
        // would include a config-root setting set to the app name.
        if (project.getPackaging().equals("war")) {
            installApp(ConfigConstants.INSTALL_PACKAGE_ALL);
        } else {
            // This is temporary. When packing type is "jar", if we
            // set installAppPackages=all, the LMP will try to install
            // the project jar and fail. Once this is fixed, we can always
            // set installAppPackages=all.
            installApp(ConfigConstants.INSTALL_PACKAGE_DEP);
        }

        // Create the Liberty runnable jar
        createUberJar();
    }

    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyBoosterDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs,
                BoostLogger.getSystemStreamLogger());

        for (String dep : dependenciesToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), libertyServerPath + "/resources"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    env);
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfig(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {

        try {
            // Generate server config
            generateLibertyServerConfig(boosterConfigs);

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    /**
     * Assumes a non-WAR packaging type (like JAR) has a WAR dependency. We assume
     * there's only 1 but don't check, just return the first one.
     * 
     * @return
     * @throws BoostException
     */
    private String getWarName() throws BoostException {

        String retVal = null;
        if (project.getPackaging().equals(ConfigConstants.WAR_PKG_TYPE)) {
            retVal = project.getBuild().getFinalName();
        } else {
            // JAR package "release", get WAR from dependency
            for (Artifact artifact : project.getArtifacts()) {
                // first WAR
                if (artifact.getType().equals("war")) {
                    retVal = artifact.getArtifactId() + "-" + artifact.getVersion();
                    break;
                }
            }
            if (retVal == null) {
                BoostLogger log = BoostLogger.getSystemStreamLogger();
                String msg = "With non-WAR packaging type, we require a WAR dependency";
                log.error(msg);
                throw new BoostException(msg);
            }
        }

        return retVal;
    }

    /**
     * Configure the Liberty runtime
     * 
     * @param boosterConfigurators
     * @throws Exception
     */
    private void generateLibertyServerConfig(List<AbstractBoosterConfig> boosterConfigurators) throws Exception {

        String encryptionKey = (String) boostProperties.get(BoostProperties.AES_ENCRYPTION_KEY);
        LibertyServerConfigGenerator libertyConfig = new LibertyServerConfigGenerator(libertyServerPath, encryptionKey,
                BoostLogger.getSystemStreamLogger());

        // Configure HTTP endpoint
        String host = (String) boostProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "*");
        libertyConfig.addHostname(host);

        String httpPort = (String) boostProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "9080");
        libertyConfig.addHttpPort(httpPort);

        String httpsPort = (String) boostProperties.getOrDefault(BoostProperties.ENDPOINT_HTTPS_PORT, "9443");
        libertyConfig.addHttpsPort(httpsPort);

        String warName = getWarName();
        libertyConfig.addApplication(warName);

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

    // Liberty Maven Plugin executions

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createLibertyServer() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("create"),
                configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()), env);
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-feature goal.
     *
     * This will install any missing features defined in the server.xml or
     * configDropins.
     *
     */
    private void installMissingFeatures() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("install-feature"), configuration(element(name("serverName"), serverName),
                element(name("features"), element(name("acceptLicense"), "false"))), env);
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-app goal.
     */
    private void installApp(String installAppPackagesVal) throws MojoExecutionException {

        Element deployPackages = element(name("deployPackages"), installAppPackagesVal);
        Element serverNameElement = element(name("serverName"), serverName);

        Xpp3Dom configuration = configuration(deployPackages, serverNameElement, getRuntimeArtifactElement());
        configuration.addChild(element(name("appsDirectory"), "apps").toDom());

        executeMojo(getPlugin(), goal("deploy"), configuration, env);
    }

    private Element getRuntimeArtifactElement() {
        return element(name("assemblyArtifact"), element(name("groupId"), runtimeGroupId),
                element(name("artifactId"), runtimeArtifactId), element(name("version"), runtimeVersion),
                element(name("type"), "zip"));
    }

    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable Liberty
     * JAR
     */
    private void createUberJar() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("package"),
                configuration(element(name("isInstall"), "false"), element(name("include"), "minify"),
                        element(name("outputDirectory"), "target/liberty-alt-output-dir"),
                        element(name("packageType"), "jar"), element(name("serverName"), serverName)),
                env);
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("debug"), configuration(element(name("serverName"), serverName),
                    element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error debugging Liberty server", e);
        }
    }

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"), configuration(element(name("serverName"), serverName),
                    element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running Liberty server", e);
        }

    }

    @Override
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("start"),
                    configuration(element(name("serverName"), serverName),
                            element(name("verifyTimeout"), String.valueOf(verifyTimeout)),
                            element(name("serverStartTimeout"), String.valueOf(serverStartTimeout)),
                            element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting Liberty server", e);
        }
    }

    @Override
    public void doStop() throws BoostException {
        try {
            executeMojo(getPlugin(), goal("stop"),
                    configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping Liberty server", e);
        }
    }

}
