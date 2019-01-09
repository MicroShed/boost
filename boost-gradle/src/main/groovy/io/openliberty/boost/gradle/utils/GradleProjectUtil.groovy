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
import org.gradle.api.artifacts.Dependency
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier

import groovy.lang.MissingPropertyException

import io.openliberty.boost.common.config.BoosterDependencyInfo

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
    
    public static List<String> getSpringFrameworkDependencies(Project project) {

        List<String> springBootDependencies = new ArrayList<String>()

        project.configurations.compile.resolvedConfiguration.resolvedArtifacts.collect { it.moduleVersion.id }.each { ModuleVersionIdentifier id ->
        
            if ( id.group.equals("org.springframework") ) {
                
                springBootDependencies.add(id.name.toString())
            }
        }
       
        return springBootDependencies
    }
    
    public static List<BoosterDependencyInfo> getBoosterDependencies(Project project) {
		List<BoosterDependencyInfo> boosterDependencies = new ArrayList<BoosterDependencyInfo>()
        //Map<String, String> boosterDependencies = new HashMap<String, String>()

        project.configurations.compile.resolvedConfiguration.resolvedArtifacts.collect { it.moduleVersion.id }.each { ModuleVersionIdentifier id ->
        
            if ( id.group.equals("io.openliberty.boosters") ) {
                BoosterDependencyInfo currBooster = new BoosterDependencyInfo(id.group.toString(), id.name.toString(), id.version.toString());
				boosterDependencies.add(currBooster);
                //boosterDependencies.put(id.name.toString(), id.version.toString())
            }
        }

        return boosterDependencies
    }
}