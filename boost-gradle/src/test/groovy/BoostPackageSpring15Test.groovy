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
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

public class BoostPackageSpring15Test extends AbstractBoostTest {

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/test-spring-boot")
        testProjectDir = new File(integTestDir, "PackageSpring15Test")
        buildFilename = "springApp-15.gradle"
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .forwardOutput()
                .withArguments("boostPackage", "boostStart", "-i", "-s")
                .build()
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
    }

    @AfterClass
    public static void teardown() {

        result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .forwardOutput()
                .withArguments("boostStop", "-i", "-s")
                .build()

        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
    }

    @Test
    public void testPackageContents() throws IOException {
        testPackageContentsforSpring15()
    }

    @Test
    public void testServlet() throws Exception {
        //test the endpoint by starting the server using boostStart command
        testServlet("http://localhost:8080/", "Greetings from Spring Boot!");
    }

}
