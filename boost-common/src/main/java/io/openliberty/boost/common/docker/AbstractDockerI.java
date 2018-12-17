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

import java.util.regex.Pattern;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;

public interface AbstractDockerI {

    public void execute(DockerClient dockerClient) throws BoostException;

    public RegistryAuthSupplier createRegistryAuthSupplier() throws BoostException;

    // Default methods

    default public String getImageName(String repository, String tag) {
        return repository + ":" + tag;
    }

    default public boolean isTagValid(String tag) {
        return Pattern.matches("[\\w][\\w.-]{0,127}", tag);
    }

    default public boolean isRepositoryValid(String repository) {
        String nameRegExp = "[a-z0-9]+((?:[._]|__|[-]*)[a-z0-9]+)*?";
        String domain = "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
        String domainRegExp = domain + "(\\." + domain + ")*?" + "(:[0-9]+)?";

        String repositoryRegExp = "(" + domainRegExp + "\\/)?" + nameRegExp + "(\\/" + nameRegExp + ")*?";

        return Pattern.matches(repositoryRegExp, repository);
    }

    default public DockerClient getDockerClient(boolean useProxy) throws BoostException {
        final RegistryAuthSupplier authSupplier = createRegistryAuthSupplier();
        try {
            return DefaultDockerClient.fromEnv().registryAuthSupplier(authSupplier).useProxy(useProxy).build();
        } catch (DockerCertificateException e) {
            throw new BoostException("Problem loading Docker certificates", e);
        }
    }

    default public boolean isValidDockerConfig(BoostLoggerI log, String repository, String tag, String artifactId)
            throws BoostException {
        if (repository.equals(artifactId) && !repository.equals(repository.toLowerCase())) {
            repository = artifactId.toLowerCase();
            log.debug(
                    "Applying all lower case letters to the default repository name to build the Docker image successfully");
        }

        if (!isRepositoryValid(repository)) {
            if (repository.equals(artifactId)) {
                throw new BoostException(
                        "The default repository name ${project.artifactId} cannot be used to build the image because it is not a valid repository name.");
            } else {
                throw new BoostException("The <repository> parameter is not configured with a valid name");
            }
        }
        if (!isTagValid(tag)) {
            throw new BoostException("The <tag> parameter is not configured with a valid name");
        }
        return true;
    }
}
