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
package io.openliberty.boost.docker;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.exceptions.DockerException;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.docker.dockerizer.Dockerizer;
import io.openliberty.boost.docker.dockerizer.spring.DockerizeLibertySpringBootJar;
import io.openliberty.boost.docker.dockerizer.spring.DockerizeSpringBootClasspath;
import io.openliberty.boost.docker.dockerizer.spring.DockerizeSpringBootJar;
import net.wasdev.wlp.maven.plugins.utils.SpringBootUtil;

/**
 * Builds a docker image from a packaged application. This goal will either
 * generate a Dockerfile or use the existing Dockerfile if it already exists. An
 * ignore file (.dockerignore) will be created if one does not exist, and if it
 * does exist the same entries will be appended to the existing .dockerignore.
 */
@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerBuildMojo extends AbstractDockerMojo {
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
	protected void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException {
		try {
			File appArchive = getAppArchive();

			// Create a Dockerfile for the application
			Dockerizer dockerizer = getDockerizer(project, appArchive, log);
			dockerizer.createDockerFile();
			dockerizer.createDockerIgnore();

			buildDockerImage(dockerClient, dockerizer);
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

		// Strictly speaking, the Liberty Uber JAR (in the "unboosted" SpringBoot Uber
		// JAR location), is not needed to build the Docker image. However, let's throw
		// an exception if we get here and it doesn't exist, assuming we've done
		// something wrong. If it turns out we want to relax this later, we could.
		File unboostedSpringBootUberJarLocation = net.wasdev.wlp.maven.plugins.utils.SpringBootUtil
				.getSpringBootUberJARLocation(project, getLog());
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

	private Dockerizer getDockerizer(MavenProject project, File appArchive, Log log) {
		// TODO: Needed future enhancements:
		// 1. Is it Spring or something else? sense with MavenProjectUtil.findSpringBootVersion(project);
		// 2. Use OpenJ9 or HotSpot? sense with property boost.docker.jvm
		if ("jar".equalsIgnoreCase(dockerizer)) {
			return new DockerizeSpringBootJar(project, appArchive, log);
		}
		if ("classpath".equalsIgnoreCase(dockerizer)) {
			return new DockerizeSpringBootClasspath(project, appArchive, log);
		}
		// TODO: Maybe don't make the Spring Boot dockerizer default after EE stuff is added
		// The current property values of 'jar', 'classpath' and 'liberty' are intentionally
		// generic so that they can be applied irrespective of the project type (Spring vs EE)
		return new DockerizeLibertySpringBootJar(project, appArchive, log);
	}

	/**
	 * Use the DockerClient to build the image
	 * 
	 */
	private void buildDockerImage(DockerClient dockerClient, Dockerizer dockerizer)
			throws MojoExecutionException, IOException {
		final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
		final String imageName = getImageName();
		BuildParam[] buidParams = getBuildParams(dockerizer);
		log.info(""); // Adding empty log for formatting purpose
		log.info("Building image: " + imageName);
		try {
			dockerClient.build(project.getBasedir().toPath(), imageName, progressHandler, buidParams);
		} catch (DockerException | InterruptedException e) {
			throw new MojoExecutionException("Unable to build image", e);
		}
	}

	private BuildParam[] getBuildParams(Dockerizer dockerizer) throws MojoExecutionException {
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
			throw new MojoExecutionException("Unable to build image", e);
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
	private static String getPathMessageText(File artifact) {
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
