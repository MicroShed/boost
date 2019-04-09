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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.utils.BoostUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class TomEEServerConfigGenerator {

	private final String CATALINA_PROPERTIES = "catalina.properties";
	private final String SERVER_XML = "server.xml";
	
	private final String CONNECTOR_ELEMENT = "Connector";
	private final String ENGINE_ELEMENT = "Engine";
	private final String HOST_ELEMENT = "Host";

    private final String configPath;
    private final String tomeeInstallPath;

    private final BoostLoggerI logger;

    public TomEEServerConfigGenerator(String configPath, BoostLoggerI logger) throws ParserConfigurationException {

        this.configPath = configPath;
        this.tomeeInstallPath = configPath + "/.."; // one directory back from 'apache-ee/conf'
        this.logger = logger;

    }
    
    
    
    /**
     * Configure an HTTP port for this server config instance.
     *
     * @param httpPort
     *            The HTTP port to use for this server.
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public void setHttpPort(String httpPort) throws Exception {

    	// Read server.xml
    	File serverXml = new File(configPath + "/" + SERVER_XML);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(serverXml);
    	doc.getDocumentElement().normalize();
    	
    	// Get Connector element
    	NodeList connectors = doc.getElementsByTagName(CONNECTOR_ELEMENT);
    	
    	// Set port value to boost variable
    	for (int i = 0; i < connectors.getLength(); i++) {
    		Element connector = (Element) connectors.item(i);
    		if (connector.getAttribute("protocol").equals("HTTP/1.1")) {
    			connector.setAttribute("port", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HTTP_PORT));
    		}
    	}
    	
    	// Overwrite content
    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(serverXml);
		transformer.transform(source, result);
    	
    	// Set boost.http.port in catalina.properties
    	addCatalinaProperty(BoostProperties.ENDPOINT_HTTP_PORT, httpPort);
    }
    
    /**
     * Configure an HTTP port for this server config instance.
     *
     * @param httpPort
     *            The HTTP port to use for this server.
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public void setHostname(String hostname) throws Exception {

    	// Read server.xml
    	File serverXml = new File(configPath + "/" + SERVER_XML);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(serverXml);
    	doc.getDocumentElement().normalize();
    	
    	// Get Engine element
    	NodeList engines = doc.getElementsByTagName(ENGINE_ELEMENT);
    	
    	// Set defaultHost value to boost variable
    	for (int i = 0; i < engines.getLength(); i++) {
    		Element engine = (Element) engines.item(i);
    		engine.setAttribute("defaultHost", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HOST));
    	}
    	
    	// Get Host element
    	NodeList hosts = doc.getElementsByTagName(HOST_ELEMENT);
    	
    	// Set defaultHost value to boost variable
    	for (int i = 0; i < hosts.getLength(); i++) {
    		Element host = (Element) hosts.item(i);
    		host.setAttribute("name", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HOST));
    	}
    	
    	// Overwrite content
    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(serverXml);
		transformer.transform(source, result);
    	
    	// Set boost.http.port in catalina.properties
    	addCatalinaProperty(BoostProperties.ENDPOINT_HOST, hostname);
    }
    
    private void addCatalinaProperty(String key, String value) throws IOException { 
    	
    	BufferedWriter output = new BufferedWriter(
    			new FileWriter(configPath + "/" + CATALINA_PROPERTIES, true));
    	output.newLine();
    	output.append(key + "=" + value);
    	output.close();
    }

    public void addJarsDirToSharedLoader() throws ParserConfigurationException {
        try {
            BufferedReader file = new BufferedReader(new FileReader(configPath + "/" + CATALINA_PROPERTIES));
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
            FileOutputStream fileOut = new FileOutputStream(configPath + "/" + CATALINA_PROPERTIES);
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
