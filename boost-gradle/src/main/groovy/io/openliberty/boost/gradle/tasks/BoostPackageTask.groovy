/*******************************************************************************
 * Copyright (c) 2018,2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package boost.gradle.tasks

import org.codehaus.groovy.GroovyException

import java.util.ArrayList
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.IOException

import org.apache.commons.io.FileUtils

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.artifacts.Dependency
import org.gradle.tooling.BuildException

import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.tasks.Copy

import boost.gradle.utils.BoostLogger
import boost.gradle.utils.GradleProjectUtil
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.config.BoosterConfigurator
import boost.common.config.BoostProperties;
import boost.common.utils.BoostUtil
import org.gradle.api.Task

import net.wasdev.wlp.gradle.plugins.extensions.PackageAndDumpExtension

public class BoostPackageTask extends AbstractBoostTask {
    BoostPackageTask() {
        configure({
            description 'Packages the application into an executable Liberty jar.'
            logging.level = LogLevel.INFO
            group 'Boost'

            doFirst {
                AbstractBoostTask.getRuntimeInstance(project).doPackage(AbstractBoostTask.getBoosterConfigs(), project, this)
            }
        })
    }
}
