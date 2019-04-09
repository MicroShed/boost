package io.openliberty.boost.maven.runtimes;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.config.ConfigConstants;
import io.openliberty.boost.common.runtimes.LibertyRuntimeI;
import io.openliberty.boost.common.utils.SpringBootUtil;
import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;
import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

public class LibertyRuntime implements LibertyRuntimeI {
    
    private final Log log;
    
    private final Map<String, String> deps;
    private final ExecutionEnvironment env;
    private final MavenProject project;
    private final RepositorySystem repoSystem;
    private final RepositorySystemSession repoSession;
    private final List<RemoteRepository> remoteRepos;
    private final Plugin mavenDepPlugin;
    
    private final String serverName = "BoostServer";
    private final String projectBuildDir;
    private final String libertyServerPath;
    private final boolean useDefaultHost;
    private final String springBootVersion;
    
    private String libertyMavenPluginGroupId = "net.wasdev.wlp.maven.plugins";
    private String libertyMavenPluginArtifactId = "liberty-maven-plugin";
    private String libertyMavenPluginVersion = "2.6.3";
    
    public LibertyRuntime(Map<String, String> deps, ExecutionEnvironment env, MavenProject project, Log log, 
            RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos, Plugin mavenDepPlugin, boolean useDefaultHost) {
        this.log = log;
        this.deps = deps;
        this.env = env;
        this.project = project;
        this.projectBuildDir = project.getBuild().getDirectory();
        this.libertyServerPath = projectBuildDir + "/liberty/wlp/usr/servers/" + serverName;
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
        this.mavenDepPlugin = mavenDepPlugin;
        this.useDefaultHost = useDefaultHost;
        this.springBootVersion = MavenProjectUtil.findSpringBootVersion(project);
    }
    
    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(libertyMavenPluginGroupId), artifactId(libertyMavenPluginArtifactId),
                version(libertyMavenPluginVersion));
    }

    @Override
    public void doPackage() throws BoostException {
        List<AbstractBoosterConfig> boosterConfigs;
        try {
            String javaCompilerTargetVersion = MavenProjectUtil.getJavaCompilerTargetVersion(project);
            System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, javaCompilerTargetVersion);
            boosterConfigs = BoosterConfigurator.getBoosterConfigs(deps, BoostLogger.getInstance());
        } catch(Exception e) {
            throw new BoostException("Error copying booster dependencies", e);
        }
        
        try {
            packageLiberty(boosterConfigs);
        } catch(Exception e) {
            throw new BoostException("Error packaging Liberty server", e);
        }
    }
    
    private void packageLiberty(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        createLibertyServer();

        /**
         * Whether the packaged Liberty Uber JAR will be the project artifact.
         * This should be the case in Spring Boot scenarios since Spring Boot
         * developers expect a runnable JAR.
         */
        boolean attach;

        /**
         * Use the classifier to determine whether we need to set the Liberty
         * Uber JAR as the project artifact, and add Spring-Boot-Version to the
         * manifest
         */
        String springBootClassifier = null;

        if (BoostUtil.isNotNullOrEmpty(springBootVersion)) { // Dealing
                                                             // with a
                                                             // spring
                                                             // boot app

            springBootClassifier = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil
                    .getSpringBootMavenPluginClassifier(project, log);

            // Check if we need to attach based on the classifier configuration
            if (BoostUtil.isNotNullOrEmpty(springBootClassifier)) {
                attach = false;
            } else {
                attach = true;
            }

            File springBootUberJar = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil.getSpringBootUberJAR(project, log);
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

            // targeting a liberty install
            copyBoosterDependencies(boosterConfigs);

            generateServerConfigEE(boosterConfigs);

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
   
    /**
     * Check that we either have a Liberty Uber JAR (in which case this is a
     * re-execution) or a Spring Boot Uber JAR (from which we will create a
     * Liberty Uber JAR) when we begin the packaging for Spring Boot projects.
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
     * Copy the Spring Boot uber JAR back as the project artifact, only if
     * Spring Boot didn't create it already
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
                        log.info("Copying back Spring Boot Uber JAR as project artifact.");
                        FileUtils.copyFile(springJar, project.getArtifact().getFile());
                    }
                }
            } else {
                log.info("Copied Spring Boot Uber JAR to " + springBootUberJarCopy.getCanonicalPath());
            }
        } catch (PluginExecutionException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
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
            log.debug("Added Spring Boot Version to manifest to prevent repackaging of Liberty Uber JAR.");
        } else {
            throw new MojoExecutionException("Project artifact is not a Liberty Uber JAR. This should never happen.");
        }
    }
    
    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to
     * copy them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyBoosterDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs, this, BoostLogger.getInstance());

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
     * Generate config for the Liberty server based on the Maven Spring Boot
     * project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfigEE(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {

        List<String> warNames = getWarNames();
        try {
            // Generate server config
            BoosterConfigurator.generateLibertyServerConfig(libertyServerPath, boosterConfigs, warNames,
                    BoostLogger.getInstance());

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
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
    
    // Liberty Maven Plugin executions
    
    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createLibertyServer() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("create-server"),
                configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()),
                env);
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
        Element installAppPackages = element(name("installAppPackages"), installAppPackagesVal);
        Element serverNameElement = element(name("serverName"), serverName);

        Xpp3Dom configuration = configuration(installAppPackages, serverNameElement, getRuntimeArtifactElement());
        if (!ConfigConstants.INSTALL_PACKAGE_SPRING.equals(installAppPackagesVal)) {
            configuration.addChild(element(name("appsDirectory"), "apps").toDom());
            configuration.addChild(element(name("looseApplication"), "true").toDom());
        }

        executeMojo(getPlugin(), goal("install-apps"), configuration, env);
    }
    
    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable
     * Liberty JAR
     * 
     * @param packageFilePath
     *            the Spring Boot Uber JAR file path, whose contents will be
     *            replaced by the Liberty Uber JAR
     * @param attach
     *            whether or not to make the packaged server the project
     *            artifact
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
                        element(name("packageFile"), packageFilePath), element(name("serverName"), serverName)),
                env);
    }
    
    private Element getRuntimeArtifactElement() {
        String groupId = "io.openliberty";
        String artifactId = "openliberty-runtime";
        String version = "19.0.0.3";
        String type = "zip";
        
        return element(name("assemblyArtifact"), element(name("groupId"), groupId),
                element(name("artifactId"), artifactId),
                element(name("version"), version),
                element(name("type"), type));
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("debug"),
                    configuration(element(name("serverName"), serverName),
                            element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error debugging Liberty server", e);
        }
    }

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"),
                    configuration(element(name("serverName"), serverName),
                            element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                    env);
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
                    configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping Liberty server", e);
        }
    }

}
