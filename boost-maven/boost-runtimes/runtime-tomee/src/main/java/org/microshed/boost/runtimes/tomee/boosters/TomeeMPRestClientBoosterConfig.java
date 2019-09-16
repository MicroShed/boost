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
package org.microshed.boost.runtimes.tomee.boosters;

import java.util.List;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPRestClientBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;

public class TomeeMPRestClientBoosterConfig extends MPRestClientBoosterConfig {

    public TomeeMPRestClientBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = super.getDependencies();
        deps.add("org.apache.cxf:cxf-rt-rs-mp-client:3.2.7");
        deps.add("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:1.1");
        return deps;
    }
}

