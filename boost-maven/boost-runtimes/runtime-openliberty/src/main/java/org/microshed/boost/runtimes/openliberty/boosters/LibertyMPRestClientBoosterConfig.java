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
import org.microshed.boost.common.boosters.MPRestClientBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPRestClientBoosterConfig extends MPRestClientBoosterConfig implements LibertyBoosterI {

    public LibertyMPRestClientBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(MP_RESTCLIENT_VERSION_11)) {
            feature = MPRESTCLIENT_11;
        } else if (getVersion().startsWith(MP_RESTCLIENT_VERSION_12)) {
            feature = MPRESTCLIENT_12;
        } else if (getVersion().startsWith(MP_RESTCLIENT_VERSION_13)) {
            feature = MPRESTCLIENT_13;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + MP_RESTCLIENT_VERSION_11 + ", "
                    + MP_RESTCLIENT_VERSION_12 + " or " + MP_RESTCLIENT_VERSION_13 + ".\n Unable to add feature "
                    + MPRESTCLIENT_11 + ", " + MPRESTCLIENT_12 + " or " + MPRESTCLIENT_13
                    + " to the server configuration";
            throw new BoostException(msg);
        }

        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
