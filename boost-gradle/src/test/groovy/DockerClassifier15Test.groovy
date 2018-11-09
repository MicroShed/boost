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
import static org.gradle.testkit.runner.TaskOutcome.*

import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import java.io.File

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder

//Tests creating a Docker image with an extension
public class DockerClassifier15Test extends AbstractBoostDockerTest {

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "DockerClassifier15Test")
        buildFilename = "dockerClassifier15Test.gradle"
        libertyImage = OL_SPRING_15_IMAGE
        imageName = "test-docker15-test"

        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)
        dockerFile = new File(testProjectDir, "Dockerfile")
        dockerClient = DockerClientBuilder.getInstance().build()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .build()
    }

    @Test
    public void runDockerContainerAndVerifyAppOnEndpoint() throws Exception {
        CreateContainerResponse container = dockerClient.createContainerCmd("${imageName}:latest")
                .withPortBindings(PortBinding.parse("9080:9080")).exec()
        Thread.sleep(3000)

        containerId = container.getId()

        dockerClient.startContainerCmd(containerId).exec()

        Thread.sleep(10000)
        testDockerContainerRunning()

        Thread.sleep(10000)
        testAppRunningOnEndpoint()
    }
}