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

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerBuildMojo extends AbstractBoostMojo {
    /**
     * Directory containing the Dockerfile to build.
     */
    @Parameter(defaultValue = "${project.basedir}",
        property = "contextDirectory",
        required = true)
    private File contextDirectory;

    /**
     * The repository to put the built image into when building the Dockerfile,
     * <tt>${project.artifactId}</tt> is used by default.
     * You should also set the <tt>tag</tt> parameter, otherwise the tag
     * <tt>${project.version}</tt> is used by default.  The <tt>tag</tt> goal needs to
     * be ran separately in order to tag the generated image with anything.
     */
    @Parameter(property = "repository", defaultValue = "${project.artifactId}")
    private String repository;

    /**
     * The tag to apply when building the Dockerfile, which is appended to the repository.
     */
    @Parameter(property = "tag", defaultValue = "${project.version}")
    private String tag;

    /**
     * Disables the build goal; it becomes a no-op.
     */
    @Parameter(property = "build.skip", defaultValue = "false")
    private boolean skipBuild;

    /**
     * Updates base images automatically.
     */
    @Parameter(property = "build.pullNewerImage", defaultValue = "true")
    private boolean pullNewerImage;

    /**
     * Do not use cache when building the image.
     */
    @Parameter(property = "build.noCache", defaultValue = "false")
    private boolean noCache;
    
    
    public void execute() throws MojoExecutionException {
        try {
            buildDockerImage();
        } catch (IOException e) {
           throw new MojoExecutionException(e.getMessage(), e);
        }    
    }

    /**
     * Invoke the spotify plugin to run the goal
     * @throws IOException 
     * 
     */
    private void buildDockerImage() throws MojoExecutionException, IOException {
        executeMojo(plugin(groupId("com.spotify"), 
                           artifactId("dockerfile-maven-plugin"), 
                           version("1.4.3")),
                           goal("build"),
                           configuration(element(name("repository"), repository), 
                                         element(name("tag"), tag),
                                         element(name("contextDirectory"), contextDirectory.getCanonicalPath()),
                                         element(name("skipBuild"), String.valueOf(skipBuild)),
                                         element(name("pullNewerImage"), String.valueOf(pullNewerImage)),
                                         element(name("noCache"), String.valueOf(noCache)),
                                         element(name("buildArgs"),
                                                 element(name("APP_FILE"), getTargetFile().getName()))
                                         ),
                           executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

}
