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
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LibertyRunMojoIT {
    private static String URL;

    private static Process process;

    @BeforeClass
    public static void init() throws Exception {
        URL = "http://localhost:8080/";

        process = Runtime.getRuntime().exec("mvn boost:run");
    }

    @AfterClass
    public static void teardown() throws Exception {
        Runtime.getRuntime().exec("mvn boost:stop");
        process.destroyForcibly();
    }

    @Test
    public void testLibertyRunMojo() throws Exception {

        // Verify server started message in logs
        int timeout = 0;
        boolean serverStarted = false;

        while (timeout < 10 && !serverStarted) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Check for startup message
                if (line.contains("CWWKF0011I")) {
                    serverStarted = true;
                    break;
                }
            }

            bufferedReader.close();

            if (!serverStarted) {

                Thread.sleep(1000);
                timeout++;
            }

        }

        assertTrue("The messages.log did not show that the server has started.", serverStarted);

        // Verify that the application is reachable
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
}
