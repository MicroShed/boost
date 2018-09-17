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
package io.openliberty.boost.docker;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ImageRef;
import com.spotify.docker.client.auth.ConfigFileRegistryAuthSupplier;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public abstract class AbstractDockerMojo extends AbstractMojo {

	/**
	 * Current Maven project.
	 */
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	/**
	 * Current Maven session.
	 */
	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Component
	private SettingsDecrypter settingsDecrypter;

	/**
	 * The repository to put the built image into, <tt>${project.artifactId}</tt> is
	 * used by default.
	 */
	@Parameter(property = "repository", defaultValue = "${project.artifactId}")
	protected String repository;

	/**
	 * The tag to apply to the built image. <tt>latest</tt> is used by default.
	 */
	@Parameter(property = "tag", defaultValue = "latest")
	protected String tag;

	/**
	 * Connect to Docker Daemon using HTTP proxy, if set.
	 */
	@Parameter(property = "useProxy", defaultValue = "true")
	protected boolean useProxy;

	protected Log log;

	protected abstract void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		execute(getDockerClient());
	}

	private DockerClient getDockerClient() throws MojoExecutionException {
		final RegistryAuthSupplier authSupplier = createRegistryAuthSupplier();
		try {
			return DefaultDockerClient.fromEnv().registryAuthSupplier(authSupplier).useProxy(useProxy).build();
		} catch (DockerCertificateException e) {
			throw new MojoExecutionException("Problem loading Docker certificates", e);
		}
	}

	private RegistryAuthSupplier createRegistryAuthSupplier() {
		RegistryAuthSupplier supplier = null;
		final ImageRef ref = new ImageRef(getImageName());
		final Settings settings = session.getSettings();
		final Server server = settings.getServer(ref.getRegistryName());

		// Check for the registry credentials in maven settings.xml and in default
		// config path
		if (server != null) {
			supplier = new MavenSettingsAuthSupplier(server, settings, settingsDecrypter, log);
		} else {
			supplier = new ConfigFileRegistryAuthSupplier();
		}
		return supplier;
	}

	protected final String getImageName() {
		return this.repository + ":" + this.tag;
	}

	protected String getImageName(String repository, String tag) {
		return repository + ":" + tag;
	}
}
