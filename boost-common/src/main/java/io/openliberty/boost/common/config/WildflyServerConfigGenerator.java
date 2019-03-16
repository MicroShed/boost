/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.wildfly.AbstractBoosterWildflyConfig;
import net.wasdev.wlp.common.plugins.util.OSUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class WildflyServerConfigGenerator {

    private final String wildflyInstallPath;
    private final String cliScript;

    private final BoostLoggerI logger;

    public WildflyServerConfigGenerator(String wildflyInstallPath, BoostLoggerI logger)
            throws ParserConfigurationException {

        this.logger = logger;
        this.wildflyInstallPath = wildflyInstallPath;

        if (OSUtil.isWindows()) {
            this.cliScript = "jboss-cli.bat";
        } else {
            this.cliScript = "jboss-cli.sh";
        }
    }

    public void addBoosterConfig(AbstractBoosterWildflyConfig configurator) throws IOException {
        List<String> commands = configurator.getCliCommands();
        if (commands != null) {
            for (String command : commands) {
                runCliScript(command);
            }
        }
    }

    public void addApplication(String pathToWar) throws IOException {
        runCliScript("deploy --force " + pathToWar);

    }

    private void runCliScript(String command) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(wildflyInstallPath + "/bin/" + cliScript,
                "--commands=embed-server," + command);
        Process cliProcess = pb.start();

        // Print error stream
        BufferedReader error = new BufferedReader(new InputStreamReader(cliProcess.getErrorStream()));
        String line;
        while ((line = error.readLine()) != null) {
            logger.debug(line);
        }

        // Print output stream
        BufferedReader in = new BufferedReader(new InputStreamReader(cliProcess.getInputStream()));
        while ((line = in.readLine()) != null) {
            logger.debug(line);
        }

        // TODO: throw exception if error stream has any content

    }
}
