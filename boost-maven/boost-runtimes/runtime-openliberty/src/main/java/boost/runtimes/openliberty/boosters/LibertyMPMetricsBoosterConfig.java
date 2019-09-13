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

import java.util.HashMap;
import java.util.Map;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.MPMetricsBoosterConfig;
import boost.common.config.BoosterConfigParams;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPMetricsBoosterConfig extends MPMetricsBoosterConfig implements LibertyBoosterI {

    public LibertyMPMetricsBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() {
        String version = getVersion();

        if (version.equals(MP_METRICS_VERSION_11)) {
            return MPMETRICS_11;
        } else if (version.equals(MP_METRICS_VERSION_20)) {
            return MPMETRICS_20;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("authentication", "false");
        libertyServerConfigGenerator.addElementWithAttributes("mpMetrics", attributes);
    }
}
