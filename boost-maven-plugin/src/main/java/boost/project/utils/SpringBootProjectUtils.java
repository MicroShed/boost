package boost.project.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static boost.project.utils.ConfigConstants.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class SpringBootProjectUtils {

    private static final String SERVER_PORT = "server.port";
    private static final String DEFAULT_SERVER_PORT = "8080";

    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    
    public static Properties getSpringBootServerProperties(String buildDir) throws IOException {

        Properties serverProperties = new Properties();

        Properties allProperties = new Properties();
        InputStream input = null;

        try {

            File appProperties = new File(buildDir + "/classes", APPLICATION_PROPERTIES_FILE);

            if (appProperties.exists()) {
                input = new FileInputStream(appProperties.getAbsolutePath());
                allProperties.load(input);
            }

        } finally {
            if (input != null) {
                input.close();
            }
        }

        String serverPort = (String) allProperties.getOrDefault(SERVER_PORT, DEFAULT_SERVER_PORT);
        serverProperties.setProperty(SERVER_PORT, serverPort);

        return serverProperties;
    }

    public static List<String> getLibertyFeaturesNeeded(MavenProject mavenProject, Log consoleLogger) {

        List<String> featuresToAdd = new ArrayList<String>();

        String springBootVersion = findSpringBootVersion(mavenProject);

        if (springBootVersion != null) {
        	
            String springBootFeature = null;

            if (springBootVersion.startsWith("1.")) {
                springBootFeature = SPRING_BOOT_15;
            } else if (springBootVersion.startsWith("2.")) {
                springBootFeature = SPRING_BOOT_20;
            } else {
                // log error for unsupported version
                consoleLogger
                .error("No supporting feature available in Open Liberty for org.springframework.boot dependency with version "
                        + springBootVersion);
            }

            if (springBootFeature != null) {
                consoleLogger
                .info("Adding the " + springBootFeature + " feature to the Open Liberty server configuration.");
                featuresToAdd.add(springBootFeature);
            }

        } else {
            consoleLogger.info(
                    "The springBoot feature was not added to the Open Liberty server because no spring-boot-starter dependencies were found.");
        }

        // Add any other Liberty features needed depending on the spring boot
        // starters defined
        List<String> springBootStarters = getSpringBootStarters(mavenProject);

        for (String springBootStarter : springBootStarters) {

            if (springBootStarter.equals("spring-boot-starter-web")) {
                // Add the servlet-4.0 feature
                featuresToAdd.add(SERVLET_40);
            }

            // TODO: Add more dependency mappings if needed.
        }

        return featuresToAdd;
    }

    /**
     * Detect spring boot version dependency
     */
    public static String findSpringBootVersion(MavenProject project) {
        String version = null;

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            if ("org.springframework.boot".equals(art.getGroupId()) && "spring-boot".equals(art.getArtifactId())) {
                version = art.getVersion();
                break;
            }
        }

        return version;
    }

    /**
     * Get all dependencies with "spring-boot-starter-*" as the artifactId.
     * These dependencies will be used to determine which additional Liberty
     * features need to be enabled.
     * 
     */
    private static List<String> getSpringBootStarters(MavenProject project) {

        List<String> springBootStarters = new ArrayList<String>();

        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact art : artifacts) {
            if (art.getArtifactId().contains("spring-boot-starter")) {
                springBootStarters.add(art.getArtifactId());
            }
        }

        return springBootStarters;
    }
}
