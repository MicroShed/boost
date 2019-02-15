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
package io.openliberty.boost.gradle

import org.gradle.api.*
import net.wasdev.wlp.gradle.plugins.extensions.ServerExtension

import io.openliberty.boost.gradle.extensions.BoostExtension
import io.openliberty.boost.gradle.utils.BoostLogger

public class Boost implements Plugin<Project> {

    final String BOOST_SERVER_NAME = 'BoostServer'

    final String OPEN_LIBERTY_VERSION = '[18.0.0.3,)'

    void apply(Project project) {
        project.extensions.create('boost', BoostExtension)
        project.configurations.create('boostApp')

        BoostLogger.init(project)

        new BoostTaskFactory(project).createTasks()

        project.pluginManager.apply('net.wasdev.wlp.gradle.plugins.Liberty')
        project.configurations.libertyApp.extendsFrom(project.configurations.boostApp)

        project.liberty.server = configureBoostServerProperties()
        configureRuntimeArtifact(project)
    }

    //Overwritten by any liberty configuration in build file
    ServerExtension configureBoostServerProperties() {
        ServerExtension boostServer = new ServerExtension()
        boostServer.name = BOOST_SERVER_NAME
        boostServer.looseApplication = false
        return boostServer
    }

    void configureRuntimeArtifact(Project project) {
        //The libertyRuntime configuration won't be null. It is added with the Liberty plugin.
        //A libertyRuntime configuration in the build.gradle will overwrite this.
        project.dependencies.add('libertyRuntime', "io.openliberty:openliberty-runtime:$OPEN_LIBERTY_VERSION")
    }
}
