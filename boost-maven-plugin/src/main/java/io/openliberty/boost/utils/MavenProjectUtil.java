package io.openliberty.boost.utils;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;


public class MavenProjectUtil {

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

}
