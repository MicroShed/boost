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

import org.junit.Test
import org.junit.BeforeClass
import static org.junit.Assert.*

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder

public class PackageAndDockerize15Test extends AbstractBoostDockerTest {

    private static final String SPRING_BOOT_15_FEATURE = "<feature>springBoot-1.5</feature>"
    private static String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "PackageAndDockerize15Test")
        buildFilename = "springApp-15.gradle"
        libertyImage = OL_SPRING_15_IMAGE
        imageName = "test-spring15"

        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        dockerFile = new File(testProjectDir, "Dockerfile")
        dockerClient = DockerClientBuilder.getInstance().build()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("boostDockerBuild", "boostPackage", "boostStart", "boostStop")
            .build()
    }

    @Test
    public void testBuildSuccess() throws IOException {
        assertEquals(SUCCESS, result.task(":installLiberty").getOutcome())
        assertEquals(SUCCESS, result.task(":libertyCreate").getOutcome())
        assertEquals(SUCCESS, result.task(":boostDockerBuild").getOutcome())
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())

        assertTrue(new File(testProjectDir, "build/libs/${imageName}.jar").exists())
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
