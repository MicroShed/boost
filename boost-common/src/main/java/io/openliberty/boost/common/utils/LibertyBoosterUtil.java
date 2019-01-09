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

import javax.xml.parsers.ParserConfigurationException;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoosterDependencyInfo;
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

    public String BOOSTERS_GROUP_ID = "io.openliberty.boosters";

    public static String BOOSTER_JAXRS = "jaxrs";
    public static String BOOSTER_JDBC = "jdbc";
    public static String BOOSTER_MPHEALTH = "mpHealth";
    public static String BOOSTER_JSONP = "jsonp";
    public static String BOOSTER_CDI = "cdi";
    public static String BOOSTER_MPCONFIG = "mpConfig";
    public static String BOOSTER_MPRESTCLIENT = "mpRestClient";
    public static String BOOSTER_OPENTRACING = "mpOpenTracing";

    protected String libertyServerPath;
    protected List<BoosterPackConfigurator> boosterPackConfigurators;
    protected BoostLoggerI logger;
    protected LibertyServerConfigGenerator serverConfig;

    public LibertyBoosterUtil(String libertyServerPath, List<BoosterDependencyInfo> dependencies, BoostLoggerI logger) {
        this.libertyServerPath = libertyServerPath;
        this.logger = logger;

        try {
            this.serverConfig = new LibertyServerConfigGenerator(libertyServerPath);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.boosterPackConfigurators = getBoosterPackConfigurators(dependencies);

    }

    /**
     * take a list of pom boost dependency strings and map to liberty features
     * for config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @return
     */
    private List<BoosterPackConfigurator> getBoosterPackConfigurators(List<BoosterDependencyInfo> dependencies) {

        List<BoosterPackConfigurator> boosterPackConfigList = new ArrayList<BoosterPackConfigurator>();

        for (BoosterDependencyInfo dep : dependencies) {
            if (dep.getArtifact().equals(BOOSTER_JDBC)) {
                JDBCBoosterPackConfigurator jdbcConfig = new JDBCBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(jdbcConfig);
            } else if (dep.getArtifact().equals(BOOSTER_JAXRS)) {
                JAXRSBoosterPackConfigurator jaxrsConfig = new JAXRSBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(jaxrsConfig);
            } else if (dep.getArtifact().equals(BOOSTER_MPHEALTH)) {
                MPHealthBoosterPackConfigurator mpHealthConfig = new MPHealthBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(mpHealthConfig);
            } else if (dep.getArtifact().equals(BOOSTER_MPCONFIG)) {
                MPConfigBoosterPackConfigurator mpConfigConfig = new MPConfigBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(mpConfigConfig);
            } else if (dep.getArtifact().equals(BOOSTER_CDI)) {
                CDIBoosterPackConfigurator CDIConfig = new CDIBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(CDIConfig);
            } else if (dep.getArtifact().equals(BOOSTER_MPRESTCLIENT)) {
                MPRestClientBoosterPackConfigurator mpRestClientConfig = new MPRestClientBoosterPackConfigurator(dep,
                        serverConfig);
                boosterPackConfigList.add(mpRestClientConfig);
            } else if (dep.getArtifact().equals(BOOSTER_JSONP)) {
                JSONPBoosterPackConfigurator jsonpConfig = new JSONPBoosterPackConfigurator(dep, serverConfig);
                boosterPackConfigList.add(jsonpConfig);
            } else if (dep.getArtifact().equals(BOOSTER_OPENTRACING)) {
                MPOpenTracingBoosterPackConfigurator mpOpenTracingConfig = new MPOpenTracingBoosterPackConfigurator(dep,
                        serverConfig);
                boosterPackConfigList.add(mpOpenTracingConfig);
            }
        }

        return boosterPackConfigList;
    }

    public void generateLibertyServerConfig(String warName) throws Exception {

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (BoosterPackConfigurator configurator : boosterPackConfigurators) {

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
