/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.apache.commons.io.FileUtils

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


public class BoostPackageDevReleaseTest extends AbstractBoostTest {
    static File resourceDir = new File("build/resources/test/devReleaseApp")
    static File testProjectDir = new File(integTestDir, "BoostPackageDevReleaseTest")
    
    static File devDir = new File(testProjectDir, 'dev')
    static File releaseDir = new File(testProjectDir, 'release')

    private static String URL = "http://localhost:9080/"

    private static String SERVLET_RESPONSE = "</font><font color=red>myHomeCounty</font></h1>"

    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        FileUtils.copyDirectory(resourceDir, testProjectDir)
        copyFile(new File("build/gradle.properties"), new File(devDir, 'gradle.properties'))
        copyFile(new File("build/gradle.properties"), new File(releaseDir, 'gradle.properties'))
        
        //Build the dev project
        BuildResult result = GradleRunner.create()
            .withProjectDir(devDir)
            .forwardOutput()
            .withArguments("install", "-i", "-s")
            .build()

        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())

        //Build the release project
        result = GradleRunner.create()
            .withProjectDir(releaseDir)
            .forwardOutput()
            .withArguments("boostPackage", "boostStart", "-i", "-s")
            .build()

        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
    }
    
    @After
    public void teardown() {
    
        BuildResult result = GradleRunner.create()
            .withProjectDir(releaseDir)
            .forwardOutput()
            .withArguments("boostStop", "-i", "-s")
            .build()
       
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
       
    }

    @Test
    public void checkForApplication() {
        assertTrue(new File(releaseDir, 'build/wlp/usr/servers/BoostServer/apps/app-1.0.war').exists())
    }
    
    @Test
    public void testServletResponse() throws Exception {
        testServlet(URL, SERVLET_RESPONSE)
    }
}