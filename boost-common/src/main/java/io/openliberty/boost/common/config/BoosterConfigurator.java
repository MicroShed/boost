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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.reflections.Reflections;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;

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
    public static List<AbstractBoosterConfig> getBoosterPackConfigurators(Map<String, String> dependencies,
            BoostLoggerI logger) throws BoostException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        List<AbstractBoosterConfig> boosterPackConfigList = new ArrayList<AbstractBoosterConfig>();

        Reflections reflections = new Reflections("io.openliberty.boost.common.boosters");

        Set<Class<? extends AbstractBoosterConfig>> allClasses = reflections.getSubTypesOf(AbstractBoosterConfig.class);
        for (Class<? extends AbstractBoosterConfig> boosterClass : allClasses) {
            if (dependencies.containsKey(AbstractBoosterConfig.getCoordindates(boosterClass))) {
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

    public static void generateLibertyServerConfig(String libertyServerPath,
            List<AbstractBoosterConfig> boosterPackConfigurators, List<String> warNames, BoostLoggerI logger)
            throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath, logger);

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
            serverConfig.addFeature(configurator.getFeature());
            serverConfig.addBoosterConfig(configurator);
        }

        // Add war configuration is necessary
        if (!warNames.isEmpty()) {
            for (String warName : warNames) {
                serverConfig.addApplication(warName);
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
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

    public static String getTargetRuntime(Map<String, String> dependencies, BoostLoggerI logger) {

        if (dependencies.containsKey("io.openliberty.boosters:tomee")) {
            logger.info("found tomee runtime target");
            return ConfigConstants.TOMEE_RUNTIME;
        } else {
            logger.info("did not find  tomee runtime target, defaulting to liberty");
            return ConfigConstants.LIBERTY_RUNTIME;
        }
    }

    public static void addTOMEEDependencyJarsToClasspath(String tomeeServerPath,
            List<AbstractBoosterConfig> boosterPackConfigurators, BoostLoggerI logger) {

        // first get the dependency coordinate strings for each booster

        TomEEServerConfigGenerator tomeeConfig = null;
        try {
            tomeeConfig = new TomEEServerConfigGenerator(tomeeServerPath, logger);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            tomeeConfig.addJarsDirToSharedLoader();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static List<String> getTomEEDependencyJarsToCopy(List<AbstractBoosterConfig> boosterPackConfigurators,
            BoostLoggerI logger) {

        Set<String> allTomEEDependencyJarsNoDups;
        List<String> tomeeDependencyJarsToCopy = new ArrayList<String>();

        for (AbstractBoosterConfig configurator : boosterPackConfigurators) {
            List<String> dependencyStringsToCopy = configurator.getTomEEDependency();
            for (String tomeeDependecyStr : dependencyStringsToCopy) {
                if (tomeeDependecyStr != null) {
                    logger.info(tomeeDependecyStr);
                    tomeeDependencyJarsToCopy.add(tomeeDependecyStr);
                }
            }

        }
        allTomEEDependencyJarsNoDups = new HashSet<>(tomeeDependencyJarsToCopy);
        tomeeDependencyJarsToCopy.clear();
        tomeeDependencyJarsToCopy.addAll(allTomEEDependencyJarsNoDups);
        return tomeeDependencyJarsToCopy;
    }
}
