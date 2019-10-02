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
import org.microshed.boost.common.boosters.MPOpenTracingBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPOpenTracingBoosterConfig extends MPOpenTracingBoosterConfig implements LibertyBoosterI {

    public LibertyMPOpenTracingBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {
        String feature = null;
        if (getVersion().startsWith(MP_OPENTRACING_VERSION_11)) {
            feature = MPOPENTRACING_11;
        } else if (getVersion().startsWith(MP_OPENTRACING_VERSION_12)) {
            feature = MPOPENTRACING_12;
        } else if (getVersion().startsWith(MP_OPENTRACING_VERSION_13)) {
            feature = MPOPENTRACING_13;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " feature = ed. Expected " + MP_OPENTRACING_VERSION_11
                    + ", " + MP_OPENTRACING_VERSION_12 + " or " + MP_OPENTRACING_VERSION_13
                    + ".\n Unable to add feature " + MPOPENTRACING_11 + ", " + MPOPENTRACING_12 + " or "
                    + MPOPENTRACING_13 + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;

    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
