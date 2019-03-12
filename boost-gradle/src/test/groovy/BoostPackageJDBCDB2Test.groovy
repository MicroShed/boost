/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.BeforeClass
import org.junit.Test

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.io.IOException

import java.util.Properties

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import static org.gradle.testkit.runner.TaskOutcome.*

public class BoostPackageJDBCDB2Test extends AbstractBoostTest {

    static File resourceDir = new File("build/resources/test/test-jdbc-db2")
    static File testProjectDir = new File(integTestDir, "BoostPackageJDBCDB2Test")
    static String buildFilename = "jdbcDb2.gradle"

    private static final String DB2_JAR = "build/wlp/usr/servers/BoostServer/resources/db2jcc-db2jcc4.jar";
    private static final String JDBC_42_FEATURE = "<feature>jdbc-4.2</feature>"
    private static String SERVER_XML = "build/wlp/usr/servers/BoostServer/server.xml"

    private final String DB_NAME = "myCustomDB"
    private final String DB_USER = "user"
    private final String AES_HASHED_PASSWORD = "{aes}Lz4sLCgwLTs="
    private final String AES_HASHED_PASSWORD_FLAG = "{aes}"
    
    @BeforeClass
    public static void setup() {
        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)
    }
    
    @Test
    public void checkFeaturesAndPropertiesTest() {

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("clean", "boostPackage", "-Dboost.db.databaseName=myCustomDB", "-Dboost.db.user=user", "-Dboost.db.password=password", "-Dboost.aes.key=test", "-i", "-s")
            .build()
            
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())

        Properties bootstrapProperties = new Properties()
        InputStream input = null

        try {
            input = new FileInputStream(new File(testProjectDir, "/build/wlp/usr/servers/BoostServer/bootstrap.properties"))

            bootstrapProperties.load(input)

            assertEquals("Incorrect boost.db.user found in bootstrap.properties.", DB_USER, bootstrapProperties.getProperty("boost.db.user"))
            assertEquals("Incorrect boost.db.databaseName found in bootstrap.properties.", DB_NAME, bootstrapProperties.getProperty("boost.db.databaseName"))
            //AES hashed password changes so we're just going to look for the aes flag.
            assertTrue("Incorrect boost.db.password found in bootstrap.properties.", bootstrapProperties.getProperty("boost.db.password").contains(AES_HASHED_PASSWORD_FLAG))
        } catch (IOException ex) {
            ex.printStackTrace()
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (IOException e) {
                    e.printStackTrace()
                }
            }
        }
        testDb2Version()
        testFeatureVersion()
    }

    @Test
    public void checkFeaturesAndPropertiesTest_withAesPassword() {

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("clean", "boostPackage", "-Dboost.db.databaseName=myCustomDB", "-Dboost.db.user=user", "-Dboost.db.password={aes}Lz4sLCgwLTs=", "-Dboost.aes.key=test", "-i", "-s")
            .build()
            
        assertEquals(SUCCESS, result.task(":boostPackage").getOutcome())
    
        Properties bootstrapProperties = new Properties()
        InputStream input = null

        try {
            input = new FileInputStream(new File(testProjectDir, "/build/wlp/usr/servers/BoostServer/bootstrap.properties"))

            bootstrapProperties.load(input)

            assertEquals("Incorrect boost.db.user found in bootstrap.properties.", DB_USER, bootstrapProperties.getProperty("boost.db.user"))
            assertEquals("Incorrect boost.db.password found in bootstrap.properties.", AES_HASHED_PASSWORD, bootstrapProperties.getProperty("boost.db.password"))
            assertEquals("Incorrect boost.db.databaseName found in bootstrap.properties.", DB_NAME, bootstrapProperties.getProperty("boost.db.databaseName"))
        } catch (IOException ex) {
            ex.printStackTrace()
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (IOException e) {
                    e.printStackTrace()
                }
            }
        }
        testDb2Version()
        testFeatureVersion()
    }

    public void testDb2Version() throws Exception {
        File targetFile = new File(testProjectDir, DB2_JAR);
        assertTrue(targetFile.toString() + "does not exist.", targetFile.exists());
    }
    
    public void testFeatureVersion() throws Exception {
        File targetFile = new File(testProjectDir, SERVER_XML);
        assertTrue(targetFile.toString() + "does not exist.", targetFile.exists());

        // Check contents of file for jaxrs feature
        boolean found = false;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(new File(testProjectDir, SERVER_XML)));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(JDBC_42_FEATURE)) {
                    found = true;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        assertTrue("The " + JDBC_42_FEATURE + " feature was not found in the server configuration", found);
    }
}