/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.maven.liberty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.apache.maven.plugins.annotations.*;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.config.ConfigConstants;
import io.openliberty.boost.common.utils.SpringBootUtil;
import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;
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

    protected List<AbstractBoosterConfig> boosterPackConfigurators;
    protected String targetRuntime;

    String springBootVersion = null;

    String libertyServerPath = null;
    String tomeeInstallPath = null;
    String tomeeConfigPath = null;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        springBootVersion = MavenProjectUtil.findSpringBootVersion(project);

        libertyServerPath = projectBuildDir + "/liberty/wlp/usr/servers/" + libertyServerName;
        tomeeInstallPath = projectBuildDir + "/apache-tomee/";
        tomeeConfigPath = tomeeInstallPath + "conf";

        try {
            Map<String, String> dependencies = MavenProjectUtil.getAllDependencies(project, repoSystem, repoSession,
                    remoteRepos, BoostLogger.getInstance());
            this.targetRuntime = BoosterConfigurator.getTargetRuntime(dependencies, BoostLogger.getInstance());
            this.boosterPackConfigurators = BoosterConfigurator.getBoosterPackConfigurators(dependencies,
                    BoostLogger.getInstance());
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (this.targetRuntime.equals(ConfigConstants.TOMEE_RUNTIME)) {
            createTomEEServer();
            updateTOMEEClasspath();
            // return;
        } else {
            createLibertyServer();
        }

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

        if (BoostUtil.isNotNullOrEmpty(springBootVersion)) { // Dealing
                                                             // with a
                                                             // spring
                                                             // boot app

            springBootClassifier = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil
                    .getSpringBootMavenPluginClassifier(project, getLog());

            // Check if we need to attach based on the classifier configuration
            if (BoostUtil.isNotNullOrEmpty(springBootClassifier)) {
                attach = false;
            } else {
                attach = true;
            }

            File springBootUberJar = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil.getSpringBootUberJAR(project,
                    getLog());
            validateSpringBootUberJAR(springBootUberJar);
            copySpringBootUberJar(springBootUberJar, attach); // Only copy back
                                                              // if we need to
                                                              // overwrite the
                                                              // project
                                                              // artifact

            generateServerConfigSpringBoot();

            installMissingFeatures();
            installApp(ConfigConstants.INSTALL_PACKAGE_SPRING);

            if (springBootUberJar != null) {
                // Create the Liberty Uber JAR from the Spring Boot Uber JAR in
                // place
                createUberJar(springBootUberJar.getAbsolutePath(), attach);
            } else {
                // The Spring Boot Uber JAR was already replaced with the
                // Liberty Uber JAR (this
                // is a re-execution in the non-classifier scenario)
                createUberJar(null, attach);
            }

            if (!BoostUtil.isNotNullOrEmpty(springBootClassifier)) {
                // If necessary, add the manifest to prevent Spring Boot from
                // repackaging again
                addSpringBootVersionToManifest(springBootVersion);
            }
        } else { // Dealing with an EE based app

            // Get the Java compiler target version
            String javaCompilerTargetVersion = MavenProjectUtil.getJavaCompilerTargetVersion(project);
            System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, javaCompilerTargetVersion);

            if (this.targetRuntime.equals(ConfigConstants.TOMEE_RUNTIME)) {
                // targeting a tomee install
                copyTomEEJarDependencies();
            } else {
                // targeting a liberty install
                copyBoosterDependencies();

                generateServerConfigEE();

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

                // Not sure this works yet, the main idea is to NOT create this with
                // a WAR
                // package type.
                if (project.getPackaging().equals("jar")) {
                    createUberJar(null, true);
                }
            }
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven Spring Boot
     * project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfigSpringBoot() throws MojoExecutionException {

        try {
            // Get Spring Boot starters from Maven project
            Map<String, String> dependencies = MavenProjectUtil.getAllDependencies(project, repoSystem, repoSession,
                    remoteRepos, BoostLogger.getInstance());

            // Generate server config
            SpringBootUtil.generateLibertyServerConfig(projectBuildDir + "/classes", libertyServerPath,
                    springBootVersion, dependencies, BoostLogger.getInstance(), useDefaultHost);

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven Spring Boot
     * project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfigEE() throws MojoExecutionException {

        List<String> warNames = getWarNames();
        try {
            // Generate server config
            BoosterConfigurator.generateLibertyServerConfig(libertyServerPath, boosterPackConfigurators, warNames,
                    BoostLogger.getInstance());

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven Spring Boot
     * project.
     * 
     * @throws MojoExecutionException
     */
    private void updateTOMEEClasspath() throws MojoExecutionException {

        try {
            // update server config
            BoosterConfigurator.addTOMEEDependencyJarsToClasspath(tomeeConfigPath, boosterPackConfigurators,
                    BoostLogger.getInstance());

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to update server configuration for the tomee server.", e);
        }
    }

    private List<String> getWarNames() {
        List<String> warNames = new ArrayList<String>();

        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getType().equals("war")) {
                warNames.add(artifact.getArtifactId() + "-" + artifact.getVersion());
            }
        }

        if (project.getPackaging().equals(ConfigConstants.WAR_PKG_TYPE)) {
            if (project.getVersion() == null) {
                warNames.add(project.getArtifactId());
            } else {
                warNames.add(project.getArtifactId() + "-" + project.getVersion());
            }
        }

        return warNames;
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
            if (springBootUberJar != null) { // Only copy the Uber JAR if it is
                                             // valid
                springBootUberJarCopy = SpringBootUtil.copySpringBootUberJar(springBootUberJar,
                        BoostLogger.getInstance());
            }

            if (springBootUberJarCopy == null) { // Copy didn't happen
                if (attach) { // If we are replacing the project artifact, then
                              // copy back the Spring Boot Uber
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

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createTomEEServer() throws MojoExecutionException {

        List<String> tomEEDependencyJarsToCopy = BoosterConfigurator
                .getTomEEDependencyJarsToCopy(boosterPackConfigurators, BoostLogger.getInstance());

        executeMojo(getTOMEEPlugin(), goal("build"),
                configuration(element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createLibertyServer() throws MojoExecutionException {

        executeMojo(getPlugin(), goal("create-server"),
                configuration(element(name("serverName"), libertyServerName), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-app goal.
     */
    private void installApp(String installAppPackagesVal) throws MojoExecutionException {

        Element installAppPackages = element(name("installAppPackages"), installAppPackagesVal);
        Element serverName = element(name("serverName"), libertyServerName);

        Xpp3Dom configuration = configuration(installAppPackages, serverName, getRuntimeArtifactElement());
        if (!ConfigConstants.INSTALL_PACKAGE_SPRING.equals(installAppPackagesVal)) {
            configuration.addChild(element(name("appsDirectory"), "apps").toDom());
            configuration.addChild(element(name("looseApplication"), "true").toDom());
        }

        executeMojo(getPlugin(), goal("install-apps"), configuration, getExecutionEnvironment());

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
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyBoosterDependencies() throws MojoExecutionException {

        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterPackConfigurators,
                BoostLogger.getInstance());

        for (String dep : dependenciesToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(getMavenDependencyPlugin(), goal("copy"),
                    configuration(element(name("outputDirectory"), libertyServerPath + "/resources"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    getExecutionEnvironment());
        }
    }

    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyTomEEJarDependencies() throws MojoExecutionException {

        List<String> tomEEDependencyJarsToCopy = BoosterConfigurator
                .getTomEEDependencyJarsToCopy(boosterPackConfigurators, BoostLogger.getInstance());

        for (String dep : tomEEDependencyJarsToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(getMavenDependencyPlugin(), goal("copy"),
                    configuration(element(name("outputDirectory"), tomeeInstallPath + "boost"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    getExecutionEnvironment());
        }
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
                        element(name("outputDirectory"), "target/liberty-alt-output-dir"),
                        element(name("packageFile"), packageFilePath), element(name("serverName"), libertyServerName)),
                getExecutionEnvironment());
    }
}
