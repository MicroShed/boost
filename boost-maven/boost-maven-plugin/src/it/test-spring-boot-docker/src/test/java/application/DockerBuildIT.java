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

package application;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DockerClientBuilder;

import org.springframework.boot.SpringBootVersion;

import io.openliberty.boost.common.docker.dockerizer.Dockerizer;

public class DockerBuildIT {
    private static File dockerFile;
    private static DockerClient dockerClient;
    private static CreateContainerResponse container;

    private static String dockerizer = System.getProperty("dockerizer");
    private static String port;

    @BeforeClass
    public static void setup() throws Exception {

        switch (dockerizer) {
        case "jar":
            port = "8080";
            break;
        case "classpath":
            port = "8080";
            break;
        default: // liberty
            port = "9080";
            break;
        }

        ExposedPort exposedPort = ExposedPort.tcp(Integer.valueOf(port));

        dockerFile = new File("Dockerfile");
        dockerClient = DockerClientBuilder.getInstance().build();
        container = dockerClient.createContainerCmd("localhost:5000/test-spring-boot-docker:latest")
                .withPortBindings(PortBinding.parse(port + ":" + port)).withExposedPorts(exposedPort).exec();
        Thread.sleep(3000);

        dockerClient.startContainerCmd(container.getId()).exec();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        dockerClient.stopContainerCmd(container.getId()).exec();
        dockerClient.removeContainerCmd(container.getId()).exec();
    }

    @Test
    public void testDockerBuildCreatesDockerfile() throws Exception {
        assertTrue(dockerFile.getCanonicalPath() + " was not created", dockerFile.exists());
    }

    @Test
    public void testDockerfileContainsCorrectLibertyImage() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(dockerFile));
        String line = reader.readLine();
        assertEquals("Expected comment not found", Dockerizer.BOOST_GEN, line);

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("FROM")) {
                switch (dockerizer) {
                case "jar":
                    validateJarOrClasspathBaseImage(line);
                    break;
                case "classpath":
                    validateJarOrClasspathBaseImage(line);
                    break;
                default: // liberty
                    validateLibertyBaseImage(line);
                    break;
                }

                break;
            }
        }
    }

    private void validateLibertyBaseImage(String line) throws Exception {
        String version = SpringBootVersion.getVersion();
        if (version != null) {
            if (version.startsWith("1.")) {
                assertTrue("Expected Open liberty base image open-liberty:springBoot1 was not found in "
                        + dockerFile.getCanonicalPath(), line.contains("open-liberty:springBoot1"));
            } else if (version.startsWith("2.")) {
                assertTrue("Expected Open liberty base image open-liberty:springBoot2 was not found in "
                        + dockerFile.getCanonicalPath(), line.contains("open-liberty:springBoot2"));
            }
        }
    }

    private void validateJarOrClasspathBaseImage(String line) throws Exception {
        assertTrue("Expected base image adoptopenjdk/openjdk8-openj9 was not found in " + dockerFile.getCanonicalPath(),
                line.contains("adoptopenjdk/openjdk8-openj9"));
    }

    @Test
    public void testDockerBuildCreatesDockerIgnoreFile() throws Exception {
        File dockerignore = new File(".dockerignore");
        assertTrue(dockerignore.getCanonicalPath() + " was not created", dockerignore.exists());
        try (FileReader fileReader = new FileReader(dockerignore)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line = bufferedReader.readLine();
                assertNotNull(".dockerignore cannot be empty", line);
                assertEquals("Expected comment not found", Dockerizer.BOOST_GEN, line);
            }
        }
    }

    @Test
    public void runDockerContainerAndVerifyAppOnEndpoint() throws Exception {

        Thread.sleep(3000);
        testDockerContainerRunning();

        Thread.sleep(10000);
        testAppRunningOnEndpoint();

    }

    public void testDockerContainerRunning() throws Exception {
        List<Container> containers = dockerClient.listContainersCmd().exec();
        // docker local registry container and image container
        assertEquals("Expected number of running containers not found", 2, containers.size());
    }

    public void testAppRunningOnEndpoint() throws Exception {
        URL requestUrl = new URL("http://" + getTestDockerHost() + ":" + port + "/spring");
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();

        if (conn != null) {
            assertEquals("Expected response code not found.", 200, conn.getResponseCode());
        }

        StringBuffer response = new StringBuffer();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        assertEquals("Expected body not found.", "Greetings from Spring Boot!", response.toString());
    }

    private static String getTestDockerHost() {

        String dockerHostEnv = System.getenv("DOCKER_HOST");
        if (dockerHostEnv == null || dockerHostEnv.isEmpty()) {
            return "localhost";
        } else {
            URI dockerHostURI = URI.create(dockerHostEnv);
            return dockerHostURI.getHost();
        }
    }

}
