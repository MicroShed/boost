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

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "docker-tag", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerTagMojo extends AbstractBoostMojo {
    /**
     * The repository to put the built image into.  You should also
     * set the <tt>tag</tt> parameter, otherwise the tag <tt>${project.version}</tt> is used by default.
     */
    @Parameter(property = "repository", required = true)
    private String repository;

    /**
     * The tag to apply to the built image.
     */
    @Parameter(property = "tag", defaultValue = "${project.version}", required = true)
    private String tag;

    /**
     * Whether to force re-assignment of an already assigned tag.
     */
    @Parameter(property = "force", defaultValue = "true", required = true)
    private boolean force;

    /**
     * Disables the tag goal; it becomes a no-op.
     */
    @Parameter(property = "tag.skip", defaultValue = "false")
    private boolean skipTag;


    public void execute() throws MojoExecutionException {
        tagDockerImage();
    }


    /**
     * Invoke the spotify plugin to run the goal
     * 
     */
    private void tagDockerImage() throws MojoExecutionException {
        executeMojo(plugin(groupId("com.spotify"), 
                           artifactId("dockerfile-maven-plugin"), 
                           version("1.4.3")),
                           goal("tag"),
                           configuration(element(name("repository"), repository), 
                                         element(name("tag"), tag),
                                         element(name("force"), String.valueOf(force)),
                                         element(name("skipTag"),String.valueOf(skipTag))
                                         ),
                           executionEnvironment(mavenProject, mavenSession, pluginManager));
    }
}
