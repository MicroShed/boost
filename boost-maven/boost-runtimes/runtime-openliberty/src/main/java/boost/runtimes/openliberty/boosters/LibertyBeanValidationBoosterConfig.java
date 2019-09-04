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

import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.BeanValidationBoosterConfig;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyBeanValidationBoosterConfig extends BeanValidationBoosterConfig implements LibertyBoosterI {

    public LibertyBeanValidationBoosterConfig(Map<String, String> dependencies, Properties boostProperties, BoostLoggerI logger)
            throws BoostException {
        super(dependencies, boostProperties, logger);
    }

    public String getFeature() {
        if (getVersion().equals(BEANVALIDATION_VERSION_20)) {
            return BEANVALIDATION_20;
        } else {
            return null;
        }
    }

    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
