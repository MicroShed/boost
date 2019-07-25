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

public class JAXRSBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the jaxrs-2.0 feature is added to server.xml when the jaxrs booster
     * version is set to 2.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testJAXRSBoosterFeature20() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyJAXRSBoosterConfig libJAXRSConfig = new LibertyJAXRSBoosterConfig(BoosterUtil.createDependenciesWithBoosterAndVersion(LibertyJAXRSBoosterConfig.class, "2.0-M1-SNAPSHOT"), logger);


        serverConfig.addFeature(libJAXRSConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JAXRS_20 + "</feature>");

        assertTrue("The " + JAXRS_20 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the jaxrs-2.1 feature is added to server.xml when the jaxrs booster
     * version is set to 2.1-M1-SNAPSHOT
     * 
     */
    @Test
    public void testJAXRSBoosterFeature21() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyJAXRSBoosterConfig libJAXRSConfig = new LibertyJAXRSBoosterConfig(BoosterUtil.createDependenciesWithBoosterAndVersion(LibertyJAXRSBoosterConfig.class, "2.1-M1-SNAPSHOT"), logger);


        serverConfig.addFeature(libJAXRSConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JAXRS_21 + "</feature>");

        assertTrue("The " + JAXRS_21 + " feature was not found in the server configuration", featureFound);

    }

}
