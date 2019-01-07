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

import static io.openliberty.boost.common.config.ConfigConstants.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JDBCBoosterPackConfigurator extends BoosterPackConfigurator {

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";

    String dependency;

    String libertyFeature;

    public JDBCBoosterPackConfigurator() {
        this.dependency = DERBY_DEFAULT;
    }

    /**
     * retrieves the default boost feature string for the jdbc dependency
     */
    public String getFeature() {
        return libertyFeature;
    }

    /**
     * writes out jdbc default config data when selected by the presence of a jdbc
     * boost dependency
     */
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
        derbyProps.setAttribute(DATABASE_NAME, SERVER_OUTPUT_DIR + "/" + DERBY_DB);
        derbyProps.setAttribute(CREATE_DATABASE, "create");
        dataSource.appendChild(derbyProps);

        serverRoot.appendChild(dataSource);

        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", DERBY_EMBEDDED);
        jdbcDriver.setAttribute(LIBRARY_REF, DERBY_LIB);
        serverRoot.appendChild(jdbcDriver);
    }

    @Override
    public void setFeature(String version) {

        if (version.equals(EE_7_VERSION)) {
            libertyFeature = JDBC_41;
        } else if (version.equals(EE_8_VERSION)) {
            libertyFeature = JDBC_42;
        }

    }

    @Override
    public String getDependency() {

        return dependency;
    }

    @Override
    public void setDependency(String dependency) {

        this.dependency = dependency;
    }
}
