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
package org.microshed.boost.runtimes.openliberty.boosters;

import static org.microshed.boost.common.config.ConfigConstants.*;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPHealthBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPHealthBoosterConfig extends MPHealthBoosterConfig implements LibertyBoosterI {

    public LibertyMPHealthBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {
        String feature = null;
        String version = getVersion();

        if (version.startsWith(MP_HEALTH_VERSION_10)) {
            feature = MPHEALTH_10;
        } else if (version.startsWith(MP_HEALTH_VERSION_20)) {
            feature = MPHEALTH_20;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + "returned. Expected " + MP_HEALTH_VERSION_10 + " or "
                    + MP_HEALTH_VERSION_20 + ".\n Unable to add feature " + MPHEALTH_10 + " or " + MPHEALTH_20
                    + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
