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

import java.util.List;
import java.util.Map;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.runtimes.TomeeRuntimeI;
import io.openliberty.boost.maven.utils.BoostLogger;

public class TomeeRuntime implements TomeeRuntimeI {
    
    private final Map<String, String> deps;
    private final ExecutionEnvironment env;
        
    private final String tomeeMavenPluginGroupId = "org.apache.tomee.maven";
    private final String tomeeMavenPluginArtifactId = "tomee-maven-plugin";
    private final String installDir;
    private final String configDir;
    
    private final Plugin mavenDepPlugin;
    
    public TomeeRuntime(Map<String, String> deps, ExecutionEnvironment env, String installDir, String configDir, Plugin mavenDepPlugin) {
        this.deps = deps;
        this.env = env;
        
        this.installDir = installDir;
        this.configDir = configDir;
        
        this.mavenDepPlugin = mavenDepPlugin;
    }
    
    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(tomeeMavenPluginGroupId), artifactId(tomeeMavenPluginArtifactId), version("8.0.0-M2"));
    }

    @Override
    public void doPackage() throws BoostException {
        try {
            List<AbstractBoosterConfig> boosterConfigs = BoosterConfigurator.getBoosterConfigs(deps, BoostLogger.getInstance());
            createTomEEServer();
            configureTomeeServer(boosterConfigs);
            copyTomEEJarDependencies(boosterConfigs);
        }
        catch(Exception e) {
            throw new BoostException("Error packaging TomEE server", e);
        }
    }
    
    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createTomEEServer() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("build"),
                configuration(element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                env);
    }
    
    
    /**
     * Generate config for the Liberty server based on the Maven Spring Boot
     * project.
     * 
     * @throws MojoExecutionException
     */
    private void configureTomeeServer(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        try {
            BoosterConfigurator.configureTomeeServer(configDir, boosterConfigs, BoostLogger.getInstance());
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to update server configuration for the Tomee server.", e);
        }
    }
    
    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyTomEEJarDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {

        List<String> tomEEDependencyJarsToCopy = BoosterConfigurator
                .getDependenciesToCopy(boosterConfigs, this, BoostLogger.getInstance());

        for (String dep : tomEEDependencyJarsToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), installDir + "boost"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    env);
        }
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {
        // TODO No debug in TomEE yet
    }

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running TomEE server", e);
        }
    }

    @Override
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("start"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting TomEE server", e);
        }
    }

    @Override
    public void doStop() throws BoostException {
        try {
            executeMojo(getPlugin(), goal("stop"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping TomEE server", e);
        }
    }

}
