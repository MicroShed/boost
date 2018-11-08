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

import org.gradle.api.logging.LogLevel

public class BoostDockerPushTask extends AbstractBoostTask {

    BoostDockerPushTask() {
        configure({
            description 'Dockerizes a Boost project.'
            logging.level = LogLevel.INFO
            group 'Boost'

            dependsOn 'boostDocker'

            project.afterEvaluate {
                if (isDockerConfigured()) {
                    finalizedBy 'dockerPush'
                }
            }
        })
    }
}