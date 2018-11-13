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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

public class MavenSettingsAuthSupplier implements RegistryAuthSupplier {

    private final Settings settings;
    private final SettingsDecrypter settingsDecrypter;
    private final Server server;
    private final Log log;

    public MavenSettingsAuthSupplier(Server server, Settings settings, SettingsDecrypter settingsDecrypter, Log log) {
        this.server = server;
        this.settings = settings;
        this.settingsDecrypter = settingsDecrypter;
        this.log = log;
    }

    @Override
    public RegistryAuth authFor(String imageName) throws DockerException {
        if (server != null) {
            return buildRegistryAuth(server);
        }
        return null;
    }

    private RegistryAuth buildRegistryAuth(Server server) throws DockerException {
        if (settingsDecrypter != null) {
            SettingsDecryptionResult result = decrypt(server);
            return RegistryAuth.builder().username(result.getServer().getUsername())
                    .password(result.getServer().getPassword()).build();
        }
        return null;
    }

    private SettingsDecryptionResult decrypt(Server server) throws DockerException {
        SettingsDecryptionRequest request = new DefaultSettingsDecryptionRequest(server);
        SettingsDecryptionResult result = settingsDecrypter.decrypt(request);
        List<SettingsProblem> problems = result.getProblems();
        if (!problems.isEmpty()) {
            for (SettingsProblem problem : problems) {
                log.error("Problem occured while decrypting the server " + server.getId() + ":" + problem);
            }
            throw new DockerException("Unable to decrypt Maven Settings Server " + server.getId());
        }
        return result;
    }

    @Override
    public RegistryAuth authForSwarm() throws DockerException {
        return null;
    }

    @Override
    public RegistryConfigs authForBuild() throws DockerException {
        final List<Server> servers = settings.getServers();
        final Map<String, RegistryAuth> settingServerConfigs = new HashMap<>();
        for (Server server : servers) {
            settingServerConfigs.put(server.getId(), buildRegistryAuth(server));
        }
        return RegistryConfigs.create(settingServerConfigs);
    }

}
