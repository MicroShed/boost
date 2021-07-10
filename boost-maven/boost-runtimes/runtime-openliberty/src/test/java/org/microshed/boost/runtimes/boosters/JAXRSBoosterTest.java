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

public class JAXRSBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    private void testJAXRSBoosterFeature(String version, String feature) throws Exception {

        boolean featureFound = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyJAXRSBoosterConfig.class, version);

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyJAXRSBoosterConfig libJAXRSConfig = new LibertyJAXRSBoosterConfig(params, logger);
        try {
            serverConfig.addFeature(libJAXRSConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + feature + "</feature>");
        } catch (BoostException be) {
        }

        assertTrue("The " + feature + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jaxrs-2.0 feature is added to server.xml when the jaxrs booster
     * version is set to 2.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testJAXRSBoosterFeature20() throws Exception {
        testJAXRSBoosterFeature("2.0-0.2.2-SNAPSHOT", JAXRS_20);
    }

    /**
     * Test that the jaxrs-2.1 feature is added to server.xml when the jaxrs booster
     * version is set to 2.1-M1-SNAPSHOT
     * 
     */
    @Test
    public void testJAXRSBoosterFeature21() throws Exception {
        testJAXRSBoosterFeature("2.1-0.2.2-SNAPSHOT", JAXRS_21);
    }

    /*
     * Test for failure when using incorrect version
     */
    @Test
    public void testJAXRSBoosterFeature21_bad_version() throws Exception {
        boolean featureFound = false;
        boolean boostExceptionGenerated = false;

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyJAXRSBoosterConfig.class, "2.x-0.2.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyJAXRSBoosterConfig libJAXRSConfig = new LibertyJAXRSBoosterConfig(params, logger);
        try {
            serverConfig.addFeature(libJAXRSConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JAXRS_21 + "</feature>");
        } catch (BoostException be) {
            if (be.toString().indexOf("Invalid version") >= 0)
                boostExceptionGenerated = true;
        }
        assertTrue("The " + JAXRS_21 + " feature was found in the server configuration", !featureFound);
        assertTrue("No BoostException generated", boostExceptionGenerated);

    }
}
