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
package io.openliberty.boost.maven.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Runs the executable archive application (in the console foreground).
 */
@Mojo(name = "run")
public class LibertyRunMojo extends AbstractLibertyMojo {

    /**
     * Clean all cached information on server start up.
     */
    @Parameter(property = "clean", defaultValue = "false")
    private boolean clean;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        Element serverName = element(name("serverName"), libertyServerName);
        Element cleanElement = element(name("clean"), String.valueOf(clean));
        Element jvmOptions = generateJVMOptionsElement("server.port");

        if (jvmOptions != null) {
            executeMojo(getPlugin(), goal("run"),
                    configuration(serverName, cleanElement, jvmOptions, getRuntimeArtifactElement()),
                    getExecutionEnvironment());
        } else {

            executeMojo(getPlugin(), goal("run"), configuration(serverName, cleanElement, getRuntimeArtifactElement()),
                    getExecutionEnvironment());
        }
    }

    private Element generateJVMOptionsElement(String... passedOptions) {
        Element jvmOptions = null;
        ArrayList<Element> jvmParms = new ArrayList();
        Properties sysProps = session.getSystemProperties();

        for (String option : passedOptions) {
            String propertyValue = sysProps.getProperty(option);
            if (propertyValue != null)
                jvmParms.add(element(name("param"), "-D" + option + "=" + propertyValue));
        }
        if (jvmParms.size() > 0) {
            Element[] jvmArray = new Element[jvmParms.size()];

            for (int i = 0; i < jvmParms.size(); i++) {
                jvmArray[i] = jvmParms.get(i);
            }
            jvmOptions = new Element(name("jvmOptions"), jvmArray);
        }
        if (jvmOptions != null) {
            StringBuilder sb = new StringBuilder();
            for (String option : passedOptions) {
                sb.append(option + ";");
            }
            getLog().info("Passed JVM Options are :" + sb.toString());
        }

        return jvmOptions;
    }

}
