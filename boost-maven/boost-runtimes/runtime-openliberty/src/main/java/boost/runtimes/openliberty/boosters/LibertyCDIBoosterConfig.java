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

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.CDIBoosterConfig;
import boost.common.config.BoosterConfigParams;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyCDIBoosterConfig extends CDIBoosterConfig implements LibertyBoosterI {

    public LibertyCDIBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    public String getFeature() {
        if (getVersion().equals(CDI_VERSION_20)) {
            return CDI_20;
        }
        if (getVersion().equals(CDI_VERSION_12)) {
            return CDI_12;
        }
        else {
            return null;
        }
    }

    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}