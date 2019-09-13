/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.runtimes.openliberty.boosters;

import static boost.common.config.ConfigConstants.*;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.MPHealthBoosterConfig;
import boost.common.config.BoosterConfigParams;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPHealthBoosterConfig extends MPHealthBoosterConfig implements LibertyBoosterI {

    public LibertyMPHealthBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() {
        String version = getVersion();

        if (version.equals(MP_HEALTH_VERSION_10)) {
            return MPHEALTH_10;
        } else if (version.equals(MP_HEALTH_VERSION_20)) {
            return MPHEALTH_20;
        }

        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
