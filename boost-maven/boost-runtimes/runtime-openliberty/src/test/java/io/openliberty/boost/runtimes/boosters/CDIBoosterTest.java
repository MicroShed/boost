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

public class CDIBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    // private Map<String, String> getCDIDependency(String version) throws
    // BoostException {
    // return
    // BoosterUtil.createDependenciesWithBoosterAndVersion(CDIBoosterConfig.class,
    // version);
    // }

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the cdi-1.2 feature is added to server.xml when the CDI booster
     * version is set to 1.2-M1-SNAPSHOT
     * 
     */
    @Test
    public void testCDIBoosterFeature_12() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyCDIBoosterConfig libCDIConfig = new LibertyCDIBoosterConfig(BoosterUtil.createDependenciesWithBoosterAndVersion(LibertyCDIBoosterConfig.class, "1.2-M1-SNAPSHOT"), logger);
        
        serverConfig.addFeature(libCDIConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + CDI_12 + "</feature>");

        assertTrue("The " + CDI_12 + " feature was not found in the server configuration", featureFound);

    }

        /**
     * Test that the cdi-2.0 feature is added to server.xml when the CDI booster
     * version is set to 2.0-M1-SNAPSHOT
     * 
     */
    @Test
    public void testCDIBoosterFeature_20() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        LibertyCDIBoosterConfig libCDIConfig = new LibertyCDIBoosterConfig(BoosterUtil.createDependenciesWithBoosterAndVersion(LibertyCDIBoosterConfig.class, "2.0-M1-SNAPSHOT"), logger);

        serverConfig.addFeature(libCDIConfig.getFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + CDI_20 + "</feature>");

        assertTrue("The " + CDI_20 + " feature was not found in the server configuration", featureFound);

    }

}
