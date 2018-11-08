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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import io.openliberty.boost.docker.dockerizer.Dockerizer;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.utils.MavenProjectUtil;
import net.wasdev.wlp.common.plugins.util.SpringBootUtil;

public abstract class SpringDockerizer extends Dockerizer {

    protected final String springBootVersion = MavenProjectUtil.findSpringBootVersion(project);

    public SpringDockerizer(MavenProject project, File appArchive, Log log) {
        super(project, appArchive, log);
    }

    /**
     * Creates a Dockerfile with appropriate LibertyBaseImage to build an efficient
     * Docker image of the Spring Boot application.
     *
     * @throws Exception
     */
    @Override
    public void createDockerFile() throws MojoExecutionException {
        if (BoostUtil.isNotNullOrEmpty(springBootVersion)) {
            if (SpringBootUtil.isSpringBootUberJar(appArchive)) {
                BoostUtil.extract(appArchive, projectDirectory);
                String startClass = getSpringStartClass();
                File dockerFile = createNewDockerFile();
                if (dockerFile != null) { // File was created
                    writeSpringBootDockerFile(dockerFile, startClass);
                }
            } else {
                throw new MojoExecutionException(appArchive.getAbsolutePath() + " file is not an executable archive. "
                        + "The repackage goal of the spring-boot-maven-plugin must be configured to run first in order to create the required executable archive.");
            }
        } else {
            throw new MojoExecutionException("Unable to create a Dockerfile because application type is not supported");
        }
    }

    protected String getSpringStartClass() throws MojoExecutionException {
        try (JarFile jarFile = new JarFile(appArchive)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue("Start-Class");
            } else {
                throw new MojoExecutionException(
                        "Could not get Spring Boot start class due to error getting app manifest.");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not get Spring Boot start class due to error opening app archive.",
                    e);
        }
    }

    private File createNewDockerFile() throws MojoExecutionException {
        try {
            File dockerFile = new File(projectDirectory, "Dockerfile");
            Files.createFile(dockerFile.toPath());
            log.info("Creating Dockerfile: " + dockerFile.getAbsolutePath());
            return dockerFile;
        } catch (FileAlreadyExistsException e1) {
            log.warn("Dockerfile already exists");
            return null;
        } catch (IOException e2) {
            throw new MojoExecutionException("Could not create Dockerfile.", e2);
        }
    }

    private void writeSpringBootDockerFile(File dockerFile, String startClass) throws MojoExecutionException {
        try {
            Files.write(dockerFile.toPath(), getDockerfileLines(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write Spring Boot Dockerfile.", e);
        }
    }

}
