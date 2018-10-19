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
package io.openliberty.boost.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

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

    public static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }
    
    /**
     * Get all dependencies with "spring-boot-starter-*" as the artifactId. These
     * dependencies will be used to determine which additional Liberty features need
     * to be enabled.
     * 
     */
    public static List<String> getSpringBootStarters(MavenProject project) {

        List<String> springBootStarters = new ArrayList<String>();

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            if (art.getArtifactId().contains("spring-boot-starter")) {
                springBootStarters.add(art.getArtifactId());
            }
        }

        return springBootStarters;
    }

}
