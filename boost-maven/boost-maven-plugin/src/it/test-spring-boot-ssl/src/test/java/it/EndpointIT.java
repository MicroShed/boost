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

import javax.net.ssl.SSLContext;

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

public class EndpointIT {
    private static String URL;

    @BeforeClass
    public static void init() {
        URL = "https://localhost:8443/";
    }

    @Test
    public void testServlet() throws Exception {

        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
                .build();

        CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        HttpGet httpGet = new HttpGet(URL);
        httpGet.setHeader("Accept", "application/xml");

        HttpResponse response = client.execute(httpGet);

        assertEquals("HTTP GET failed", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ResponseHandler<String> handler = new BasicResponseHandler();
        String body = handler.handleResponse(response);

        assertTrue("Unexpected response body", body.contains("Greetings from Spring Boot!"));
    }
}
