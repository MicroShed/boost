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

import static org.microshed.boost.common.config.ConfigConstants.*;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.JDBCBoosterConfig;
import org.microshed.boost.common.config.BoostProperties;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.openliberty.LibertyServerConfigGenerator;
import org.microshed.boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyJDBCBoosterConfig extends JDBCBoosterConfig implements LibertyBoosterI {

    public LibertyJDBCBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);

    }

    @Override
    public String getFeature() {
        String compilerVersion = System.getProperty(BoostProperties.INTERNAL_COMPILER_TARGET);

        if ("1.8".equals(compilerVersion) || "8".equals(compilerVersion) || "9".equals(compilerVersion)
                || "10".equals(compilerVersion)) {
            return JDBC_42;
        } else if ("11".equals(compilerVersion)) {
            return JDBC_43;
        } else {
            return JDBC_41; // Default to the spec for Liberty's
                            // minimum supported JRE (version 7
                            // as of 17.0.0.3)
        }
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) throws BoostException {
        try {
            libertyServerConfigGenerator.addDataSource(getDriverInfo(), getDatasourceProperties());
        } catch (Exception e) {
            throw new BoostException("Error when configuring JDBC data source.", e);
        }
    }
}
