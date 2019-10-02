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
import org.microshed.boost.common.boosters.BeanValidationBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyBeanValidationBoosterConfig extends BeanValidationBoosterConfig implements LibertyBoosterI {

    public LibertyBeanValidationBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(BEANVALIDATION_VERSION_20)) {
            feature = BEANVALIDATION_20;
        }
        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + BEANVALIDATION_VERSION_20
                    + "\n Unable to add Feature " + BEANVALIDATION_20 + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
