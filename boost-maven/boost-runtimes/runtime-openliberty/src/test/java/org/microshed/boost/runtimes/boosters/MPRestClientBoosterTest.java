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

package org.microshed.boost.runtimes.boosters;

import static org.junit.Assert.assertTrue;
import static org.microshed.boost.common.config.ConfigConstants.*;

import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.*;
import org.microshed.boost.runtimes.utils.BoosterUtil;
import org.microshed.boost.runtimes.utils.CommonLogger;
import org.microshed.boost.runtimes.utils.ConfigFileUtils;

public class MPRestClientBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    public void testMPRestClientBoosterFeature(String version, String feature) throws Exception {

        boolean featureFound = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPRestClientBoosterConfig.class, version);

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPRestClientBoosterConfig libMPOpenTracingConfig = new LibertyMPRestClientBoosterConfig(params, logger);

        try {
            serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + feature + "</feature>");
        } catch (BoostException be) {
        }

        assertTrue("The " + feature + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpRestClient-1.1 feature is added to server.xml when the
     * MPRestClient booster version is set to 1.1-0.2.2-SNAPSHOT
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature11() throws Exception {
        testMPRestClientBoosterFeature("1.1-0.2.2-SNAPSHOT", MPRESTCLIENT_11);
    }

    /**
     * Test that the mpRestClient-1.2 feature is added to server.xml when the
     * MPRestClient booster version is set to "1.2-0.2.2-SNAPSHOT"
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature12() throws Exception {
        testMPRestClientBoosterFeature("1.2-0.2.2-SNAPSHOT", MPRESTCLIENT_12);
    }

    /**
     * Test that the mpRestClient-1.3 feature is added to server.xml when the
     * MPRestClient booster version is set to "1.3-0.2.2-SNAPSHOT".
     * 
     */
    @Test
    public void testMPRestClientBoosterFeature13() throws Exception {
        testMPRestClientBoosterFeature("1.3-0.2.2-SNAPSHOT", MPRESTCLIENT_13);
    }

    /*
     * Test Missing version
     */
    @Test
    public void testMPRestClientBoosterFeature13_bad_version() throws Exception {

        boolean featureFound = false;
        boolean boostExceptionGenerated = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPRestClientBoosterConfig.class, "1.x-0.2.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPRestClientBoosterConfig libMPOpenTracingConfig = new LibertyMPRestClientBoosterConfig(params, logger);

        try {
            serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                    "<feature>" + MPRESTCLIENT_13 + "</feature>");
        } catch (BoostException be) {

            if (be.toString().indexOf("Invalid version") >= 0)
                boostExceptionGenerated = true;
        }

        assertTrue("The " + MPRESTCLIENT_13 + " feature was found in the server configuration", !featureFound);
        assertTrue("No BoostException generated", boostExceptionGenerated);
    }
}
