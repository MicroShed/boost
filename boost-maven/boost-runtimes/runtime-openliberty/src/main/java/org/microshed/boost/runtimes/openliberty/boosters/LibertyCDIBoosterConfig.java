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
import org.microshed.boost.common.boosters.CDIBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyCDIBoosterConfig extends CDIBoosterConfig implements LibertyBoosterI {

    public LibertyCDIBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(CDI_VERSION_20)) {
            feature = CDI_20;
        } else if (getVersion().startsWith(CDI_VERSION_12)) {
            feature = CDI_12;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + CDI_VERSION_20 + " or "
                    + CDI_VERSION_12 + "\n Unable to add " + CDI_20 + " or " + CDI_12 + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}