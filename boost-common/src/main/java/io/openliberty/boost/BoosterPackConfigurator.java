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

package io.openliberty.boost;

import org.w3c.dom.Document;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public interface BoosterPackConfigurator {
		
	/**
	 * method to return the string feature name - will be written to the feature manager stanza of server.xml
	 * @return
	 */
	public abstract String getFeatureString();
	
	/**
	 * method to write out the default config for a particular feature into server.xml
	 * @param doc
	 */
	public abstract void writeConfigToServerXML(Document doc);
	
	public abstract void setFeatureString(String featureStr);
}
