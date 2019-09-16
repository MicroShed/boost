/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import utils.BoostConstants;
import utils.LibertyConfigFileUtils;

public class Db2PasswordAesIT implements BoostConstants {

    private final String DB_NAME = "myCustomDB";
    private final String DB_USER = "user";
    private final String ENCODED_DB_PASS = "{aes}Lz4sLCgwLTs=";

    @BeforeClass
    public static void init() {
        // TODO: this should eventually be moved to a unit test
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));
    }

    @Test
    public void checkPropertiesTest() throws Exception {
        String variablesXml = "target/liberty/wlp/usr/servers/defaultServer/configDropins/defaults/variables.xml";

        assertEquals("Incorrect boost_db_user found in server config.", DB_USER,
                LibertyConfigFileUtils.findVariableInXml(variablesXml, DATASOURCE_USER));

        assertEquals("Incorrect boost_db_password found in server config.", ENCODED_DB_PASS,
                LibertyConfigFileUtils.findVariableInXml(variablesXml, DATASOURCE_PASSWORD));

        assertEquals("Incorrect boost_db_databaseName found in server config.", DB_NAME,
                LibertyConfigFileUtils.findVariableInXml(variablesXml, DATASOURCE_DATABASE_NAME));
    }
}
