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

package io.openliberty.boost.gradle.extensions

import org.gradle.util.ConfigureUtil

class BoostExtension {

	BoostDockerExtension docker

	def docker(Closure closure){
        docker = new BoostDockerExtension()
        ConfigureUtil.configure(closure, docker)
	}

	BoostPackageExtension packaging

	def packaging(Closure closure){
        packaging = new BoostPackageExtension()
        ConfigureUtil.configure(closure, packaging)
	}
}