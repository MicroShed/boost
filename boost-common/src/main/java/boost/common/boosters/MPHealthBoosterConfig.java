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
package boost.common.boosters;

import java.util.ArrayList;
import java.util.List;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import boost.common.config.BoosterConfigParams;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mp-health")
public class MPHealthBoosterConfig extends AbstractBoosterConfig {

    public MPHealthBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params.getProjectDependencies().get(getCoordinates(MPHealthBoosterConfig.class)));
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }
}
