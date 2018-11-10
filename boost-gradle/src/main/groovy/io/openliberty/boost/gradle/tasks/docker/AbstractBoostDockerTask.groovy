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
package io.openliberty.boost.gradle.tasks.docker

import com.spotify.docker.client.auth.RegistryAuthSupplier
import com.spotify.docker.client.auth.ConfigFileRegistryAuthSupplier

import io.openliberty.boost.gradle.tasks.AbstractBoostTask
import io.openliberty.boost.gradle.utils.BoostLogger
import io.openliberty.boost.common.BoostException
import io.openliberty.boost.common.docker.AbstractDockerI

import java.io.File

import org.gradle.api.Project
import org.gradle.api.GradleException

public abstract class AbstractBoostDockerTask extends AbstractBoostTask implements AbstractDockerI {

    void doExecute(String artifactId) throws GradleException {
        try {
            if(isValidDockerConfig(BoostLogger.getInstance(), project.boost.docker.dockerRepo + artifactId, project.boost.docker.tag, artifactId)) {
                execute(getDockerClient(project.boost.docker.useProxy));
            }
        } catch (BoostException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    @Override
    public RegistryAuthSupplier createRegistryAuthSupplier() throws BoostException {
        return new ConfigFileRegistryAuthSupplier()
    }
    
    public String getArtifactId(Project project, String springBootVersion) throws GradleException {
        File appFile
        if (springBootVersion != null) {
            if (project.plugins.hasPlugin('java')) {
                if (springBootVersion.startsWith("2.")) {
                    appFile = project.bootJar.archivePath
                } else if (springBootVersion.startsWith("1.")){
                    appFile = project.jar.archivePath
                    //Checking for classifier in bootRepackage and adding to archiveName
                    if (project.bootRepackage.classifier != null && !project.bootRepackage.classifier.isEmpty()) {
                        String appArchiveName = //Adding classifier to the appArchive name
                            appFile.getName().substring(0, appFile.getName().lastIndexOf(".")) +
                            '-' + 
                            project.bootRepackage.classifier.toString() + 
                            appFile.getName().substring(appFile.getName().lastIndexOf("."))
                        appFile = new File(appFile.getParent(), appArchiveName)
                    }
                } 
            } else {
                throw new GradleException ('Unable to determine the project artifact name to add to the container. Please use the java plugin.')
            }
            
            //Getting image name from boost docker extension if it is set, otherwise we use the file name w/o extension
            if (isDockerConfigured() && project.boost.docker.imageName != null && !project.boost.docker.imageName.isEmpty()) {
                return project.boost.docker.imageName
            } else {
                return appFile.getName().substring(0, appFile.getName().lastIndexOf("."))
            }
        }  
    }    
}