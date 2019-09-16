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
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.*;
import org.microshed.boost.runtimes.utils.BoosterUtil;
import org.microshed.boost.runtimes.utils.CommonLogger;
import org.microshed.boost.runtimes.utils.ConfigFileUtils;

public class MPOpenTracingBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the mpOpenTracing-1.1 feature is added to server.xml when the
     * MPOpenTracing booster version is set to 1.1-M1-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenTracingBoosterFeature11() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.1-0.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(params,
                logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPOPENTRACING_11 + "</feature>");

        assertTrue("The " + MPOPENTRACING_11 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpOpenTracing-1.1 feature is added to server.xml when the
     * MPOpenTracing booster version is set to 0.2-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenTracingBoosterFeature12() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.2-0.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(params,
                logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPOPENTRACING_12 + "</feature>");

        assertTrue("The " + MPOPENTRACING_12 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpOpenTracing-1.3 feature is added to server.xml when the
     * MPOpenTracing booster version is set to 0.2-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenTracingBoosterFeature13() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.3-0.2-SNAPSHOT");

        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(params,
                logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPOPENTRACING_13 + "</feature>");

        assertTrue("The " + MPOPENTRACING_13 + " feature was not found in the server configuration", featureFound);

    }

}
