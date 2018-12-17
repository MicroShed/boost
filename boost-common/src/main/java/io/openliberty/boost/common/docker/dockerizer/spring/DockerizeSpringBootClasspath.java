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
package io.openliberty.boost.common.docker.dockerizer.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.docker.DockerParameters;

public class DockerizeSpringBootClasspath extends SpringDockerizer {

    public DockerizeSpringBootClasspath(File projectDirectory, File outputDirectory, File appArchive,
            String springBootVersion, DockerParameters params, BoostLoggerI log) {
        super(projectDirectory, outputDirectory, appArchive, springBootVersion, params, log);
    }

    public Map<String, String> getBuildArgs() {
        // Nothing at this time
        return new HashMap<String, String>();
    }

    @Override
    public List<String> getDockerfileLines() throws BoostException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(BOOST_GEN);
        lines.add("FROM adoptopenjdk/openjdk8-openj9");
        lines.add("VOLUME /tmp");
        lines.add("ARG DEPENDENCY=" + params.getDependencyFolder());
        lines.add("COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib");
        lines.add("COPY ${DEPENDENCY}/META-INF /app/META-INF");
        lines.add("COPY ${DEPENDENCY}/BOOT-INF/classes /app");
        lines.add("ENTRYPOINT [\"java\",\"-cp\",\"app:app/lib/*\",\"" + getSpringStartClass() + "\"]");
        return lines;
    }

    @Override
    public List<String> getDockerIgnoreList() {
        List<String> lines = new ArrayList<String>();
        lines.add("*.log");
        lines.add("target/liberty");
        lines.add(".gradle/");
        lines.add("build/wlp");
        return lines;
    }

}
