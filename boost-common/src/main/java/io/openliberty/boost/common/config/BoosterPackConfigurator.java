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

package io.openliberty.boost.common.config;

import java.util.Properties;

import org.w3c.dom.Document;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public abstract class BoosterPackConfigurator {
		
	protected String EE_7_VERSION = "0.1-SNAPSHOT";
	protected String EE_8_VERSION = "0.2-SNAPSHOT";
	
	/**
	 * Return the Liberty feature name
	 * @return
	 */
	public abstract String getFeature();
	
	/**
	 * Add the server.xml configuration for this booster
	 * @param doc
	 */
	public abstract void addServerConfig(Document doc);

	/**
	 * Return the properties required by this booster
	 */
	public abstract Properties getServerProperties();

	/**
	 * Return the dependency that this booster requires
	 * @return
	 */
	public abstract String getDependency();
	
}
