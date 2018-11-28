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

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod

public class BoostPackageJaxRS20Test extends AbstractBoostTest {

    static BuildResult result

    static File resourceDir = new File("build/resources/test/jaxrsTestApp")
    static File testProjectDir = new File(integTestDir, "BoostPackageJaxRS20Test")
    static String buildFilename = "testJaxrs20.gradle"

    private static String URL = "http://localhost:9080/api/hello"

    private static final String JAXRS_20_FEATURE = "<feature>jaxrs-2.0</feature>"
    private static String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"
    private static String SERVLET_RESPONSE = "Hello World From Your Friends at Liberty Boost EE!"

    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostPackage", "boostStart", "-i", "-s")
            .build()
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
    public void testPackageSuccess() throws IOException {
        assertEquals(SUCCESS, result.task(":installLiberty").getOutcome())
        assertEquals(SUCCESS, result.task(":libertyCreate").getOutcome())
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())

        assertTrue(new File(testProjectDir, "testWar.jar").exists())
    }

    @Test //Testing that springBoot-2.0 feature was added to the packaged server.xml
    public void testPackageContents() throws IOException {
        File targetFile = new File(testProjectDir, SERVER_XML)
        assertTrue(targetFile.getCanonicalFile().toString() + "does not exist.", targetFile.exists())
        
        // Check contents of file for jaxrs-2.0 feature
        boolean found = false
        BufferedReader br = null
        
        try {
            br = new BufferedReader(new FileReader(targetFile));
            String line
            while ((line = br.readLine()) != null) {
                if (line.contains(JAXRS_20_FEATURE)) {
                    found = true
                    break
                }
            }
        } finally {
            if (br != null) {
                br.close()
            }
        }
        
        assertTrue("The " + JAXRS_20_FEATURE + " feature was not found in the server configuration", found);    
    }

    @Test
    public void testServletResponse() throws Exception {
        testServlet(URL, SERVLET_RESPONSE)
    }
}
