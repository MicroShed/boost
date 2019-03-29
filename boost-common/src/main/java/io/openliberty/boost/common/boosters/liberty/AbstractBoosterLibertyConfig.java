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

package io.openliberty.boost.common.boosters.liberty;

import java.util.Properties;

import org.w3c.dom.Document;

import io.openliberty.boost.common.boosters.AbstractBoosterConfig;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public abstract class AbstractBoosterLibertyConfig extends AbstractBoosterConfig {

    /**
     * Return the Liberty feature name
     * 
     * @return
     */
    public abstract String getFeature();

    /**
     * Add the server.xml configuration for this booster
     * 
     * @param doc
     */
    public abstract void addServerConfig(Document doc);

    /**
     * Return the properties required by this booster
     */
    public abstract Properties getServerProperties();

}
