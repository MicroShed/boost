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
package io.openliberty.boost.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterPackConfigurator;
import io.openliberty.boost.common.config.JDBCBoosterPackConfigurator;

public class LibertyBoosterUtilTest {

    /**
     * Test that the database name property is set to the default
     * 
     */
    @Test
    public void testGetBoosterPackConfigurators_jdbc_default() {

        // Set JDBC as a dependency
        Map<String, String> dependencies = new HashMap<String, String>();
        dependencies.put(LibertyBoosterUtil.BOOSTER_JDBC, "0.2-SNAPSHOT");

        // Get booster configurators
        BoostLoggerI logger = Mockito.mock(BoostLoggerI.class);
        List<BoosterPackConfigurator> boosters = LibertyBoosterUtil.getBoosterPackConfigurators(dependencies, logger);

        // Check that the JDBCBoosterPackConfigurator was created
        BoosterPackConfigurator booster = boosters.get(0);
        assertTrue("JDBC booster was not found in booster configurator list",
                booster instanceof JDBCBoosterPackConfigurator);

        // Check that the custom databaseName is set
        assertEquals("Database name is not correct", JDBCBoosterPackConfigurator.DEFAULT_DERBY_DATABASE_NAME,
                booster.getServerProperties().getProperty(BoostProperties.DATASOURCE_DATABASE_NAME));

    }

    /**
     * Test that the database name property is overridden
     * 
     */
    @Test
    public void testGetBoosterPackConfigurators_jdbc_with_databaseName() {

        String databaseName = "myCustomDatabaseName";

        // Set system properties
        Properties allProperties = new Properties();
        allProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);
        System.setProperties(allProperties);

        // Set JDBC as a dependency
        Map<String, String> dependencies = new HashMap<String, String>();
        dependencies.put(LibertyBoosterUtil.BOOSTER_JDBC, "0.2-SNAPSHOT");

        // Get booster configurators
        BoostLoggerI logger = Mockito.mock(BoostLoggerI.class);
        List<BoosterPackConfigurator> boosters = LibertyBoosterUtil.getBoosterPackConfigurators(dependencies, logger);

        // Check that the JDBCBoosterPackConfigurator was created
        BoosterPackConfigurator booster = boosters.get(0);
        assertTrue("JDBC booster was not found in booster configurator list",
                booster instanceof JDBCBoosterPackConfigurator);

        // Check that the custom databaseName is set
        assertEquals("Database name is not correct", databaseName,
                booster.getServerProperties().getProperty(BoostProperties.DATASOURCE_DATABASE_NAME));

    }

}
