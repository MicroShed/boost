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
package io.openliberty.boost.common.docker;

import java.util.Scanner;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;

public interface DockerPushI extends AbstractDockerI {

    default public void dockerPush(DockerClient dockerClient, String artifactId, String repository, String tag,
            BoostLoggerI log) throws BoostException {
        final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
        final String currentImage = getImageName(repository, tag);
        String newImage = null;

        if (artifactId.equals(repository)) {
            log.warn("Cannot push the image with the default repository name " + repository);
            System.out.println("\nEnter the repository name in the format [REGISTRYHOST/][USERNAME/]NAME : ");

            @SuppressWarnings("resource")
            Scanner in = new Scanner(System.in);
            String newRepository = in.next();
            if (newRepository == null) {
                throw new BoostException("The repository name cannot be null");
            }
            if (!isRepositoryValid(newRepository)) {
                throw new BoostException("The repository name is not valid.");
            }
            newImage = getImageName(newRepository, tag);
        }

        try {
            if (newImage != null) {
                dockerClient.tag(currentImage, newImage);
                log.info("Successfully tagged " + currentImage + " with " + newImage);
                dockerClient.push(newImage, progressHandler);
            } else {
                dockerClient.push(currentImage, progressHandler);
            }
        } catch (DockerException | InterruptedException e) {
            throw new BoostException("Unable to push image", e);
        }
    }

}
