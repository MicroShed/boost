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
import org.junit.AfterClass
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS


public class BoostPackageJaxRS20Test extends AbstractBoostTest {

    private static String URL = "http://localhost:9080/api/hello"

    private static final String JAXRS_20_FEATURE = "<feature>jaxrs-2.0</feature>"
    private static String SERVLET_RESPONSE = "Hello World From Your Friends at Liberty Boost EE!"

    @BeforeClass
    public static void setup() {
        resourceDir = new File("build/resources/test/jaxrsTestApp")
        testProjectDir = new File(integTestDir, "BoostPackageJaxRS20Test")
        buildFilename = "testJaxrs20.gradle"
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

        //Add back in with packaging option
        //assertTrue(new File(testProjectDir, "testWar.jar").exists())
    }

    @Test
    public void testPackageContents() throws IOException {
        testFeatureInServerXML(JAXRS_20_FEATURE);
    }

    @Test
    public void testServletResponse() throws Exception {
        testServlet(URL, SERVLET_RESPONSE)
    }
}
