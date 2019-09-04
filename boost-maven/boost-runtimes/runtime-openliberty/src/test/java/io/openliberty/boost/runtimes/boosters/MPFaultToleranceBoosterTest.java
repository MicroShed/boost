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

public class MPFaultToleranceBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the mpFaultTolerance-1.1 feature is added to server.xml when the
     * MPFaultTolerance booster version is set to 1.1-1.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testMPFaultToleranceBoosterFeature11() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
        		.createDependenciesWithBoosterAndVersion(LibertyMPFaultToleranceBoosterConfig.class, "1.1-1.0-M1-SNAPSHOT");
        
        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPFaultToleranceBoosterConfig libMPFTConfig = new LibertyMPFaultToleranceBoosterConfig(params, logger);

        serverConfig.addFeature(libMPFTConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPFAULTTOLERANCE_11 + "</feature>");

        assertTrue("The " + MPFAULTTOLERANCE_11 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the mpFaultTolerance-2.0 feature is added to server.xml when the
     * MPFaultTolerance booster version is set to 1.2-1.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testMPFaultToleranceBoosterFeature20() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), null, logger);

        Map<String, String> dependencies = BoosterUtil
        		.createDependenciesWithBoosterAndVersion(LibertyMPFaultToleranceBoosterConfig.class, "2.0-1.0-M1-SNAPSHOT");
        
        BoosterConfigParams params = new BoosterConfigParams(dependencies, new Properties());
        LibertyMPFaultToleranceBoosterConfig libMPFTConfig = new LibertyMPFaultToleranceBoosterConfig(params, logger);

        serverConfig.addFeature(libMPFTConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + MPFAULTTOLERANCE_20 + "</feature>");

        assertTrue("The " + MPFAULTTOLERANCE_20 + " feature was not found in the server configuration", featureFound);

    }

}
