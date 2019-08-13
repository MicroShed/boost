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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;

import boost.common.BoostLoggerI;
import io.openliberty.boost.runtimes.utils.BoosterUtil;
import io.openliberty.boost.runtimes.utils.CommonLogger;
import io.openliberty.boost.runtimes.utils.ConfigFileUtils;
import boost.runtimes.openliberty.boosters.*;

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
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.1-0-M1-SNAPSHOT"),
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
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.2-0-M1-SNAPSHOT"),
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
     * MPOpenTracing booster version is set to 0.1.3-SNAPSHOT
     * 
     */
    @Test
    public void testMPOpenTracingBoosterFeature13() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyMPOpenTracingBoosterConfig libMPOpenTracingConfig = new LibertyMPOpenTracingBoosterConfig(BoosterUtil
                .createDependenciesWithBoosterAndVersion(LibertyMPOpenTracingBoosterConfig.class, "1.3-0-M1-SNAPSHOT"),
                logger);

        serverConfig.addFeature(libMPOpenTracingConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPOPENTRACING_13 + "</feature>");

        assertTrue("The " + MPOPENTRACING_13 + " feature was not found in the server configuration", featureFound);

    }

}
