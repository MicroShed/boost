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
package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.MPOPENTRACING_11;
import java.util.Map;
import io.openliberty.boost.common.boosters.MPOpenTracingBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyMPOpenTracingBoosterConfig extends MPOpenTracingBoosterConfig implements LibertyBoosterI {

    public LibertyMPOpenTracingBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger)
            throws BoostException {
        super(dependencies, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().equals(MP_20_VERSION)) {
            return MPOPENTRACING_11;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
