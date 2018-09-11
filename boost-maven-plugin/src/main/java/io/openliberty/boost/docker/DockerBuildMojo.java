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
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.exceptions.DockerException;

import io.openliberty.boost.BoostException;
import net.wasdev.wlp.maven.plugins.utils.SpringBootUtil;

/**
 * Builds a docker image from a packaged application.
 *
 */
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
            Dockerize dockerize = new Dockerize(project, new File(project.getBuild().getDirectory()), appArchive, log);
            dockerize.createDockerFile();

            buildDockerImage(dockerClient, appArchive);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Find the location of the Spring Boot Uber JAR
     * 
     * @throws BoostException
     */
    private File getAppArchive() throws BoostException {
        File appArchive;

        // First try to get the Spring Boot Uber JAR as the project artifact as a result
        // of the spring-boot-maven-plugin execution, handling classifier scenario if
        // necessary.
        appArchive = SpringBootUtil.getSpringBootUberJAR(project, getLog());
        if (appArchive != null) {
            return appArchive;
        }

        // If Boost replaced the project artifact, then the appArchive path will
        // actually point to the Liberty Uber JAR. Check if this is the case and if so,
        // use the .spring artifact that we preserved during the Boost packaging
        // process.
        appArchive = new File(io.openliberty.boost.utils.SpringBootUtil
                .getBoostedSpringBootUberJarPath(project.getArtifact().getFile()));
        if (appArchive != null && appArchive.exists() && appArchive.isFile()
                && net.wasdev.wlp.common.plugins.util.SpringBootUtil.isSpringBootUberJar(appArchive)) {
            getLog().info("Found Spring Boot Uber JAR with .spring extension.");
            return appArchive;
        }

        // At this point we did not find the Spring Boot Uber JAR in any of its expected
        // locations.
        throw new BoostException("Could not find Spring Boot Uber JAR.");
    }

    /**
     * Use the DockerClient to build the image
     * 
     */
    private void buildDockerImage(DockerClient dockerClient, File appArchive)
            throws MojoExecutionException, IOException {
        final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
        final String imageName = getImageName();
        BuildParam[] buidParams = getBuildParams(appArchive);
        log.info("Building image: " + imageName);
        try {
            dockerClient.build(project.getBasedir().toPath(), imageName, progressHandler, buidParams);
        } catch (DockerException | InterruptedException e) {
            throw new MojoExecutionException("Unable to build image", e);
        }
    }

    private BuildParam[] getBuildParams(File appArchive) throws MojoExecutionException {
        final ArrayList<BuildParam> buildParamsList = new ArrayList<>();
        final BuildParam[] buildParams;
        if (pullNewerImage) {
            buildParamsList.add(BuildParam.pullNewerImage());
        }
        if (noCache) {
            buildParamsList.add(BuildParam.noCache());
        }

        buildArgs.put("APP_FILE", appArchive.getName());

        try {
            final String encodedBuildArgs = URLEncoder.encode(new Gson().toJson(buildArgs), "utf-8");
            buildParamsList.add(new BuildParam("buildargs", encodedBuildArgs));
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Unable to build image", e);
        }

        buildParams = buildParamsList.toArray(new BuildParam[buildParamsList.size()]);
        return buildParams;
    }

}
