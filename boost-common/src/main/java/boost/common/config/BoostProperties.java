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

package boost.common.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import boost.common.BoostLoggerI;

public final class BoostProperties {

    // Boost specific
    public static final String BOOST_PROP_PREFIX = "boost_";

    // HTTP Endpoint properties
    public static final String ENDPOINT_HOST = "boost_http_host";
    public static final String ENDPOINT_HTTP_PORT = "boost_http_port";
    public static final String ENDPOINT_HTTPS_PORT = "boost_http_securePort";

    // Datasource default properties
    public static final String DATASOURCE_PREFIX = "boost_db_";
    public static final String DATASOURCE_DATABASE_NAME = "boost_db_databaseName";
    public static final String DATASOURCE_SERVER_NAME = "boost_db_serverName";
    public static final String DATASOURCE_PORT_NUMBER = "boost_db_portNumber";
    public static final String DATASOURCE_USER = "boost_db_user";
    public static final String DATASOURCE_PASSWORD = "boost_db_password";
    public static final String DATASOURCE_CREATE_DATABASE = "boost_db_createDatabase";
    public static final String DATASOURCE_URL = "boost_db_url";

    public static final String AES_ENCRYPTION_KEY = "boost_aes_key";

    public static final String INTERNAL_COMPILER_TARGET = "boost.internal.compiler.target";

    /**
     * Return a list of all properties that need to be encrypted
     * 
     * @return
     */
    public static Map<String, String> getPropertiesToEncrypt() {
        Map<String, String> propertiesToEncrypt = new HashMap<String, String>();

        // Add default encryption types for properties we define
        propertiesToEncrypt.put(DATASOURCE_PASSWORD, "aes");

        return propertiesToEncrypt;
    }

    public static Properties getConfiguredBoostProperties(Properties projectProperties, BoostLoggerI logger) {
        Properties systemProperties = System.getProperties();

        Properties boostProperties = new Properties();

        // Add project properties first to allow them to be overriden by
        // system properties (set at command line)
        for (Map.Entry<Object, Object> entry : projectProperties.entrySet()) {

            if (entry.getKey().toString().startsWith(BOOST_PROP_PREFIX)) {

                // logger.debug("Found boost property: " +
                // entry.getKey() + ":" + entry.getValue());

                boostProperties.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {

            if (entry.getKey().toString().startsWith(BOOST_PROP_PREFIX)) {

                // logger.debug("Found boost property: " +
                // entry.getKey() + ":" + entry.getValue());

                boostProperties.put(entry.getKey(), entry.getValue());
            }
        }

        return boostProperties;
    }

}
