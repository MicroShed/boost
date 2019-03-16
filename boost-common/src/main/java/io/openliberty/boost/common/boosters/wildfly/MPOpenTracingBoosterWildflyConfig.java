/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.common.boosters.wildfly;

import java.util.List;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

@BoosterCoordinates(AbstractBoosterWildflyConfig.BOOSTERS_GROUP_ID + ":mpOpenTracing")
public class MPOpenTracingBoosterWildflyConfig extends AbstractBoosterWildflyConfig {

    public MPOpenTracingBoosterWildflyConfig(Map<String, String> dependencies, BoostLoggerI logger)
            throws BoostException {
    }

    @Override
    public List<String> getCliCommands() {
        return null;
    }

    @Override
    public String getDependency() {
        return null;
    }
}
