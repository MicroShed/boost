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

@Mojo(name = "docker-push", defaultPhase = LifecyclePhase.DEPLOY)
public class DockerPushMojo extends AbstractBoostMojo {
    
    /**
     * The repository to put the built image into, <tt>${project.artifactId}</tt> is used by deafult.
     * You should also set the <tt>tag</tt> parameter, otherwise the tag <tt>${project.version}</tt> 
     * is used by default.
     */
    @Parameter(property = "repository", defaultValue = "${project.artifactId}")
    private String repository;

    /**
     * The tag to apply to the built image.
     */
    @Parameter(property = "tag", defaultValue = "${project.version}")
    private String tag;

    /**
     * Disables the push goal; it becomes a no-op.
     */
    @Parameter(property = "push.skip", defaultValue = "false")
    private boolean skipPush;
    
    /**
     * A maven server id, in order to use maven settings to supply server auth.
     */
    @Parameter(property = "useMavenSettingsForAuth", defaultValue = "false")
    private boolean useMavenSettingsForAuth;
    
    @Parameter(property = "username")
    private String username;

    @Parameter(property = "password")
    private String password;


    public void execute() throws MojoExecutionException {
        pushDockerImage();
    }

    /**
     * Invoke the spotify plugin to run the goal
     * 
     */
    private void pushDockerImage() throws MojoExecutionException {
        if(mavenProject.getArtifactId().equals(repository)) {
            throw new MojoExecutionException("Cannot push the image with the default image name. Provide repository configuration with [REGISTRYHOST/][USERNAME/]NAME and run docker-tag before");
        }
        executeMojo(plugin(groupId("com.spotify"), artifactId("dockerfile-maven-plugin"), version("1.4.3")),
                goal("push"), 
                configuration(element(name("repository"), repository), 
                              element(name("tag"), tag),                                               
                              element(name("skipPush"),String.valueOf(skipPush)),
                              element(name("useMavenSettingsForAuth"), String.valueOf(useMavenSettingsForAuth)),
                              element(name("username"), username),
                              element(name("password"), password)
                              ), 
                executionEnvironment(mavenProject, mavenSession, pluginManager));
    }
}
