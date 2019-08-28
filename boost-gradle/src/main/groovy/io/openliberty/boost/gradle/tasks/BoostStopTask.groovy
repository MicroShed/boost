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
package boost.gradle.tasks

import org.gradle.api.logging.LogLevel

public class BoostStopTask extends AbstractBoostTask {

    BoostStopTask() {
        configure({
            description 'Stops the Boost application'
            logging.level = LogLevel.INFO
            group 'Boost'

            doFirst {
                AbstractBoostTask.getRuntimeInstance(project).doStop(project, this)
            }
        })
    }
}