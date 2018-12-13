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
package it;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.ConnectException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class EndpointIT {
    private static String URL;

    @BeforeClass
    public static void init() {
        URL = "http://localhost:8080/";
    }

    @Test
    public void testServlet() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(URL);

        try {
            int statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            String response = method.getResponseBodyAsString(1000);

            assertTrue("Unexpected response body", response.contains("Greetings from Spring Boot!"));
        } finally {
            method.releaseConnection();
        }
    }

    @Test(expected = ConnectException.class)
    public void testNoOtherPortAvailableExceptApplicationPort() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod("http://localhost:9080/");

        int statusCode = client.executeMethod(method);
    }

    @Test
    public void testBoostStrapProperties() throws Exception {
        File propertiesFile = new File("target/liberty/wlp/usr/servers/BoostServer/bootstrap.properties");
        assertTrue(propertiesFile.getAbsolutePath() + " does not exist", propertiesFile.exists());
        try (FileReader fileReader = new FileReader(propertiesFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line = bufferedReader.readLine();
                assertNotNull(propertiesFile.getAbsolutePath() + " cannot be empty", line);
                assertTrue("Comment not found", line.startsWith("#"));
                line = bufferedReader.readLine();
                assertEquals("Expected line not found", "server.liberty.use-default-host=false", line);
            }
        }
    }
}
