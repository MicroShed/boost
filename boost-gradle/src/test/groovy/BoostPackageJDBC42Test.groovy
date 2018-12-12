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
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

public class BoostPackageJDBC42Test extends AbstractBoostTest {

    static File resourceDir = new File("build/resources/test/test-jdbc")
    static File testProjectDir = new File(integTestDir, "PackageJDBCTest")
    static String buildFilename = "build.gradle"

    private static String URL = "http://localhost:9080/"; 

    private static final String JDBC_42_FEATURE = "<feature>jdbc-4.2</feature>"
    private static String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"
    private static String SERVLET_RESPONSE = "</font><font color=red>myHomeCounty</font></h1>"
    
    @Before
    public void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)
        
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostPackage", "boostStart", "-i", "-s")
            .build()
            
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
        assertEquals(SUCCESS, result.task(":boostStart").getOutcome())
    }
    
    @After
    public void teardown() {
    
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostStop", "-i", "-s")
            .build()
       
        assertEquals(SUCCESS, result.task(":boostStop").getOutcome())
       
    }
    
    @Test 
    public void testPackageContents() throws IOException {
        File targetFile = new File(testProjectDir, SERVER_XML)
        assertTrue(targetFile.getCanonicalFile().toString() + "does not exist.", targetFile.exists())
        
        // Check contents of file for jdbc-4.2 feature
        boolean found = false
        BufferedReader br = null
        
        try {
            br = new BufferedReader(new FileReader(targetFile));
            String line
            while ((line = br.readLine()) != null) {
                if (line.contains(JDBC_42_FEATURE)) {
                    found = true
                    break
                }
            }
        } finally {
            if (br != null) {
                br.close()
            }
        }
        
        assertTrue("The " + JDBC_42_FEATURE + " feature was not found in the server configuration", found);    
    }
    
    @Test
    public void testServletResponse() throws Exception {
        testServlet(URL, SERVLET_RESPONSE)
    }
}
