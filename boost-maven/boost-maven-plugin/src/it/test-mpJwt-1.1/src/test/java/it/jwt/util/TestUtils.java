// tag::copyright[]
/**
 * *****************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - Initial implementation
 * *****************************************************************************
 */
// end::copyright[]
package jwt.util;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestUtils {

    public static Response processRequest(String url, String method, String payload,
            String authHeader) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);
        Builder builder = target.request();
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        if (authHeader != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return (payload != null)
                ? builder.build(method, Entity.json(payload)).invoke()
                : builder.build(method).invoke();
    }

    public static JsonObject toJsonObj(String json) {
        JsonReader jReader = Json.createReader(new StringReader(json));
        return jReader.readObject();
    }

}
