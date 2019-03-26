/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.common.config;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.utils.BoostUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class TomEEServerConfigGenerator {

    private final String configPath;
    private final String tomeeInstallPath;

    private final BoostLoggerI logger;

    public TomEEServerConfigGenerator(String configPath, BoostLoggerI logger) throws ParserConfigurationException {

        this.configPath = configPath;
        this.tomeeInstallPath = configPath + "/.."; // one directory back from 'apache-ee/conf'
        this.logger = logger;

        // generateDocument();

        // featuresAdded = new HashSet<String>();
        // bootstrapProperties = new Properties();
    }

    public void addJarsDirToSharedLoader() throws ParserConfigurationException {
        try {
            BufferedReader file = new BufferedReader(new FileReader(configPath + "/catalina.properties"));
            String line;
            String replaceString = "shared.loader=";
            String replaceWithString = "shared.loader=\"${catalina.home}/boost\",\"${catalina.home}/"
                    + ConfigConstants.TOMEEBOOST_JAR_DIR + "/*.jar\"";
            StringBuffer inputBuffer = new StringBuffer();

            createTOMEEBoostJarDir();

            while ((line = file.readLine()) != null) {
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            String inputStr = inputBuffer.toString();

            file.close();

            System.out.println(inputStr); // check that it's inputted right

            // this if structure determines whether or not to replace "0" or "1"

            inputStr = inputStr.replace(replaceString, replaceWithString);

            // check if the new input is right
            System.out.println("----------------------------------\n" + inputStr);

            // write the new String with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(configPath + "/catalina.properties");
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
    }

    private void createTOMEEBoostJarDir() {
        File dir = new File(tomeeInstallPath + "/" + ConfigConstants.TOMEEBOOST_JAR_DIR);

        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (successful) {
            // creating the directory succeeded
            System.out.println("directory was created successfully");
        } else {
            // creating the directory failed
            System.out.println("failed trying to create the directory");
        }
    }

}
