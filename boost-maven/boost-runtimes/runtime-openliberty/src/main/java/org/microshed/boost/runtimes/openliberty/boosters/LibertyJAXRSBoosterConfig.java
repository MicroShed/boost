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

import static org.microshed.boost.common.config.ConfigConstants.JAXRS_20;
import static org.microshed.boost.common.config.ConfigConstants.JAXRS_21;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.JAXRSBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyJAXRSBoosterConfig extends JAXRSBoosterConfig implements LibertyBoosterI {

    public LibertyJAXRSBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(JAXRS_VERSION_20)) {
            feature = JAXRS_20;
        } else if (getVersion().startsWith(JAXRS_VERSION_21)) {
            feature = JAXRS_21;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + JAXRS_VERSION_20 + " or "
                    + JAXRS_VERSION_21;
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
