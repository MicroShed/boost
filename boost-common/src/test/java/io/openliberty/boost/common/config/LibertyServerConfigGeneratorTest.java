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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

import io.openliberty.boost.common.config.ConfigFileUtils;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import io.openliberty.boost.common.utils.BoostUtil;

import static io.openliberty.boost.common.config.ConfigConstants.*;
import static io.openliberty.boost.common.config.DOMUtils.getDirectChildrenByTag;

public class LibertyServerConfigGeneratorTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    /**
     * Test adding feature
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddSpringFeature() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath());
        serverConfig.addFeature(SPRING_BOOT_15);
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + SPRING_BOOT_15 + "</feature>");

        assertTrue("The " + SPRING_BOOT_15 + " feature was not found in the server configuration", featureFound);

    }

    @Test
    public void testZeroFeaturesInDefaultServerConfig()
            throws ParserConfigurationException, TransformerException, IOException {
        LibertyServerConfigGenerator g = new LibertyServerConfigGenerator(outputDir.getRoot().getAbsolutePath());
        Element serverRoot = g.getServerDoc().getDocumentElement();
        List<Element> featureMgrList = getDirectChildrenByTag(serverRoot, FEATURE_MANAGER);
        assertEquals("Didn't find one and only one featureMgr", 1, featureMgrList.size());
        Element featureMgr = featureMgrList.get(0);
        List<Element> featureList = getDirectChildrenByTag(featureMgr, FEATURE);
        assertEquals("Didn't find empty list of features", 0, featureList.size());
    }
    
    /**
     * Test that the EE8 JDBC version (jdbc-4.2) has been added by the JDBC booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_EE8() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath());
        
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", new Properties());
        
        serverConfig.addFeature(configurator.getFeature());
        
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }
    
    /**
     * Test that the EE8 JDBC version (jdbc-4.2) has been added by the JDBC booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_EE7() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath());
        
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.1-SNAPSHOT", new Properties());
        
        serverConfig.addFeature(configurator.getFeature());
        
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");

        assertTrue("The " + JDBC_41 + " feature was not found in the server configuration", featureFound);

    }
    
    /**
     * Test that the server is configured with the default Derby datasource
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_Derby() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath());
        
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", new Properties());
        
        serverConfig.addBoosterConfig(configurator);
        
        Element serverRoot = serverConfig.getServerDoc().getDocumentElement();
        
        // Check that the <library> element is correctly configured
        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
        assertEquals("Didn't find one and only one library", 1, libraryList.size());
   
        Element library = libraryList.get(0);
        assertEquals("Library id is not correct", DERBY_LIB, library.getAttribute("id"));
        
        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
        assertEquals("Fileset includes attribute is not correct", "derby*.jar", fileset.getAttribute("includes"));
        
        // Check that the <dataSource> element is correctly configured
        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());
        
        Element dataSource = dataSourceList.get(0);
        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
        assertEquals("DataSource jdbcDriverRef is not correct", DERBY_EMBEDDED, dataSource.getAttribute(JDBC_DRIVER_REF));
        
        Element propertiesDerbyEmbedded = getDirectChildrenByTag(dataSource, PROPERTIES_DERBY_EMBEDDED).get(0);
        assertEquals("The createDatabase attribute is not correct", "create", propertiesDerbyEmbedded.getAttribute(CREATE_DATABASE));
        assertEquals("The databaseName attribute is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME), propertiesDerbyEmbedded.getAttribute(DATABASE_NAME));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());
        
        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", DERBY_EMBEDDED, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", DERBY_LIB, jdbcDriver.getAttribute(LIBRARY_REF));
    }
    
    /**
     * Test that the databaseName property is correctly written to bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_databaseName() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath());
        
        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "myDatabase");
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties);
        
        serverConfig.addBoosterConfig(configurator);
        
        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties, BoostProperties.DATASOURCE_DATABASE_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME + " is not correct", "myDatabase", propertyFound);
    }

}
