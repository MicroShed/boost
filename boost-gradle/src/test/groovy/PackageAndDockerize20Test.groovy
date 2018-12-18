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

import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.BeforeClass
import com.github.dockerjava.core.DockerClientBuilder

import static org.junit.Assert.assertTrue

public class PackageAndDockerize20Test extends AbstractBoostDockerTest {

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "PackageAndDockerize20Test")
        buildFilename = "springApp-20.gradle"
        libertyImage = OL_SPRING_20_IMAGE
        repository = "gs-spring-boot-0.1.0"
        dockerPort = "9080"

        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        dockerFile = new File(testProjectDir, "Dockerfile")
        dockerClient = DockerClientBuilder.getInstance().build()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostDockerBuild", "boostPackage", "boostStart", "boostStop", "-i", "-s")
            .build()
    }

    @Test
    public void testBuildSuccess() throws IOException {
        testDockerPackageTask()
        assertTrue(new File(testProjectDir, "build/libs/${repository}.jar").exists())
    }

    @Test
    public void testPackageContents() throws IOException {
        testPackageContentsforSpring20()
    }
}
