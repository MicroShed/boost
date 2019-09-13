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
package io.openliberty.boost.runtimes.utils;

import static io.openliberty.boost.runtimes.utils.DOMUtils.getDirectChildrenByTag;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigFileUtils {

    public static boolean findStringInServerXml(String serverXMLPath, String stringToFind) throws IOException {

        boolean found = false;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(serverXMLPath));
            String line;
            while ((line = br.readLine()) != null && !found) {
                if (line.contains(stringToFind)) {
                    found = true;
                }
            }

        } catch (FileNotFoundException e) {
            fail("The file " + serverXMLPath + " does not exist");
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return found;
    }

    public static String findVariableInXml(String variablesXmlPath, String variableName) throws Exception {

        String variableValue = null;

        File variablesXml = new File(variablesXmlPath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(variablesXml);

        Element serverRoot = doc.getDocumentElement();

        List<Element> variablesList = getDirectChildrenByTag(serverRoot, "variable");
        for (Element variable : variablesList) {
            if (variableName.equals(variable.getAttribute("name"))) {
                variableValue = variable.getAttribute("defaultValue");
            }
        }

        return variableValue;
    }

}
