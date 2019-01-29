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
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue

public class PackageSpring15Test extends AbstractBoostTest {

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "PackageSpring15Test")
        buildFilename = "springApp-15.gradle"
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .forwardOutput()
                .withArguments("build", "boostStart", "boostStop", "-i", "-s")
                .build()
    }

    @Test
    public void testPackageSuccess() throws IOException {
        testPackageTask()
        assertTrue(new File(testProjectDir, "build/libs/test-spring15.jar").exists())
    }

    @Test
    public void testPackageContents() throws IOException {
        testPackageContentsforSpring15()
    }
}
