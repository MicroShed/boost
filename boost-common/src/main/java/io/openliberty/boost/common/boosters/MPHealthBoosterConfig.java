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

import static io.openliberty.boost.common.config.ConfigConstants.MPHEALTH_10;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import io.openliberty.boost.common.runtimes.RuntimeI;
import io.openliberty.boost.common.config.ServerConfigGenerator;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mpHealth")
public class MPHealthBoosterConfig extends AbstractBoosterConfig {

    String libertyFeature = null;

    public MPHealthBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        String version = dependencies.get(getCoordinates(this.getClass()));
        // if it is the 2.0 version = MP2.0 feature level
        if (version.equals(MP_20_VERSION)) {
            libertyFeature = MPHEALTH_10;
        }
    }

    @Override
    public String getLibertyFeature() {
        return libertyFeature;
    }

    @Override
    public void addServerConfig(ServerConfigGenerator config) {
        // No config to write
    }

    @Override
    public List<String> getDependencies(RuntimeI runtime) {
    	List<String> deps = new ArrayList<String>();
        // if(runtime instanceof TomeeRuntimeI) {
        //     deps.add("org.apache.geronimo:geronimo-health:1.0.1");
        //     deps.add("org.apache.geronimo:geronimo-health-common:1.0.1");
        //     deps.add("org.eclipse.microprofile.health:microprofile-health-api:1.0");
        // }
        return deps;
    }
}
