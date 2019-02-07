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

package io.openliberty.boost.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.openliberty.boost.common.BoostLoggerI;

public final class BoostProperties {
   
    public static final String DATASOURCE_DATABASE_NAME = "boost.db.databaseName";
    public static final String DATASOURCE_SERVER_NAME = "boost.db.serverName";
    public static final String DATASOURCE_PORT_NUMBER = "boost.db.portNumber";
    public static final String DATASOURCE_USER = "boost.db.user";
    public static final String DATASOURCE_PASSWORD = "boost.db.password";
    public static final String INTERNAL_COMPILER_TARGET = "boost.internal.compiler.target";
    public static final String PASSWORD_ENCRYPTION_METHOD = "boost.pkg.prop.encryption";
    public static final String PASSWORD_ENCRYPTION_KEY = "boost.pkg.prop.encryption.key";

    /**
     * Return a list of all boost properties
     * 
     * @return
     */
    public static List<String> getAllSupportedProperties() {

        List<String> supportedProperties = new ArrayList<String>();

        supportedProperties.add(DATASOURCE_DATABASE_NAME);
        supportedProperties.add(DATASOURCE_SERVER_NAME);
        supportedProperties.add(DATASOURCE_PORT_NUMBER);
        supportedProperties.add(DATASOURCE_USER);
        supportedProperties.add(DATASOURCE_PASSWORD);
        supportedProperties.add(INTERNAL_COMPILER_TARGET);
        supportedProperties.add(PASSWORD_ENCRYPTION_METHOD);
        supportedProperties.add(PASSWORD_ENCRYPTION_KEY);

        return supportedProperties;
    }

    /**
     * Return a list of all properties that need to be encrypted
     * 
     * @return
     */
    public static List<String> getPropertiesToEncrypt() {
        List<String> propertiesToEncrypt = new ArrayList<String>();
        propertiesToEncrypt.add(DATASOURCE_PASSWORD);
        return propertiesToEncrypt;
    }
    
    public static Properties getConfiguredBoostProperties(BoostLoggerI logger) {
        List<String> supportedProps = BoostProperties.getAllSupportedProperties();
        Properties systemProperties = System.getProperties();

        Properties boostProperties = new Properties();

        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {

            if (supportedProps.contains(entry.getKey().toString())) {

                // logger.debug("Found boost property: " + entry.getKey() + ":"
                // + entry.getValue());

                boostProperties.put(entry.getKey(), entry.getValue());
            }
        }

        return boostProperties;
    }

}
