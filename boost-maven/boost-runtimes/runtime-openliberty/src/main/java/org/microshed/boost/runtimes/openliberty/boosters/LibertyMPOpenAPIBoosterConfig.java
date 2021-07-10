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
import org.microshed.boost.common.boosters.MPOpenAPIBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPOpenAPIBoosterConfig extends MPOpenAPIBoosterConfig implements LibertyBoosterI {

    public LibertyMPOpenAPIBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {
        String feature = null;
        if (getVersion().startsWith(MP_OPENAPI_VERSION_10)) {
            feature = MPOPENAPI_10;
        } else if (getVersion().startsWith(MP_OPENAPI_VERSION_11)) {
            feature = MPOPENAPI_11;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + MP_OPENAPI_VERSION_10 + " or "
                    + MP_OPENAPI_VERSION_11 + ".\n Unable to add feature " + MPOPENAPI_10 + " or " + MPOPENAPI_11
                    + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
