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

    public JDBCBoosterPackConfigurator(BoosterDependencyInfo depInfo, LibertyServerConfigGenerator srvrXML) {
		super(depInfo, srvrXML);
		// TODO Auto-generated constructor stub
	}

	/**
     * The artifactId of the dependency for this booster that needs to be copied
     * to the server
     */
    private final String DEPENDENCY_ARTIFACT = "org.apache.derby:derby:10.14.2.0";

    /**
     * writes out jdbc default config data when selected by the presence of a jdbc
     * boost dependency
     */
	@Override
    public void addServerConfig(Document doc) {
        
		// write out the feature Manager stanza
		if (dependencyInfo.getVersion().equals(EE_7_VERSION)) {
			serverXML.addFeature(JDBC_41);
        } else if (dependencyInfo.getVersion().equals(EE_8_VERSION)) {
        	serverXML.addFeature(JDBC_42);
        }

		//write out config stanzas
		
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
    public String getDependencyToCopy() {
        this.dependency = dependency;
    }
}
