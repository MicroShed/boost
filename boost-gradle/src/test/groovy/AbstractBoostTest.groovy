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

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.io.FileUtils

import org.gradle.testkit.runner.BuildResult
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertNotNull

public class AbstractBoostTest {
    private static final String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"
    private static final String SPRING_BOOT_15_FEATURE = "<feature>springBoot-1.5</feature>"
    private static final String SPRING_BOOT_20_FEATURE = "<feature>springBoot-2.0</feature>"

    protected static File integTestDir = new File('build/testBuilds')

    protected static File resourceDir
    protected static File testProjectDir
    protected static String buildFilename
    protected static BuildResult result

    protected static void deleteDir(File dir) {
        if (dir.exists()) {
            if (!dir.deleteDir()) {
                throw new AssertionError("Unable to delete directory '$dir.canonicalPath'.")
            }
        }
    }
    
    protected static void createDir(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new AssertionError("Unable to create directory '$dir.canonicalPath'.")
            }
        }
    }
    
    protected static File copyBuildFiles(File buildFilename, File buildDir) {
        copyFile(buildFilename, new File(buildDir, 'build.gradle'))
        copyFile(new File("build/gradle.properties"), new File(buildDir, 'gradle.properties'))
    }

    protected static File createTestProject(File parent, File sourceDir, String buildFilename) {
        if (!sourceDir.exists()){
            throw new AssertionError("The source file '${sourceDir.canonicalPath}' doesn't exist.")
        }
        try {
            // Copy all resources except the individual test .gradle files
            // Do copy settings.gradle.
            FileUtils.copyDirectory(sourceDir, parent, new FileFilter() {
               public boolean accept (File pathname) {
                   return (!pathname.getPath().endsWith(".gradle") ||
                    pathname.getPath().endsWith("settings.gradle") ||
                        pathname.getPath().endsWith("build.gradle"))
               }
            })

            // copy the needed gradle build and property files
            File sourceFile = new File(sourceDir, buildFilename)
            copyBuildFiles(sourceFile, parent)

        } catch (IOException e) {
            throw new AssertionError("Unable to copy directory '${parent.canonicalPath}'.")
        }
    }

    protected static File copyFile(File sourceFile, File destFile) {
        if (!sourceFile.exists()){
            throw new AssertionError("The source file '${sourceFile.canonicalPath}' doesn't exist.")
        }
        try {
            FileUtils.copyFile(sourceFile, destFile)
        } catch (Exception e) {
            throw new AssertionError("Unable to create file '${destFile.canonicalPath}'.")
        }
    }

    protected void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null
        try {
            output = new BufferedWriter(new FileWriter(destination))
            output.write(content)
        } finally {
            if (output != null) {
                output.close()
            }
        }
    }

    public void testServlet(String url, String responseString) throws Exception {
        HttpClient client = new HttpClient()

        GetMethod method = new GetMethod(url)

        try {
            int statusCode = client.executeMethod(method)

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode)

            String response = method.getResponseBodyAsString(10000)

            assertTrue("Unexpected response body",
                    response.contains(responseString))
        } finally {
            method.releaseConnection()
        }
    }

    protected void testPackageTask() throws IOException {
        assertEquals(SUCCESS, result.task(":installLiberty").getOutcome())
        assertEquals(SUCCESS, result.task(":libertyCreate").getOutcome())
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
    }

    protected void testDockerPackageTask() throws IOException {
        assertEquals(SUCCESS, result.task(":installLiberty").getOutcome())
        assertEquals(SUCCESS, result.task(":libertyCreate").getOutcome())
        assertEquals(SUCCESS, result.task(":boostDockerBuild").getOutcome())
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
    }

    protected void testPackageContentsforSpring15() throws IOException {
        testFeatureInServerXML(SPRING_BOOT_15_FEATURE)
    }

    protected void testPackageContentsforSpring20() throws IOException {
        testFeatureInServerXML(SPRING_BOOT_20_FEATURE)
    }

    protected void testFeatureInServerXML(String feature) {
        File targetFile = new File(testProjectDir, SERVER_XML)
        assertTrue(targetFile.getCanonicalFile().toString() + "does not exist.", targetFile.exists())
        boolean found = false
        BufferedReader br = null

        try {
            br = new BufferedReader(new FileReader(targetFile));
            String line
            while ((line = br.readLine()) != null) {
                if (line.contains(feature)) {
                    found = true
                    break
                }
            }
        } finally {
            if (br != null) {
                br.close()
            }
        }
        assertTrue("The " + feature + " feature was not found in the server configuration", found);
    }

    /*
     * Test the packaged Spring Boot liberty jar using java -jar command
     */
    public void testSpringBootEndpoint(File packagedJar) throws Exception{
        Process proc = Runtime.getRuntime().exec("java -jar " + packagedJar.getAbsolutePath());
        String line = null;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))
            line = reader.readLine()
            while (line != null) {
                if (line.contains("CWWKT0016I")) {
                    break
                }
                line = reader.readLine()
            }
        } finally {
            if(reader !=null){
                reader.close()
            }
        }
        assertNotNull("The endpoint is not available", line)
        assertTrue("Expected log not found", line.contains("CWWKT0016I") && line.contains("default_host"))
        int start = line.indexOf("http");
        String url = line.substring(start);
        testServlet("http://localhost:8080/", "Greetings from Spring Boot!")
        proc.destroy()
    }
}

