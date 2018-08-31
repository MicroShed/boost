package boost.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class Dockerize {

    private final File projectDirectory;
    private final File outputDirectory;
    private final File appArchive;
    private final Log log;

    private static final String LIBERTY_IMAGE_1 = "open-liberty:springBoot1";
    private static final String LIBERTY_IMAGE_2 = "open-liberty:springBoot2";
    private static final String LIB_INDEX_CACHE = "lib.index.cache";
    private static final String ARG_SOURCE_APP = "--sourceAppPath";
    private static final String ARG_DEST_THIN_APP = "--targetThinAppPath";
    private static final String ARG_DEST_LIB_CACHE = "--targetLibCachePath";
    private static final String FROM = "FROM ";
    private static final String COPY = "COPY ";
    private static final String RUN = "RUN ";

    public Dockerize(File projectDirectory, File outputDirectory, File appArchive, Log log) {
        this.projectDirectory = projectDirectory;
        this.outputDirectory = outputDirectory;
        this.appArchive = appArchive;
        this.log = log;
    }

    /**
     * Create a Dockerfile with appropriate LibertyBaseImage to build an efficient
     * docker image of the Spring Boot application.
     * 
     * @param springBootVersion
     * @throws MojoExecutionException
     * @throws IOException
     */
    public void createSpringBootDockerFile(String springBootVersion) throws MojoExecutionException, IOException {
        if (isFileExecutable(appArchive)) {
            try {
                File dockerFile = createDockerFile();
                String libertySBImage = getLibertySpringBootBaseImage(springBootVersion);
                writeSpringBootDockerFile(dockerFile, libertySBImage);

            } catch (FileAlreadyExistsException e) {
                log.warn("Dockerfile already exists");
            }
        } else {
            throw new MojoExecutionException(appArchive.getCanonicalPath() + " file is not an executable archive. "
                    + "The repackage goal of the spring-boot-maven-plugin must be configured to run first in order to create the required executable archive.");
        }
    }

    private File createDockerFile() throws IOException {
        File dockerFile = new File(projectDirectory, "Dockerfile");
        Files.createFile(dockerFile.toPath());
        log.info("Creating Dockerfile: " + dockerFile.getAbsolutePath());
        return dockerFile;
    }

    @SuppressWarnings("resource")
    private boolean isFileExecutable(File file) throws IOException {
        if (file.exists()) {
            Manifest manifest = new JarFile(file).getManifest();
            if (manifest != null) {
                String startClass = manifest.getMainAttributes().getValue("Start-Class");
                if (startClass != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getLibertySpringBootBaseImage(String springBootVersion) throws MojoExecutionException {
        String libertyImage = null;
        if (springBootVersion.startsWith("1.")) {
            libertyImage = LIBERTY_IMAGE_1;
        } else if (springBootVersion.startsWith("2.")) {
            libertyImage = LIBERTY_IMAGE_2;
        } else {
            throw new MojoExecutionException(
                    "No supporting docker image found for Open Liberty for the Spring Boot version "
                            + springBootVersion);
        }
        return libertyImage;
    }

    private void writeSpringBootDockerFile(File dockerFile, String libertyImage) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(FROM + libertyImage + " as " + "staging");

        lines.add("\n");
        lines.add("# The APP_FILE ARG provides the final name of the Spring Boot application archive");
        lines.add("ARG" + " " + "APP_FILE");

        lines.add("\n");
        lines.add("# Stage the fat JAR");
        lines.add(COPY + outputDirectory.getName() + "/" + "${APP_FILE}" + " " + "/staging/" + "${APP_FILE}");

        lines.add("\n");
        lines.add("# Thin the fat application; stage the thin app output and the library cache");
        lines.add(RUN + "springBootUtility thin " + ARG_SOURCE_APP + "=" + "/staging/" + "${APP_FILE}" + " "
                + ARG_DEST_THIN_APP + "=" + "/staging/" + "thin-${APP_FILE}" + " " + ARG_DEST_LIB_CACHE + "="
                + "/staging/" + LIB_INDEX_CACHE);

        lines.add("\n");
        lines.add("# Final stage, only copying the liberty installation (includes primed caches)");
        lines.add("# and the lib.index.cache and thin application");
        lines.add(FROM + libertyImage);

        lines.add("\n");
        lines.add("ARG" + " " + "APP_FILE");

        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/" + LIB_INDEX_CACHE + " " + "/" + LIB_INDEX_CACHE);

        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/thin-${APP_FILE}" + " "
                + "/config/dropins/spring/thin-${APP_FILE}");
        Files.write(dockerFile.toPath(), lines, Charset.forName("UTF-8"));
    }
}
