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

import static org.microshed.boost.common.config.ConfigConstants.JPA_21;
import static org.microshed.boost.common.config.ConfigConstants.JPA_22;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.JPABoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyJPABoosterConfig extends JPABoosterConfig implements LibertyBoosterI {

    public LibertyJPABoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public String getFeature() throws BoostException {

        String feature = null;
        if (getVersion().startsWith(JPA_VERSION_21)) {
            feature = JPA_21;
        } else if (getVersion().startsWith(JPA_VERSION_22)) {
            feature = JPA_22;
        }

        if (feature == null) {
            String msg = "Invalid version " + getVersion() + " returned. Expected " + JPA_VERSION_21 + " or "
                    + JPA_VERSION_22 + ".\n Unable to add feature " + JPA_21 + " or " + JPA_22
                    + " to the server configuration";
            throw new BoostException(msg);
        }
        return feature;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
