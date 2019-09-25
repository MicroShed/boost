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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.maven.shared.utils.io.FileUtils;

public class BaseDevTest {

    static File tempProj;
    static File basicDevProj;
    static File logFile;
    static File targetDir;
    static File pom;
    static BufferedWriter writer;
    static Process process;

    static int logFileLineCount;

    protected static void setUpBeforeClass(String devModeParams, String logFileName)
            throws IOException, InterruptedException, FileNotFoundException {

        logFileLineCount = 0;

        tempProj = Files.createTempDirectory("temp").toFile();
        assertTrue(tempProj.exists());

        basicDevProj = new File("../resources/basic-dev-project");
        assertTrue(basicDevProj.exists());

        FileUtils.copyDirectoryStructure(basicDevProj, tempProj);
        assertTrue(tempProj.listFiles().length > 0);

        logFile = new File(logFileName + ".txt");
        assertTrue(logFile.createNewFile());

        pom = new File(tempProj, "pom.xml");
        assertTrue(pom.exists());

        replaceVersion();

        startDevMode(devModeParams);
    }

    private static void startDevMode(String devModeParams)
            throws IOException, InterruptedException, FileNotFoundException {
        // run dev mode on project
        StringBuilder command = new StringBuilder("mvn io.openliberty.tools:liberty-maven-plugin:3.0.1:dev");
        if (devModeParams != null) {
            command.append(" " + devModeParams);
        }
        ProcessBuilder builder = buildProcess(command.toString());

        // builder.redirectError(logFile);
        builder.redirectErrorStream(true);
        builder.redirectOutput(logFile);
        process = builder.start();
        assertTrue(process.isAlive());

        OutputStream stdin = process.getOutputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stdin));
    }

    protected static void cleanUpAfterClass(String testClassName) throws Exception {
        stopDevMode();

        if (tempProj != null && tempProj.exists()) {
            File messagesLog = new File(tempProj, "target/liberty/wlp/usr/servers/defaultServer/logs/messages.log");
            File copyMessagesLog = new File(testClassName + "_messages.log");
            FileUtils.copyFile(messagesLog, copyMessagesLog);
            FileUtils.deleteDirectory(tempProj);
        }
    }

    protected static void stopDevMode() throws IOException, InterruptedException, FileNotFoundException {
        markEndOfLogFile();

        // shut down dev mode
        if (writer != null) {
            writer.write("exit"); // trigger dev mode to shut down
            writer.flush();
            writer.close();

            // test that dev mode has stopped running.
            assertTrue(checkLogForMessage("CWWKE0036I"));
        }
    }

    protected static void testModifyJavaFile() throws IOException, InterruptedException {
        // modify a java file
        File srcHelloWorld = new File(tempProj, "src/main/java/com/demo/HelloWorld.java");
        File targetHelloWorld = new File(targetDir, "classes/com/demo/HelloWorld.class");
        assertTrue(srcHelloWorld.exists());
        assertTrue(targetHelloWorld.exists());

        long lastModified = targetHelloWorld.lastModified();
        String str = "// testing";
        BufferedWriter javaWriter = new BufferedWriter(new FileWriter(srcHelloWorld, true));
        javaWriter.append(' ');
        javaWriter.append(str);

        javaWriter.close();

        Thread.sleep(5000); // wait for compilation
        boolean wasModified = targetHelloWorld.lastModified() > lastModified;
        assertTrue(wasModified);
    }

    private static ProcessBuilder buildProcess(String processCommand) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(tempProj);

        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("windows")) {
            builder.command("CMD", "/C", processCommand);
        } else {
            builder.command("bash", "-c", processCommand);
        }
        return builder;
    }

    private static void replaceVersion() throws IOException {
        replaceString("RUNTIME_VERSION", "0.2.2-SNAPSHOT", pom);
    }

    protected static void replaceString(String str, String replacement, File file) throws IOException {
        Path path = file.toPath();
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);

        content = content.replaceAll(str, replacement);
        Files.write(path, content.getBytes(charset));
    }

    protected static boolean checkLogForMessage(String message) throws InterruptedException, FileNotFoundException {

        boolean found = false;

        int timeout = 1;
        while (!found && timeout <= 90) {
            timeout++;
            Thread.sleep(1000);

            found = findMessageInLog(message);
        }

        return found;
    }

    protected static void markEndOfLogFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(logFile);

        int lineCount = 0;
        while (scanner.hasNextLine()) {
            scanner.nextLine();
            lineCount++;
        }

        logFileLineCount = lineCount;
    }

    private static boolean findMessageInLog(String message) throws FileNotFoundException {
        Scanner scanner = new Scanner(logFile);

        // Skip to last marked eof
        skipLines(scanner);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(message)) {
                return true;
            }
        }
        return false;
    }

    private static void skipLines(Scanner scanner) {
        for (int i = 0; i < logFileLineCount; i++) {
            scanner.nextLine();
        }
    }
}
