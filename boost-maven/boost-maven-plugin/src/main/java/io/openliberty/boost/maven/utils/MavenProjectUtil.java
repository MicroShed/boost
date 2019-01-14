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
package io.openliberty.boost.maven.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import io.openliberty.boost.common.utils.LibertyBoosterUtil;

public class MavenProjectUtil {

    /**
     * Detect spring boot version dependency
     */
    public static String findSpringBootVersion(MavenProject project) {
        String version = null;

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            if ("org.springframework.boot".equals(art.getGroupId()) && "spring-boot".equals(art.getArtifactId())) {
                version = art.getVersion();
                break;
            }
        }

        return version;
    }

    public static Map<String, String> getAllDependencies(MavenProject project, BoostLogger logger) {

        Map<String, String> dependencies = new HashMap<String, String>();
        logger.debug("Processing project for dependencies.");

        for (Artifact artifact : project.getArtifacts()) {
            logger.debug("Found dependency while processing project: " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId() + ":" + artifact.getVersion());

            dependencies.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact.getVersion());
        }

        return dependencies;
    }

}
