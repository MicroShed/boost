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

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Add features to a Liberty server configuration
 *
 */
public class LibertyFeatureConfigGenerator {

	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	Element featureManager;
	Document doc;

	public LibertyFeatureConfigGenerator() {
	
		try {

			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();

			// Create FeatureManager element
			doc = docBuilder.newDocument();
			Element serverRoot = doc.createElement("server");
			doc.appendChild(serverRoot);
			featureManager = doc.createElement("featureManager");
			serverRoot.appendChild(featureManager);

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  }
	}
	
	/**
	 * Add a feature to the feature config
	 *
	 */
	public void addFeature(String featureName) {

		Element feature = doc.createElement("feature");
		feature.appendChild(doc.createTextNode(featureName));
		featureManager.appendChild(feature);
	}
	
	/**
	 * Write the feature config to the configDropins/overrides directory of the specified server
	 *
	 */
	public void writeToServer(String serverPath) {
	
		// Create configDropins/overrides directory
		new File(serverPath + "/configDropins/overrides").mkdirs();
		
		try {
		
			// Write XML to configDropins/overrides
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(serverPath + "/configDropins/overrides/featureList.xml"));
			transformer.transform(source, result);
		
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
	  	}
		
	}
}
