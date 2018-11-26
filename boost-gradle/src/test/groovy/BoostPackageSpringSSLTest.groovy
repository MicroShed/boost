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

public class BoostPackageSpringSSLTest extends AbstractBoostTest {

    static File resourceDir = new File("build/resources/test/test-spring-boot-ssl")
    static File testProjectDir = new File(integTestDir, "PackageSpringSSLTest")
    static String buildFilename = "build.gradle"

    private static String URL = "https://localhost:8443/"; 

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
    public void testServlet() throws Exception {

        def nullTrustManager = [
            checkClientTrusted: { chain, authType ->  },
            checkServerTrusted: { chain, authType ->  },
            getAcceptedIssuers: { null }
        ]

        def nullHostnameVerifier = [
            verify: { hostname, session -> true }
        ]

        SSLContext sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
           
        CloseableHttpClient client = HttpClients.custom()
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                        .build();
        
        HttpGet httpGet = new HttpGet(URL);
        httpGet.setHeader("Accept", "application/xml");
            
        HttpResponse response = client.execute(httpGet);
        
        assertEquals("HTTP GET failed", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        
        ResponseHandler<String> handler = new BasicResponseHandler();
        String body = handler.handleResponse(response);

        assertTrue("Unexpected response body", body.contains("Greetings from Spring Boot!"));
        
        
        
    }
}
