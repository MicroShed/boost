package io.openliberty.boost.common.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.CREATE_DATABASE;
import static io.openliberty.boost.common.config.ConfigConstants.DATABASE_NAME;
import static io.openliberty.boost.common.config.ConfigConstants.DATASOURCE;
import static io.openliberty.boost.common.config.ConfigConstants.DB2_DRIVER_REF;
import static io.openliberty.boost.common.config.ConfigConstants.DB2_LIB;
import static io.openliberty.boost.common.config.ConfigConstants.DEFAULT_DATASOURCE;
import static io.openliberty.boost.common.config.ConfigConstants.DERBY_EMBEDDED_DRIVER_REF;
import static io.openliberty.boost.common.config.ConfigConstants.DERBY_LIB;
import static io.openliberty.boost.common.config.ConfigConstants.FILESET;
import static io.openliberty.boost.common.config.ConfigConstants.JDBC_41;
import static io.openliberty.boost.common.config.ConfigConstants.JDBC_42;
import static io.openliberty.boost.common.config.ConfigConstants.JDBC_DRIVER;
import static io.openliberty.boost.common.config.ConfigConstants.JDBC_DRIVER_REF;
import static io.openliberty.boost.common.config.ConfigConstants.LIBRARY;
import static io.openliberty.boost.common.config.ConfigConstants.LIBRARY_REF;
import static io.openliberty.boost.common.config.ConfigConstants.PORT_NUMBER;
import static io.openliberty.boost.common.config.ConfigConstants.PROPERTIES_DB2_JCC;
import static io.openliberty.boost.common.config.ConfigConstants.PROPERTIES_DERBY_EMBEDDED;
import static io.openliberty.boost.common.config.ConfigConstants.RESOURCES;
import static io.openliberty.boost.common.config.ConfigConstants.SERVER_NAME;
import static io.openliberty.boost.common.utils.DOMUtils.getDirectChildrenByTag;
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
import org.mockito.Mockito;
import org.w3c.dom.Element;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.utils.ConfigFileUtils;

public class JDBCBoosterTest {
    
    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();
    
    private final String DB2_DEPENDENCY_VERSION = "db2jcc4";
    
    BoostLoggerI logger = Mockito.mock(BoostLoggerI.class);

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

        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", new Properties(),
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

        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.1-SNAPSHOT", new Properties(),
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

        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", new Properties(),
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

        String db2Dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", new Properties(),
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
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties, null);

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
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties, null);

        serverConfig.addBoosterConfig(configurator);

        serverConfig.writeToServer();

        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_DATABASE_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
                + " is not correct", JDBCBoosterConfig.DEFAULT_DERBY_DATABASE_NAME, propertyFound);
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

        String db2Dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties,
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

        String db2Dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_PORT_NUMBER, "55555");
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties,
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

        String db2Dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        properties.put(BoostProperties.DATASOURCE_SERVER_NAME, "1.1.1.1");
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties,
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

        String db2Dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + DB2_DEPENDENCY_VERSION;

        Properties properties = new Properties();
        JDBCBoosterConfig configurator = new JDBCBoosterConfig("0.2-SNAPSHOT", properties,
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
