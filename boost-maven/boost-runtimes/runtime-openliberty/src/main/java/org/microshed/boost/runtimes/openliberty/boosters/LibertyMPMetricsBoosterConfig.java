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

import java.util.HashMap;
import java.util.Map;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPMetricsBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyMPMetricsBoosterConfig extends MPMetricsBoosterConfig implements LibertyBoosterI {

    public LibertyMPMetricsBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {
        String version = getVersion();
        String feature = null;

        if (version.startsWith(MP_METRICS_VERSION_11)) {
            feature = MPMETRICS_11;
        } else if (version.startsWith(MP_METRICS_VERSION_20)) {
            feature = MPMETRICS_20;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + MP_METRICS_VERSION_11 + " or "
                    + MP_METRICS_VERSION_20 + ".\n Unable to add feature " + MPMETRICS_11 + " or " + MPMETRICS_20
                    + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("authentication", "false");
        libertyServerConfigGenerator.addElementWithAttributes("mpMetrics", attributes);
    }
}
