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
    public String getFeature() {
        if (getVersion().startsWith(JPA_VERSION_21)) {
            return JPA_21;
        } else if (getVersion().startsWith(JPA_VERSION_22)) {
            return JPA_22;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
