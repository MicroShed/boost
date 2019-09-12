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
package boost.common.boosters;

import static boost.common.config.ConfigConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import boost.common.config.BoostProperties;
import boost.common.config.BoosterConfigParams;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {

    public static final String DB2_DEFAULT_PORT_NUMBER = "50000";
    public static final String MYSQL_DEFAULT_PORT_NUMBER = "3306";
    public static final String POSTGRESQL_DEFAULT_PORT_NUMBER = "5432";

    public static String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";
    public static String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    public static String DERBY_GROUP_ID = "org.apache.derby";
    public static String DERBY_ARTIFACT_ID = "derby";
    public static String DB2_GROUP_ID = "com.ibm.db2.jcc";
    public static String DB2_ARTIFACT_ID = "db2jcc";
    public static String MYSQL_GROUP_ID = "mysql";
    public static String MYSQL_ARTIFACT_ID = "mysql-connector-java";
    public static String POSTGRESQL_GROUP_ID = "org.postgresql";
    public static String POSTGRESQL_ARTIFACT_ID = "postgresql";

    public static String DERBY_DEFAULT_VERSION = "10.14.2.0";

    public static String DRIVER_CLASS_NAME = "driverClassName";
    public static String DRIVER_NAME = "driverName";
    public static String DRIVER_JAR = "driverJar";

    protected Properties boostConfigProperties;
    private String dependency;
    private Map<String, String> driverInfo;

    public JDBCBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params.getProjectDependencies().get(getCoordinates(JDBCBoosterConfig.class)));

        Map<String, String> projectDependencies = params.getProjectDependencies();
        this.boostConfigProperties = params.getBoostProperties();

        driverInfo = new HashMap<String, String>();

        if (projectDependencies.containsKey(DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID)) {
            String version = projectDependencies.get(DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID);
            dependency = DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID + ":" + version;

            driverInfo.put(DRIVER_NAME, DERBY_ARTIFACT_ID);
            driverInfo.put(DRIVER_CLASS_NAME, DERBY_DRIVER_CLASS_NAME);
            driverInfo.put(DRIVER_JAR, DERBY_ARTIFACT_ID + "-" + version + ".jar");

        } else if (projectDependencies.containsKey(DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID)) {
            String version = projectDependencies.get(DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID);
            dependency = DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID + ":" + version;

            driverInfo.put(DRIVER_NAME, DB2_ARTIFACT_ID);
            driverInfo.put(DRIVER_CLASS_NAME, DB2_DRIVER_CLASS_NAME);
            driverInfo.put(DRIVER_JAR, DB2_ARTIFACT_ID + "-" + version + ".jar");

        } else if (projectDependencies.containsKey(MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID)) {
            String version = projectDependencies.get(MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID);
            dependency = MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID + ":" + version;

            driverInfo.put(DRIVER_NAME, MYSQL_ARTIFACT_ID);
            driverInfo.put(DRIVER_CLASS_NAME, MYSQL_DRIVER_CLASS_NAME);
            driverInfo.put(DRIVER_JAR, MYSQL_ARTIFACT_ID + "-" + version + ".jar");

        } else if (projectDependencies.containsKey(POSTGRESQL_GROUP_ID + ":" + POSTGRESQL_ARTIFACT_ID)) {
            String version = projectDependencies.get(POSTGRESQL_GROUP_ID + ":" + POSTGRESQL_ARTIFACT_ID);
            dependency = POSTGRESQL_GROUP_ID + ":" + POSTGRESQL_ARTIFACT_ID + ":" + version;

            driverInfo.put(DRIVER_NAME, POSTGRESQL_ARTIFACT_ID);
            driverInfo.put(DRIVER_CLASS_NAME, POSTGRESQL_DRIVER_CLASS_NAME);
            driverInfo.put(DRIVER_JAR, POSTGRESQL_ARTIFACT_ID + "-" + version + ".jar");

        } else {
            dependency = DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID + ":" + DERBY_DEFAULT_VERSION;

            driverInfo.put(DRIVER_NAME, DERBY_ARTIFACT_ID);
            driverInfo.put(DRIVER_CLASS_NAME, DERBY_DRIVER_CLASS_NAME);
            driverInfo.put(DRIVER_JAR, DERBY_ARTIFACT_ID + "-" + DERBY_DEFAULT_VERSION + ".jar");
        }
    }

    public Properties getDatasourceProperties() {

        Properties datasourceProperties = new Properties();

        // Find and add all "boost.db." properties.
        for (String key : boostConfigProperties.stringPropertyNames()) {
            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
                String value = (String) boostConfigProperties.get(key);
                datasourceProperties.put(key, value);
            }
        }

        // TODO: Are defaults adding any value here? Should we just eliminate
        // them?
        if (!datasourceProperties.containsKey(BoostProperties.DATASOURCE_URL)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_DATABASE_NAME)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_SERVER_NAME)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_PORT_NUMBER)) {

            // No db connection properties have been specified. Set defaults.
            if (dependency.contains(DERBY_GROUP_ID)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
                datasourceProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");

            } else if (dependency.contains(DB2_GROUP_ID)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_URL,
                        "jdbc:db2://localhost:" + DB2_DEFAULT_PORT_NUMBER);

            } else if (dependency.contains(MYSQL_GROUP_ID)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_URL,
                        "jdbc:mysql://localhost:" + MYSQL_DEFAULT_PORT_NUMBER);

            } else if (dependency.contains(POSTGRESQL_GROUP_ID)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_URL,
                        "jdbc:postgresql://localhost:" + POSTGRESQL_DEFAULT_PORT_NUMBER);
            }
        }
        return datasourceProperties;
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = new ArrayList<String>();
        deps.add(dependency);

        return deps;
    }

    public Map<String, String> getDriverInfo() {
        return driverInfo;
    }
}
