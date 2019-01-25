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
package io.openliberty.boost.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.boosters.CDIBoosterConfig;
import io.openliberty.boost.common.boosters.JAXRSBoosterConfig;
import io.openliberty.boost.common.boosters.JDBCBoosterConfig;
import io.openliberty.boost.common.boosters.JSONPBoosterConfig;
import io.openliberty.boost.common.boosters.MPConfigBoosterConfig;
import io.openliberty.boost.common.boosters.MPHealthBoosterConfig;
import io.openliberty.boost.common.boosters.MPOpenTracingBoosterConfig;
import io.openliberty.boost.common.boosters.MPRestClientBoosterConfig;

public class BoosterConfigurator {

    /**
     * take a list of pom boost dependency strings and map to liberty features
     * for config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @return
     */
    public static List<AbstractBoosterConfig> getBoosterPackConfigurators(Map<String, String> dependencies,
            BoostLoggerI logger) throws BoostException {

        List<AbstractBoosterConfig> boosterPackConfigList = new ArrayList<AbstractBoosterConfig>();

        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(JDBCBoosterConfig.class))) {
            JDBCBoosterConfig jdbcConfig = new JDBCBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(jdbcConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(JAXRSBoosterConfig.class))) {
            JAXRSBoosterConfig jaxrsConfig = new JAXRSBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(jaxrsConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(MPHealthBoosterConfig.class))) {
            MPHealthBoosterConfig mpHealthConfig = new MPHealthBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(mpHealthConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(MPConfigBoosterConfig.class))) {
            MPConfigBoosterConfig mpConfigConfig = new MPConfigBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(mpConfigConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(CDIBoosterConfig.class))) {
            CDIBoosterConfig CDIConfig = new CDIBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(CDIConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(MPRestClientBoosterConfig.class))) {
            MPRestClientBoosterConfig mpRestClientConfig = new MPRestClientBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(mpRestClientConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(JSONPBoosterConfig.class))) {
            JSONPBoosterConfig jsonpConfig = new JSONPBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(jsonpConfig);
        }
        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(MPOpenTracingBoosterConfig.class))) {
            MPOpenTracingBoosterConfig mpOpenTracingConfig = new MPOpenTracingBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(mpOpenTracingConfig);
        }

        return boosterPackConfigList;
    }

    public static void generateLibertyServerConfig(String libertyServerPath,
            List<AbstractBoosterConfig> boosterPackConfigurators, String warName, BoostLoggerI logger) throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath, logger);

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
            serverConfig.addFeature(configurator.getFeature());
            serverConfig.addBoosterConfig(configurator);
        }

        // Add war configuration is necessary
        if (warName != null) {
            serverConfig.addApplication(warName);
        } else {
            throw new Exception(
                    "Unsupported Maven packaging type - Liberty Boost currently supports WAR packaging type only.");
        }

        serverConfig.writeToServer();
    }

    public static List<String> getDependenciesToCopy(List<AbstractBoosterConfig> boosterPackConfigurators,
            BoostLoggerI logger) {

        List<String> dependenciesToCopy = new ArrayList<String>();

        for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
            String dependencyToCopy = configurator.getDependency();

            if (dependencyToCopy != null) {
                logger.info(dependencyToCopy);
                dependenciesToCopy.add(dependencyToCopy);
            }
        }

        return dependenciesToCopy;
    }

}
