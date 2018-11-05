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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder

public class PackageAndDockerize15Test extends AbstractBoostTest {
    private static File dockerFile
    private static DockerClient dockerClient
    private static BuildResult result

    private static String containerId

    static File resourceDir = new File("build/resources/test/springApp")
    static File testProjectDir = new File(integTestDir, "PackageAndDockerize15Test")
    static String buildFilename = "docker15Test.gradle"

    private static final String SPRING_BOOT_15_FEATURE = "<feature>springBoot-1.5</feature>"
    private static String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"

    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        dockerFile = new File(testProjectDir, "Dockerfile")
        dockerClient = DockerClientBuilder.getInstance().build()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("boostDocker", "boostPackage", "boostStart", "boostStop")
            .build()
    }

    @AfterClass
    public static void cleanup() throws Exception {
        dockerClient.stopContainerCmd(containerId).exec()

        dockerClient.removeContainerCmd(containerId).exec()
    }

    @Test
    public void testBuildSuccess() throws IOException {
        assertEquals(SUCCESS, result.task(":installLiberty").getOutcome())
        assertEquals(SUCCESS, result.task(":libertyCreate").getOutcome())
        assertEquals(SUCCESS, result.task(":boostDocker").getOutcome())
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())

        assertTrue(new File(testProjectDir, "build/libs/test-docker15.jar").exists())
    }

    @Test
    public void testDockerSuccess() throws IOException {
        assertEquals(SUCCESS, result.task(":boostDocker").getOutcome())
    }  
    
    @Test
    public void testDockerizeCreatesDockerfile() throws Exception {
        assertTrue(dockerFile.getCanonicalPath() + " was not created", dockerFile.exists())
    }
    
    @Test
    public void testDockerfileContainsCorrectLibertyImage() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(dockerFile))

        assertTrue("Expected Open liberty base image open-liberty:springBoot1 was not found in " + dockerFile.getCanonicalPath(), reader.readLine().contains("open-liberty:springBoot1"))

    }
    
    @Test
    public void runDockerContainerAndVerifyAppOnEndpoint() throws Exception {
        CreateContainerResponse container = dockerClient.createContainerCmd("test-docker15:latest")
                .withPortBindings(PortBinding.parse("9080:9080")).exec()
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
        URL requestUrl = new URL("http://localhost:9080/")
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

    @Test //Testing that springBoot-1.5 feature was added to the packaged server.xml
    public void testPackageContents() throws IOException {
        File targetFile = new File(testProjectDir, SERVER_XML)
        assertTrue(targetFile.getCanonicalFile().toString() + "does not exist.", targetFile.exists())
        
        // Check contents of file for springBoot-20 feature
        boolean found = false
        BufferedReader br = null
        
        try {
            br = new BufferedReader(new FileReader(targetFile));
            String line
            while ((line = br.readLine()) != null) {
                if (line.contains(SPRING_BOOT_15_FEATURE)) {
                    found = true
                    break
                }
            }
        } finally {
            if (br != null) {
                br.close()
            }
        }
        
        assertTrue("The "+SPRING_BOOT_15_FEATURE+" feature was not found in the server configuration", found);    
    }
}
