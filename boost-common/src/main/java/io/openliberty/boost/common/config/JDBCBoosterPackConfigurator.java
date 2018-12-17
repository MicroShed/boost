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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JDBCBoosterPackConfigurator extends BoosterPackConfigurator {

    /**
     * The artifactId of the dependency for this booster that needs to be copied to
     * the server
     */
    private final String DEPENDENCY_ARTIFACT = "org.apache.derby:derby:10.14.2.0";

    String libertyFeature = null;

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
        lib.setAttribute("id", "DerbyLib");
        Element fileLoc = doc.createElement(FILESET);
        fileLoc.setAttribute("dir", "resources");
        fileLoc.setAttribute("includes", "derby*.jar");
        lib.appendChild(fileLoc);
        serverRoot.appendChild(lib);

        // Add datasource
        Element dataSource = doc.createElement(DATASOURCE);
        dataSource.setAttribute("id", "DefaultDataSource");
        dataSource.setAttribute(JDBC_DRIVER_REF, "DerbyEmbedded");

        Element derbyProps = doc.createElement(PROPERTIES_DERBY_EMBEDDED);
        derbyProps.setAttribute(DATABASE_NAME, "${server.output.dir}/DerbyDB");
        derbyProps.setAttribute("createDatabase", "create");
        dataSource.appendChild(derbyProps);

        serverRoot.appendChild(dataSource);

        Element jdbcDriver = doc.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", "DerbyEmbedded");
        jdbcDriver.setAttribute(LIBRARY_REF, "DerbyLib");
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
    public String getDependencyToCopy() {

        return DEPENDENCY_ARTIFACT;
    }
}
