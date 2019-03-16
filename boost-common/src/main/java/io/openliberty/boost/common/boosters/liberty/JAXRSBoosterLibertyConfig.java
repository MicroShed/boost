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

import org.w3c.dom.Document;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

import static io.openliberty.boost.common.config.LibertyConfigConstants.*;

import java.util.Map;
import java.util.Properties;

@BoosterCoordinates(AbstractBoosterLibertyConfig.BOOSTERS_GROUP_ID + ":jaxrs")
public class JAXRSBoosterLibertyConfig extends AbstractBoosterLibertyConfig {

    String libertyFeature = null;

    public JAXRSBoosterLibertyConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        String version = dependencies.get(getCoordindates(this.getClass()));
        if (version.equals(EE_7_VERSION)) {
            libertyFeature = JAXRS_20;
        } else if (version.equals(EE_8_VERSION)) {
            libertyFeature = JAXRS_21;
        }
    }

    @Override
    public String getFeature() {
        return libertyFeature;
    }

    @Override
    public void addServerConfig(Document doc) {
        // No config to write
    }

    @Override
    public String getDependency() {

        return null;
    }

    @Override
    public Properties getServerProperties() {

        return null;
    }
}
