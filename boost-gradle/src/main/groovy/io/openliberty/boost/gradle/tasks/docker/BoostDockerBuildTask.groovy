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

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel

import com.spotify.docker.client.DockerClient

import io.openliberty.boost.common.BoostException
import io.openliberty.boost.common.docker.DockerBuildI
import io.openliberty.boost.gradle.utils.GradleProjectUtil
import io.openliberty.boost.gradle.utils.BoostLogger

import net.wasdev.wlp.common.plugins.util.SpringBootUtil

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.charset.Charset
import java.util.ArrayList
import java.io.File

public class BoostDockerBuildTask extends AbstractBoostDockerTask implements DockerBuildI {

    String springBootVersion
    String appName
    File appFile

    BoostDockerBuildTask() {
        configure({
            description 'Dockerizes a Boost project.'
            logging.level = LogLevel.INFO
            group 'Boost'

            project.afterEvaluate {
                springBootVersion = GradleProjectUtil.findSpringBootVersion(project)
                if (springBootVersion != null) {
                    if (project.plugins.hasPlugin('java')) {
                        if (springBootVersion.startsWith("2.")) {
                            dependsOn 'bootJar'
                            appFile = project.bootJar.archivePath
                            if (isDockerConfigured()) { //We won't add the project to the build lifecycle if Docker isn't configured
                                project.bootJar.finalizedBy 'boostDockerBuild'
                            }
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
                            
                            dependsOn 'bootRepackage'

                            if (isDockerConfigured()) {
                                project.bootRepackage.finalizedBy 'boostDockerBuild'
                            }
                        } 
                    } else {
                        throw new GradleException ('Unable to determine the project artifact name to add to the container. Please use the java plugin.')
                    }

                    //Getting image name from boost docker extension if it is set, otherwise we use the file name w/o extension
                    if (isDockerConfigured() && project.boost.docker.imageName != null && !project.boost.docker.imageName.isEmpty()) {
                        logger.info ("Setting image name to: ${project.boost.docker.imageName}")
                        appName = project.boost.docker.imageName
                    } else {
                        appName = appFile.getName().substring(0, appFile.getName().lastIndexOf("."))
                        logger.info ("Setting image name to: ${appName}")
                    }
                }
            }
            
            doFirst {
                if (appFile == null) { //if we didn't set the appName during configuration we can get it from the project, will be set for springboot projects
                    if (!project.configurations.archives.allArtifacts.isEmpty()) {
                        appFile = project.configurations.archives.allArtifacts[0].getFile()
                        appName = appFile.getName().substring(0, appFile.getName().lastIndexOf("."))
                    } else {
                        throw new GradleException ('Unable to determine the project artifact name.')
                    }
                }
                doExecute(appName)
            }
        })
    }
    
    @Override
    public void execute(DockerClient dockerClient) throws BoostException {
        File projectDirectory = project.projectDir
        File outputDirectory = new File(project.buildDir.getAbsolutePath(), 'libs')
        
        dockerBuild(project.boost.docker.dockerizer, dockerClient, projectDirectory, outputDirectory, springBootVersion, project.boost.docker.pullNewerImage,
                project.boost.docker.noCache, project.boost.docker.buildArgs, project.boost.docker.dockerRepo + appName, project.boost.docker.tag, BoostLogger.getInstance())
    }

    /**
     * Find the location of the Spring Boot Uber JAR
     * 
     * @throws BoostException
     */
    @Override
    public File getAppArchive() throws BoostException {
        return appFile
    }
}