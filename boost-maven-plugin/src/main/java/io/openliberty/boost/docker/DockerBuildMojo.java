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
package io.openliberty.boost.docker;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerBuildMojo extends AbstractDockerMojo {
    /**
     * Do not pull newer base image and use the cache instead.
     */
    @Parameter(property = "pullNewerImage", defaultValue = "false")
    private boolean pullNewerImage;

    /**
     * Do not use cache when building the image.
     */
    @Parameter(property = "noCache", defaultValue = "false")
    private boolean noCache;

    /**
     * Custom build arguments.
     */
    @Parameter(property = "buildArgs")
    private Map<String, String> buildArgs;

    @Override
    protected void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException {
        try {
            File appArchive = getAppArchive();
            // Create a Dockerfile for the application
            Dockerize dockerize = new Dockerize(project, outputDirectory, appArchive);
            dockerize.createDockerFile();
            
            buildDockerImage(dockerClient, appArchive);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Use the DockerClient to build the image
     * 
     */
    private void buildDockerImage(DockerClient dockerClient, File appArchive)
            throws MojoExecutionException, IOException {
        final ArrayList<DockerClient.BuildParam> buildParameters = new ArrayList<>();
        final DockerClient.BuildParam[] buildParametersArray;
        final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
        final String imageName = getImageName();

        if (pullNewerImage) {
            buildParameters.add(DockerClient.BuildParam.pullNewerImage());
        }
        if (noCache) {
            buildParameters.add(DockerClient.BuildParam.noCache());
        }

        buildArgs.put("APP_FILE", appArchive.getName());

        try {
            final String encodedBuildArgs = URLEncoder.encode(new Gson().toJson(buildArgs), "utf-8");
            buildParameters.add(new DockerClient.BuildParam("buildargs", encodedBuildArgs));
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Could not build image", e);
        }

        buildParametersArray = buildParameters.toArray(new DockerClient.BuildParam[buildParameters.size()]);

        log.info("Image will be built as " + imageName);
        log.info("");
        try {
            dockerClient.build(projectDirectory.toPath(), getImageName(), progressHandler, buildParametersArray);
        } catch (DockerException | InterruptedException e) {
            throw new MojoExecutionException("Could not build image", e);
        }
    }

}
