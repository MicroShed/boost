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

package io.openliberty.boost.utils;

import static io.openliberty.boost.utils.ConfigConstants.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;

import io.openliberty.boost.BoostException;
import io.openliberty.boost.BoostLoggerI;
import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

public class SpringBootUtil {

    private static final String SERVER_PORT = "server.port";
    private static final String DEFAULT_SERVER_PORT = "8080";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String DEFAULT_SERVER_ADDRESS = "localhost";

    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";

    /**
     * Get the expected path of the Spring Boot Uber JAR (with .spring extension)
     * that was preserved during the Boost packaging process. No guarantee that the
     * path exists or the artifact is indeed a Spring Boot Uber JAR.
     * 
     * @param artifact
     * @return the canonical path
     * @throws BoostException
     */
    public static String getBoostedSpringBootUberJarPath(File artifact) throws BoostException {
        if (artifact == null || !artifact.exists() || !artifact.isFile()) {
            throw new BoostException("Could not find a project artifact.");
        }

        try {
            return artifact.getCanonicalFile().getPath() + ".spring";
        } catch (IOException e) {
            throw new BoostException("Error getting Spring Boot uber JAR path.", e);
        }
    }

    /**
     * Copy the Spring Boot Uber JAR to a .spring extension to preserve it
     * 
     * @param artifact
     * @return the destination file if the operation was performed successfully, null otherwise
     * @throws PluginExecutionException
     */
    public static File copySpringBootUberJar(File artifact, BoostLoggerI logger) throws PluginExecutionException {
        if (artifact == null || !artifact.exists() || !artifact.isFile()) {
            throw new BoostException("Could not find a project artifact.");
        }

        File springJar = new File(getBoostedSpringBootUberJarPath(artifact));

        // We are sure the artifact is a Spring Boot uber JAR if it has
        // Spring-Boot-Version in the manifest, but not a wlp directory
        if (net.wasdev.wlp.common.plugins.util.SpringBootUtil.isSpringBootUberJar(artifact)
                && !BoostUtil.isLibertyJar(artifact, logger)) {
            try {
                FileUtils.copyFile(artifact, springJar);
                return springJar;
            } catch (IOException e) {
                throw new BoostException("Error copying Spring Boot Uber JAR.", e);
            }
        }
        return null;
    }

    /**
     * Add the Spring Boot Version property to the Manifest file in the Liberty Uber
     * JAR. This is to trick Spring Boot into not repackaging it.
     * 
     * @param artifact
     * @param springBootVersion
     * @throws BoostException
     */
    public static void addSpringBootVersionToManifest(File artifact, String springBootVersion, BoostLoggerI logger) throws BoostException {
        if (artifact == null || !artifact.exists() || !artifact.isFile()) {
            throw new BoostException("Could not find a project artifact.");
        }

        if (!BoostUtil.isLibertyJar(artifact, logger)) {
            throw new BoostException(
                    "The project artifact is not a Liberty JAR. This should not happen.");
        }

        Path path = artifact.toPath();

        try (FileSystem zipfs = FileSystems.newFileSystem(path, SpringBootUtil.class.getClassLoader())) {
            Path zipPath = zipfs.getPath("/META-INF/MANIFEST.MF");

            InputStream is = Files.newInputStream(zipPath);

            Manifest manifest = new Manifest(is);
            manifest.getMainAttributes().put(
                    new Attributes.Name(net.wasdev.wlp.common.plugins.util.SpringBootUtil.BOOT_VERSION_ATTRIBUTE),
                    springBootVersion);

            ByteArrayOutputStream manifestOs = new ByteArrayOutputStream();
            manifest.write(manifestOs);
            InputStream manifestIs = new ByteArrayInputStream(manifestOs.toByteArray());
            Files.copy(manifestIs, zipPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new BoostException("Error updating manifest file.", e);
        }
    }

    public static Properties getSpringBootServerProperties(String buildDir) throws IOException {
        Properties serverProperties = new Properties();
        Properties allProperties = new Properties();
        InputStream input = null;

        try {

            File appProperties = new File(buildDir + "/classes", APPLICATION_PROPERTIES_FILE);

            if (appProperties.exists()) {
                input = new FileInputStream(appProperties.getAbsolutePath());
                allProperties.load(input);
            }

        } finally {
            if (input != null) {
                input.close();
            }
        }

        String serverPort = (String) allProperties.getOrDefault(SERVER_PORT, DEFAULT_SERVER_PORT);
        String serverAddress = (String) allProperties.getOrDefault(SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
        serverProperties.setProperty(SERVER_PORT, serverPort);
        serverProperties.setProperty(SERVER_ADDRESS, serverAddress);

        return serverProperties;
    }

    public static List<String> getLibertyFeaturesForSpringBoot(String springBootVersion,
            List<String> springBootStarters, BoostLoggerI logger) {
        List<String> featuresToAdd = new ArrayList<String>();

        if (springBootVersion != null) {
            String springBootFeature = null;

            if (springBootVersion.startsWith("1.")) {
                springBootFeature = SPRING_BOOT_15;
            } else if (springBootVersion.startsWith("2.")) {
                springBootFeature = SPRING_BOOT_20;
            } else {
                // log error for unsupported version
                logger.error(
                        "No supporting feature available in Open Liberty for org.springframework.boot dependency with version "
                                + springBootVersion);
            }

            if (springBootFeature != null) {
                logger.info("Adding the " + springBootFeature + " feature to the Open Liberty server configuration.");
                featuresToAdd.add(springBootFeature);
            }

        } else {
            logger.info(
                    "The springBoot feature was not added to the Open Liberty server because no spring-boot-starter dependencies were found.");
        }

        // Add any other Liberty features needed depending on the spring boot starters
        // defined
        for (String springBootStarter : springBootStarters) {
            if (springBootStarter.equals("spring-boot-starter-web")) {
                // Add the servlet-4.0 feature
                featuresToAdd.add(SERVLET_40);
            } else if (springBootStarter.equals("spring-boot-starter-websocket")) {
                // Add the websocket-1.1 feature
                featuresToAdd.add(WEBSOCKET_11);
            }
        }

        return featuresToAdd;
    }

}
