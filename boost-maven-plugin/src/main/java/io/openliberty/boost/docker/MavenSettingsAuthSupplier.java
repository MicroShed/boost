package io.openliberty.boost.docker;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

public class MavenSettingsAuthSupplier implements RegistryAuthSupplier {

    private final Settings settings;
    private final SettingsDecrypter settingsDecrypter;
    private final Server server;

    private static final Logger log = LoggerFactory.getLogger(MavenSettingsAuthSupplier.class);

    public MavenSettingsAuthSupplier(Server server, Settings settings, SettingsDecrypter settingsDecrypter) {
        this.server = server;
        this.settings = settings;
        this.settingsDecrypter = settingsDecrypter;
    }

    @Override
    public RegistryAuth authFor(String imageName) throws DockerException {
        if (server != null) {
            return createRegistryAuth(server);
        }
        return null;
    }

    private RegistryAuth createRegistryAuth(Server server) throws DockerException {
        SettingsDecryptionRequest request = new DefaultSettingsDecryptionRequest(server);
        SettingsDecryptionResult result = settingsDecrypter.decrypt(request);

        if (result.getProblems().isEmpty()) {
            log.debug("Successfully decrypted Maven server password");
        } else {
            for (SettingsProblem problem : result.getProblems()) {
                log.error("Problems decrypting the Maven settings server {}: {}", server.getId(), problem);
            }

            throw new DockerException("Failed to decrypt Maven server password");
        }

        return RegistryAuth.builder().username(server.getUsername()).password(result.getServer().getPassword()).build();
    }

    @Override
    public RegistryAuth authForSwarm() throws DockerException {
        return null;
    }

    @Override
    public RegistryConfigs authForBuild() throws DockerException {
        final Map<String, RegistryAuth> allConfigs = new HashMap<>();
        for (Server server : settings.getServers()) {
            allConfigs.put(server.getId(), createRegistryAuth(server));
        }
        return RegistryConfigs.create(allConfigs);
    }

}
