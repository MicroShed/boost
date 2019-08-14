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
package boost.gradle.tasks

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.ServiceLoader

import java.net.URL
import java.net.URLClassLoader

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project

import boost.common.boosters.AbstractBoosterConfig
import boost.common.config.BoosterConfigurator
import boost.gradle.runtimes.GradleRuntimeI
import boost.gradle.utils.BoostLogger
import boost.gradle.utils.GradleProjectUtil

abstract class AbstractBoostTask extends DefaultTask {

    private static GradleRuntimeI runtime
    private static ClassLoader projectClassLoader
    private static List<AbstractBoosterConfig> boosterConfigs

    protected static Map<String, String> dependencies

    private static void init(Project project) throws GradleException {
        try {
            // TODO move this into getRuntimeInstance()
            dependencies = GradleProjectUtil.getAllDependencies(project, BoostLogger.getInstance())

            List<File> compileClasspathJars = new ArrayList<File>()

            List<URL> pathUrls = new ArrayList<URL>()
            for (File compilePathElement : project.configurations.compileClasspath.resolve()) {
                pathUrls.add(compilePathElement.toURI().toURL())
                if (compilePathElement.toString().endsWith(".jar")) {
                    compileClasspathJars.add(compilePathElement)
                }
            }
            Class thisClass = new Object(){}.getClass().getEnclosingClass();
            URL[] urlsForClassLoader = pathUrls.toArray(new URL[pathUrls.size()])
            projectClassLoader = new URLClassLoader(urlsForClassLoader, thisClass.getClassLoader())

            boosterConfigs = BoosterConfigurator.getBoosterConfigs(compileClasspathJars, projectClassLoader,
                    dependencies, BoostLogger.getInstance())

        } catch (Exception e) {
            throw new GradleException(e.getMessage(), e)
        }
    }

    public static GradleRuntimeI getRuntimeInstance(Project project) throws GradleException {
        if (runtime == null) {
            init(project)
            try {
                ServiceLoader<GradleRuntimeI> runtimes = ServiceLoader.load(GradleRuntimeI.class, projectClassLoader)
                if (!runtimes.iterator().hasNext()) {
                    throw new GradleException(
                            "No target Boost runtime was detected. Please add a runtime and restart the build.")
                }
                for (GradleRuntimeI runtimeI : runtimes) {
                    if (runtime != null) {
                        throw new GradleException(
                                "There are multiple Boost runtimes on the classpath. Configure the project to use one runtime and restart the build.")
                    }
                    runtime = runtimeI.getClass().newInstance()
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new GradleException("Error while looking for Boost runtime.")
            }
        }
        return runtime
    }

    public static void resetRuntime() {
        runtime = null
    }

    public static List<AbstractBoosterConfig> getBoosterConfigs() {
        return boosterConfigs;
    }

    protected ClassLoader getProjectClassLoader() {
        return projectClassLoader
    }

    protected boolean isPackageConfigured() {
        return project.boost.packaging != null
    }
}