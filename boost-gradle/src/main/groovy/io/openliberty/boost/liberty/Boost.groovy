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
package io.openliberty.boost.liberty

import org.gradle.api.*
import net.wasdev.wlp.gradle.plugins.extensions.ServerExtension

import io.openliberty.boost.liberty.extensions.BoostExtension

public class Boost implements Plugin<Project> {

    final String BOOST_SERVER_NAME = 'BoostServer'

    void apply(Project project) {
        project.extensions.create('boost', BoostExtension)

        new BoostTaskFactory(project).createTasks()

        project.pluginManager.apply('net.wasdev.wlp.gradle.plugins.Liberty')
        project.pluginManager.apply('com.palantir.docker')

        project.liberty.server = configureBoostServerProperties()
    }

    //Overwritten by any liberty configuration in build file
    ServerExtension configureBoostServerProperties() {
        ServerExtension boostServer = new ServerExtension()
        boostServer.name = BOOST_SERVER_NAME
        boostServer.looseApplication = false
        return boostServer
    }
}