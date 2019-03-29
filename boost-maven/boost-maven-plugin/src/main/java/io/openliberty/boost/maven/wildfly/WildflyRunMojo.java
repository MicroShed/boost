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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import net.wasdev.wlp.common.plugins.util.OSUtil;

/**
 * Runs the executable archive application (in the console foreground).
 */
@Mojo(name = "wildfly-run")
public class WildflyRunMojo extends AbstractWildflyMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        String wildflyInstallPath = projectBuildDir + "/wildfly-" + runtimeArtifact.getVersion();

        String startScript = "standalone.sh";
        if (OSUtil.isWindows()) {
            startScript = "standalone.bat";
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(wildflyInstallPath + "/bin/jboss-cli.sh embed-server --std-out=echo");
        pb.inheritIO();

        Process process = null;
        try {
            process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Could not start Wildfly server", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

    }

}
