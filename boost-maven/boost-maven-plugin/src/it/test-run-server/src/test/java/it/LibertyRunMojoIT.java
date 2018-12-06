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

import net.wasdev.wlp.common.plugins.util.OSUtil;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LibertyRunMojoIT {
    private static String URL = "http://localhost:8080/";

    private static Process process = null;

    private static String CustomPortURL = "http://localhost:8081/";

    private static String runtimeGroupId;
    private static String runtimeArtifactId;
    private static String runtimeVersion;

    private static String mvnCmd;

    public static void setupMvnPath() {

        String mvnPath = System.getenv("M2_HOME") + "/bin/mvn";

        if (OSUtil.isWindows()) {
            mvnCmd = "cmd.exe /c " + mvnPath;
        } else {
            mvnCmd = mvnPath;
        }
    }

    public static void init() throws Exception {
        String port = null;
        init(port);
    }

    public static void init(String port) throws Exception {

        setupMvnPath();

        runtimeGroupId = System.getProperty("runtimeGroupId");
        runtimeArtifactId = System.getProperty("runtimeArtifactId");
        runtimeVersion = System.getProperty("runtimeVersion");

        String passPort = null;
        String runCommand = null;
        if (port != null) {
            runCommand = mvnCmd + " boost:run -Dserver.port=" + port + " -DruntimeGroupId=" + runtimeGroupId
                    + " -DruntimeArtifactId=" + runtimeArtifactId + " -DruntimeVersion=" + runtimeVersion;
        } else {
            runCommand = mvnCmd + " boost:run -DruntimeGroupId=" + runtimeGroupId + " -DruntimeArtifactId="
                    + runtimeArtifactId + " -DruntimeVersion=" + runtimeVersion;
        }
        process = Runtime.getRuntime().exec(runCommand);
    }

    @AfterClass
    public static void teardown() throws Exception {
        String stopCommand = mvnCmd + " boost:stop -DruntimeGroupId=" + runtimeGroupId + " -DruntimeArtifactId="
                + runtimeArtifactId + " -DruntimeVersion=" + runtimeVersion;
        Runtime.getRuntime().exec(stopCommand);
        process.destroyForcibly();
    }

    @Test
    public void testLibertyRunMojo() throws Exception {

        // make sure server is not already running
        if (process != null)
            teardown();

        init();

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

    @Test
    public void testLibertyRunMojoCustomPort() throws Exception {
        // make sure server is not already running
        if (process != null)
            teardown();

        // run server with new port.
        init("8081");
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
        GetMethod method = new GetMethod(CustomPortURL);
        try {
            int statusCode = client.executeMethod(method);
            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);
            String response = method.getResponseBodyAsString(1000);
            assertTrue("Unexpected response body", response.contains("Greetings from Spring Boot!"));
        } finally {
            method.releaseConnection();
        }
    }

    @Test
    public void testLibertyRunMojoReturnDefaultPort() throws Exception {
        // Start server with custom port
        testLibertyRunMojoCustomPort();
        // Make sure we go back to the original port after run with custom port
        testLibertyRunMojo();
    }
}
