/*-
 ******************************************************************************
 * This file was copied from source:
 *
 *    https://github.com/spotify/dockerfile-maven/blob/2fc613abcad667f6b99d1e99124c02cc7b1c6bbd/plugin/src/main/java/com/spotify/plugin/dockerfile/BuildMojo.java
 *    
 * and modified below.
 *
 ******************************************************************************
 * -------------------
 * ORIGINAL COPYRIGHT:
 * -------------------
 *
 * -\-\-
 * Dockerfile Maven Plugin
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 ******************************************************************************
 */

/*
 ******************************************************************************
 * We now include the copyright for our modification:

 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (after modifications describe above)
 ******************************************************************************
 */
package io.openliberty.boost.maven.docker;

import java.io.File;
import java.util.Map;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spotify.docker.client.DockerClient;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.docker.DockerBuildI;
import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;
import net.wasdev.wlp.maven.plugins.utils.SpringBootUtil;

/**
 * Builds a docker image from a packaged application. This goal will either
 * generate a Dockerfile or use the existing Dockerfile if it already exists. An
 * ignore file (.dockerignore) will be created if one does not exist, and if it
 * does exist the same entries will be appended to the existing .dockerignore.
 */
@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerBuildMojo extends AbstractDockerMojo implements DockerBuildI {
    /**
     * Pull newer version of the image, if set. Use the cache by default when
     * building the image.
     */
    @Parameter(property = "pullNewerImage", defaultValue = "false")
    private boolean pullNewerImage;

    /**
     * Do not use cache when building the image.
     */
    @Parameter(property = "noCache", defaultValue = "false")
    private boolean noCache;

    /**
     * Set build time variables.
     */
    @Parameter(property = "buildArgs")
    private Map<String, String> buildArgs;

    /**
     * Sets the type of docker build to run.
     */
    @Parameter(property = "dockerizer", defaultValue = "liberty")
    private String dockerizer;

    @Override
    public void execute(DockerClient dockerClient) throws BoostException {
        File projectDirectory = project.getBasedir();
        File outputDirectory = new File(project.getBuild().getDirectory());
        String springBootVersion = MavenProjectUtil.findSpringBootVersion(project);
        dockerBuild(dockerizer, dockerClient, projectDirectory, outputDirectory, springBootVersion, pullNewerImage,
                noCache, buildArgs, repository, tag, BoostLogger.getInstance());
    }

    /**
     * Find the location of the Spring Boot Uber JAR
     * 
     * @throws BoostException
     */
    @Override
    public File getAppArchive() throws BoostException {
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

        // Strictly speaking, the Liberty Uber JAR (in the "unboosted" SpringBoot Uber
        // JAR location), is not needed to build the Docker image. However, let's throw
        // an exception if we get here and it doesn't exist, assuming we've done
        // something wrong. If it turns out we want to relax this later, we could.
        File unboostedSpringBootUberJarLocation = SpringBootUtil.getSpringBootUberJARLocation(project, getLog());
        if (!unboostedSpringBootUberJarLocation.exists()) {
            String excMsg = "Expected file does not exist at path = "
                    + getPathMessageText(unboostedSpringBootUberJarLocation)
                    + ". Make sure you have executed the spring-boot:repackage goal first before attempting the current goal.";
            throw new BoostException(excMsg);
        }
        // Get the Boost location for the SpringBoot Uber JAR
        appArchive = new File(io.openliberty.boost.common.utils.SpringBootUtil
                .getBoostedSpringBootUberJarPath(unboostedSpringBootUberJarLocation));
        if (!appArchive.exists()) {
            String excMsg = "Expected file does not exist at path = " + getPathMessageText(appArchive)
                    + ". Make sure you have executed the spring-boot:repackage goal first before attempting the current goal.";
            throw new BoostException(excMsg);
        }
        if (net.wasdev.wlp.common.plugins.util.SpringBootUtil.isSpringBootUberJar(appArchive)) {
            getLog().info("Found Spring Boot Uber JAR with .spring extension.");
            return appArchive;
        }

        // At this point we did not find the Spring Boot Uber JAR in any of its expected
        // locations.
        throw new BoostException("Could not find Spring Boot Uber JAR.");
    }

}
