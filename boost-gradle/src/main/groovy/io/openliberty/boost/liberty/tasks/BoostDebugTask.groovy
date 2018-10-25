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
package io.openliberty.boost.liberty.tasks

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel

public class BoostDebugTask extends AbstractBoostTask {

    BoostDebugTask() {
        configure({
            description 'Runs the Boost application in debug mode.'
            logging.level = LogLevel.INFO
            group 'Boost'

            finalizedBy 'libertyDebug'

            doFirst {
                logger.info('Running the application in debug mode.')
            }
        })
    }   
}