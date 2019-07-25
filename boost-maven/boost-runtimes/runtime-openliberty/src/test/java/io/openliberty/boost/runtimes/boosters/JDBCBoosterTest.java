/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package io.openliberty.boost.runtimes.boosters;

import static boost.common.config.ConfigConstants.*;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.config.BoostProperties;
import io.openliberty.boost.runtimes.utils.BoosterUtil;
import io.openliberty.boost.runtimes.utils.CommonLogger;
import io.openliberty.boost.runtimes.utils.ConfigFileUtils;
import boost.runtimes.openliberty.boosters.*;

public class JDBCBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the jdbc-4.1 feature is added as the default when the Java
     * compiler target is set to less than 7 (1.6)
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     * @throws BoostException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE_16() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.6");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);
        
        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");

        assertTrue("The " + JDBC_41 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.1 feature is added when the Java compiler target is
     * 1.7 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE_17() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.7");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);
        
        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.1 feature is added when the Java compiler target is
     * 7 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE_7() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "7");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);

        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_41 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.2 feature is added when the Java compiler target is
     * 1.8 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE_18() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "1.8");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);

        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.2 feature is added when the Java compiler target is
     * 8 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE_8() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "8");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);

        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.2 feature is added when the Java compiler target is
     * 9 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE9() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "9");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);

        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_42 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jdbc-4.3 feature is added when the Java compiler target is
     * 11 booster
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterFeature_SE11() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Set compiler target property
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, "11");

        LibertyJDBCBoosterConfig libJDBCConfig = new LibertyJDBCBoosterConfig(BoosterUtil.getJDBCDependency(), logger);

        serverConfig.addFeature(libJDBCConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JDBC_43 + "</feature>");

        assertTrue("The " + JDBC_42 + " feature was not found in the server configuration", featureFound);

    }

}
