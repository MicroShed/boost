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
package io.openliberty.boost.common.boosters;

import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import io.openliberty.boost.common.utils.BoostUtil;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {

    public static String DERBY_DEPENDENCY = "org.apache.derby:derby";
    public static String DB2_DEPENDENCY = "com.ibm.db2.jcc:db2jcc";
    public static String MYSQL_DEPENDENCY = "mysql:mysql-connector-java";

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";

    private String dependency;
    private String libertyFeature;
    List<String> tomeeDependencyStrings = new ArrayList<String>();

    private Properties serverProperties;

    public JDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {

        // Check for user defined database dependencies
        String configuredDatabaseDep = null;

        if (dependencies.containsKey(JDBCBoosterConfig.DERBY_DEPENDENCY)) {
            String derbyVersion = dependencies.get(JDBCBoosterConfig.DERBY_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterConfig.DERBY_DEPENDENCY + ":" + derbyVersion;

        } else if (dependencies.containsKey(JDBCBoosterConfig.DB2_DEPENDENCY)) {
            String db2Version = dependencies.get(JDBCBoosterConfig.DB2_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + db2Version;

        } else if (dependencies.containsKey(JDBCBoosterConfig.MYSQL_DEPENDENCY)) {
            String mysqlVersion = dependencies.get(JDBCBoosterConfig.MYSQL_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterConfig.MYSQL_DEPENDENCY + ":" + mysqlVersion;
        }

        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
        init(boostConfigProperties, configuredDatabaseDep);
    }

    private void init(Properties boostConfigProperties, String configuredDatabaseDep) {

        // Feature version is determined by the Java compiler target value.
        String compilerVersion = boostConfigProperties.getProperty(BoostProperties.INTERNAL_COMPILER_TARGET);

        if ("1.8".equals(compilerVersion) || "8".equals(compilerVersion) || "9".equals(compilerVersion)
                || "10".equals(compilerVersion)) {
            this.libertyFeature = JDBC_42;
        } else if ("11".equals(compilerVersion)) {
            this.libertyFeature = JDBC_43;
        } else {
            this.libertyFeature = JDBC_41; // Default to the spec for Liberty's
                                           // minimum supported JRE (version 7
                                           // as of 17.0.0.3)
        }

        if (configuredDatabaseDep == null) {
            this.dependency = DERBY_DEFAULT;
        } else {
            this.dependency = configuredDatabaseDep;
        }

        // Set server properties
        this.serverProperties = new Properties();

        // Initialize defaults and required properties for each datasource vendor
        if (this.dependency.startsWith(DERBY_DEPENDENCY)) {
            // Embedded Derby requires a database name. Set a default for this and create
            // it.
            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
            this.serverProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");

        } else if (this.dependency.startsWith(DB2_DEPENDENCY)) {
            // For DB2, since we are expecting the database to exist, there is no
            // default value we can set for databaseName that would be of any use.
            // Likewise, for user and password, there isn't anything we could set
            // here that would make sense. Since these properties are required,
            // set empty strings as there values to create place holders. If they
            // are not overridden by the user at package time, they can be overridden
            // at runtime.
            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_USER, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_PASSWORD, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_SERVER_NAME, LOCALHOST);
            this.serverProperties.put(BoostProperties.DATASOURCE_PORT_NUMBER, DB2_DEFAULT_PORT_NUMBER);

        } else if (this.dependency.startsWith(MYSQL_DEPENDENCY)) {
            // Same set of minimum requirements for MySQL
            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_USER, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_PASSWORD, "");
            this.serverProperties.put(BoostProperties.DATASOURCE_SERVER_NAME, LOCALHOST);
            this.serverProperties.put(BoostProperties.DATASOURCE_PORT_NUMBER, MYSQL_DEFAULT_PORT_NUMBER);
        }

        // Find and add all "boost.db." properties. This will override any default
        // values
        for (String key : boostConfigProperties.stringPropertyNames()) {
            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
                String value = (String) boostConfigProperties.get(key);
                this.serverProperties.put(key, value);
            }
        }
    }

    @Override
    public String getFeature() {
        return libertyFeature;
    }

    @Override
    public Properties getServerProperties() {

        return serverProperties;
    }

    @Override
    public String getDependency() {

        return dependency;
    }

    @Override
    public void addServerConfig(Document doc) {

        if (dependency.startsWith(DERBY_DEPENDENCY)) {
            addDatasourceConfig(doc, PROPERTIES_DERBY_EMBEDDED, DERBY_JAR);

        } else if (dependency.startsWith(DB2_DEPENDENCY)) {
            addDatasourceConfig(doc, PROPERTIES_DB2_JCC, DB2_JAR);

        } else if (dependency.startsWith(MYSQL_DEPENDENCY)) {
            // Use generic <properties> element for MySQL
            addDatasourceConfig(doc, PROPERTIES, MYSQL_JAR);
        }
    }

    private void addDatasourceConfig(Document doc, String datasourcePropertiesElement, String datasourceJar) {

        Element serverRoot = doc.getDocumentElement();

        // Find the root server element
        NodeList list = doc.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals("server")) {
                serverRoot = (Element) list.item(i);
            }
        }

        // Add library
        Element lib = doc.createElement(LIBRARY);
        lib.setAttribute("id", JDBC_LIBRARY_1);
        Element fileLoc = doc.createElement(FILESET);
        fileLoc.setAttribute("dir", RESOURCES);
        fileLoc.setAttribute("includes", datasourceJar);
        lib.appendChild(fileLoc);
        serverRoot.appendChild(lib);

        // Add datasource
        Element dataSource = doc.createElement(DATASOURCE);
        dataSource.setAttribute("id", DEFAULT_DATASOURCE);
        dataSource.setAttribute(JDBC_DRIVER_REF, JDBC_DRIVER_1);

        // Add all configured datasource properties
        Element props = doc.createElement(datasourcePropertiesElement);
        addDatasourceProperties(props);
        dataSource.appendChild(props);

        serverRoot.appendChild(dataSource);

        // Add jdbc driver
        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", JDBC_DRIVER_1);
        jdbcDriver.setAttribute(LIBRARY_REF, JDBC_LIBRARY_1);
        serverRoot.appendChild(jdbcDriver);

        // Add container authentication
        if (this.serverProperties.containsKey(BoostProperties.DATASOURCE_USER)
                && this.serverProperties.containsKey(BoostProperties.DATASOURCE_PASSWORD)) {
            dataSource.setAttribute(CONTAINER_AUTH_DATA_REF, DATASOURCE_AUTH_DATA);

            Element containerAuthData = doc.createElement(AUTH_DATA);
            containerAuthData.setAttribute("id", DATASOURCE_AUTH_DATA);
            containerAuthData.setAttribute(USER, BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER));
            containerAuthData.setAttribute(PASSWORD, BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD));
            serverRoot.appendChild(containerAuthData);
        }
    }

    private void addDatasourceProperties(Element properties) {
        for (String property : this.serverProperties.stringPropertyNames()) {
            // We are using container authentication. Do not include user or password here
            if (!property.equals(BoostProperties.DATASOURCE_USER)
                    && !property.equals(BoostProperties.DATASOURCE_PASSWORD)) {

                String attribute = property.replace(BoostProperties.DATASOURCE_PREFIX, "");
                properties.setAttribute(attribute, BoostUtil.makeVariable(property));
            }
        }
    }

    @Override
    public List<String> getTomEEDependency() {
        return tomeeDependencyStrings;
    }
}
