package io.openliberty.boost.common.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.*;
import static io.openliberty.boost.common.utils.DOMUtils.getDirectChildrenByTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.utils.BoosterUtil;
import io.openliberty.boost.common.utils.CommonLogger;
import io.openliberty.boost.common.utils.ConfigFileUtils;

public class JDBCBoosterTest {

//    @Rule
//    public TemporaryFolder outputDir = new TemporaryFolder();
//
//    @Rule
//    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();
//
//    private Map<String, String> getJDBCDependency() throws BoostException {
//        return BoosterUtil.createDependenciesWithBoosterAndVersion(JDBCBoosterConfig.class, "0.1-SNAPSHOT");
//    }
//
//    private final String DB2_DEPENDENCY_VERSION = "db2jcc4";
//    private final String MYSQL_DEPENDENCY_VERSION = "8.0.15";
//
//    BoostLoggerI logger = CommonLogger.getInstance();
//
//    /**
//     * Test that the jdbc-4.1 feature is added as the default when the Java
//     * compiler target is set to less than 7 (1.6)
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     * @throws BoostException
//     * @throws SecurityException
//     * @throws NoSuchMethodException
//     * @throws InvocationTargetException
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     * @throws InstantiationException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE_16() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.6");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");
//
//        assertTrue("The " + JDBC_41 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.1 feature is added when the Java compiler target is
//     * 1.7 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE_17() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.7");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.1 feature is added when the Java compiler target is
//     * 7 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE_7() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "7");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.2 feature is added when the Java compiler target is
//     * 1.8 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE_18() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.8");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.2 feature is added when the Java compiler target is
//     * 8 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE_8() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "8");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.2 feature is added when the Java compiler target is
//     * 9 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE9() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "9");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the jdbc-4.3 feature is added when the Java compiler target is
//     * 11 booster
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterFeature_SE11() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set compiler target property
//        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "11");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
//        serverConfig.writeToServer();
//
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_43 + "</feature>");
//
//        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);
//
//    }
//
//    /**
//     * Test that the Liberty server.xml is fully configured with the default
//     * Derby datasource
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_Derby() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        boosters.get(0).addServerConfig(serverConfig);
//
//        Element serverRoot = serverConfig.getServerDoc().getDocumentElement();
//
//        // Check that the <library> element is correctly configured
//        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
//        assertEquals("Didn't find one and only one library", 1, libraryList.size());
//
//        Element library = libraryList.get(0);
//        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));
//
//        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
//        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
//        assertEquals("Fileset includes attribute is not correct", DERBY_JAR, fileset.getAttribute("includes"));
//
//        // Check that the <dataSource> element is correctly configured
//        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
//        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());
//
//        Element dataSource = dataSourceList.get(0);
//        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
//        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
//                dataSource.getAttribute(JDBC_DRIVER_REF));
//
//        List<Element> propertiesDerbyEmbeddedList = getDirectChildrenByTag(dataSource, PROPERTIES_DERBY_EMBEDDED);
//        assertEquals("Didn't find one and only one " + PROPERTIES_DERBY_EMBEDDED, 1,
//                propertiesDerbyEmbeddedList.size());
//
//        Element propertiesDerbyEmbedded = propertiesDerbyEmbeddedList.get(0);
//        assertEquals("The createDatabase attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_CREATE_DATABASE),
//                propertiesDerbyEmbedded.getAttribute(CREATE_DATABASE));
//        assertEquals("The databaseName attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
//                propertiesDerbyEmbedded.getAttribute(DATABASE_NAME));
//
//        // Check that the <jdbcDriver> element is correctly configured
//        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
//        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());
//
//        Element jdbcDriver = jdbcDriverList.get(0);
//        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
//        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));
//    }
//
//    /**
//     * Test that the server.xml is fully configured with the DB2 datasource
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_DB2() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.DB2_DEPENDENCY, DB2_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        Element serverRoot = serverConfig.getServerDoc().getDocumentElement();
//
//        // Check that the <library> element is correctly configured
//        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
//        assertEquals("Didn't find one and only one library", 1, libraryList.size());
//
//        Element library = libraryList.get(0);
//        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));
//
//        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
//        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
//        assertEquals("Fileset includes attribute is not correct", DB2_JAR, fileset.getAttribute("includes"));
//
//        // Check that the <dataSource> element is correctly configured
//        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
//        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());
//
//        Element dataSource = dataSourceList.get(0);
//        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
//        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
//                dataSource.getAttribute(JDBC_DRIVER_REF));
//        assertEquals("DataSource containerAuthDataRef is not correct", DATASOURCE_AUTH_DATA,
//                dataSource.getAttribute(CONTAINER_AUTH_DATA_REF));
//
//        List<Element> propertiesDb2JccList = getDirectChildrenByTag(dataSource, PROPERTIES_DB2_JCC);
//        assertEquals("Didn't find one and only one " + PROPERTIES_DB2_JCC, 1, propertiesDb2JccList.size());
//
//        Element propertiesDb2Jcc = propertiesDb2JccList.get(0);
//        assertEquals("The databaseName attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
//                propertiesDb2Jcc.getAttribute(DATABASE_NAME));
//        assertEquals("The serverName attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME),
//                propertiesDb2Jcc.getAttribute(SERVER_NAME));
//        assertEquals("The portNumber attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER),
//                propertiesDb2Jcc.getAttribute(PORT_NUMBER));
//
//        // Check that the <jdbcDriver> element is correctly configured
//        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
//        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());
//
//        Element jdbcDriver = jdbcDriverList.get(0);
//        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
//        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));
//
//        // Check that the <containerAuthData> element is correctly configured
//        List<Element> authDataList = getDirectChildrenByTag(serverRoot, AUTH_DATA);
//        assertEquals("Didn't find one and only one authData", 1, authDataList.size());
//
//        Element authData = authDataList.get(0);
//        assertEquals("AuthData id is not correct", DATASOURCE_AUTH_DATA, authData.getAttribute("id"));
//        assertEquals("AuthData user is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER),
//                authData.getAttribute(USER));
//        assertEquals("AuthData password is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD),
//                authData.getAttribute(PASSWORD));
//    }
//
//    /**
//     * Test that the server.xml is fully configured with the MySQL datasource
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_MySQL() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Add JDBC booster and MySQL to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.MYSQL_DEPENDENCY, MYSQL_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        Element serverRoot = serverConfig.getServerDoc().getDocumentElement();
//
//        // Check that the <library> element is correctly configured
//        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
//        assertEquals("Didn't find one and only one library", 1, libraryList.size());
//
//        Element library = libraryList.get(0);
//        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));
//
//        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
//        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
//        assertEquals("Fileset includes attribute is not correct", MYSQL_JAR, fileset.getAttribute("includes"));
//
//        // Check that the <dataSource> element is correctly configured
//        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
//        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());
//
//        Element dataSource = dataSourceList.get(0);
//        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
//        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
//                dataSource.getAttribute(JDBC_DRIVER_REF));
//        assertEquals("DataSource containerAuthDataRef is not correct", DATASOURCE_AUTH_DATA,
//                dataSource.getAttribute(CONTAINER_AUTH_DATA_REF));
//
//        List<Element> propertiesList = getDirectChildrenByTag(dataSource, PROPERTIES);
//        assertEquals("Didn't find one and only one properties", 1, propertiesList.size());
//
//        Element properties = propertiesList.get(0);
//        assertEquals("The databaseName attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
//                properties.getAttribute(DATABASE_NAME));
//        assertEquals("The serverName attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME), properties.getAttribute(SERVER_NAME));
//        assertEquals("The portNumber attribute is not correct",
//                BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER), properties.getAttribute(PORT_NUMBER));
//
//        // Check that the <jdbcDriver> element is correctly configured
//        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
//        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());
//
//        Element jdbcDriver = jdbcDriverList.get(0);
//        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
//        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));
//
//        // Check that the <containerAuthData> element is correctly configured
//        List<Element> authDataList = getDirectChildrenByTag(serverRoot, AUTH_DATA);
//        assertEquals("Didn't find one and only one authData", 1, authDataList.size());
//
//        Element authData = authDataList.get(0);
//        assertEquals("AuthData id is not correct", DATASOURCE_AUTH_DATA, authData.getAttribute("id"));
//        assertEquals("AuthData user is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER),
//                authData.getAttribute(USER));
//        assertEquals("AuthData password is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD),
//                authData.getAttribute(PASSWORD));
//    }
//
//    /**
//     * Test that the configured databaseName property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_databaseName_configured() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set database name property
//        System.setProperty(BoostProperties.DATASOURCE_DATABASE_NAME, "myDatabase");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_DATABASE_NAME);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
//                + " is not correct", "myDatabase", propertyFound);
//    }
//
//    /**
//     * Test that the default derby databaseName property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_databaseName_derby_default() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_DATABASE_NAME);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
//                + " is not correct", DERBY_DB, propertyFound);
//    }
//
//    /**
//     * Test that the configured createDatabase property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_createDatabase_configured() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set database name property
//        System.setProperty(BoostProperties.DATASOURCE_CREATE_DATABASE, "false");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_CREATE_DATABASE);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_CREATE_DATABASE
//                + " is not correct", "false", propertyFound);
//    }
//
//    /**
//     * Test that the default derby databaseName property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_createDatabase_derby_default() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_CREATE_DATABASE);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_CREATE_DATABASE
//                + " is not correct", "create", propertyFound);
//    }
//
//    /**
//     * Test that the default portNumber property for db2 is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_portNumber_db2_default() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.DB2_DEPENDENCY, DB2_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_PORT_NUMBER);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PORT_NUMBER
//                + " is not correct", DB2_DEFAULT_PORT_NUMBER, propertyFound);
//    }
//
//    /**
//     * Test that the default portNumber property for mysql is correctly written
//     * to bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_portNumber_mysql_default() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.MYSQL_DEPENDENCY, MYSQL_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_PORT_NUMBER);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PORT_NUMBER
//                + " is not correct", MYSQL_DEFAULT_PORT_NUMBER, propertyFound);
//    }
//
//    /**
//     * Test that the configured portNumber property is correctly written to
//     * bootstrap.properties (Using DB2 dependency for testing)
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_portNumber_configured() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        System.setProperty(BoostProperties.DATASOURCE_PORT_NUMBER, "55555");
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.DB2_DEPENDENCY, DB2_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_PORT_NUMBER);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PORT_NUMBER
//                + " is not correct", "55555", propertyFound);
//    }
//
//    /**
//     * Test that the configured serverName property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_serverName_configured() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        System.setProperty(BoostProperties.DATASOURCE_SERVER_NAME, "1.1.1.1");
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.DB2_DEPENDENCY, DB2_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_SERVER_NAME);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_SERVER_NAME
//                + " is not correct", "1.1.1.1", propertyFound);
//    }
//
//    /**
//     * Test that the default serverName property is correctly written to
//     * bootstrap.properties
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_serverName_default() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Add JDBC booster and DB2 to dependency map
//        Map<String, String> dependencies = getJDBCDependency();
//        dependencies.put(JDBCBoosterConfig.DB2_DEPENDENCY, DB2_DEPENDENCY_VERSION);
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(dependencies, logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_SERVER_NAME);
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_SERVER_NAME
//                + " is not correct", "localhost", propertyFound);
//    }
//
//    /**
//     * Test that a configured datasource property is correctly written to
//     * bootstrap.properties and server.xml
//     * 
//     * @throws ParserConfigurationException
//     * @throws TransformerException
//     * @throws IOException
//     */
//    @Test
//    public void testAddJdbcBoosterConfig_with_generic_property() throws Exception {
//
//        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
//                outputDir.getRoot().getAbsolutePath(), logger);
//
//        // Set database name property
//        System.setProperty(BoostProperties.DATASOURCE_PREFIX + "randomProperty", "randomValue");
//
//        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(), logger);
//
//        serverConfig.addBoosterConfig(boosters.get(0));
//
//        serverConfig.writeToServer();
//
//        // Find property in bootstrap.properties
//        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";
//
//        String propertyFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
//                BoostProperties.DATASOURCE_PREFIX + "randomProperty");
//
//        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_PREFIX
//                + "randomProperty" + " is not correct", "randomValue", propertyFound);
//
//        // Find property in server.xml
//        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
//        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "randomProperty=\""
//                + BoostUtil.makeVariable(BoostProperties.DATASOURCE_PREFIX + "randomProperty") + "\"");
//
//        assertTrue("The property was not found in the server configuration", featureFound);
//    }

}
