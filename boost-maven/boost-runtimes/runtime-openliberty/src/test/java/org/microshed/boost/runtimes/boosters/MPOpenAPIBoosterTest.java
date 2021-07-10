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

public class MPOpenAPIBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    private void testMPOpenAPIBoosterFeature(String version, String feature) throws Exception {

        boolean featureFound = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenAPIBoosterConfig.class, version);

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPOpenAPIBoosterConfig libMPOpenAPIConfig = new LibertyMPOpenAPIBoosterConfig(params, logger);
        try {
            serverConfig.addFeature(libMPOpenAPIConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + feature + "</feature>");
        } catch (BoostException be) {
        }
        assertTrue("The " + feature + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpOpenAPI-1.0 feature is added to server.xml when the MPOpenAPI
     * booster version is set to 1.0-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenAPIBoosterFeature10() throws Exception {
        testMPOpenAPIBoosterFeature("1.0-0.2.2-SNAPSHOT", MPOPENAPI_10);
    }

    /**
     * Test that the mpOpenAPI-1.1 feature is added to server.xml when the MPOpenAPI
     * booster version is set to 0.1.1-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenAPIBoosterFeature11() throws Exception {
        testMPOpenAPIBoosterFeature("1.1-0.2.2-SNAPSHOT", MPOPENAPI_11);
    }

    /*
     * Test invalid version
     */
    @Test
    public void testMPOpenAPIBoosterFeature11_bad_version() throws Exception {

        boolean featureFound = false;
        boolean boostExceptionGenerated = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenAPIBoosterConfig.class, "1.x-0.2.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPOpenAPIBoosterConfig libMPOpenAPIConfig = new LibertyMPOpenAPIBoosterConfig(params, logger);
        try {
            serverConfig.addFeature(libMPOpenAPIConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + MPOPENAPI_11 + "</feature>");
        } catch (BoostException be) {
            if (be.toString().indexOf("Invalid version") >= 0)
                boostExceptionGenerated = true;

        }
        assertTrue("The " + MPOPENAPI_11 + " feature was found in the server configuration", !featureFound);
        assertTrue("No BoostException generated", boostExceptionGenerated);

    }
}
