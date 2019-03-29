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
package io.openliberty.boost.common.boosters.wildfly;

import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import io.openliberty.boost.common.utils.BoostUtil;

import static io.openliberty.boost.common.config.LibertyConfigConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@BoosterCoordinates(AbstractBoosterWildflyConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterWildflyConfig extends AbstractBoosterWildflyConfig {

    public static String DERBY_DEPENDENCY = "org.apache.derby:derby";
    public static String DB2_DEPENDENCY = "com.ibm.db2.jcc:db2jcc";
    public static String MYSQL_DEPENDENCY = "mysql:mysql-connector-java";

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";

    private String dependency;
    private List<String> configurationCommands;

    public JDBCBoosterWildflyConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {

        // Check for user defined database dependencies
        String configuredDatabaseDep = null;

        if (dependencies.containsKey(JDBCBoosterWildflyConfig.DERBY_DEPENDENCY)) {
            String derbyVersion = dependencies.get(JDBCBoosterWildflyConfig.DERBY_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterWildflyConfig.DERBY_DEPENDENCY + ":" + derbyVersion;

        } else if (dependencies.containsKey(JDBCBoosterWildflyConfig.DB2_DEPENDENCY)) {
            String db2Version = dependencies.get(JDBCBoosterWildflyConfig.DB2_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterWildflyConfig.DB2_DEPENDENCY + ":" + db2Version;

        } else if (dependencies.containsKey(JDBCBoosterWildflyConfig.MYSQL_DEPENDENCY)) {
            String mysqlVersion = dependencies.get(JDBCBoosterWildflyConfig.MYSQL_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterWildflyConfig.MYSQL_DEPENDENCY + ":" + mysqlVersion;
        }

        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
        init(boostConfigProperties, configuredDatabaseDep);
    }

    private void init(Properties boostConfigProperties, String configuredDatabaseDep) {

        if (configuredDatabaseDep == null) {
            this.dependency = DERBY_DEFAULT;
        } else {
            this.dependency = configuredDatabaseDep;
        }


//        // Initialize defaults and required properties for each datasource
//        // vendor
//        if (this.dependency.startsWith(DERBY_DEPENDENCY)) {
//            // Embedded Derby requires a database name. Set a default for this
//            // and create it.
//            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
//            this.serverProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");
//
//        } else if (this.dependency.startsWith(DB2_DEPENDENCY)) {
//            // For DB2, since we are expecting the database to exist, there is
//            // no
//            // default value we can set for databaseName that would be of any
//            // use.
//            // Likewise, for user and password, there isn't anything we could
//            // set
//            // here that would make sense. Since these properties are required,
//            // set empty strings as there values to create place holders. If
//            // they
//            // are not overridden by the user at package time, they can be
//            // overridden
//            // at runtime.
//            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_USER, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_PASSWORD, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_SERVER_NAME, LOCALHOST);
//            this.serverProperties.put(BoostProperties.DATASOURCE_PORT_NUMBER, DB2_DEFAULT_PORT_NUMBER);
//
//        } else if (this.dependency.startsWith(MYSQL_DEPENDENCY)) {
//            // Same set of minimum requirements for MySQL
//            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_USER, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_PASSWORD, "");
//            this.serverProperties.put(BoostProperties.DATASOURCE_SERVER_NAME, LOCALHOST);
//            this.serverProperties.put(BoostProperties.DATASOURCE_PORT_NUMBER, MYSQL_DEFAULT_PORT_NUMBER);
//        }
//
//        // Find and add all "boost.db." properties. This will override any
//        // default values
//        for (String key : boostConfigProperties.stringPropertyNames()) {
//            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
//                String value = (String) boostConfigProperties.get(key);
//                this.serverProperties.put(key, value);
//            }
//        }
    }
    
    @Override
    public List<String> getCliCommands() {
    	return configurationCommands;
    }

    @Override
    public String getDependency() {

        return dependency;
    }
}
