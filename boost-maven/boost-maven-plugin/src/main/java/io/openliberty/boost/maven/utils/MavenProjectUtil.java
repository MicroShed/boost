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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import io.openliberty.boost.common.config.BoosterDependencyInfo;

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

    /**
     * Get all dependencies with "org.springframework" as the groupId. These
     * dependencies will be used to determine which additional Liberty features
     * need to be enabled.
     * 
     */
    public static List<String> getSpringFrameworkDependencies(MavenProject project) {

        List<String> springFrameworkDependencies = new ArrayList<String>();

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            String groupId = art.getGroupId();
            if (groupId.equals("org.springframework")) {
                springFrameworkDependencies.add(art.getArtifactId());
            }
        }

        return springFrameworkDependencies;
    }

    public static List<BoosterDependencyInfo> getBoosterDependencies(MavenProject project, BoostLogger logger) {

        List<BoosterDependencyInfo> boosterDependencies = new ArrayList<BoosterDependencyInfo>();
        logger.debug("Processing project for dependencies.");

        for (Artifact artifact : project.getArtifacts()) {
            logger.debug("Found dependency while processing project: " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId() + ":" + artifact.getVersion());

            if (artifact.getGroupId().equals("io.openliberty.boosters")) {
                BoosterDependencyInfo currBooster = new BoosterDependencyInfo(artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getVersion());
                boosterDependencies.add(currBooster);
            }
        }

        return dependencies;
    }

}
