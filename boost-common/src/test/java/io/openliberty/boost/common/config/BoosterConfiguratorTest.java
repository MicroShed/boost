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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.boosters.JDBCBoosterConfig;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.utils.BoosterUtil;
import io.openliberty.boost.common.utils.CommonLogger;

public class BoosterConfiguratorTest {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private Map<String, String> getJDBCDependency() throws BoostException {
        return BoosterUtil.createDependenciesWithBoosterAndVersion(JDBCBoosterConfig.class, "0.1-SNAPSHOT");
    }

    /**
     * Test that the database name property is set to the default
     * 
     * @throws BoostException
     * 
     */
    @Test
    public void testGetBoosterPackConfigurators_jdbc_default() throws Exception {

        // Get booster configurators
        BoostLoggerI logger = CommonLogger.getInstance();
        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(),
                logger);

        // Check that the JDBCBoosterPackConfigurator was created
        AbstractBoosterConfig booster = boosters.get(0);
        assertTrue("JDBC booster was not found in booster configurator list", booster instanceof JDBCBoosterConfig);

        // Check that the custom databaseName is set
        assertEquals("Database name is not correct", ConfigConstants.DERBY_DB,
                booster.getServerProperties().getProperty(BoostProperties.DATASOURCE_DATABASE_NAME));

    }

    /**
     * Test that the database name property is overridden
     * 
     * @throws BoostException
     * 
     */
    @Test
    public void testGetBoosterPackConfigurators_jdbc_with_databaseName() throws Exception {
        String databaseName = "myCustomDatabaseName";

        // Set system properties
        Properties allProperties = new Properties();
        allProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);
        System.setProperties(allProperties);

        // Get booster configurators
        BoostLoggerI logger = CommonLogger.getInstance();
        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(getJDBCDependency(),
                logger);

        // Check that the JDBCBoosterPackConfigurator was created
        AbstractBoosterConfig booster = boosters.get(0);
        assertTrue("JDBC booster was not found in booster configurator list", booster instanceof JDBCBoosterConfig);

        // Check that the custom databaseName is set
        assertEquals("Database name is not correct", databaseName,
                booster.getServerProperties().getProperty(BoostProperties.DATASOURCE_DATABASE_NAME));

    }

}
