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

import org.gradle.api.logging.LogLevel

import com.spotify.docker.client.DockerClient

import io.openliberty.boost.common.BoostException
import io.openliberty.boost.common.docker.DockerPushI

import io.openliberty.boost.gradle.utils.BoostLogger
import io.openliberty.boost.gradle.utils.GradleProjectUtil

public class BoostDockerPushTask extends AbstractBoostDockerTask implements DockerPushI {

    String appName

    BoostDockerPushTask() {
        configure({
            description 'Dockerizes a Boost project.'
            logging.level = LogLevel.INFO
            group 'Boost'

            dependsOn 'boostDockerBuild'

            project.afterEvaluate {
                appName = getArtifactId(project, GradleProjectUtil.findSpringBootVersion(project))
            }

            doFirst {
                doExecute(appName)
            }
        })
    }
    
    @Override
    public void execute(DockerClient dockerClient) throws BoostException {
        dockerPush(dockerClient, appName,
            project.boost.docker.dockerRepo, project.boost.docker.tag, BoostLogger.getInstance());
    }
}