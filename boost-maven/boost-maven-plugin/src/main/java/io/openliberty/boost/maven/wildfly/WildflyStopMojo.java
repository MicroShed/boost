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
package io.openliberty.boost.maven.wildfly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import net.wasdev.wlp.common.plugins.util.OSUtil;

/**
 * Runs the executable archive application (in the console foreground).
 */
@Mojo(name = "wildfly-stop")
public class WildflyStopMojo extends AbstractWildflyMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        String wildflyInstallPath = projectBuildDir + "/wildfly-" + runtimeArtifact.getVersion();

        String cliScript = "jboss-cli.sh";
        if (OSUtil.isWindows()) {
            cliScript = "jboss-cli.bat";
        }

        List<String> stopCommand = new ArrayList<String>();
        stopCommand.add(wildflyInstallPath + "/bin/" + cliScript);
        stopCommand.add("--connect");
        stopCommand.add("shutdown");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(stopCommand);

        try {
            pb.start();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not stop Wildfly server", e);
        }
    }

}
