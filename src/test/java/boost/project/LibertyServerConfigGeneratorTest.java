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
package boost.project;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LibertyServerConfigGeneratorTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private static final String SPRING_BOOT_15_FEATURE = "springBoot-1.5";

    /**
     * Test adding feature
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddSpringFeature() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator();
        serverConfig.addFeature(SPRING_BOOT_15_FEATURE);
        serverConfig.writeToServer(outputDir.getRoot().getAbsolutePath());

        boolean featureFound = findStringInServerXml("<feature>" + SPRING_BOOT_15_FEATURE + "</feature>");

        assertTrue("The " + SPRING_BOOT_15_FEATURE + " feature was not found in the server configuration",
                featureFound);

    }

    private boolean findStringInServerXml(String stringToFind) throws IOException {

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean found = false;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(serverXML));
            String line;
            while ((line = br.readLine()) != null && !found) {
                if (line.contains(stringToFind)) {
                    found = true;
                }
            }

        } catch (FileNotFoundException e) {
            fail("The file " + serverXML + " does not exist");
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return found;
    }
}