package io.openliberty.boost.common.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

import io.openliberty.boost.common.BoostLoggerI;

public class ClassFinderUtil {
    public static List<Class<?>> findClassesImplementing(final Class<?> interfaceClass, final String fromPackage, ClassLoader classLoader, BoostLoggerI logger) {
        final List<Class<?>> rVal = new ArrayList<Class<?>>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(classLoader, fromPackage);
            if (targets != null) {
                for (Class<?> aTarget : targets) {
                    if (aTarget == null) {
                        continue;
                    }
                    else if (aTarget.equals(interfaceClass)) {
                        logger.debug("Found the interface definition.");
                        continue;
                    }
                    else if (!interfaceClass.isAssignableFrom(aTarget)) {
                        logger.debug("Class '" + aTarget.getName() + "' is not a " + interfaceClass.getName());
                        continue;
                    }
                    else {
                        logger.debug("Found class " + aTarget.getName() + " implementing " + interfaceClass.getName() + ".");
                        rVal.add(aTarget);
                    }
                }
            }
        }
        catch (ClassNotFoundException e) {
            logger.debug("Error reading package name.");
            e.printStackTrace();
        }
        catch (IOException e) {
            logger.debug("Error reading classes in package.");
            e.printStackTrace();
        }

        return rVal;
    }

    /**
     * Load all classes from a package.
     * 
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class[] getAllClassesFromPackage(ClassLoader classLoader, final String packageName) throws ClassNotFoundException, IOException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String fileName = resource.getFile();
            dirs.add(new File(fileName.substring(5, fileName.length() - (path.length() + 2))));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClassesInJar(directory, packageName, classLoader));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Finds classes in a jar.
     * 
     * @param jarFile
     * @param packageName
     * @param classLoader
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */

    public static List<Class<?>> findClassesInJar(File jarFile, String packageName, ClassLoader classLoader) throws ClassNotFoundException, IOException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        JarFile jar = new JarFile(jarFile);
        // Getting the files into the jar
        Enumeration<? extends JarEntry> enumeration = jar.entries();

        // Iterates into the files in the jar file
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();

            // Is this a class?
            if (zipEntry.getName().endsWith(".class")) {

                // Relative path of file into the jar.
                String className = zipEntry.getName();

                // Complete class name
                className = className.replace(".class", "").replace("/", ".");
                // Load class definition from JVM
                Class<?> clazz = classLoader.loadClass(className);

                classes.add(clazz);
            }
        }

        return classes;
    }
}