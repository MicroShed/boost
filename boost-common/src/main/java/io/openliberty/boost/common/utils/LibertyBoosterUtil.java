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
package io.openliberty.boost.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterPackConfigurator;
import io.openliberty.boost.common.config.CDIBoosterPackConfigurator;
import io.openliberty.boost.common.config.JAXRSBoosterPackConfigurator;
import io.openliberty.boost.common.config.JDBCBoosterPackConfigurator;
import io.openliberty.boost.common.config.JSONPBoosterPackConfigurator;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;
import io.openliberty.boost.common.config.MPConfigBoosterPackConfigurator;
import io.openliberty.boost.common.config.MPHealthBoosterPackConfigurator;
import io.openliberty.boost.common.config.MPOpenTracingBoosterPackConfigurator;
import io.openliberty.boost.common.config.MPRestClientBoosterPackConfigurator;

public class LibertyBoosterUtil {

    public static String BOOSTER_JAXRS = "io.openliberty.boosters:jaxrs";
    public static String BOOSTER_JDBC = "io.openliberty.boosters:jdbc";

    public String BOOSTERS_GROUP_ID = "io.openliberty.boosters";

    public static String BOOSTER_MPHEALTH = "io.openliberty.boosters:mpHealth";
    public static String BOOSTER_JSONP = "io.openliberty.boosters:jsonp";
    public static String BOOSTER_CDI = "io.openliberty.boosters:cdi";
    public static String BOOSTER_MPCONFIG = "io.openliberty.boosters:mpConfig";
    public static String BOOSTER_MPRESTCLIENT = "io.openliberty.boosters:mpRestClient";
    public static String BOOSTER_OPENTRACING = "io.openliberty.boosters:mpOpenTracing";

    // protected String libertyServerPath;
    // protected List<BoosterPackConfigurator> boosterPackConfigurators;
    // protected BoostLoggerI logger;

    /*
     * public LibertyBoosterUtil(String libertyServerPath, Map<String, String>
     * dependencies, BoostLoggerI logger) { this.libertyServerPath =
     * libertyServerPath; this.logger = logger;
     * 
     * this.boosterPackConfigurators = getBoosterPackConfigurators(dependencies); }
     */
    /**
     * take a list of pom boost dependency strings and map to liberty features for
     * config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @return
     */
    public static List<BoosterPackConfigurator> getBoosterPackConfigurators(Map<String, String> dependencies,
            BoostLoggerI logger) {

        List<BoosterPackConfigurator> boosterPackConfigList = new ArrayList<BoosterPackConfigurator>();

        Properties configuredBoostProperties = getConfiguredBoostProperties(logger);

        if (dependencies.containsKey(BOOSTER_JDBC)) {

            String version = dependencies.get(BOOSTER_JDBC);

            // Check for user defined database dependencies
            String configuredDatabaseDep = null;

            if (dependencies.containsKey(JDBCBoosterPackConfigurator.DERBY_DEPENDENCY)) {
                String derbyVersion = dependencies.get(JDBCBoosterPackConfigurator.DERBY_DEPENDENCY);
                configuredDatabaseDep = JDBCBoosterPackConfigurator.DERBY_DEPENDENCY + ":" + derbyVersion;

            } else if (dependencies.containsKey(JDBCBoosterPackConfigurator.DB2_DEPENDENCY)) {
                String db2Version = dependencies.get(JDBCBoosterPackConfigurator.DB2_DEPENDENCY);
                configuredDatabaseDep = JDBCBoosterPackConfigurator.DB2_DEPENDENCY + ":" + db2Version;
            }

            JDBCBoosterPackConfigurator jdbcConfig = new JDBCBoosterPackConfigurator(version, configuredBoostProperties,
                    configuredDatabaseDep);

            boosterPackConfigList.add(jdbcConfig);

        }
        if (dependencies.containsKey(BOOSTER_JAXRS)) {

            String version = dependencies.get(BOOSTER_JAXRS);

            JAXRSBoosterPackConfigurator jaxrsConfig = new JAXRSBoosterPackConfigurator(version);

            boosterPackConfigList.add(jaxrsConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPHEALTH)) {

            String version = dependencies.get(BOOSTER_MPHEALTH);

            MPHealthBoosterPackConfigurator mpHealthConfig = new MPHealthBoosterPackConfigurator(version);

            boosterPackConfigList.add(mpHealthConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPCONFIG)) {

            String version = dependencies.get(BOOSTER_MPCONFIG);

            MPConfigBoosterPackConfigurator mpConfigConfig = new MPConfigBoosterPackConfigurator(version);

            boosterPackConfigList.add(mpConfigConfig);
        }
        if (dependencies.containsKey(BOOSTER_CDI)) {

            String version = dependencies.get(BOOSTER_CDI);

            CDIBoosterPackConfigurator CDIConfig = new CDIBoosterPackConfigurator(version);

            boosterPackConfigList.add(CDIConfig);
        }
        if (dependencies.containsKey(BOOSTER_MPRESTCLIENT)) {

            String version = dependencies.get(BOOSTER_MPRESTCLIENT);

            MPRestClientBoosterPackConfigurator mpRestClientConfig = new MPRestClientBoosterPackConfigurator(version);

            boosterPackConfigList.add(mpRestClientConfig);
        }
        if (dependencies.containsKey(BOOSTER_JSONP)) {

            String version = dependencies.get(BOOSTER_JSONP);

            JSONPBoosterPackConfigurator jsonpConfig = new JSONPBoosterPackConfigurator(version);

            boosterPackConfigList.add(jsonpConfig);
        }
        if (dependencies.containsKey(BOOSTER_OPENTRACING)) {

            String version = dependencies.get(BOOSTER_OPENTRACING);

            MPOpenTracingBoosterPackConfigurator mpOpenTracingConfig = new MPOpenTracingBoosterPackConfigurator(
                    version);

            boosterPackConfigList.add(mpOpenTracingConfig);
        }

        return boosterPackConfigList;
    }

    public static void generateLibertyServerConfig(String libertyServerPath,
            List<BoosterPackConfigurator> boosterPackConfigurators, String warName) throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath);

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (BoosterPackConfigurator configurator : boosterPackConfigurators) {
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

    public static List<String> getDependenciesToCopy(List<BoosterPackConfigurator> boosterPackConfigurators,
            BoostLoggerI logger) {

        List<String> dependenciesToCopy = new ArrayList<String>();

        for (BoosterPackConfigurator configurator : boosterPackConfigurators) {
            String dependencyToCopy = configurator.getDependency();

            if (dependencyToCopy != null) {
                logger.info(dependencyToCopy);
                dependenciesToCopy.add(dependencyToCopy);
            }
        }

        return dependenciesToCopy;
    }

    private static Properties getConfiguredBoostProperties(BoostLoggerI logger) {

        List<String> supportedProps = BoostProperties.getAllSupportedProperties();
        Properties systemProperties = System.getProperties();

        Properties boostProperties = new Properties();

        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {

            if (supportedProps.contains(entry.getKey().toString())) {

                logger.debug("Found boost property: " + entry.getKey() + ":" + entry.getValue());

                boostProperties.put(entry.getKey(), entry.getValue());
            }
        }

        return boostProperties;

    }
}
