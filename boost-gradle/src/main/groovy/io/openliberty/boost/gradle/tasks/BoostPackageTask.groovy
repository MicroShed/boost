/*******************************************************************************
 * Copyright (c) 2018,2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package io.openliberty.boost.gradle.tasks

import org.codehaus.groovy.GroovyException

import java.util.ArrayList
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.IOException

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.artifacts.Dependency
import org.gradle.tooling.BuildException

import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.tasks.Copy

import io.openliberty.boost.gradle.utils.BoostLogger
import io.openliberty.boost.gradle.utils.GradleProjectUtil
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.config.BoosterConfigurator
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.utils.BoostUtil
import io.openliberty.boost.common.utils.SpringBootUtil
import org.gradle.api.Task

import net.wasdev.wlp.gradle.plugins.extensions.PackageAndDumpExtension

public class BoostPackageTask extends AbstractBoostTask {

    List<AbstractBoosterConfig> boosterPackConfigurators

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
                    Task springBootTask = findSpringBootTask(springBootVersion)

                    boostPackage.archive = getSpringBootArchivePath(springBootTask)

                    if (springBootVersion.startsWith('2.')) {
                        if (project.plugins.hasPlugin('war')) {
                            dependsOn 'bootWar'
                        } else if (project.plugins.hasPlugin('java')) {
                            dependsOn 'bootJar'
                        }
                    } else if (springBootVersion.startsWith('1.')){
                        dependsOn 'bootRepackage'
                    }

                    //Skipping if Docker is configured
                    if (!isDockerConfigured() || isPackageConfigured()) {
                        springBootTask.finalizedBy 'boostPackage'
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
                boostPackage.include = "runnable, minify"
                 if (!project.plugins.hasPlugin('war')) {
                    finalizedBy 'libertyPackage'
                }
            }

            //The task will perform this before any other task actions
            doFirst {

                libertyServerPath = "${project.buildDir}/wlp/usr/servers/${project.liberty.server.name}"
                if (isPackageConfigured()) {
                    if(project.boost.packaging.packageName != null && !project.boost.packaging.packageName.isEmpty()) {
                        boostPackage.archive = "${project.buildDir}/libs/${project.boost.packaging.packageName}"
                    }
                }

                project.liberty.server.packageLiberty = boostPackage

                if (isSpringProject()) {
                    Task springBootTask = findSpringBootTask(springBootVersion)
                    File springBootUberJar = new File(getSpringBootArchivePath(springBootTask))

                    if (springBootUberJar != null && !springBootUberJar.exists()) {
                        throw new GradleException(springBootUberJar.getAbsolutePath() + " Spring Boot Uber JAR does not exist");
                    }

                    validateSpringBootUberJAR(springBootUberJar)
                    copySpringBootUberJar(springBootUberJar)
                    generateServerConfigSpringBoot()

                } else if (project.plugins.hasPlugin('war') || !project.configurations.boostApp.isEmpty()) {
                    // Get booster dependencies from project
                    Map<String, String> dependencies = GradleProjectUtil.getAllDependencies(project, BoostLogger.getInstance())
                    
                    // Determine the Java compiler target version and set this internally 
                    System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, project.findProperty("targetCompatibility").toString())
            
                    boosterPackConfigurators = BoosterConfigurator.getBoosterPackConfigurators(dependencies, BoostLogger.getInstance())

                    copyBoosterDependencies()

                    generateServerConfigEE()

                } else {
                    throw new GradleException('Could not package the project with boostPackage. The boostPackage task must be used with a SpringBoot or Java EE project.')
                }

                logger.info('Packaging the applicaiton.')
            }
        })
    }

    public boolean isSpringProject() {
        return springBootVersion != null && !springBootVersion.isEmpty()
    }

    public Task findSpringBootTask(String springBootVersion) {
        Task task

        if (springBootVersion == null) {
            throw new GradleException("Spring Boot version cannot be null")
        }

        //Do not change the order of war and java
        if (springBootVersion.startsWith('2.')) {
            if (project.plugins.hasPlugin('war')) {
                task = project.bootWar
            } else if (project.plugins.hasPlugin('java')) {
                task = project.bootJar
            }
        } else if (springBootVersion.startsWith('1.')) {
            if (project.plugins.hasPlugin('war')) {
                task = project.war
            } else if (project.plugins.hasPlugin('java')) {
                task = project.jar
            }
        }
        return task
    }

    public String getSpringBootArchivePath(Task springBootTask) {
        String archiveOutputPath;

        if (springBootVersion.startsWith('2.')) {
            archiveOutputPath = springBootTask.archivePath.getAbsolutePath()
        }
        else if(springBootVersion.startsWith('1.')) {
            archiveOutputPath = springBootTask.archivePath.getAbsolutePath()
            if (project.bootRepackage.classifier != null && !project.bootRepackage.classifier.isEmpty()) {
                archiveOutputPath = archiveOutputPath.substring(0, archiveOutputPath.lastIndexOf(".")) + "-" + project.bootRepackage.classifier + "." + springBootTask.extension
            }
        }
        return archiveOutputPath
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

    protected void generateServerConfigEE() throws GradleException {
        List<String> warNames = new ArrayList<String>()

        if (project.plugins.hasPlugin('war')) {

            if (project.war.version == null) {
                warNames.add(project.war.baseName)
            } else {
                warNames.add(project.war.baseName + "-" + project.war.version)
            }
        } else {
            warNames = getWarNameFromBoostApps()
        }

        try {

            BoosterConfigurator.generateLibertyServerConfig(libertyServerPath, boosterPackConfigurators, warNames, BoostLogger.getInstance());

        } catch (Exception e) {
            throw new GradleException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    protected void generateServerConfigSpringBoot() throws GradleException {

        try {
            // Get Spring Boot starters from Maven project
            Map<String, String> dependencies = GradleProjectUtil.getAllDependencies(project, BoostLogger.getInstance());

            // Generate server config
            SpringBootUtil.generateLibertyServerConfig("${project.buildDir}/resources/main", libertyServerPath,
                    springBootVersion, dependencies, BoostLogger.getInstance());

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

    protected void copyBoosterDependencies() {

        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterPackConfigurators, BoostLogger.getInstance());

        def boosterConfig = project.getConfigurations().create('boosterDependency')

        dependenciesToCopy.each { dep ->

            project.getDependencies().add(boosterConfig.name, dep)

        }

        project.copy {
            from project.configurations.boosterDependency
            into "${project.buildDir}/wlp/usr/servers/BoostServer/resources"
            include '*.jar'
        }
    }

    //Runs through the dependencies in the boostApp configuration and pulls out the first war name.
    protected List<String> getWarNameFromBoostApps() {
        List<String> warNames = new ArrayList<String>()
        for (def dep : project.configurations.boostApp) {
            if (FilenameUtils.getExtension(dep.name).equals('war')) {
                warNames.add(dep.name.substring(0, dep.name.length() - 4))
            }
        }
        return warNames
    }
}
