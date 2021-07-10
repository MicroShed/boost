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

public class CDIBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    private void testCDIBoosterFeature(String version, String feature) throws Exception {

        boolean featureFound = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyCDIBoosterConfig.class, version);

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyCDIBoosterConfig libCDIConfig = new LibertyCDIBoosterConfig(params, logger);
        try {
            serverConfig.addFeature(libCDIConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + feature + "</feature>");
        } catch (BoostException be) {
        }

        assertTrue("The " + feature + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the cdi-1.2 feature is added to server.xml when the CDI booster
     * version is set to 1.2-M1-SNAPSHOT
     * 
     */
    @Test
    public void testCDIBoosterFeature_12() throws Exception {
        testCDIBoosterFeature("1.2-0.2.2-SNAPSHOT", CDI_12);
    }

    /**
     * Test that the cdi-2.0 feature is added to server.xml when the CDI booster
     * version is set to 2.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testCDIBoosterFeature_20() throws Exception {
        testCDIBoosterFeature("2.0-0.2.2-SNAPSHOT", CDI_20);
    }

    /**
     * Test when incorrect version is passed.
     */
    @Test
    public void testCDIBoosterFeature_12_bad_version() throws Exception {

        boolean boostExceptionGenerated = false;
        boolean featureFound = false;
        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyCDIBoosterConfig.class, "1.x-0.2.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyCDIBoosterConfig libCDIConfig = new LibertyCDIBoosterConfig(params, logger);

        try {
            serverConfig.addFeature(libCDIConfig.getFeature());
            serverConfig.writeToServer();

            String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
            featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + CDI_12 + "</feature>");
        } catch (BoostException be) {
            if (be.toString().indexOf("Invalid version") >= 0)
                boostExceptionGenerated = true;
        }
        assertTrue("The " + CDI_12 + " feature was found in the server configuration", !featureFound);
        assertTrue("No BoostException generated", boostExceptionGenerated);

    }
}
