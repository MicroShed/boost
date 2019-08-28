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
package boost.gradle

import org.gradle.api.Project

import boost.gradle.tasks.BoostStartTask
import boost.gradle.tasks.BoostRunTask
import boost.gradle.tasks.BoostStopTask
import boost.gradle.tasks.BoostPackageTask
import boost.gradle.tasks.BoostDebugTask

class BoostTaskFactory {
    Project project

    BoostTaskFactory(Project project) {
        this.project = project
    }

    void createTasks() {
        project.tasks.create('boostStart', BoostStartTask)
        project.tasks.create('boostRun', BoostRunTask)
        project.tasks.create('boostStop', BoostStopTask)
        project.tasks.create('boostPackage', BoostPackageTask)
        project.tasks.create('boostDebug', BoostDebugTask)
        
    }
}