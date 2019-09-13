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

import static boost.common.config.ConfigConstants.DB2_DEFAULT_PORT_NUMBER;
import static boost.common.config.ConfigConstants.DERBY_DB;
import static boost.common.config.ConfigConstants.MYSQL_DEFAULT_PORT_NUMBER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import boost.common.config.BoostProperties;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mp-jwt")
public class MPJWTBoosterConfig extends AbstractBoosterConfig {

    protected Properties boostMPProperties;

    public MPJWTBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies.get(getCoordinates(MPJWTBoosterConfig.class)));

        boostMPProperties = BoostProperties.getConfiguredMPProperties(logger);
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }

}
