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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


public class PackageExtensionTest extends AbstractBoostTest {

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "PackageExtensionTest")
        buildFilename = "packageExtensionTest.gradle"
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .forwardOutput()
                .withArguments("build", "-i", "-s")
                .build()
    }

    @Test
    public void testPackageSuccess() throws IOException {
        assertEquals(TaskOutcome.SUCCESS, result.task(":boostPackage").getOutcome())

        assertTrue(new File(testProjectDir, "build/libs/extensionTest.jar").exists())
    }
}
