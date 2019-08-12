// ******************************************************************************
//  Copyright (c) 2017 IBM Corporation and others.
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  which accompanies this distribution, and is available at
//  http://www.eclipse.org/legal/epl-v10.html
//
//  Contributors:
//  IBM Corporation - initial API and implementation
// ******************************************************************************
package jwt.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
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
import javax.ws.rs.core.Response.Status;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.junit.Assert;

import java.security.Key;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jose4j.base64url.SimplePEMEncoder;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;

public class JwtVerifier {

    // The algorithm used to sign the JWT.
    private static final String JWT_ALGORITHM = "SHA256withRSA";

    // The issuer of the JWT. This must match the issuer that the liberty server expects,
    // defined in server.xml.
    private static final String JWT_ISSUER = System.getProperty("jwt.issuer", "http://openliberty.io");

    // The hostname we'll use in our tests. The hostname of the backend service.
    private static final String libertyHostname = "localhost";

    // The SSL port we'll use in our tests. The ssl port of the backend service.
    private static final String libertySslPort = "5050";

    private static String keystorePath;
    
    private static final String secret = "secret";
        

    /**
     * Make a microprofile-compliant JWT with the correct secret key.
     *
     * @return A base 64 encoded JWT.
     */
    public String createJwt(String username, String groups) throws GeneralSecurityException, IOException, Exception {
    	    if( username != null && groups != null) {
       	    String[] claims = new String[2];
       	    claims[0] = groups;
       	    claims[1] = "customClaim=Custom";
            return JwtBuilder.buildJwt(username, "http://openliberty.io", claims);
    	    } else {
    	    	   throw new Exception("group or user name is null");
    	    }
    	    
    }



    /** Create a groups array to put in the JWT. */
    private static JsonArray getGroupArray(Set<String> groups) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        if (groups != null) {
            for (String group : groups) {
                arrayBuilder.add(group);
            }
        }

        return arrayBuilder.build();
    }



    public static JsonObject toJsonObj(String json) {
        JsonReader jReader = Json.createReader(new StringReader(json));
        return jReader.readObject();
    }

    private Response processRequest(String url, String method, String payload, String authHeader) {
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
}
