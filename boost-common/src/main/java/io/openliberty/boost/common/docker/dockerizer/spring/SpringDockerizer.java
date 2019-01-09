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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.docker.DockerParameters;
import io.openliberty.boost.common.docker.dockerizer.Dockerizer;
import io.openliberty.boost.common.utils.BoostUtil;
import net.wasdev.wlp.common.plugins.util.SpringBootUtil;

public abstract class SpringDockerizer extends Dockerizer {

    public final String SPRING_BOOT_VERSION;
    public final DockerParameters params;

    public SpringDockerizer(File projectDirectory, File outputDirectory, File appArchive, String springBootVersion,
            DockerParameters params, BoostLoggerI log) {
        super(projectDirectory, outputDirectory, appArchive, log);
        this.SPRING_BOOT_VERSION = springBootVersion;
        this.params = params;
    }

    /**
     * Creates a Dockerfile with appropriate LibertyBaseImage to build an efficient
     * Docker image of the Spring Boot application.
     *
     * @throws Exception
     */
    @Override
    public void createDockerFile() throws BoostException {
        if (BoostUtil.isNotNullOrEmpty(SPRING_BOOT_VERSION)) {
            if (SpringBootUtil.isSpringBootUberJar(appArchive)) {
                BoostUtil.extract(appArchive, projectDirectory, params.getDependencyFolder());
                String startClass = getSpringStartClass();
                File dockerFile = createNewDockerFile();
                if (dockerFile != null) { // File was created
                    writeSpringBootDockerFile(dockerFile, startClass);
                }
            } else {
                throw new BoostException(appArchive.getAbsolutePath() + " file is not an executable archive. "
                        + "The repackage goal of the spring-boot-maven-plugin must be configured to run first in order to create the required executable archive.");
            }
        } else {
            throw new BoostException("Unable to create a Dockerfile because application type is not supported");
        }
    }

    protected String getAppPathString() {

        Path projPath = projectDirectory.toPath();
        Path outputPath = outputDirectory.toPath();

        // goes from '~/proj/build/lib' to 'build/lib'
        Path appPath = projPath.relativize(outputPath);

        // On Windows the last line might be 'build\lib'
        return appPath.toString().replace(File.separatorChar, '/');
    }

    protected String getSpringStartClass() throws BoostException {
        try (JarFile jarFile = new JarFile(appArchive)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue("Start-Class");
            } else {
                throw new BoostException("Could not get Spring Boot start class due to error getting app manifest.");
            }
        } catch (IOException e) {
            throw new BoostException("Could not get Spring Boot start class due to error opening app archive.", e);
        }
    }

    private File createNewDockerFile() throws BoostException {
        try {
            File dockerFile = new File(projectDirectory, "Dockerfile");
            Files.createFile(dockerFile.toPath());
            log.info("Creating Dockerfile: " + dockerFile.getAbsolutePath());
            return dockerFile;
        } catch (FileAlreadyExistsException e1) {
            log.warn("Dockerfile already exists");
            return null;
        } catch (IOException e2) {
            throw new BoostException("Could not create Dockerfile.", e2);
        }
    }

    private void writeSpringBootDockerFile(File dockerFile, String startClass) throws BoostException {
        try {
            Files.write(dockerFile.toPath(), getDockerfileLines(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new BoostException("Could not write Spring Boot Dockerfile.", e);
        }
    }

}
