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
package io.openliberty.boost.gradle.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

import groovy.lang.MissingPropertyException

public class GradleProjectUtil {

    /**
     * Detect spring boot version dependency
     */
    public static String findSpringBootVersion(Project project) {
        String version = null

        try {
            for (Dependency dep : project.buildscript.configurations.classpath.getAllDependencies().toArray()) {
                if ("org.springframework.boot".equals(dep.getGroup()) && "spring-boot-gradle-plugin".equals(dep.getName())) {
                    version = dep.getVersion()
                    break
                }
            }
        } catch (MissingPropertyException e) {
            project.getLogger().warn('No buildscript configuration found.')
            return version
        }

        return version        
    }
    
    public static Map<String, String> getAllDependencies(Project project, BoostLogger logger) {
        Map<String, String> dependencies = new HashMap<String, String>()
		logger.debug("Processing project for dependencies.")

        try {
            //Projects without the war/java plugin won't have this configuration
            //compileClasspath is not a regular Configuration object so we have to go through without using getAllDependenciesFromConfiguration()
            project.configurations.getByName('compileClasspath').resolvedConfiguration.resolvedArtifacts.collect { it.moduleVersion.id }.each { ModuleVersionIdentifier id ->
                logger.debug("Found dependency while processing project: " + id.group.toString() + ":"
                        + id.name.toString() + ":" + id.version.toString())
                        
                dependencies.put(id.group.toString() + ":" + id.name.toString(), id.version.toString())
            }
        } catch (UnknownConfigurationException ue) {
            logger.debug("The compileClasspath configuration was not found.")
        }

        //Will always have the boostApp configuration since we create it in apply()
        //Just pulling in the transitive booster dependencies for the apps
        dependencies.putAll(getAllBoosterDependenciesFromConfiguration(project.configurations.boostApp, logger))

        return dependencies
    }

    private static Map<String, String> getAllBoosterDependenciesFromConfiguration(Configuration configuration, BoostLogger logger) {
        Map<String, String> dependencies = new HashMap<String, String>()
        configuration.resolvedConfiguration.resolvedArtifacts.collect { it.moduleVersion.id }.each { ModuleVersionIdentifier id ->
        	logger.debug("Found dependency while processing project: " + id.group.toString() + ":"
                    + id.name.toString() + ":" + id.version.toString())
                    
            if (id.group.toString().equals('io.openliberty.boosters')) {
                dependencies.put(id.group.toString() + ":" + id.name.toString(), id.version.toString())
            }
        }
        return dependencies
    }
}