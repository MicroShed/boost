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

package io.openliberty.boost.common.utils;

import static io.openliberty.boost.common.config.ConfigConstants.*;

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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.ConfigConstants;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

public class SpringBootUtil {

    public static final String SERVER_PORT = "server.port";
    public static final String DEFAULT_SERVER_PORT = "8080";
    public static final String SERVER_ADDRESS = "server.address";
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    public static final String SERVER_SSL_KEYSTORE = "server.ssl.key-store";
    public static final String SERVER_SSL_KEYSTORE_PASSWORD = "server.ssl.key-store-password";
    public static final String SERVER_SSL_KEYSTORE_TYPE = "server.ssl.key-store-type";
    public static final String SERVER_SSL_KEYSTORE_PROVIDER = "server.ssl.key-store-provider";
    public static final String SERVER_SSL_KEY_PASSWORD = "server.ssl.key-password";
    public static final String SERVER_SSL_KEY_ALIAS = "server.ssl.key-alias";

    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";

    private static final String SPRING_WEBMVC = "spring-webmvc";
    private static final String SPRING_WEBSOCKET = "spring-websocket";

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
     * @return the destination file if the operation was performed successfully,
     *         null otherwise
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
    public static void addSpringBootVersionToManifest(File artifact, String springBootVersion, BoostLoggerI logger)
            throws BoostException {
        if (artifact == null || !artifact.exists() || !artifact.isFile()) {
            throw new BoostException("Could not find a project artifact.");
        }

        if (!BoostUtil.isLibertyJar(artifact, logger)) {
            throw new BoostException("The project artifact is not a Liberty JAR. This should not happen.");
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

    public static Properties getSpringBootServerProperties(String springBootProjectResources) throws IOException {
        Properties serverProperties = new Properties();
        Properties allProperties = new Properties();
        InputStream input = null;

        try {

            File applicationPropertiesFile = new File(springBootProjectResources, APPLICATION_PROPERTIES_FILE);

            if (applicationPropertiesFile.exists()) {
                input = new FileInputStream(applicationPropertiesFile.getAbsolutePath());
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

        String serverSslKeystore = (String) allProperties.getProperty(SERVER_SSL_KEYSTORE);
        String serverSslKeystorePassword = (String) allProperties.getProperty(SERVER_SSL_KEYSTORE_PASSWORD);

        if (serverSslKeystore != null && serverSslKeystorePassword != null) {
            serverProperties.setProperty(SERVER_SSL_KEYSTORE, serverSslKeystore);
            serverProperties.setProperty(SERVER_SSL_KEYSTORE_PASSWORD, serverSslKeystorePassword);

            // Add any additional SSL properties
            String serverSslKeystoreType = (String) allProperties.getProperty(SERVER_SSL_KEYSTORE_TYPE);
            String serverSslKeystoreProvider = (String) allProperties.getProperty(SERVER_SSL_KEYSTORE_PROVIDER);
            String serverSslKeyAlias = (String) allProperties.getOrDefault(SERVER_SSL_KEY_ALIAS, "default");
            String serverSslKeyPassword = (String) allProperties.getProperty(SERVER_SSL_KEY_PASSWORD);

            if (serverSslKeystoreType != null) {
                serverProperties.setProperty(SERVER_SSL_KEYSTORE_TYPE, serverSslKeystoreType);
            }
            if (serverSslKeystoreProvider != null) {
                serverProperties.setProperty(SERVER_SSL_KEYSTORE_PROVIDER, serverSslKeystoreProvider);
            }

            if (serverSslKeyPassword != null) {
                serverProperties.setProperty(SERVER_SSL_KEY_PASSWORD, serverSslKeyPassword);
                serverProperties.setProperty(SERVER_SSL_KEY_ALIAS, serverSslKeyAlias);
            }
        }

        return serverProperties;
    }

    public static List<String> getLibertyFeaturesForSpringBoot(String springBootVersion,
            List<String> springFrameworkDependencies, BoostLoggerI logger) {
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

        // Add any other Liberty features needed depending on the spring
        // framework dependencies defined
        for (String dependency : springFrameworkDependencies) {
            if (dependency.equals(SPRING_WEBMVC)) {
                // Add the servlet-4.0 feature
                featuresToAdd.add(SERVLET_40);
            } else if (dependency.equals(SPRING_WEBSOCKET)) {
                // Add the websocket-1.1 feature
                featuresToAdd.add(WEBSOCKET_11);
            }
        }

        return featuresToAdd;
    }

    /**
     * Generate Liberty server configuration files based on the Spring Boot
     * application configuration.
     * 
     * @param springBootProjectResources
     *            - Path to the Spring Boot application classpath resources
     * @param libertyServerPath
     *            - Path to the Liberty server config directory
     * @param springBootVersion
     *            - Version of Spring Boot for the application
     * @param springBootStarters
     *            - List of Spring Boot starters used by this application
     * @param logger
     *            - BoostLogger for information logging
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public static void generateLibertyServerConfig(String springBootProjectResources, String libertyServerPath,
            String springBootVersion, List<String> springBootStarters, BoostLoggerI logger)
            throws ParserConfigurationException, IOException, TransformerException {

        logger.info("Generating Liberty server configuration");

        // Generate Liberty configuration
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath);

        // Find and add appropriate springBoot features
        List<String> featuresNeededForSpringBootApp = getLibertyFeaturesForSpringBoot(springBootVersion,
                springBootStarters, logger);

        serverConfig.addFeatures(featuresNeededForSpringBootApp);

        // Get Spring Boot server properties
        Properties springBootServerProps = getSpringBootServerProperties(springBootProjectResources);

        // Configure SSL and endpoints
        if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE)) {

            Map<String, String> keystoreProperties = new HashMap<String, String>();
            Map<String, String> keyProperties = new HashMap<String, String>();

            // For each Spring Boot keystore property, add an entry to the
            // keystore map which maps our
            // Liberty keystore attribute to a bootstrap variable with the same
            // name as the Spring Boot property.
            // The Spring Boot properties will then be added to the server's
            // bootstrap.properties file.
            keystoreProperties.put(ConfigConstants.LOCATION, makeVariable(SpringBootUtil.SERVER_SSL_KEYSTORE));

            if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_PASSWORD)) {
                keystoreProperties.put(ConfigConstants.PASSWORD,
                        makeVariable(SpringBootUtil.SERVER_SSL_KEYSTORE_PASSWORD));
            }
            if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_TYPE)) {
                keystoreProperties.put(ConfigConstants.TYPE, makeVariable(SpringBootUtil.SERVER_SSL_KEYSTORE_TYPE));
            }
            if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEYSTORE_PROVIDER)) {
                keystoreProperties.put(ConfigConstants.PROVIDER,
                        makeVariable(SpringBootUtil.SERVER_SSL_KEYSTORE_PROVIDER));
            }

            // Add any key properties to the separate key map.
            if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEY_PASSWORD)) {
                keyProperties.put(ConfigConstants.KEY_PASSWORD, makeVariable(SpringBootUtil.SERVER_SSL_KEY_PASSWORD));
            }
            if (springBootServerProps.containsKey(SpringBootUtil.SERVER_SSL_KEY_ALIAS)) {
                keyProperties.put(ConfigConstants.NAME, makeVariable(SpringBootUtil.SERVER_SSL_KEY_ALIAS));
            }

            // Create keystore element in server.xml and endpoint with http
            // disabled.
            serverConfig.addKeystore(keystoreProperties, keyProperties);
            serverConfig.addHttpEndpoint(makeVariable(SpringBootUtil.SERVER_ADDRESS), "-1",
                    makeVariable(SpringBootUtil.SERVER_PORT));
            serverConfig.addFeature(ConfigConstants.TRANSPORT_SECURITY_10);

            // Since the keystore for the Spring Boot app is created manually
            // and already exists,
            // if it is specified on the classpath, we need to copy it to the
            // Liberty server. Otherwise,
            // we can just reference the external location without needing to
            // copy the file.
            String keystoreFile = springBootServerProps.getProperty(SpringBootUtil.SERVER_SSL_KEYSTORE);

            if (keystoreFile.contains("classpath:")) {

                // Keystore is in resources directory of spring boot application
                keystoreFile = keystoreFile.replace("classpath:", "");
                springBootServerProps.put(SpringBootUtil.SERVER_SSL_KEYSTORE, keystoreFile);

                // Copy keystore to Liberty
                Path springBootKeystorePath = Paths.get(springBootProjectResources, keystoreFile);
                Path libertyKeystorePath = Paths.get(libertyServerPath + "/resources/security/" + keystoreFile);
                Path libertySecurityPath = Paths.get(libertyServerPath + "/resources/security");

                Files.createDirectories(libertySecurityPath);
                Files.copy(springBootKeystorePath, libertyKeystorePath, StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } else {
            serverConfig.addHttpEndpoint(makeVariable(SpringBootUtil.SERVER_ADDRESS),
                    makeVariable(SpringBootUtil.SERVER_PORT), null);
        }

        // Add properties to bootstrap properties
        serverConfig.addBootstrapProperties(springBootServerProps);

        serverConfig.writeToServer();
    }

    private static String makeVariable(String propertyName) {
        return "${" + propertyName + "}";
    }
}
