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

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import io.openliberty.boost.common.runtimes.RuntimeI;
import io.openliberty.boost.common.config.ServerConfigGenerator;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mpConfig")
public class MPConfigBoosterConfig extends AbstractBoosterConfig {

    String libertyFeature = null;

    public MPConfigBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        String version = dependencies.get(getCoordinates(this.getClass()));
        // if it is the 1.0 version = EE7 feature level
        if (version.equals(MP_20_VERSION)) {
            libertyFeature = MPCONFIG_13;
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
        //     deps.add("org.apache.geronimo.config:geronimo-config-impl:1.2.1");
        //     deps.add("org.eclipse.microprofile.config:microprofile-config-api:1.3");
        //     deps.add("org.osgi:org.osgi.annotation.versioning:1.0.0");
        // }
        return deps;
    }
}
