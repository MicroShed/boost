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
package io.openliberty.boost.maven.wildfly;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

public abstract class AbstractWildflyMojo extends MojoSupport {


    protected String wildflyMavenPluginGroupId = "org.wildfly.plugins";
    protected String wildflyMavenPluginArtifactId = "wildfly-maven-plugin";

    protected String mavenDependencyPluginGroupId = "org.apache.maven.plugins";
    protected String mavenDependencyPluginArtifactId = "maven-dependency-plugin";

    @Parameter(defaultValue = "2.0.1.Final", readonly = true)
    protected String wildflyMavenPluginVersion;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected String projectBuildDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;
    
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;
    
    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true )
    protected List<RemoteRepository> remoteRepos;
    
    @Component
    protected RepositorySystem repoSystem;

    @Component
    protected BuildPluginManager pluginManager;

    @Parameter
    protected ArtifactItem runtimeArtifact;

    @Parameter(property = "useDefaultHost", defaultValue = "false", readonly = true)
    protected boolean useDefaultHost;

    protected Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(wildflyMavenPluginGroupId), artifactId(wildflyMavenPluginArtifactId),
                version(wildflyMavenPluginVersion));
    }

    protected Plugin getMavenDependencyPlugin() throws MojoExecutionException {
        return plugin(groupId(mavenDependencyPluginGroupId), artifactId(mavenDependencyPluginArtifactId));
    }

    protected Element getRuntimeArtifactElement() {
        return element(name("artifactItem"), element(name("groupId"), runtimeArtifact.getGroupId()),
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
            runtimeArtifact.setGroupId("org.wildfly");
            runtimeArtifact.setArtifactId("wildfly-dist");
            runtimeArtifact.setVersion("16.0.0.Final");
            runtimeArtifact.setType("zip");
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        createDefaultRuntimeArtifactIfNeeded();
    }

}
