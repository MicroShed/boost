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
package io.openliberty.boost.common.boosters;

import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.runtimes.RuntimeI;
import io.openliberty.boost.common.config.ServerConfigGenerator;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {

	public static String DERBY = "derby";
	public static String DB2 = "db2";
	public static String MYSQL = "mysql";
	
	public static String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	public static String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";
	public static String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	
    public static String DERBY_DEPENDENCY = "org.apache.derby:derby";
    public static String DB2_DEPENDENCY = "com.ibm.db2.jcc:db2jcc";
    public static String MYSQL_DEPENDENCY = "mysql:mysql-connector-java";

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";

    private String dependency;
    private String libertyFeature;
    private Properties datasourceProperties;
    private String productName;

    public JDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {

    	Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
    	
        // Determine JDBC driver dependency
        if (dependencies.containsKey(JDBCBoosterConfig.DERBY_DEPENDENCY)) {
            String derbyVersion = dependencies.get(JDBCBoosterConfig.DERBY_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.DERBY_DEPENDENCY + ":" + derbyVersion;
            this.productName = DERBY;

        } else if (dependencies.containsKey(JDBCBoosterConfig.DB2_DEPENDENCY)) {
            String db2Version = dependencies.get(JDBCBoosterConfig.DB2_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + db2Version;
            this.productName = DB2;

        } else if (dependencies.containsKey(JDBCBoosterConfig.MYSQL_DEPENDENCY)) {
            String mysqlVersion = dependencies.get(JDBCBoosterConfig.MYSQL_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.MYSQL_DEPENDENCY + ":" + mysqlVersion;
            this.productName = MYSQL;
            
        } else {
        	this.dependency = DERBY_DEFAULT;
        	this.productName = DERBY;
        }
        
        // Determine Liberty feature version based on Java compiler target value.
        String compilerVersion = boostConfigProperties.getProperty(BoostProperties.INTERNAL_COMPILER_TARGET);

        if ("1.8".equals(compilerVersion) || "8".equals(compilerVersion) || "9".equals(compilerVersion)
                || "10".equals(compilerVersion)) {
            this.libertyFeature = JDBC_42;
        } else if ("11".equals(compilerVersion)) {
            this.libertyFeature = JDBC_43;
        } else {
            this.libertyFeature = JDBC_41; // Default to the spec for Liberty's
                                           // minimum supported JRE (version 7
                                           // as of 17.0.0.3)
        }
        
        initDatasourceProperties(boostConfigProperties);
    }

    private void initDatasourceProperties(Properties boostConfigProperties) {
    	
        datasourceProperties = new Properties();
        
        // Find and add all "boost.db." properties. 
        for (String key : boostConfigProperties.stringPropertyNames()) {
            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
                String value = (String) boostConfigProperties.get(key);
                datasourceProperties.put(key, value);
            }
        }
        
        if (!datasourceProperties.containsKey(BoostProperties.DATASOURCE_URL) &&
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_DATABASE_NAME) && 
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_SERVER_NAME) && 
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_PORT_NUMBER)) {
        	
        	// No db connection properties have been specified. Set defaults.
        	if (productName.equals(DERBY)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
		        datasourceProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");
		        
        	} else if (productName.equals(DB2)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_URL, "jdbc:db2://localhost:" + DB2_DEFAULT_PORT_NUMBER);
        		
        	} else if (productName.equals(MYSQL)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_URL, "jdbc:mysql://localhost:" + MYSQL_DEFAULT_PORT_NUMBER);
        	}
        }   
    }
    
    @Override
    public String getLibertyFeature() {
    	return this.libertyFeature;
    }

    @Override
    public void addServerConfig(ServerConfigGenerator config) throws Exception {
    	
        config.addDataSource(productName, datasourceProperties);
    }

    @Override
    public List<String> getDependencies(RuntimeI runtime) {
        List<String> deps = new ArrayList<String>();
        deps.add(dependency);
        
        return deps;
    }
}
