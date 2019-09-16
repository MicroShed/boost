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

import static boost.common.config.ConfigConstants.JAXRS_20;
import static boost.common.config.ConfigConstants.JAXRS_21;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.JAXRSBoosterConfig;
import boost.common.config.BoosterConfigParams;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyJAXRSBoosterConfig extends JAXRSBoosterConfig implements LibertyBoosterI {

    public LibertyJAXRSBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().startsWith(JAXRS_VERSION_20)) {
            return JAXRS_20;
        } else if (getVersion().startsWith(JAXRS_VERSION_21)) {
            return JAXRS_21;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
