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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import io.openliberty.boost.utils.MavenProjectUtil;
import net.wasdev.wlp.common.plugins.util.SpringBootUtil;

public class Dockerize {

    private final MavenProject project;
    private final File projectDirectory;
    private final File outputDirectory;
    private final File appArchive;
    private final Log log;
    private final String springBootVersion;

    private static final String LIBERTY_IMAGE_1 = "open-liberty:springBoot1";
    private static final String LIBERTY_IMAGE_2 = "open-liberty:springBoot2";
    private static final String LIB_INDEX_CACHE = "lib.index.cache";
    private static final String ARG_SOURCE_APP = "--sourceAppPath";
    private static final String ARG_DEST_THIN_APP = "--targetThinAppPath";
    private static final String ARG_DEST_LIB_CACHE = "--targetLibCachePath";
    private static final String FROM = "FROM ";
    private static final String COPY = "COPY ";
    private static final String RUN = "RUN ";
    
    public Dockerize(MavenProject project, File appArchive, Log log) {
        this.project = project;
        this.projectDirectory = project.getBasedir();
        this.outputDirectory = new File(project.getBuild().getDirectory());
        this.appArchive = appArchive;
        this.log = log;
        this.springBootVersion = MavenProjectUtil.findSpringBootVersion(project);
    }

    public void createDockerFile() throws Exception {        
        if (springBootVersion != null) {
            createSpringBootDockerFile(springBootVersion);
        } else {
            throw new MojoExecutionException("Unable to create a Dockerfile because Application type is not supported");
        }
    }

    /**
     * Create a Dockerfile with appropriate LibertyBaseImage to build an efficient
     * docker image of the Spring Boot application.
     * 
     * @param springBootVersion
     * @throws MojoExecutionException
     * @throws IOException
     */
    private void createSpringBootDockerFile(String springBootVersion) throws MojoExecutionException, IOException {
        try {
            if (SpringBootUtil.isSpringBootUberJar(appArchive)) {
                File dockerFile = createNewDockerFile(projectDirectory);
                String libertySBImage = getLibertySpringBootBaseImage(springBootVersion);
                writeSpringBootDockerFile(dockerFile, libertySBImage);
            } else {
                throw new MojoExecutionException(appArchive.getAbsolutePath() + " file is not an executable archive. "
                        + "The repackage goal of the spring-boot-maven-plugin must be configured to run first in order to create the required executable archive.");
            }
        } catch (FileAlreadyExistsException e1) {
            log.warn("Dockerfile already exists");
        }
    }

    private File createNewDockerFile(File dockerfileDirectory) throws IOException {
        File dockerFile = new File(dockerfileDirectory, "Dockerfile");
        Files.createFile(dockerFile.toPath());
        log.info("Creating Dockerfile: " + dockerFile.getAbsolutePath());
        return dockerFile;
    }

    private String getLibertySpringBootBaseImage(String springBootVersion) throws MojoExecutionException {
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

    private void writeSpringBootDockerFile(File dockerFile, String libertyImage) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(FROM + libertyImage + " as " + "staging");

        lines.add("\n");
        lines.add("# The APP_FILE ARG provides the final name of the Spring Boot application archive");
        lines.add("ARG" + " " + "APP_FILE");

        lines.add("\n");
        lines.add("# Stage the fat JAR");
        lines.add(COPY + outputDirectory.getName() + "/" + "${APP_FILE}" + " " + "/staging/" + "${APP_FILE}");

        lines.add("\n");
        lines.add("# Thin the fat application; stage the thin app output and the library cache");
        lines.add(RUN + "springBootUtility thin " + ARG_SOURCE_APP + "=" + "/staging/" + "${APP_FILE}" + " "
                + ARG_DEST_THIN_APP + "=" + "/staging/" + "thin-${APP_FILE}" + " " + ARG_DEST_LIB_CACHE + "="
                + "/staging/" + LIB_INDEX_CACHE);

        lines.add("\n");
        lines.add("# Final stage, only copying the liberty installation (includes primed caches)");
        lines.add("# and the lib.index.cache and thin application");
        lines.add(FROM + libertyImage);

        lines.add("\n");
        lines.add("ARG" + " " + "APP_FILE");

        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/" + LIB_INDEX_CACHE + " " + "/" + LIB_INDEX_CACHE);

        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/thin-${APP_FILE}" + " "
                + "/config/dropins/spring/thin-${APP_FILE}");
        Files.write(dockerFile.toPath(), lines, Charset.forName("UTF-8"));
    }

    public void createDockerIgnore() throws Exception {
        if (springBootVersion != null) {
            createSpringBootDockerIgnore();
        } else {
            throw new MojoExecutionException("Unable to create a .dockerignore file because application type is not supported");
        }      
    }

    private void createSpringBootDockerIgnore() throws IOException {
        File dockerIgnore = new File(projectDirectory, ".dockerignore");
        boolean dockerIgnoreAlreadyExists = false;
        try {
            Files.createFile(dockerIgnore.toPath());
            log.info("Creating .dockerignore: "+ dockerIgnore.getAbsolutePath());          
        } catch (FileAlreadyExistsException e) {
            log.warn("The .dockerignore file already exists");
            dockerIgnoreAlreadyExists = true;
        }
        writeDockerIgnore(dockerIgnore, dockerIgnoreAlreadyExists);
    }

    private void writeDockerIgnore(File dockerIgnore, boolean dockerIgnoreAlreadyExists) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        if (pluginAlreadyExecuted(dockerIgnore)) {
            return;
        }

        //Ignore the build.log getting generated by integration tests and the liberty runtime folder
        if (dockerIgnoreAlreadyExists && dockerIgnore.length() > 0) {
            lines.add("\n");
        }
        lines.add("# The following lines are added by boost-maven-plugin");           
        lines.add("build.log");          
        lines.add("target/liberty"); 
        Files.write(dockerIgnore.toPath(), lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
    }

    private boolean pluginAlreadyExecuted(File dockerIgnore) throws IOException {
        try (FileReader fileReader = new FileReader(dockerIgnore)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                   if ("# The following lines are added by boost-maven-plugin".equals(line)) {
                       return true;
                   }
                }
            }
        }
        return false;
    }
}
