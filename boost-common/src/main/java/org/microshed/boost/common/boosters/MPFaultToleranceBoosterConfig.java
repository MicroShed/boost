/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.microshed.boost.common.boosters;

import java.util.ArrayList;
import java.util.List;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import org.microshed.boost.common.config.BoosterConfigParams;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mp-fault-tolerance")
public class MPFaultToleranceBoosterConfig extends AbstractBoosterConfig {

    public MPFaultToleranceBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params.getProjectDependencies().get(getCoordinates(MPFaultToleranceBoosterConfig.class)), logger);
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }
}
