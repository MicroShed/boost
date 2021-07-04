/*
 * Copyright (c) 2019 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.system.test.app;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.MicroProfileApplication;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class AppContainerConfig implements SharedContainerConfiguration {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>().withNetworkAliases("testpostgres")
            .withDatabaseName("test").withExposedPorts(5432).withInitScript("init.sql");

    @Container
    public static MicroProfileApplication app = new MicroProfileApplication()
            .withEnv("POSTGRES_HOSTNAME", "testpostgres").withEnv("boost_db_portNumber", "5432")
            .withEnv("boost_db_databaseName", "test").withEnv("boost_db_username", "test")
            .withEnv("boost_db_password", "test").withAppContextRoot("");

    @Override
    public void startContainers() {
        postgres.start();
        app.start();
    }

}
