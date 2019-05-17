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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.reflections.Reflections;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.runtimes.RuntimeI;

public class BoosterConfigurator {

    /**
     * take a list of pom boost dependency strings and map to liberty features for
     * config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @param logger
     * @return
     * @throws BoostException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static List<AbstractBoosterConfig> getBoosterConfigs(Map<String, String> dependencies,
            BoostLoggerI logger) throws BoostException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        List<AbstractBoosterConfig> boosterPackConfigList = new ArrayList<AbstractBoosterConfig>();

        Reflections reflections = new Reflections("io.openliberty.boost.common.boosters");

        Set<Class<? extends AbstractBoosterConfig>> allClasses = reflections.getSubTypesOf(AbstractBoosterConfig.class);
        for (Class<? extends AbstractBoosterConfig> boosterClass : allClasses) {
            if (dependencies.containsKey(AbstractBoosterConfig.getCoordinates(boosterClass))) {
                Constructor<?> cons = boosterClass.getConstructor(Map.class, BoostLoggerI.class);
                Object o = cons.newInstance(dependencies, logger);
                if (o instanceof AbstractBoosterConfig) {
                    boosterPackConfigList.add((AbstractBoosterConfig) o);
                } else {
                    throw new BoostException(
                            "Found a class in io.openliberty.boost.common.boosters that did not extend AbstractBoosterConfig. This should never happen.");
                }
            }
        }

        return boosterPackConfigList;
    }
    
    // /**
    //  * Configure the TomEE runtime
    //  * 
    //  * @param tomeeConfigPath
    //  * @param boosterPackConfigurators
    //  * @param logger
    //  * @throws Exception
    //  */
    // public static void configureTomeeServer(String tomeeConfigPath,
    //         List<AbstractBoosterConfig> boosterPackConfigurators, BoostLoggerI logger) throws Exception {

    //     TomEEServerConfigGenerator tomeeConfig = new TomEEServerConfigGenerator(tomeeConfigPath, logger);
    //     tomeeConfig.addJarsDirToSharedLoader();
        
    //     // Configure HTTP endpoint
    //     Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
        
    //     String hostname = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "localhost");
    //     tomeeConfig.addHostname(hostname);
        
    //     String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "8080");
    //     tomeeConfig.addHttpPort(httpPort);

    //     // Loop through configuration objects and add config
    //     for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
    //     	configurator.addServerConfig(tomeeConfig);
    //     }
    // }

    public static List<String> getDependenciesToCopy(List<AbstractBoosterConfig> boosterPackConfigurators,
            RuntimeI runtime, BoostLoggerI logger) {

        Set<String> allDependencyJarsNoDups;
        List<String> dependencyJarsToCopy = new ArrayList<String>();

        for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
            List<String> dependencyStringsToCopy = configurator.getDependencies(runtime);
            for (String depStr : dependencyStringsToCopy) {
                if (depStr != null) {
                    logger.info(depStr);
                    dependencyJarsToCopy.add(depStr);
                }
            }
        }
        
        allDependencyJarsNoDups = new HashSet<>(dependencyJarsToCopy);
        dependencyJarsToCopy.clear();
        dependencyJarsToCopy.addAll(allDependencyJarsNoDups);
        return dependencyJarsToCopy;
    }
}
