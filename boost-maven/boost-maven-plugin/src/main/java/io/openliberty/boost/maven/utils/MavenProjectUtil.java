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
    
    /**
     * Get all dependencies with "org.springframework" as the groupId. These
     * dependencies will be used to determine which additional Liberty features need
     * to be enabled.
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

}
