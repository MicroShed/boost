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

public class DockerizeLibertySpringBootJar extends SpringDockerizer {

    private static final String LIBERTY_IMAGE_1 = "open-liberty:springBoot1";
    private static final String LIBERTY_IMAGE_2 = "open-liberty:springBoot2";
    private static final String LIB_INDEX_CACHE = "lib.index.cache";
    private static final String ARG_SOURCE_APP = "--sourceAppPath";
    private static final String ARG_DEST_THIN_APP = "--targetThinAppPath";
    private static final String ARG_DEST_LIB_CACHE = "--targetLibCachePath";
    private static final String FROM = "FROM ";
    private static final String COPY = "COPY ";
    private static final String RUN = "RUN ";

    public DockerizeLibertySpringBootJar(MavenProject project, File appArchive, Log log) {
    		super(project, appArchive, log);
    }
    
    public Map<String, String> getBuildArgs() {
        Map<String, String> buildArgs = new HashMap<String, String>();
        buildArgs.put("APP_FILE", appArchive.getName());
        return buildArgs;
    }

    private String getLibertySpringBootBaseImage() throws MojoExecutionException {
        String libertyImage = null;
        if (springBootVersion.startsWith("1.")) {
            libertyImage = LIBERTY_IMAGE_1;
        } else if (springBootVersion.startsWith("2.")) {
            libertyImage = LIBERTY_IMAGE_2;
		} else {
			throw new MojoExecutionException(
                    "No supporting docker image found for Open Liberty for the Spring Boot version "
                            + springBootVersion);
        }
        return libertyImage;
    }

    public List<String> getDockerfileLines() throws MojoExecutionException {
        String libertyImage = getLibertySpringBootBaseImage();
        ArrayList<String> lines = new ArrayList<>();
        lines.add(BOOST_GEN);
        lines.add(FROM + libertyImage + " as " + "staging");

        lines.add("");
        lines.add("# The APP_FILE ARG provides the final name of the Spring Boot application archive");
        lines.add("ARG" + " " + "APP_FILE");

        lines.add("");
        lines.add("# Stage the fat JAR");
        lines.add(COPY + outputDirectory.getName() + "/" + "${APP_FILE}" + " " + "/staging/" + "${APP_FILE}");

        lines.add("");
        lines.add("# Thin the fat application; stage the thin app output and the library cache");
        lines.add(RUN + "springBootUtility thin " + ARG_SOURCE_APP + "=" + "/staging/" + "${APP_FILE}" + " "
                + ARG_DEST_THIN_APP + "=" + "/staging/" + "thin-${APP_FILE}" + " " + ARG_DEST_LIB_CACHE + "="
                + "/staging/" + LIB_INDEX_CACHE);

        lines.add("");
        lines.add("# Final stage, only copying the liberty installation (includes primed caches)");
        lines.add("# and the lib.index.cache and thin application");
        lines.add(FROM + libertyImage);
        lines.add("ARG" + " " + "APP_FILE");
        lines.add(COPY + "--from=staging " + "/staging/" + LIB_INDEX_CACHE + " " + "/" + LIB_INDEX_CACHE);
        lines.add(COPY + "--from=staging " + "/staging/thin-${APP_FILE}" + " "
                + "/config/dropins/spring/thin-${APP_FILE}");
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
