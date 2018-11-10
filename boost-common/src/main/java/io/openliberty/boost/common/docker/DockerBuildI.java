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
package io.openliberty.boost.common.docker;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.exceptions.DockerException;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.docker.dockerizer.spring.DockerizeLibertySpringBootJar;
import io.openliberty.boost.common.docker.dockerizer.spring.DockerizeSpringBootClasspath;
import io.openliberty.boost.common.docker.dockerizer.spring.DockerizeSpringBootJar;
import io.openliberty.boost.common.docker.dockerizer.spring.SpringDockerizer;

public abstract interface DockerBuildI extends AbstractDockerI {

    public File getAppArchive() throws BoostException;

    // Default methods

    default public void dockerBuild(String dockerizer, DockerClient dockerClient, File projectDirectory,
            File outputDirectory, String springBootVersion, boolean pullNewerImage, boolean noCache,
            Map<String, String> buildArgs, String repository, String tag, BoostLoggerI log) throws BoostException {
        try {
            File appArchive = getAppArchive();

            // Create a Dockerfile for the application
            SpringDockerizer springDockerizer = getDockerizer(dockerizer, projectDirectory, outputDirectory, appArchive,
                    springBootVersion, log);
            springDockerizer.createDockerFile();
            springDockerizer.createDockerIgnore();

            buildDockerImage(projectDirectory.toPath(), dockerClient, springDockerizer, pullNewerImage, noCache,
                    buildArgs, repository, tag, log);
        } catch (Exception e) {
            throw new BoostException(e.getMessage(), e);
        }
    }

    default public SpringDockerizer getDockerizer(String dockerizer, File projectDirectory, File outputDirectory,
            File appArchive, String springBootVersion, BoostLoggerI log) {

        // TODO: Needed future enhancements:
        // 1. Is it Spring or something else? sense with
        // MavenProjectUtil.findSpringBootVersion(project);
        // 2. Use OpenJ9 or HotSpot? sense with property boost.docker.jvm
        if ("jar".equalsIgnoreCase(dockerizer)) {
            return new DockerizeSpringBootJar(projectDirectory, outputDirectory, appArchive, springBootVersion, log);
        }
        if ("classpath".equalsIgnoreCase(dockerizer)) {
            return new DockerizeSpringBootClasspath(projectDirectory, outputDirectory, appArchive, springBootVersion,
                    log);
        }
        // TODO: Maybe don't make the Spring Boot dockerizer default after EE stuff is
        // added
        // The current property values of 'jar', 'classpath' and 'liberty' are
        // intentionally
        // generic so that they can be applied irrespective of the project type (Spring
        // vs EE)
        return new DockerizeLibertySpringBootJar(projectDirectory, outputDirectory, appArchive, springBootVersion, log);
    }

    /**
     * Use the DockerClient to build the image
     * 
     */
    default public void buildDockerImage(Path baseDir, DockerClient dockerClient, SpringDockerizer dockerizer,
            boolean pullNewerImage, boolean noCache, Map<String, String> buildArgs, String repository, String tag,
            BoostLoggerI log) throws BoostException, IOException {
        final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
        final String imageName = getImageName(repository, tag);
        BuildParam[] buidParams = getBuildParams(dockerizer, pullNewerImage, noCache, buildArgs);
        log.info(""); // Adding empty log for formatting purpose
        log.info("Building image: " + imageName);
        try {
            dockerClient.build(baseDir, imageName, progressHandler, buidParams);
        } catch (DockerException | InterruptedException e) {
            throw new BoostException("Unable to build image", e);
        }
    }

    default BuildParam[] getBuildParams(SpringDockerizer dockerizer, boolean pullNewerImage, boolean noCache,
            Map<String, String> buildArgs) throws BoostException {
        final ArrayList<BuildParam> buildParamsList = new ArrayList<>();
        final BuildParam[] buildParams;
        if (pullNewerImage) {
            buildParamsList.add(BuildParam.pullNewerImage());
        }
        if (noCache) {
            buildParamsList.add(BuildParam.noCache());
        }

        buildArgs.putAll(dockerizer.getBuildArgs());

        try {
            final String encodedBuildArgs = URLEncoder.encode(new Gson().toJson(buildArgs), "utf-8");
            buildParamsList.add(new BuildParam("buildargs", encodedBuildArgs));
        } catch (UnsupportedEncodingException e) {
            throw new BoostException("Unable to build image", e);
        }

        buildParams = buildParamsList.toArray(new BuildParam[buildParamsList.size()]);
        return buildParams;
    }

    /**
     * Get the artifact path
     *
     * @param artifact
     * @return the canonical path, if it can be obtained successfully, otherwise the
     *         absolute path
     */
    default String getPathMessageText(File artifact) {
        String retVal = null;
        try {
            if (artifact != null) {
                retVal = artifact.getCanonicalPath();
            }
        } catch (IOException ioexc) {
            retVal = artifact.getAbsolutePath();
        }
        return retVal;
    }

}
