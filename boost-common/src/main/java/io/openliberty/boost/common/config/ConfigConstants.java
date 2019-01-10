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

public final class ConfigConstants {

    public static final String FEATURE = "feature";
    public static final  String DEPENDENCY = "dependency";
    public static final  String GROUPID = "groupId";
    public static final  String ARTIFACTID = "artifactId";
    public static final  String VERSION = "version";
    public static final  String WAR_PKG_TYPE = "war";

    public static final  String SPRING_BOOT_PROJ = "spring-boot-project";
    public static final  String NORMAL_PROJ = "project";

    public static final  String FEATURE_MANAGER = "featureManager";
    public static final  String HTTP_ENDPOINT = "httpEndpoint";
    public static final  String DEFAULT_HTTP_ENDPOINT = "defaultHttpEndpoint";

    public static final  String APPLICATION = "application";

    // KeyStore configuration values
    public static final  String KEYSTORE = "keyStore";
    public static final  String DEFAULT_KEYSTORE = "defaultKeyStore";
    public static final  String KEY_ENTRY = "keyEntry";
    public static final  String KEY_PASSWORD = "keyPassword";

    // Datasource configuration values
    public static final  String DATASOURCE = "dataSource";
    public static final  String DATABASE_NAME = "databaseName";
    public static final  String JNDI_NAME = "jndiName";
    public static final  String JDBC_DRIVER_REF = "jdbcDriverRef";
    public static final  String JDBC_DRIVER = "jdbcDriver";
    public static final  String LIBRARY_REF = "libraryRef";
    public static final  String LIBRARY = "library";
    public static final  String FILESET = "fileset";
    public static final  String PROPERTIES_DERBY_EMBEDDED = "properties.derby.embedded";

    public static final  String DEFAULT_DATASOURCE = "DefaultDataSource";
    public static final  String DERBY_EMBEDDED = "DerbyEmbedded";
    public static final  String DERBY_LIB = "DerbyLib";
    public static final  String CREATE_DATABASE = "createDatabase";
    public static final  String DERBY_DB = "DerbyDB";
    
    // General purpose configuration values
    public static final  String LOCATION = "location";
    public static final  String PASSWORD = "password";
    public static final  String TYPE = "type";
    public static final  String PROVIDER = "provider";
    public static final  String NAME = "name";
    public static final  String CONTEXT_ROOT = "context-root";
    public static final  String RESOURCES = "resources";

    public static final  String SPRING_BOOT_15 = "springBoot-1.5";
    public static final  String SPRING_BOOT_20 = "springBoot-2.0";
    public static final  String SERVLET_40 = "servlet-4.0";

    public static final  String WEBSOCKET_11 = "websocket-1.1";
    public static final  String TRANSPORT_SECURITY_10 = "transportSecurity-1.0";
    public static final  String JAXRS_20 = "jaxrs-2.0";
    public static final  String JAXRS_21 = "jaxrs-2.1";
    public static final  String JDBC_41 = "jdbc-4.1";
    public static final  String JDBC_42 = "jdbc-4.2";

    public static final  String SHARED_RESOURCES_DIR = "${shared.resource.dir}";
    public static final  String SERVER_OUTPUT_DIR = "${server.output.dir}";
    
}
