package io.openliberty.boost.common.docker;

public class DockerParameters {
    
    private final String DEPENDENCY_FOLDER;
    
    public DockerParameters(String dependencyFolder) {
        this.DEPENDENCY_FOLDER = dependencyFolder;
    }
    
    public String getDependencyFolder() {
        return DEPENDENCY_FOLDER;
    }

}
