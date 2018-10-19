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

package io.openliberty.boost.utils;

public interface ConfigConstants {

	public String FEATURE = "feature";
	public String DEPENDENCY = "dependency";
	public String GROUPID = "groupId";
	public String ARTIFACTID = "artifactId";
	public String VERSION = "version";
	
	public String FEATURE_MANAGER = "featureManager";
	public String HTTP_ENDPOINT = "httpEndpoint";
	public String DEFAULT_HTTP_ENDPOINT = "defaultHttpEndpoint";
	
	public String APPLICATION = "application";

    public String SPRING_BOOT_15 = "springBoot-1.5";
    public String SPRING_BOOT_20 = "springBoot-2.0";
    public String SERVLET_40 = "servlet-4.0";

    public String WEBSOCKET_11 = "websocket-1.1";
}
