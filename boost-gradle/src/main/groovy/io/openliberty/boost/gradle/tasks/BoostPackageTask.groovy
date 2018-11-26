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
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.IOException

import org.apache.commons.io.FileUtils

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.artifacts.Dependency
import org.gradle.tooling.BuildException

import io.openliberty.boost.gradle.utils.BoostLogger
import io.openliberty.boost.gradle.utils.GradleProjectUtil
import io.openliberty.boost.common.utils.BoostUtil
import io.openliberty.boost.common.utils.LibertyServerConfigGenerator
import io.openliberty.boost.common.utils.SpringBootUtil

import net.wasdev.wlp.gradle.plugins.extensions.PackageAndDumpExtension

public class BoostPackageTask extends AbstractBoostTask {

    String springBootVersion = GradleProjectUtil.findSpringBootVersion(this.project)

    String libertyServerPath = null; 
    
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

                libertyServerPath = "${project.buildDir}/wlp/usr/servers/BoostServer"
                
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
                    validateSpringBootUberJAR(springUberJar)
                    copySpringBootUberJar(springUberJar)
                    generateServerConfigSpringBoot()
                    
                } else { //JavaEE projects?
                    throw new GradleException('Could not package the project with boostPackage. The boostPackage task must be used with a SpringBoot project.')
                }

                logger.info('Packaging the applicaiton.')
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

    public String getClassifier() {
        String classifier = null

        if (isSpringProject()) {
            if (springBootVersion.startsWith('2.')) {
                classifier = project.tasks.getByName('bootJar').classifier
            } else if (springBootVersion.startsWith('1.')) {
                classifier = project.tasks.getByName('bootRepackage').classifier
            }
        }

        return classifier
    }

    protected void generateServerConfigSpringBoot() throws GradleException {
    
       try {
            // Get Spring Boot starters from Maven project
            List<String> springBootDependencies = getSpringBootDependencies();

            // Generate server config
            SpringBootUtil.generateLibertyServerConfig("${project.buildDir}/resources/main", libertyServerPath,
                    springBootVersion, springBootDependencies, BoostLogger.getInstance());

        } catch (Exception e) {
            throw new GradleException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    protected void copySpringBootUberJar(File springBootUberJar) throws GradleException {
        try {
            File springBootUberJarCopy = null
            if (springBootUberJar != null) { // Only copy the Uber JAR if it is valid
                springBootUberJarCopy = SpringBootUtil.copySpringBootUberJar(springBootUberJar,
                        BoostLogger.getInstance())
            }

            if (springBootUberJarCopy == null) {
                logger.info('Plugin should replace the project archive: ' + shouldReplaceProjectArchive())
                if (shouldReplaceProjectArchive()) {
                    if (!project.configurations.archives.allArtifacts.isEmpty()) {
                        File springJar = new File(
                            SpringBootUtil.getBoostedSpringBootUberJarPath(project.configurations.archives.allArtifacts[0].getFile()))
                        if (net.wasdev.wlp.common.plugins.util.SpringBootUtil.isSpringBootUberJar(springJar)) {
                            logger.info("Copying back Spring Boot Uber JAR as project artifact.")
                            FileUtils.copyFile(springJar, project.configurations.archives.allArtifacts[0].getFile())
                        }
                    }
                }
            } else {
                logger.info("Copied Spring Boot Uber JAR to " + springBootUberJarCopy.getCanonicalPath())
            }
        } catch (BuildException | IOException e) {
            throw new GradleException(e.getMessage(), e)
        }
        
    }

    protected void validateSpringBootUberJAR(File springBootUberJar) throws GradleException {
        if (!project.configurations.archives.allArtifacts.isEmpty()) {
            if (!BoostUtil.isLibertyJar(project.configurations.archives.allArtifacts[0].getFile(), BoostLogger.getInstance())
                && springBootUberJar == null) {
            throw new GradleException (
                    "A valid Spring Boot Uber JAR was not found. Run the 'bootJar' task and try again.")
            }
        }
    }

    //returns true if bootJar is using the same archiveName as jar
    private boolean shouldReplaceProjectArchive() {
        if (project.plugins.hasPlugin('java')) {
            if (springBootVersion.startsWith('2.')) {
                return project.jar.archiveName == project.bootJar.archiveName
            }
        }
        return false
    }
}
