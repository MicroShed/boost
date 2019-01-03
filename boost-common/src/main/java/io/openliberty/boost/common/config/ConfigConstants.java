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

package io.openliberty.boost.common.config;

public interface ConfigConstants {

    public String FEATURE = "feature";
    public String DEPENDENCY = "dependency";
    public String GROUPID = "groupId";
    public String ARTIFACTID = "artifactId";
    public String VERSION = "version";
    public String WAR_PKG_TYPE = "war";

    public String SPRING_BOOT_PROJ = "spring-boot-project";
    public String NORMAL_PROJ = "project";

    public String FEATURE_MANAGER = "featureManager";
    public String HTTP_ENDPOINT = "httpEndpoint";
    public String DEFAULT_HTTP_ENDPOINT = "defaultHttpEndpoint";

    public String APPLICATION = "application";

    // KeyStore configuration values
    public String KEYSTORE = "keyStore";
    public String DEFAULT_KEYSTORE = "defaultKeyStore";
    public String KEY_ENTRY = "keyEntry";
    public String KEY_PASSWORD = "keyPassword";

    // Datasource configuration values
    public String DATASOURCE = "dataSource";
    public String DATABASE_NAME = "databaseName";
    public String JNDI_NAME = "jndiName";
    public String JDBC_DRIVER_REF = "jdbcDriverRef";
    public String JDBC_DRIVER = "jdbcDriver";
    public String LIBRARY_REF = "libraryRef";
    public String LIBRARY = "library";
    public String FILESET = "fileset";
    public String PROPERTIES_DERBY_EMBEDDED = "properties.derby.embedded";

    // General purpose configuration values
    public String LOCATION = "location";
    public String PASSWORD = "password";
    public String TYPE = "type";
    public String PROVIDER = "provider";
    public String NAME = "name";
    public String CONTEXT_ROOT = "context-root";

    public String SPRING_BOOT_15 = "springBoot-1.5";
    public String SPRING_BOOT_20 = "springBoot-2.0";
    public String SERVLET_40 = "servlet-4.0";

    public String WEBSOCKET_11 = "websocket-1.1";
    public String TRANSPORT_SECURITY_10 = "transportSecurity-1.0";
    public String JAXRS_20 = "jaxrs-2.0";
    public String JAXRS_21 = "jaxrs-2.1";
    public String JDBC_41 = "jdbc-4.1";
    public String JDBC_42 = "jdbc-4.2";
    public String CDI_20 = "cdi-2.0";
    public String MPHEALTH_10 = "mpHealth-1.0";
    public String MPRESTCLIENT_11 = "mpRestClient-1.1";
    public String JSONP_11 = "jsonp-1.1";
    public String MPCONFIG_13 = "mpConfig-1.3";
    public String MPOPENTRACING_10 = "mpOpenTracing-1.0";

    public String SHARED_RESOURCES_DIR = "${shared.resource.dir}";
    public String SERVER_OUTPUT_DIR = "${server.output.dir}";
}
