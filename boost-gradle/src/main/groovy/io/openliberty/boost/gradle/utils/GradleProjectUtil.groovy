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
package boost.gradle.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier

import groovy.lang.MissingPropertyException

public class GradleProjectUtil {
    
    public static Map<String, String> getAllDependencies(Project project, BoostLogger logger) {
        Map<String, String> dependencies = new HashMap<String, String>()
		logger.debug("Processing project for dependencies.")

        project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect { it.moduleVersion.id }.each { ModuleVersionIdentifier id ->
        	logger.debug("Found dependency while processing project: " + id.group.toString() + ":"
                    + id.name.toString() + ":" + id.version.toString())
                    
            dependencies.put(id.group.toString() + ":" + id.name.toString(), id.version.toString())
        }

        return dependencies
    }
}