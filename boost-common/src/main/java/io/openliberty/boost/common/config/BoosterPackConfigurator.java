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

import java.io.IOException;
import java.util.Map;

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
     * 
     * @return
     */
    public abstract String getFeature();

    /**
     * method to write out the default config for a particular feature into
     * server.xml
     * 
     * @param doc
     */
    public abstract void addServerConfig(Document doc);

    /**
     * Set the Liberty feature based on the booster dependency version
     * 
     * @param version
     */
    public abstract void setFeature(String version);

    /**
     * Return the artifactId of the dependency jar to copy to the server
     * 
     * @return
     */
    public abstract String getDependencyToCopy();

}
