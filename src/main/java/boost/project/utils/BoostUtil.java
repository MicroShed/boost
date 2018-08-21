package boost.project.utils;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import boost.project.BoostException;

public class BoostUtil {

    public static boolean isLibertyJar(File artifact) throws BoostException, IOException {
        boolean isLibertyJar;
        JarFile artifactJar;
        
        try {
            artifactJar = new JarFile(artifact);
            isLibertyJar =  artifactJar.getEntry("wlp") != null;
        } catch (Exception e) {
            throw new BoostException("Error checking Liberty artifact.", e);
        }
        
        if(artifactJar != null) {
            artifactJar.close();
        }
        
        return isLibertyJar;
    }
    
}
