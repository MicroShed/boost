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

class BoostDockerExtension {

    String dockerizer = "liberty"
	String imageName = null
	String dockerRepo = ""
	String tag = "latest"
	boolean useProxy = false
	boolean pullNewerImage = false
	boolean noCache = false
	Map<String, String> buildArgs = new HashMap<String, String>()

}