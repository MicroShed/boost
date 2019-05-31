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
package boost.runtimes.boosters;

import java.util.List;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.MPHealthBoosterConfig;

public class TomeeMPHealthBoosterConfig extends MPHealthBoosterConfig implements TomeeBoosterI {

    public TomeeMPHealthBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
    public List<String> getDependencies() {
    	List<String> deps = super.getDependencies();
        deps.add("org.apache.geronimo:geronimo-health:1.0.1");
        deps.add("org.apache.geronimo:geronimo-health-common:1.0.1");
        deps.add("org.eclipse.microprofile.health:microprofile-health-api:1.0");
        return deps;
    }
}
