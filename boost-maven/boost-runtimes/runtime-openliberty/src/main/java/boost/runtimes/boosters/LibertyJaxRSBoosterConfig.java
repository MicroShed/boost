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

import static io.openliberty.boost.common.config.ConfigConstants.JAXRS_20;
import static io.openliberty.boost.common.config.ConfigConstants.JAXRS_21;
import java.util.Map;
import io.openliberty.boost.common.boosters.JAXRSBoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyJaxRSBoosterConfig extends JAXRSBoosterConfig implements LibertyBoosterI {

    public LibertyJaxRSBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().equals(EE_7_VERSION)) {
            return JAXRS_20;
        } else if (getVersion().equals(EE_8_VERSION)) {
            return JAXRS_21;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
