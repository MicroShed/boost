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
package io.openliberty.boost.docker.dockerizer.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class DockerizeSpringBootJar extends SpringDockerizer {

	public DockerizeSpringBootJar(MavenProject project, File appArchive, Log log) {
		super(project, appArchive, log);
	}

	public Map<String, String> getBuildArgs() {
		Map<String, String> buildArgs = new HashMap<String, String>();
		buildArgs.put("JAR_FILE", "target/" + appArchive.getName());
		return buildArgs;
	}

	public List<String> getDockerfileLines() throws MojoExecutionException {
		ArrayList<String> lines = new ArrayList<>();
		lines.add(BOOST_GEN);
		lines.add("FROM adoptopenjdk/openjdk8-openj9");
		lines.add("VOLUME /tmp");
		lines.add("ARG JAR_FILE");
		lines.add("COPY ${JAR_FILE} app.jar");
		lines.add("ENTRYPOINT [\"java\",\"-Djava.security.egd=file:/dev/./urandom\",\"-jar\",\"/app.jar\"]");
		return lines;
	}

	@Override
	public List<String> getDockerIgnoreList() {
		List<String> lines = new ArrayList<String>();
		lines.add("*.log");
		lines.add("target/liberty");
		return lines;
	}

}
