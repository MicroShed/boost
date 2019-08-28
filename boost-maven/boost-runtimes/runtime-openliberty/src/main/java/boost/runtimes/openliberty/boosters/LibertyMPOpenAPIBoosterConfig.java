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

import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.MPOpenAPIBoosterConfig;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPOpenAPIBoosterConfig extends MPOpenAPIBoosterConfig implements LibertyBoosterI {

    public LibertyMPOpenAPIBoosterConfig(Map<String, String> dependencies, Properties boostProperties, BoostLoggerI logger) throws BoostException {
        super(dependencies, boostProperties, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().equals(MP_OPENAPI_VERSION_10)) {
            return MPOPENAPI_10;
        } else if (getVersion().equals(MP_OPENAPI_VERSION_11)) {
            return MPOPENAPI_11;
        }

        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
