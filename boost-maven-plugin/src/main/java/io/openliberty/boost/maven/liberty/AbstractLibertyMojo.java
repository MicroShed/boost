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

}
