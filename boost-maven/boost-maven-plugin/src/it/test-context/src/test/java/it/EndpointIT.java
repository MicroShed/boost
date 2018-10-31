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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class EndpointIT {
    private static String URL;
    /*
     * private static String runtimeGroupId; private static String
     * runtimeArtifactId; private static String runtimeVersion;
     */
    private static Process process;

    private static String HelloResource;
    private static String osname;
    // private static boolean isWindows;
    private static String originalStatement = "        return \"Hello World From Your Friends at Liberty Boost EE! start over - added the ctxRoot support and now added looseApp support - is it back?!?!?\";";
    private static String updatedStatement = "        return \"Hello World From Your Friends at Liberty Boost EE! Updated after compile\";";

    @BeforeClass
    public static void init() {
        URL = "http://localhost:9080/api/hello";
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
            updateApplicationSource(updatedStatement, originalStatement);

            mavenCompile();

            Thread.sleep(1000);

            method = new GetMethod(URL);

            statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            response = method.getResponseBodyAsString(10000);

            // Reset java files.
            updateApplicationSource(originalStatement, updatedStatement);

            assertTrue("Unexpected response body 2nd request = " + response,
                    response.contains("Hello World From Your Friends at Liberty Boost EE! Updated after compile"));

        } finally {
            method.releaseConnection();
        }

    }

    private void mavenCompile() throws IOException {
        String command;
        command = "mvn compile";
        runCommand(command);
    }

    private static void setupHelloResources() {

        String sep = File.separator;
        HelloResource = "src" + sep + "main" + sep + "java" + sep + "com" + sep + "example" + sep
                + "HelloResource.java";

    }

    private void updateApplicationSource(String newStatement, String oldStatement) throws Exception {
        BufferedWriter bw;
        FileReader fr;
        FileWriter fw;
        BufferedReader br;
        String s;
        boolean updatedApplication = false;
        ArrayList<String> fileContents = new ArrayList<String>();

        try {
            fr = new FileReader(HelloResource);
            br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                if (s.trim().equals(oldStatement.trim())) {
                    fileContents.add(newStatement);
                    updatedApplication = true;
                } else
                    fileContents.add(s);
            }
            br.close();

            if (!updatedApplication)
                throw new Exception("Statement not found in application " + oldStatement);

            fw = new FileWriter(HelloResource);
            bw = new BufferedWriter(fw);

            for (String statement : fileContents) {
                bw.write(statement + '\n');
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            System.out.println("File was not found! " + e.toString());
        } catch (IOException e) {
            System.out.println("No file found!" + e.toString());
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
