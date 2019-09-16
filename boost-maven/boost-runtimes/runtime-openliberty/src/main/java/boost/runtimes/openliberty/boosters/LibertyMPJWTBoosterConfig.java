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
package boost.runtimes.openliberty.boosters;

import static boost.common.config.ConfigConstants.MPJWT_11;

import java.util.Map;
import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.MPJWTBoosterConfig;
import boost.common.config.BoosterConfigParams;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;
import java.util.Properties;

public class LibertyMPJWTBoosterConfig extends MPJWTBoosterConfig implements LibertyBoosterI {

    public LibertyMPJWTBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().equals(MP_JWT_VERSION_11)) {
            return MPJWT_11;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) throws BoostException {

        try {
            if (!!!boostMPProperties.isEmpty())
                libertyServerConfigGenerator.addEnvironemntVariables(boostMPProperties);

        } catch (Exception e) {
            throw new BoostException("Error when configuring mp-jwt " + e.toString());
        }

    }

}
