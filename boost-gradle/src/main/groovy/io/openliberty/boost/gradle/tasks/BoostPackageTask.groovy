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

package io.openliberty.boost.gradle.tasks

import java.util.ArrayList

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.artifacts.Dependency
import org.gradle.tooling.BuildException

import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

import io.openliberty.boost.gradle.utils.BoostLogger
import io.openliberty.boost.gradle.utils.GradleProjectUtil
import io.openliberty.boost.common.utils.BoostUtil
import io.openliberty.boost.common.utils.PackageUtil
import io.openliberty.boost.common.utils.SpringBootUtil

import net.wasdev.wlp.gradle.plugins.extensions.PackageAndDumpExtension

public class BoostPackageTask extends AbstractBoostTask {

    String springBootVersion = GradleProjectUtil.findSpringBootVersion(this.project)

    String libertyServerPath = null
    
    BoostPackageTask() {
        configure({
            description 'Packages the application into an executable Liberty jar.'
            logging.level = LogLevel.INFO
            group 'Boost'

            dependsOn 'libertyCreate'

            mustRunAfter 'boostDockerBuild'

            //There are some things that this task does before we can package up the server into a JAR
            PackageAndDumpExtension boostPackage = new PackageAndDumpExtension()

            //We have to check these properties after the build.gradle file has been evaluated.
            //Some properties could get set after our plugin is applied.
            project.afterEvaluate {
                //Configuring spring plugin task dependencies
                if (isSpringProject()) {
                    if(springBootVersion.startsWith('2.')) {
                        boostPackage.archive = project.bootJar.archivePath.toString()
                        dependsOn 'bootJar'

                        //Skipping if Docker is configured
                        if (!isDockerConfigured() || isPackageConfigured()) {
                            project.bootJar.finalizedBy 'boostPackage'
                        }
                    } else if(springBootVersion.startsWith('1.')) { //Assume 1.5?
                        //No bootJar task so we have to get the archive name from the jar or war tasks
                        if (project.plugins.hasPlugin('java')) {
                            //Going to use the jar task archiveName for the boostRepackage name
                            //bootRepackage can use any jar task, might need to check that too
                            boostPackage.archive = project.jar.archivePath.toString()
                        } else if (project.plugins.hasPlugin('war')) { //Might also need a case for wars
                            boostPackage.archive = project.war.archivePath.toString()
                        } else {
                            throw new GradleException('Could not determine project artifact name.')
                        }

                        //Handle classifier
                        if (project.bootRepackage.classifier != null && !project.bootRepackage.classifier.isEmpty()) {
                            boostPackage.archive = //Adding classifier to the boost archiveName
                                boostPackage.archive.substring(0, boostPackage.archive.lastIndexOf(".")) +
                                '-' + project.bootRepackage.classifier.toString() +
                                boostPackage.archive.substring(boostPackage.archive.lastIndexOf("."))
                        }
                        dependsOn 'bootRepackage'

                        //Skipping if Docker is configured
                        if (!isDockerConfigured() || isPackageConfigured()) {
                            project.bootRepackage.finalizedBy 'boostPackage'
                        }
                    }
                } else {
                    if (project.plugins.hasPlugin('war')) {
                        boostPackage.archive = project.war.archiveName.substring(0, project.war.archiveName.lastIndexOf("."))

                        //Skipping if Docker is configured
                        if (!isDockerConfigured() || isPackageConfigured()) {
                            //Assemble works for the ear task too
                            project.war.finalizedBy 'boostPackage'
                        }
                    } //ear check here when supported
                }
                //Configuring liberty plugin task dependencies and parameters
                //installFeature should check the server.xml in the server directory and install the missing feature
                project.tasks.getByName('libertyPackage').dependsOn 'installApps', 'installFeature'
                project.tasks.getByName('installApps').mustRunAfter 'installFeature'
                finalizedBy 'libertyPackage'
                boostPackage.include = "runnable, minify"
            }

            //The task will perform this before any other task actions
            doFirst {

                libertyServerPath = "${project.buildDir}/wlp/usr/servers/${project.liberty.server.name}"
                
                if (isPackageConfigured() && project.boost.packaging.packageName != null && !project.boost.packaging.packageName.isEmpty()) {
                    boostPackage.archive = "${project.buildDir}/libs/${project.boost.packaging.packageName}"
                }
                
                project.liberty.server.packageLiberty = boostPackage

                if (isSpringProject()) {
                    File springUberJar
                    if(springBootVersion.startsWith('2.')) {
                        springUberJar = project.bootJar.archivePath
                    } else if(springBootVersion.startsWith('1.')) {
                        if (project.plugins.hasPlugin('java')) {
                            springUberJar = project.jar.archivePath
                        } else if (project.plugins.hasPlugin('war')) {
                            springUberJar = project.war.archivePath
                        }
                    }
                    if (!project.configurations.archives.allArtifacts.isEmpty()) {
                        String springResourceDir = "${project.buildDir}/resources/main"
                        File projectArtifact = project.configurations.archives.allArtifacts[0].getFile()

                        SpringBootUtil.copySpringBootUberJar(springUberJar, projectArtifact, shouldReplaceProjectArchive(), BoostLogger.getInstance())
                        SpringBootUtil.generateLibertyServerConfig(springResourceDir, libertyServerPath, springBootVersion, getSpringBootDependencies(), BoostLogger.getInstance())
                    } else {
                        throw new GradleException('Could not determine the project artifact for the Spring Boot project.')
                    }
                        
                } else if (project.plugins.hasPlugin('war')) {
                    PackageUtil.generateServerXMLJ2EE(getBoosterConfigsFromDependencies(), libertyServerPath, project.war.baseName, project.war.version, 'war')
                } else {
                    throw new GradleException('Could not package the project with boostPackage. The boostPackage task must be used with a SpringBoot or Java EE project.')
                }

                logger.info('Packaging the application.')
            }
        })
    }   

    public boolean isSpringProject() {
        return springBootVersion != null && !springBootVersion.isEmpty()
    }

    protected List<String> getSpringBootDependencies() {

        List<String> springBootDependencies = new ArrayList<String>()

        project.configurations.compile.resolvedConfiguration.resolvedArtifacts.each { art ->
            if ("${art.name}".contains("spring")) {
                springBootDependencies.add(art.name)
            }
        }
       
        return springBootDependencies
    }

    protected List<String> getBoosterConfigsFromDependencies() {
        List<String> dependencyList = new ArrayList<String>()

        def componentIds = project.configurations.compile.incoming.resolutionResult.allDependencies.collect { it.selected.id }
        def result = project.dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(MavenModule, MavenPomArtifact)
            .execute()

        for (component in result.resolvedComponents) {
            logger.debug("Adding ${component.id} to dependency list.")
            component.getArtifacts(MavenPomArtifact).each { dependencyList.add(component.id.toString()) }
        }

        return dependencyList
    }

    //returns true if bootJar is using the same archiveName as jar
    protected boolean shouldReplaceProjectArchive() {
        if (project.plugins.hasPlugin('java')) {
            if (springBootVersion.startsWith('2.')) {
                return project.jar.archiveName == project.bootJar.archiveName
            }
        }
        return false
    }
}
