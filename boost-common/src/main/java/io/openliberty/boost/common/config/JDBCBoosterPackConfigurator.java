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

import io.openliberty.boost.common.config.BoosterPackConfigurator;
import io.openliberty.boost.common.utils.BoostUtil;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JDBCBoosterPackConfigurator extends BoosterPackConfigurator {

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";
    
    public static String DEFAULT_DATABASE_NAME = SERVER_OUTPUT_DIR + "/" + DERBY_DB;

    private String dependency;
    private String libertyFeature;
    
    private Properties serverProperties;

    public JDBCBoosterPackConfigurator(String version, Properties boostConfigProperties) {
    	
    	// Set the Liberty feature based on the booster version
    	if (version.equals(EE_7_VERSION)) {
            this.libertyFeature = JDBC_41;
        } else if (version.equals(EE_8_VERSION)) {
            this.libertyFeature = JDBC_42;
        }
    	
        this.dependency = DERBY_DEFAULT;
        
        // Set server properties
        serverProperties = new Properties();
        String databaseName = (String) boostConfigProperties.getOrDefault(BoostProperties.DATASOURCE_DATABASE_NAME, DEFAULT_DATABASE_NAME);
        this.serverProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);
    }

    @Override
    public String getFeature() {
        return libertyFeature;
    }

    @Override
    public void addServerConfig(Document doc) {

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
        dataSource.setAttribute(JDBC_DRIVER_REF, DERBY_EMBEDDED);

        Element derbyProps = doc.createElement(PROPERTIES_DERBY_EMBEDDED);
        derbyProps.setAttribute(DATABASE_NAME, BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME));
        derbyProps.setAttribute(CREATE_DATABASE, "create");
        dataSource.appendChild(derbyProps);

        serverRoot.appendChild(dataSource);

        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", DERBY_EMBEDDED);
        jdbcDriver.setAttribute(LIBRARY_REF, DERBY_LIB);
        serverRoot.appendChild(jdbcDriver);
    }
    
    @Override
    public Properties getServerProperties() {
    	
    	return serverProperties;
    }

    @Override
    public String getDependency() {

        return dependency;
    }

    public void setDependency(String dependency) {

        this.dependency = dependency;
    }
}
