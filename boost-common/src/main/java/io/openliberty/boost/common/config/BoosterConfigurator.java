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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.boosters.liberty.AbstractBoosterLibertyConfig;
import io.openliberty.boost.common.boosters.wildfly.AbstractBoosterWildflyConfig;

public class BoosterConfigurator {

    /**
     * take a list of pom boost dependency strings and map to liberty features
     * for config. return a list of feature configuration objects for each found
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
    public static List<AbstractBoosterLibertyConfig> getBoosterLibertyConfigurators(Map<String, String> dependencies,
            BoostLoggerI logger) throws BoostException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        List<AbstractBoosterLibertyConfig> boosterConfigList = new ArrayList<AbstractBoosterLibertyConfig>();

        Reflections reflections = new Reflections("io.openliberty.boost.common.boosters.liberty");

        Set<Class<? extends AbstractBoosterLibertyConfig>> allClasses = reflections
                .getSubTypesOf(AbstractBoosterLibertyConfig.class);
        for (Class<? extends AbstractBoosterLibertyConfig> boosterClass : allClasses) {
            if (dependencies.containsKey(AbstractBoosterLibertyConfig.getCoordindates(boosterClass))) {
                Constructor<?> cons = boosterClass.getConstructor(Map.class, BoostLoggerI.class);
                Object o = cons.newInstance(dependencies, logger);
                if (o instanceof AbstractBoosterLibertyConfig) {
                    boosterConfigList.add((AbstractBoosterLibertyConfig) o);
                } else {
                    throw new BoostException(
                            "Found a class in io.openliberty.boost.common.boosters that did not extend AbstractBoosterLibertyConfig. This should never happen.");
                }
            }
        }

        return boosterConfigList;
    }

    public static List<AbstractBoosterWildflyConfig> getBoosterWildflyConfigurators(Map<String, String> dependencies,
            BoostLoggerI logger) throws BoostException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        List<AbstractBoosterWildflyConfig> boosterConfigList = new ArrayList<AbstractBoosterWildflyConfig>();

        Reflections reflections = new Reflections("io.openliberty.boost.common.boosters.wildfly");

        Set<Class<? extends AbstractBoosterWildflyConfig>> allClasses = reflections
                .getSubTypesOf(AbstractBoosterWildflyConfig.class);
        for (Class<? extends AbstractBoosterWildflyConfig> boosterClass : allClasses) {
            if (dependencies.containsKey(AbstractBoosterWildflyConfig.getCoordindates(boosterClass))) {
                Constructor<?> cons = boosterClass.getConstructor(Map.class, BoostLoggerI.class);
                Object o = cons.newInstance(dependencies, logger);
                if (o instanceof AbstractBoosterWildflyConfig) {
                    boosterConfigList.add((AbstractBoosterWildflyConfig) o);
                } else {
                    throw new BoostException(
                            "Found a class in io.openliberty.boost.common.boosters that did not extend AbstractBoosterWildflyConfig. This should never happen.");
                }
            }
        }

        return boosterConfigList;
    }

    public static void generateLibertyServerConfig(String libertyServerPath,
            List<AbstractBoosterLibertyConfig> boosterPackConfigurators, List<String> warNames, BoostLoggerI logger)
            throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath, logger);

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (AbstractBoosterLibertyConfig configurator : boosterPackConfigurators) {
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
    
    public static void generateWildflyServerConfig(String wildflyInstallPath,
            List<AbstractBoosterWildflyConfig> boosterPackConfigurators, List<Path> warFiles, BoostLoggerI logger)
            throws Exception {

    	WildflyServerConfigGenerator serverConfig = new WildflyServerConfigGenerator(wildflyInstallPath, logger);

        // Loop through configuration objects and get features and XML config
        // (if any)
        for (AbstractBoosterWildflyConfig configurator : boosterPackConfigurators) {
            serverConfig.addBoosterConfig(configurator);
        }

        // Add war configuration is necessary
        if (!warFiles.isEmpty()) {
            for (Path warFile : warFiles) {
            	serverConfig.addApplication(warFile.toString());
                
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
        }

    }

    public static <T extends AbstractBoosterConfig> List<String> getDependenciesToCopy(List<T> boosterConfigurators,
            BoostLoggerI logger) {

        List<String> dependenciesToCopy = new ArrayList<String>();

        for (AbstractBoosterConfig configurator : boosterConfigurators) {
            String dependencyToCopy = configurator.getDependency();

            if (dependencyToCopy != null) {
                logger.info(dependencyToCopy);
                dependenciesToCopy.add(dependencyToCopy);
            }
        }

        return dependenciesToCopy;
    }

}
