/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

import io.openliberty.boost.utils.LibertyServerConfigGenerator;

import static io.openliberty.boost.utils.ConfigConstants.*;
import static io.openliberty.boost.utils.ConfigFileUtils.findStringInServerXml;
import static io.openliberty.boost.utils.DOMUtils.getDirectChildrenByTag;

public class LibertyServerConfigGeneratorTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    /**
     * Test adding feature
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddSpringFeature() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(outputDir.getRoot().getAbsolutePath());
        serverConfig.addFeature(SPRING_BOOT_15);
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = findStringInServerXml(serverXML, "<feature>" + SPRING_BOOT_15 + "</feature>");

        assertTrue("The " + SPRING_BOOT_15 + " feature was not found in the server configuration", featureFound);

    }

    @Test
    public void testZeroFeaturesInDefaultServerConfig()
            throws ParserConfigurationException, TransformerException, IOException {
        LibertyServerConfigGenerator g = new LibertyServerConfigGenerator(outputDir.getRoot().getAbsolutePath());
        Element serverRoot = g.doc.getDocumentElement();
        List<Element> featureMgrList = getDirectChildrenByTag(serverRoot, FEATURE_MANAGER);
        assertEquals("Didn't find one and only one featureMgr", 1, featureMgrList.size());
        Element featureMgr = featureMgrList.get(0);
        List<Element> featureList = getDirectChildrenByTag(featureMgr, FEATURE);
        assertEquals("Didn't find empty list of features", 0, featureList.size());
    }

}