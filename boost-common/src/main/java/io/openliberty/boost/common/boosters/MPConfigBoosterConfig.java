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
package io.openliberty.boost.common.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.MPCONFIG_13;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mpConfig")
public class MPConfigBoosterConfig extends AbstractBoosterConfig {

    String libertyFeature = null;
    List<String> tomeeDependencyStrings = new ArrayList<String>();

    public MPConfigBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        String version = dependencies.get(getCoordindates(this.getClass()));
        // if it is the 1.0 version = EE7 feature level
        if (version.equals(MP_20_VERSION)) {
            libertyFeature = MPCONFIG_13;
        }

        tomeeDependencyStrings.add("org.apache.geronimo.config:geronimo-config-impl:1.2.1");
        tomeeDependencyStrings.add("org.eclipse.microprofile.config:microprofile-config-api:1.3");
        tomeeDependencyStrings.add("org.osgi:org.osgi.annotation.versioning:1.0.0");
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
    public List<String> getTomEEDependency() {
        return tomeeDependencyStrings;
    }

    @Override
    public Properties getServerProperties() {
        // TODO Auto-generated method stub
        return null;
    }
}
