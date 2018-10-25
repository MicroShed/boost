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
import org.junit.Before
import org.junit.Test

import java.io.File
import java.io.IOException

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

public class BoostFunctionalTest extends AbstractBoostTest {

    String buildFileContent =   "buildscript {\n\t" + 
                                    "repositories {\n\t\t" + 
                                        "mavenLocal()\n\t\t" +
                                        "mavenCentral()\n\t\t" +
                                        "maven {\n\t\t\t" +
                                            "url 'https://oss.sonatype.org/content/repositories/snapshots/'\n\t\t" +
                                        "}\n\t" +
                                    "}\n\t" +
                                    "dependencies {\n\t\t" +
                                        "classpath \"io.openliberty.boost:boost-gradle-plugin:\$boostVersion\"\n\t" +
                                    "}\n" +
                                "}\n\n" +
                                "apply plugin: 'boost'"

    @Before
    void setup () {
        testProjectDir = new File(integTestDir, 'BoostFunctionalTest')
        
        createDir(testProjectDir)
        writeFile(new File(testProjectDir, 'build.gradle'), buildFileContent)
        copyFile(new File("build/gradle.properties"), new File(testProjectDir, 'gradle.properties'))
    }

    @Test
    public void testStartAndStopTasks() throws IOException {
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("boostStart", "boostStop")
            .build()

        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
    }
}
