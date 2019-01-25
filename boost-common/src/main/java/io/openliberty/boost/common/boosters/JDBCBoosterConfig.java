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

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {

    public static String DERBY_DEPENDENCY = "org.apache.derby:derby";
    public static String DB2_DEPENDENCY = "com.ibm.db2.jcc:db2jcc";

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";
    public static String DEFAULT_DERBY_DATABASE_NAME = DERBY_DB;
    public static String DEFAULT_DB2_DATABASE_NAME = DB2_DB;

    private String dependency;
    private String libertyFeature;

    private Properties serverProperties;

    public JDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {        
        String version = dependencies.get(getCoordindates(this.getClass()));

        // Check for user defined database dependencies
        String configuredDatabaseDep = null;

        if (dependencies.containsKey(JDBCBoosterConfig.DERBY_DEPENDENCY)) {
            String derbyVersion = dependencies.get(JDBCBoosterConfig.DERBY_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterConfig.DERBY_DEPENDENCY + ":" + derbyVersion;

        } else if (dependencies.containsKey(JDBCBoosterConfig.DB2_DEPENDENCY)) {
            String db2Version = dependencies.get(JDBCBoosterConfig.DB2_DEPENDENCY);
            configuredDatabaseDep = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + db2Version;
        }

        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
        init(version, boostConfigProperties, configuredDatabaseDep);
    }
    
    /**
     * For tests only
     */
    protected JDBCBoosterConfig(String version, Properties boostConfigProperties, String configuredDatabaseDep) {
        init(version, boostConfigProperties, configuredDatabaseDep);
    }
    
    private void init(String version, Properties boostConfigProperties, String configuredDatabaseDep) {
        // Set the Liberty feature based on the booster version
        if (version.equals(EE_7_VERSION)) {
            this.libertyFeature = JDBC_41;
        } else if (version.equals(EE_8_VERSION)) {
            this.libertyFeature = JDBC_42;
        }

        if (configuredDatabaseDep == null) {
            this.dependency = DERBY_DEFAULT;
        } else {
            this.dependency = configuredDatabaseDep;
        }

        // Set server properties
        this.serverProperties = new Properties();

        if (this.dependency.startsWith(DERBY_DEPENDENCY)) {
            String databaseName = (String) boostConfigProperties.getOrDefault(BoostProperties.DATASOURCE_DATABASE_NAME,
                    DEFAULT_DERBY_DATABASE_NAME);
            this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);

        } else if (this.dependency.startsWith(DB2_DEPENDENCY)) {

            // If serverName or portNumber are not provided, use DB2's default "localhost"
            // and "50000"
            String serverName = (String) boostConfigProperties.getOrDefault(BoostProperties.DATASOURCE_SERVER_NAME,
                    LOCALHOST);
            this.serverProperties.put(BoostProperties.DATASOURCE_SERVER_NAME, serverName);

            String portNumber = (String) boostConfigProperties.getOrDefault(BoostProperties.DATASOURCE_PORT_NUMBER,
                    "50000");
            this.serverProperties.put(BoostProperties.DATASOURCE_PORT_NUMBER, portNumber);

            // For databaseName, user, and password, there is no default that would serve
            // any purpose. The variables
            // will be configured in the server.xml, but no values will be set in
            // bootstrap.properties. The values will
            // need to be passed by the user at runtime.
            String databaseName = (String) boostConfigProperties.get(BoostProperties.DATASOURCE_DATABASE_NAME);
            if (databaseName != null) {
                this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);
            }

            String user = (String) boostConfigProperties.get(BoostProperties.DATASOURCE_USER);
            if (user != null)
                this.serverProperties.put(BoostProperties.DATASOURCE_USER, user);

            String password = (String) boostConfigProperties.get(BoostProperties.DATASOURCE_PASSWORD);
            if (password != null) {
                this.serverProperties.put(BoostProperties.DATASOURCE_PASSWORD, password);
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

            addDerbyConfig(doc);

        } else if (dependency.startsWith(DB2_DEPENDENCY)) {

            addDb2Config(doc);
        }
    }

    private void addDerbyConfig(Document doc) {

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
        lib.setAttribute("id", DERBY_LIB);
        Element fileLoc = doc.createElement(FILESET);
        fileLoc.setAttribute("dir", RESOURCES);
        fileLoc.setAttribute("includes", "derby*.jar");
        lib.appendChild(fileLoc);
        serverRoot.appendChild(lib);

        // Add datasource
        Element dataSource = doc.createElement(DATASOURCE);
        dataSource.setAttribute("id", DEFAULT_DATASOURCE);
        dataSource.setAttribute(JDBC_DRIVER_REF, DERBY_EMBEDDED_DRIVER_REF);

        Element derbyProps = doc.createElement(PROPERTIES_DERBY_EMBEDDED);
        derbyProps.setAttribute(DATABASE_NAME, BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME));
        derbyProps.setAttribute(CREATE_DATABASE, "create");
        dataSource.appendChild(derbyProps);

        serverRoot.appendChild(dataSource);

        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", DERBY_EMBEDDED_DRIVER_REF);
        jdbcDriver.setAttribute(LIBRARY_REF, DERBY_LIB);
        serverRoot.appendChild(jdbcDriver);
    }

    private void addDb2Config(Document doc) {

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
        lib.setAttribute("id", DB2_LIB);
        Element fileLoc = doc.createElement(FILESET);
        fileLoc.setAttribute("dir", RESOURCES);
        fileLoc.setAttribute("includes", "db2jcc*.jar");
        lib.appendChild(fileLoc);
        serverRoot.appendChild(lib);

        // Add datasource
        Element dataSource = doc.createElement(DATASOURCE);
        dataSource.setAttribute("id", DEFAULT_DATASOURCE);
        dataSource.setAttribute(JDBC_DRIVER_REF, DB2_DRIVER_REF);
        dataSource.setAttribute(CONTAINER_AUTH_DATA_REF, DATASOURCE_AUTH_DATA);

        Element db2Props = doc.createElement(PROPERTIES_DB2_JCC);
        db2Props.setAttribute(DATABASE_NAME, BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME));
        db2Props.setAttribute(SERVER_NAME, BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME));
        db2Props.setAttribute(PORT_NUMBER, BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER));
        dataSource.appendChild(db2Props);
        serverRoot.appendChild(dataSource);

        Element containerAuthData = doc.createElement(AUTH_DATA);
        containerAuthData.setAttribute("id", DATASOURCE_AUTH_DATA);
        containerAuthData.setAttribute(USER, BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER));
        containerAuthData.setAttribute(PASSWORD, BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD));
        serverRoot.appendChild(containerAuthData);

        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", DB2_DRIVER_REF);
        jdbcDriver.setAttribute(LIBRARY_REF, DB2_LIB);
        serverRoot.appendChild(jdbcDriver);
    }
}
