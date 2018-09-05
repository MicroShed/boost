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
package io.openliberty.boost.docker;

import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

@Mojo(name = "docker-push", defaultPhase = LifecyclePhase.INSTALL)
public class DockerPushMojo extends AbstractDockerMojo {

    @Override
    protected void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException {
        pushDockerImage(dockerClient);

    }

    /**
     * Use DockerClient to push the image
     * 
     */
    private void pushDockerImage(DockerClient dockerClient) throws MojoExecutionException {
        final DockerLoggingProgressHandler progressHandler = new DockerLoggingProgressHandler(log);
        final String currentImage = getImageName();
        String newImage = null;

        if (project.getArtifactId().equals(repository)) {
            log.warn("Cannot push the image with the default repository name " + repository);
            System.out.println("\nEnter the repository name in the format [REGISTRYHOST/][USERNAME/]NAME : ");

            @SuppressWarnings("resource")
            Scanner in = new Scanner(System.in);
            String newRepository = in.next();
            if (newRepository == null) {
                throw new NullPointerException("Repository name cannot be null");
            }
            newImage = getImageName(newRepository, tag);
        }

        try {
            if (newImage != null) {
                dockerClient.tag(currentImage, newImage);
                log.info("Successfully tagged " + currentImage + " with " + newImage);
                log.info("");
                dockerClient.push(newImage, progressHandler);
            } else {
                dockerClient.push(currentImage, progressHandler);
            }
        } catch (DockerException | InterruptedException e) {
            throw new MojoExecutionException("Could not push image", e);
        }

    }

}
