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

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoosterPackConfigurator;
import io.openliberty.boost.common.config.JAXRSBoosterPackConfigurator;
import io.openliberty.boost.common.config.JDBCBoosterPackConfigurator;
import io.openliberty.boost.common.config.LibertyServerConfigGenerator;

public class LibertyBoosterUtil {

    public String BOOSTERS_GROUP_ID = "io.openliberty.boosters";

    public static String BOOSTER_JAXRS = "jaxrs";
    public static String BOOSTER_JDBC = "jdbc";

    protected String libertyServerPath;
    protected List<BoosterPackConfigurator> boosterPackConfigurators;
    protected BoostLoggerI logger;

    public LibertyBoosterUtil(String libertyServerPath, Map<String, String> dependencies, BoostLoggerI logger) {
        this.libertyServerPath = libertyServerPath;
        this.logger = logger;

        this.boosterPackConfigurators = getBoosterPackConfigurators(dependencies);
    }

    /**
     * take a list of pom boost dependency strings and map to liberty features for
     * config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @return
     */
    private List<BoosterPackConfigurator> getBoosterPackConfigurators(Map<String, String> dependencies) {

        List<BoosterPackConfigurator> boosterPackConfigList = new ArrayList<BoosterPackConfigurator>();

        for (String dep : dependencies.keySet()) {
            if (dep.equals(BOOSTER_JDBC)) {
                JDBCBoosterPackConfigurator jdbcConfig = new JDBCBoosterPackConfigurator();
                jdbcConfig.setFeature(dependencies.get(dep));
                boosterPackConfigList.add(jdbcConfig);

            } else if (dep.equals(BOOSTER_JAXRS)) {
                JAXRSBoosterPackConfigurator jaxrsConfig = new JAXRSBoosterPackConfigurator();
                jaxrsConfig.setFeature(dependencies.get(dep));
                boosterPackConfigList.add(jaxrsConfig);
            }

        }

        return boosterPackConfigList;
    }

    public void generateLibertyServerConfig(String warName) throws Exception {

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

    public List<String> getDependenciesToCopy() {

        List<String> dependenciesToCopy = new ArrayList<String>();

        for (BoosterPackConfigurator configurator : boosterPackConfigurators) {
            String dependencyToCopy = configurator.getDependencyToCopy();

            if (dependencyToCopy != null) {

                dependenciesToCopy.add(dependencyToCopy);
            }
        }

        return dependenciesToCopy;
    }
}
