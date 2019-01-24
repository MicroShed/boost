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

    public static String BOOSTER_JAXRS = "io.openliberty.boosters:jaxrs";

    public String BOOSTERS_GROUP_ID = "io.openliberty.boosters";

    public static String BOOSTER_MPHEALTH = "io.openliberty.boosters:mpHealth";
    public static String BOOSTER_JSONP = "io.openliberty.boosters:jsonp";
    public static String BOOSTER_CDI = "io.openliberty.boosters:cdi";
    public static String BOOSTER_MPCONFIG = "io.openliberty.boosters:mpConfig";
    public static String BOOSTER_MPRESTCLIENT = "io.openliberty.boosters:mpRestClient";
    public static String BOOSTER_OPENTRACING = "io.openliberty.boosters:mpOpenTracing";

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

//        if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(JDBCBoosterConfig.class))) {
        if (dependencies.containsKey("io.openliberty.boosters:jdbc")) {

            JDBCBoosterConfig jdbcConfig = new JDBCBoosterConfig(dependencies, logger);
            boosterPackConfigList.add(jdbcConfig);
        }
        if (dependencies.containsKey(BOOSTER_JAXRS)) {

            String version = dependencies.get(BOOSTER_JAXRS);

            JAXRSBoosterConfig jaxrsConfig = new JAXRSBoosterConfig(version);

            boosterPackConfigList.add(jaxrsConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPHEALTH)) {

            String version = dependencies.get(BOOSTER_MPHEALTH);

            MPHealthBoosterConfig mpHealthConfig = new MPHealthBoosterConfig(version);

            boosterPackConfigList.add(mpHealthConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPCONFIG)) {

            String version = dependencies.get(BOOSTER_MPCONFIG);

            MPConfigBoosterConfig mpConfigConfig = new MPConfigBoosterConfig(version);

            boosterPackConfigList.add(mpConfigConfig);
        }
        if (dependencies.containsKey(BOOSTER_CDI)) {

            String version = dependencies.get(BOOSTER_CDI);

            CDIBoosterConfig CDIConfig = new CDIBoosterConfig(version);

            boosterPackConfigList.add(CDIConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPRESTCLIENT)) {

            String version = dependencies.get(BOOSTER_MPRESTCLIENT);

            MPRestClientBoosterConfig mpRestClientConfig = new MPRestClientBoosterConfig(version);

            boosterPackConfigList.add(mpRestClientConfig);
        }
        if (dependencies.containsKey(BOOSTER_JSONP)) {

            String version = dependencies.get(BOOSTER_JSONP);

            JSONPBoosterConfig jsonpConfig = new JSONPBoosterConfig(version);

            boosterPackConfigList.add(jsonpConfig);
        }
        if (dependencies.containsKey(BOOSTER_OPENTRACING)) {

            String version = dependencies.get(BOOSTER_OPENTRACING);

            MPOpenTracingBoosterConfig mpOpenTracingConfig = new MPOpenTracingBoosterConfig(
                    version);

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
