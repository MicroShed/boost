/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.microshed.boost.runtimes.openliberty.boosters;

import static org.microshed.boost.common.config.ConfigConstants.MPJWT_11;

import java.util.Map;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPJWTBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPJWTBoosterConfig extends MPJWTBoosterConfig implements LibertyBoosterI {

    public LibertyMPJWTBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(MP_JWT_VERSION_11)) {
            feature = MPJWT_11;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + MP_JWT_VERSION_11
                    + ".\n Unable to add feature " + MPJWT_11 + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }

}
