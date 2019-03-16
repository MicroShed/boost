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
package io.openliberty.boost.maven.wildfly;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;

import io.openliberty.boost.common.boosters.wildfly.AbstractBoosterWildflyConfig;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Packages an existing application into a Wildfly executable jar so that the
 * application can be run from the command line using java -jar. (This is for
 * the 'jar' packaging type).
 *
 */
@Mojo(name = "wildfly-package", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class WildflyPackageMojo extends AbstractWildflyMojo {

    protected List<AbstractBoosterWildflyConfig> boosterConfigurators;

    String wildflyInstallPath;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        wildflyInstallPath = projectBuildDir + "/wildfly-" + runtimeArtifact.getVersion();

        installWildfly();

        // /**
        // * Whether the packaged Liberty Uber JAR will be the project artifact.
        // * This should be the case in Spring Boot scenarios since Spring Boot
        // * developers expect a runnable JAR.
        // */
        // boolean attach;

        // Determine the Java compiler target version and set this
        // internally
        String javaCompilerTargetVersion = MavenProjectUtil.getJavaCompilerTargetVersion(project);
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, javaCompilerTargetVersion);

        try {
            // Get booster dependencies from project
            Map<String, String> dependencies = MavenProjectUtil.getAllDependencies(project, repoSystem, repoSession,
                    remoteRepos, BoostLogger.getInstance());

            this.boosterConfigurators = BoosterConfigurator.getBoosterWildflyConfigurators(dependencies,
                    BoostLogger.getInstance());
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
        copyBoosterDependencies();

        configureServer();
    }

    private void installWildfly() throws MojoExecutionException {

        executeMojo(getMavenDependencyPlugin(), goal("unpack"),
                configuration(element(name("outputDirectory"), projectBuildDir),
                        element(name("artifactItems"), getRuntimeArtifactElement())),
                getExecutionEnvironment());
    }

    /**
     * Generate config for the Wildfly server based on the Maven
     * 
     * @throws MojoExecutionException
     */
    private void configureServer() throws MojoExecutionException {

        List<Path> warFiles = getWarFiles();
        try {
            // Generate server config
            BoosterConfigurator.generateWildflyServerConfig(wildflyInstallPath, boosterConfigurators, warFiles,
                    BoostLogger.getInstance());

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to configure the Wildfly server.", e);
        }
    }

    private List<Path> getWarFiles() {
        List<Path> warFiles = new ArrayList<Path>();

        // TODO: are these war files downloaded to target? Do we need to copy
        // them using the dependency plugin?
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getType().equals("war")) {
                warFiles.add(Paths.get(artifact.getArtifactId() + "-" + artifact.getVersion()));
            }
        }

        if (project.getVersion() == null) {
            warFiles.add(Paths.get(projectBuildDir + "/" + project.getArtifactId() + ".war"));
        } else {
            warFiles.add(
                    Paths.get(projectBuildDir + "/" + project.getArtifactId() + "-" + project.getVersion() + ".war"));
        }

        return warFiles;
    }

	/**
	 * Get all booster dependencies and invoke the maven-dependency-plugin to
	 * copy them to the Liberty server.
	 *
	 * @throws MojoExecutionException
	 *
	 */
	private void copyBoosterDependencies() throws MojoExecutionException {

		List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigurators,
				BoostLogger.getInstance());

		for (String dep : dependenciesToCopy) {

			String[] dependencyInfo = dep.split(":");

			executeMojo(getMavenDependencyPlugin(), goal("copy"),
					configuration(element(name("outputDirectory"), projectBuildDir + "/boostDependencies"),
							element(name("artifactItems"),
									element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
											element(name("artifactId"), dependencyInfo[1]),
											element(name("version"), dependencyInfo[2])))),
					getExecutionEnvironment());
		}
	}

}
