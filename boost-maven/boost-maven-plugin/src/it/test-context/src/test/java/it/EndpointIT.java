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
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class EndpointIT {
    private static String URL;
    private static Process process;

    private static String HelloResource;
    private static String HelloResourceReplace;
    private static String HelloResourceTemp;
    private static String osname;
    private static boolean isWindows;

    @BeforeClass
    public static void init() {
        URL = "http://localhost:9080/api/hello";
        osname = System.getProperty("os.name");
        System.out.println("OS Version =" + osname);
        if (osname.contains("Windows")) {
            isWindows = true;
        } else {
            isWindows = false;
        }
        setupHelloResources();
    }

    @Test
    public void testServlet() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(URL);

        try {
            int statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            String response = method.getResponseBodyAsString(10000);

            assertTrue("Unexpected response body",
                    response.contains("Hello World From Your Friends at Liberty Boost EE!"));
        } finally {
            method.releaseConnection();
        }
    }

    @Test
    public void testServletAfterCompile() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(URL);

        try {
            int statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            String response = method.getResponseBodyAsString(10000);

            assertTrue("Unexpected response body 1st request = " + response, response.contains(
                    "Hello World From Your Friends at Liberty Boost EE! start over - added the ctxRoot support and now added looseApp support - is it back?!?!?"));

            // Change text from HTTP Get
            moveSourceFile(HelloResource, HelloResourceTemp);
            moveSourceFile(HelloResourceReplace, HelloResource);

            mavenCompile();

            Thread.sleep(1000);

            method = new GetMethod(URL);

            statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            response = method.getResponseBodyAsString(10000);

            // Reset java files.
            moveSourceFile(HelloResource, HelloResourceReplace);
            moveSourceFile(HelloResourceTemp, HelloResource);

            assertTrue("Unexpected response body 2nd request = " + response,
                    response.contains("Hello World From Your Friends at Liberty Boost EE! Updated after compile"));

        } finally {
            method.releaseConnection();
        }

    }

    private void moveSourceFile(String fromFile, String toFile) throws IOException {
        String moveCmd;
        if (isWindows) {
            moveCmd = "move ";
        } else {
            moveCmd = "mv ";
        }
        String runCommand = moveCmd + fromFile + " " + toFile;
        runCommand(runCommand);

    }

    private void mavenCompile() throws IOException {
        String command;
        if (!isWindows)
            command = "touch " + HelloResource;
        else
            command = "copy /b " + HelloResource + " +,,";
        runCommand(command);

        command = "mvn compile";
        runCommand(command);
    }

    private static void setupHelloResources() {

        if (isWindows) {
            HelloResource = "src\\main\\java\\com\\example\\HelloResource.java";
            HelloResourceReplace = "src\\main\\java\\com\\example\\HelloResource.replace";
            HelloResourceTemp = "src\\main\\java\\com\\example\\HelloResource.tmp";
        } else {
            HelloResource = "src/main/java/com/example/HelloResource.java";
            HelloResourceReplace = "src/main/java/com/example/HelloResource.replace";
            HelloResourceTemp = "src/main/java/com/example/HelloResource.tmp";
        }

    }

    private void runCommand(String command) throws IOException {

        System.out.println("Command to run = " + command);

        process = Runtime.getRuntime().exec(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        System.out.println("command result =" + result);
    }
}
