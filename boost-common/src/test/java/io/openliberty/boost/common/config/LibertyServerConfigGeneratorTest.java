/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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

import org.mockito.Mockito;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

import io.openliberty.boost.common.config.ConfigFileUtils;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.BoostLoggerI;

import static io.openliberty.boost.common.config.ConfigConstants.*;
import static io.openliberty.boost.common.config.DOMUtils.getDirectChildrenByTag;

public class LibertyServerConfigGeneratorTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private final String DB2_DEPENDENCY_VERSION = "db2jcc4";

    // Get booster configurators
    BoostLoggerI logger = Mockito.mock(BoostLoggerI.class);

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
                outputDir.getRoot().getAbsolutePath(), logger);
        serverConfig.addFeature(SPRING_BOOT_15);
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + SPRING_BOOT_15 + "</feature>");

        assertTrue("The " + SPRING_BOOT_15 + " feature was not found in the server configuration", featureFound);

    }

    @Test
    public void testZeroFeaturesInDefaultServerConfig()
            throws ParserConfigurationException, TransformerException, IOException {
        LibertyServerConfigGenerator g = new LibertyServerConfigGenerator(outputDir.getRoot().getAbsolutePath(), logger);
        Element serverRoot = g.getServerDoc().getDocumentElement();
        List<Element> featureMgrList = getDirectChildrenByTag(serverRoot, FEATURE_MANAGER);
        assertEquals("Didn't find one and only one featureMgr", 1, featureMgrList.size());
        Element featureMgr = featureMgrList.get(0);
        List<Element> featureList = getDirectChildrenByTag(featureMgr, FEATURE);
        assertEquals("Didn't find empty list of features", 0, featureList.size());
    }

    /**
     * Test that the EE8 JDBC version (jdbc-4.2) has been added by the JDBC
     * booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_EE8() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", new Properties(),
                null);

        serverConfig.addFeature(configurator.getFeature());

        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the EE8 JDBC version (jdbc-4.2) has been added by the JDBC
     * booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_EE7() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.1-SNAPSHOT", new Properties(),
                null);

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
    public void testAddJdbcBoosterConfig_Derby()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", new Properties(),
                null);

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
        assertEquals("DataSource jdbcDriverRef is not correct", DERBY_EMBEDDED_DRIVER_REF,
                dataSource.getAttribute(JDBC_DRIVER_REF));

        Element propertiesDerbyEmbedded = getDirectChildrenByTag(dataSource, PROPERTIES_DERBY_EMBEDDED).get(0);
        assertEquals("The createDatabase attribute is not correct", "create",
                propertiesDerbyEmbedded.getAttribute(CREATE_DATABASE));
        assertEquals("The databaseName attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
                propertiesDerbyEmbedded.getAttribute(DATABASE_NAME));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());

        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", DERBY_EMBEDDED_DRIVER_REF, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", DERBY_LIB, jdbcDriver.getAttribute(LIBRARY_REF));
    }

    /**
     * Test that the server is configured with the DB2 datasource
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_DB2() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        String db2Dependency = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", new Properties(),
                db2Dependency);

        serverConfig.addBoosterConfig(configurator);

        Element serverRoot = serverConfig.getServerDoc().getDocumentElement();

        // Check that the <library> element is correctly configured
        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
        assertEquals("Didn't find one and only one library", 1, libraryList.size());

        Element library = libraryList.get(0);
        assertEquals("Library id is not correct", DB2_LIB, library.getAttribute("id"));

        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
        assertEquals("Fileset includes attribute is not correct", "db2jcc*.jar", fileset.getAttribute("includes"));

        // Check that the <dataSource> element is correctly configured
        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());

        Element dataSource = dataSourceList.get(0);
        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
        assertEquals("DataSource jdbcDriverRef is not correct", DB2_DRIVER_REF,
                dataSource.getAttribute(JDBC_DRIVER_REF));

        Element propertiesDb2Jcc = getDirectChildrenByTag(dataSource, PROPERTIES_DB2_JCC).get(0);
        assertEquals("The databaseName attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
                propertiesDb2Jcc.getAttribute(DATABASE_NAME));
        assertEquals("The serverName attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME),
                propertiesDb2Jcc.getAttribute(SERVER_NAME));
        assertEquals("The portNumber attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER),
                propertiesDb2Jcc.getAttribute(PORT_NUMBER));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());

        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", DB2_DRIVER_REF, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", DB2_LIB, jdbcDriver.getAttribute(LIBRARY_REF));
    }

    /**
     * Test that the configured databaseName property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_databaseName_configured()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_DATABASE_NAME, "myDatabase");
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties, null);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_DATABASE_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
                + " is not correct", "myDatabase", propertyFound);
    }

    /**
     * Test that the default derby databaseName property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_databaseName_derby_default()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        Properties properties = new Properties();
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties, null);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_DATABASE_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
                + " is not correct", JDBCBoosterPackConfigurator.DEFAULT_DERBY_DATABASE_NAME, propertyFound);
    }

    /**
     * Test that the default portNumber property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_portNumber_default()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        String db2Dependency = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties,
                db2Dependency);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_PORT_NUMBER);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PORT_NUMBER
                + " is not correct", "50000", propertyFound);
    }

    /**
     * Test that the configured portNumber property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_portNumber_configured()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        String db2Dependency = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_PORT_NUMBER, "55555");
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties,
                db2Dependency);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_PORT_NUMBER);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PORT_NUMBER
                + " is not correct", "55555", propertyFound);
    }

    /**
     * Test that the configured serverName property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_serverName_configured()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        String db2Dependency = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_SERVER_NAME, "1.1.1.1");
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties,
                db2Dependency);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_SERVER_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_SERVER_NAME
                + " is not correct", "1.1.1.1", propertyFound);
    }

    /**
     * Test that the default serverName property is correctly written to
     * bootstrap.properties
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_with_serverName_default()
            throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        String db2Dependency = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        JDBCBoosterPackConfigurator configurator = new JDBCBoosterPackConfigurator("0.2-SNAPSHOT", properties,
                db2Dependency);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_SERVER_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_SERVER_NAME
                + " is not correct", "localhost", propertyFound);
    }

}
