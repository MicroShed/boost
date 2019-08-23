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
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import javax.ws.rs.BadRequestException;

public class EndpointIT {
    private static String URL;

    @BeforeClass
    public static void init() {
        String port = System.getProperty("boost.http.port");
        URL = "http://localhost:" + port + "/api/hello";
    }

    @Test
    public void testServlet() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(URL + "/hello");

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
    public void testServletWithString() throws Exception {
        HttpClient client = new HttpClient();

        // this request should fail the bean validation on the string data param
        // min=2, max=10
        GetMethod method = new GetMethod(URL + "/AndyAndyAndy");

        try {
            int statusCode = client.executeMethod(method);

            assertEquals("HTTP GET succeeded", HttpStatus.SC_BAD_REQUEST, statusCode);

            String response = method.getResponseBodyAsString(10000);

            assertTrue("Unexpected response body", (response.isEmpty()));
        } finally {
            method.releaseConnection();
        }
    }
}
