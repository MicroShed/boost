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

import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;

import boost.common.BoostLoggerI;
import boost.common.config.BoosterConfigParams;
import io.openliberty.boost.runtimes.utils.BoosterUtil;
import io.openliberty.boost.runtimes.utils.CommonLogger;
import io.openliberty.boost.runtimes.utils.ConfigFileUtils;
import boost.runtimes.openliberty.boosters.*;

public class MPRestClientBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the mpRestClient-1.1 feature is added to server.xml when the
     * MPRestClient booster version is set to 1.1-0.2
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature11() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPRestClientBoosterConfig.class, "1.1-0.2");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPRestClientBoosterConfig libMPOpenTracingConfig = new LibertyMPRestClientBoosterConfig(params, logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPRESTCLIENT_11 + "</feature>");

        assertTrue("The " + MPRESTCLIENT_11 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpRestClient-1.2 feature is added to server.xml when the
     * MPRestClient booster version is set to "1.2-0.2"
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature12() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPRestClientBoosterConfig.class, "1.2-0.2");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPRestClientBoosterConfig libMPOpenTracingConfig = new LibertyMPRestClientBoosterConfig(params, logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPRESTCLIENT_12 + "</feature>");

        assertTrue("The " + MPRESTCLIENT_12 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpRestClient-1.3 feature is added to server.xml when the
     * MPRestClient booster version is set to "1.3-0.2".
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature13() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPRestClientBoosterConfig.class, "1.3-0.2");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPRestClientBoosterConfig libMPOpenTracingConfig = new LibertyMPRestClientBoosterConfig(params, logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPRESTCLIENT_13 + "</feature>");

        assertTrue("The " + MPRESTCLIENT_13 + " feature was not found in the server configuration", featureFound);

    }

}
