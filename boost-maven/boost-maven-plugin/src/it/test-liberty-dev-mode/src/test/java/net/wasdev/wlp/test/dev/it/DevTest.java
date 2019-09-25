/*******************************************************************************
 * (c) Copyright IBM Corporation 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.wlp.test.dev.it;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Scanner;

import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DevTest extends BaseDevTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setUpBeforeClass(null, "DevTest");

        // check that dev mode has fully started
        assertTrue(checkLogForMessage("Press the Enter key to run tests on demand."));

        // verify that the target directory was created
        targetDir = new File(tempProj, "target");
        assertTrue(targetDir.exists());
    }

    @AfterClass
    public static void cleanUpAfterClass() throws Exception {
        BaseDevTest.cleanUpAfterClass("DevTest");
    }

    /**
     * Test that a basic java source file change is detected
     * 
     * @throws Exception
     */
    @Test
    public void basicTest() throws Exception {
        testModifyJavaFile();
    }

    /**
     * Test that changes to boost properties in the pom.xml are detected
     * 
     * @throws Exception
     */
    @Test
    public void changePortPropertyTest() throws Exception {
        markEndOfLogFile();

        File targetVariablesXML = new File(targetDir,
                "/liberty/wlp/usr/servers/defaultServer/configDropins/defaults/variables.xml");
        assertTrue(targetVariablesXML.exists());

        replaceString("<boost_http_port>9000</boost_http_port>", "<boost_http_port>9001</boost_http_port>", pom);

        // check for application started message
        assertTrue(checkLogForMessage("CWWKT0016I"));

        boolean found = false;
        Scanner scanner = new Scanner(targetVariablesXML);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("<variable defaultValue=\"9001\" name=\"boost_http_port\"/>")) {
                found = true;
            }
        }
        assertTrue("The updated boost_http_port variable was not found in the variables.xml file.", found);
    }

    /**
     * Test that resource files are properly detected when added
     * 
     * @throws Exception
     */
    @Test
    public void resourceFileChangeTest() throws Exception {
        markEndOfLogFile();

        // make a resource file change
        File resourceDir = new File(tempProj, "src/main/resources");
        assertTrue(resourceDir.exists());

        File propertiesFile = new File(resourceDir, "microprofile-config.properties");
        assertTrue(propertiesFile.createNewFile());

        Thread.sleep(2000); // wait for compilation
        File targetPropertiesFile = new File(targetDir, "classes/microprofile-config.properties");
        assertTrue(targetPropertiesFile.exists());
        assertTrue(checkLogForMessage("CWWKZ0003I"));

        // delete a resource file
        assertTrue(propertiesFile.delete());
        Thread.sleep(2000);
        assertFalse(targetPropertiesFile.exists());
    }

    /**
     * Test that test files are properly detected when created and modified
     * 
     * @throws Exception
     */
    @Test
    public void testDirectoryTest() throws Exception {
        // create the test directory
        File testDir = new File(tempProj, "src/test/java");
        assertTrue(testDir.mkdirs());

        // creates a java test file
        File unitTestSrcFile = new File(testDir, "UnitTest.java");
        String unitTest = "import org.junit.Test;\n" + "import static org.junit.Assert.*;\n" + "\n"
                + "public class UnitTest {\n" + "\n" + "    @Test\n" + "    public void testTrue() {\n"
                + "        assertTrue(true);\n" + "\n" + "    }\n" + "}";
        Files.write(unitTestSrcFile.toPath(), unitTest.getBytes());
        assertTrue(unitTestSrcFile.exists());

        Thread.sleep(2000); // wait for compilation
        File unitTestTargetFile = new File(targetDir, "/test-classes/UnitTest.class");
        assertTrue(unitTestTargetFile.exists());
        long lastModified = unitTestTargetFile.lastModified();

        // modify the test file
        String str = "// testing";
        BufferedWriter javaWriter = new BufferedWriter(new FileWriter(unitTestSrcFile, true));
        javaWriter.append(' ');
        javaWriter.append(str);

        javaWriter.close();

        Thread.sleep(5000); // wait for compilation
        assertTrue(unitTestTargetFile.lastModified() > lastModified);

        // delete the test file
        assertTrue(unitTestSrcFile.delete());
        Thread.sleep(2000);
        assertFalse(unitTestTargetFile.exists());

    }

    /**
     * Test to make sure tests are run when manually invoked
     * 
     * @throws Exception
     */
    @Test
    public void manualTestsInvocationTest() throws Exception {
        markEndOfLogFile();

        writer.write("\n");
        writer.flush();

        assertTrue(checkLogForMessage("Unit tests finished."));
        assertTrue(checkLogForMessage("Integration tests finished."));
    }

    /**
     * Test to make sure comile failures are resolved when the correct booster
     * is added.
     * 
     * @throws Exception
     */
    @Test
    public void resolveDependencyTest() throws Exception {
        markEndOfLogFile();

        // create the HealthCheck class, expect a compilation error
        File systemHealthRes = new File("../resources/SystemHealth.java");
        assertTrue(systemHealthRes.exists());
        File systemHealthSrc = new File(tempProj, "/src/main/java/com/demo/SystemHealth.java");
        File systemHealthTarget = new File(targetDir, "/classes/com/demo/SystemHealth.class");

        FileUtils.copyFile(systemHealthRes, systemHealthSrc);
        assertTrue(systemHealthSrc.exists());

        assertTrue(checkLogForMessage("Source compilation had errors"));
        assertFalse(systemHealthTarget.exists());

        markEndOfLogFile();

        // add mpHealth dependency to pom.xml
        String mpHealthComment = "<!-- <dependency>\n" + "        <groupId>org.microshed.boost.boosters</groupId>\n"
                + "        <artifactId>mp-health</artifactId>\n" + "        </dependency> -->";
        String mpHealth = "<dependency>\n" + "        <groupId>org.microshed.boost.boosters</groupId>\n"
                + "        <artifactId>mp-health</artifactId>\n" + "    </dependency>";
        replaceString(mpHealthComment, mpHealth, pom);

        // Wait for boost:package to complete
        assertTrue(checkLogForMessage("Server defaultServer package complete"));

        markEndOfLogFile();

        String str = "// testing";
        BufferedWriter javaWriter = new BufferedWriter(new FileWriter(systemHealthSrc, true));
        javaWriter.append(' ');
        javaWriter.append(str);

        javaWriter.close();

        Thread.sleep(10000); // Wait until compilation is complete

        assertTrue(checkLogForMessage("Source compilation was successful."));
        assertTrue(systemHealthTarget.exists());

    }

}
