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
import org.junit.Test

import org.junit.AfterClass


import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.ExposedPort

import io.openliberty.boost.common.docker.dockerizer.Dockerizer

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

public abstract class AbstractBoostDockerTest extends AbstractBoostTest {
    protected static final String OL_SPRING_15_IMAGE = "open-liberty:springBoot1"
    protected static final String OL_SPRING_20_IMAGE = "open-liberty:springBoot2"

    protected static final String OPEN_J9_IMAGE = "adoptopenjdk/openjdk8-openj9"

    protected static File dockerFile
    protected static DockerClient dockerClient

    protected static String containerId

    protected static String repository
    protected static String libertyImage
    protected static String dockerPort

    @AfterClass
    public static void cleanup() throws Exception {
        dockerClient.stopContainerCmd(containerId).exec()

        dockerClient.removeContainerCmd(containerId).exec()
    }

    @Test
    public void testDockerSuccess() throws IOException {
        assertEquals(SUCCESS, result.task(":boostDockerBuild").getOutcome())
    }  
    
    @Test
    public void testDockerizeCreatesDockerfile() throws Exception {
        assertTrue(dockerFile.getCanonicalPath() + " was not created", dockerFile.exists())
    }
    
    @Test
    public void testDockerfileContainsCorrectLibertyImage() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(dockerFile))

        assertTrue("Expected Liberty generated Dockerfile line ${Dockerizer.BOOST_GEN} was not found in " + dockerFile.getCanonicalPath(), reader.readLine().contains(Dockerizer.BOOST_GEN))
        assertTrue("Expected Open liberty base image ${libertyImage} was not found in " + dockerFile.getCanonicalPath(), reader.readLine().contains(libertyImage))
    }
    
    @Test
    public void runDockerContainerAndVerifyAppOnEndpoint() throws Exception {
        ExposedPort exposedPort = ExposedPort.tcp(Integer.valueOf(dockerPort))
        
        CreateContainerResponse container = dockerClient.createContainerCmd("${repository}:latest")
                .withPortBindings(PortBinding.parse(dockerPort + ":" + dockerPort)).withExposedPorts(exposedPort).exec()
        Thread.sleep(3000)

        containerId = container.getId()

        dockerClient.startContainerCmd(containerId).exec()

        Thread.sleep(10000)
        testDockerContainerRunning()

        Thread.sleep(10000)
        testAppRunningOnEndpoint()
    }

    public void testDockerContainerRunning() throws Exception {
        List<Container> containers = dockerClient.listContainersCmd().exec()
        // docker local registry conatiner and image container
        assertEquals("Expected number of running containers not found", 2, containers.size())
    }

    public void testAppRunningOnEndpoint() throws Exception {
        URL requestUrl = new URL("http://" + getTestDockerHost() + ":" + dockerPort)
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection()

        if (conn != null) {
            assertEquals("Expected response code not found.", 200, conn.getResponseCode())
        }

        StringBuffer response = new StringBuffer()

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line)
        }
        assertEquals("Expected body not found.", "Greetings from Spring Boot!", response.toString())
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