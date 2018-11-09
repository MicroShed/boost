package io.openliberty.boost.common.docker;

import java.util.regex.Pattern;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.openliberty.boost.common.BoostException;

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

}
