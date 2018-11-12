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
package io.openliberty.boost.maven.docker;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import com.spotify.docker.client.DockerClient;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.docker.DockerPushI;
import io.openliberty.boost.maven.utils.BoostLogger;

/**
 * Pushes a docker image to the docker repository.
 *
 */
@Mojo(name = "docker-push", defaultPhase = LifecyclePhase.INSTALL)
public class DockerPushMojo extends AbstractDockerMojo implements DockerPushI {

    @Override
    public void execute(DockerClient dockerClient) throws BoostException {
        dockerPush(dockerClient, project.getArtifactId(), repository, tag, BoostLogger.getInstance());
    }

}
