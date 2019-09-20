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
package org.microshed.boost.maven.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.microshed.boost.common.boosters.AbstractBoosterConfig;
import org.microshed.boost.common.config.BoostProperties;
import org.microshed.boost.common.config.BoosterConfigurator;
import org.microshed.boost.common.runtimes.RuntimeI;
import org.microshed.boost.maven.runtimes.RuntimeParams;
import org.microshed.boost.maven.utils.BoostLogger;
import org.microshed.boost.maven.utils.MavenProjectUtil;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

public abstract class AbstractMojo extends MojoSupport {

    private ClassLoader projectClassLoader;
    private List<AbstractBoosterConfig> boosterConfigs;
    private Properties boostProperties;

    protected String mavenDependencyPluginGroupId = "org.apache.maven.plugins";
    protected String mavenDependencyPluginArtifactId = "maven-dependency-plugin";

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected String projectBuildDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    protected List<RemoteRepository> remoteRepos;

    @Component
    protected BuildPluginManager pluginManager;

    @Component
    protected RepositorySystem repoSystem;

    protected Map<String, String> dependencies;

    protected Plugin getMavenDependencyPlugin() throws MojoExecutionException {
        return plugin(groupId(mavenDependencyPluginGroupId), artifactId(mavenDependencyPluginArtifactId));
    }

    protected ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment(project, session, pluginManager);
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            init();
            BoostLogger boostLogger = new BoostLogger(getLog());
            // TODO move this into getRuntimeInstance()
            this.dependencies = MavenProjectUtil.getAllDependencies(project, repoSystem, repoSession, remoteRepos,
                    boostLogger);

            List<File> compileClasspathJars = new ArrayList<File>();

            List<URL> pathUrls = new ArrayList<URL>();
            for (String compilePathElement : project.getCompileClasspathElements()) {
                pathUrls.add(new File(compilePathElement).toURI().toURL());
                if (compilePathElement.endsWith(".jar")) {
                    compileClasspathJars.add(new File(compilePathElement));
                }
            }
            URL[] urlsForClassLoader = pathUrls.toArray(new URL[pathUrls.size()]);
            this.projectClassLoader = new URLClassLoader(urlsForClassLoader, this.getClass().getClassLoader());

            boostProperties = BoostProperties.getConfiguredBoostProperties(project.getProperties(), boostLogger);

            boosterConfigs = BoosterConfigurator.getBoosterConfigs(compileClasspathJars, projectClassLoader,
                    dependencies, boostProperties, boostLogger);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected RuntimeI getRuntimeInstance() throws MojoExecutionException {

        RuntimeI runtime = null;

        RuntimeParams params = new RuntimeParams(boosterConfigs, boostProperties, getExecutionEnvironment(), project,
                getLog(), repoSystem, repoSession, remoteRepos, getMavenDependencyPlugin());
        try {
            ServiceLoader<RuntimeI> runtimes = ServiceLoader.load(RuntimeI.class, projectClassLoader);
            if (!runtimes.iterator().hasNext()) {
                throw new MojoExecutionException(
                        "No target Boost runtime was detected. Please add a runtime and restart the build.");
            }
            for (RuntimeI runtimeI : runtimes) {
                if (runtime != null) {
                    throw new MojoExecutionException(
                            "There are multiple Boost runtimes on the classpath. Configure the project to use one runtime and restart the build.");
                }
                runtime = runtimeI.getClass().getConstructor(params.getClass()).newInstance(params);
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new MojoExecutionException("Error while looking for Boost runtime.");
        }

        return runtime;
    }

    protected ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }

}
