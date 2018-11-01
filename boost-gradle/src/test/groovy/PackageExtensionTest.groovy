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
import org.junit.Test

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

public class PackageExtensionTest extends AbstractBoostTest {

    static File resourceDir = new File("build/resources/test/springApp")
    static File testProjectDir = new File(integTestDir, "PackageExtensionTest")
    static String buildFilename = "packageExtensionTest.gradle"

    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)
    }

    @Test
    public void testPackageSuccess() throws IOException {
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .build()

        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())

        assertTrue(new File(testProjectDir, "build/libs/extensionTest.jar").exists())
    }
}
