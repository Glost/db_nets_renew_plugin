/*
 * Created on Jul 30, 2004
 *
 */
package de.renew.plugin;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;


/**
 * @author Sven Offermann
 *
 */
public class Loader {
    //Directory that contains external library need by renew
    private static final String DIR_COMMONLIBS = "libs";

    //Filename of the jar file that contains all necessary files to load renew
    private static final String JARFILE_LOADER = "loader.jar";

    public static void main(String[] args) {
        try {
            // Create new ClassLoader that knows all lib classes
            // from the jars in the libs directory and create a 
            // new PluginManager instance with this new ClassLoader.
            // get the location of the libs directory
            URL url = Loader.class.getProtectionDomain().getCodeSource()
                                  .getLocation();

            String base = url.toExternalForm();
            base = base.substring(0, base.lastIndexOf("/"));
            url = new URL(base + "/");
            loadRenew(url, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to find alls jars in url to create a new classpath for renew. Looks inside
     * all found jars for nested jars. Invokes the PluginManagers main method with args
     * after the new cp has been created.
     * @param baseURL to the directory that contains loader.jar,
     * the plugin folder and the external liberaries folder
     * @param args the arguments that should be passed to renew
     * @throws MalformedURLException
     */
    public static void loadRenew(URL baseURL, String[] args)
            throws MalformedURLException {
        //URL baseURL = url;
        String base = baseURL.toExternalForm();
        URL libURL = new URL(base + DIR_COMMONLIBS);


        // get classPath from the actual system class loader
        Vector<URL> newCP = new Vector<URL>();
        URL[] oldCP = ((URLClassLoader) ClassLoader.getSystemClassLoader())
                          .getURLs();
        for (int x = 0; x < oldCP.length; x++) {
            newCP.add(oldCP[x]);
        }


        // add all jars in the libs directory and its subdirectories
        // to the new classpath
        File libsDir = null;
        try {
            libsDir = new File(new URI(libURL.toExternalForm()));
            newCP.addAll(getJars(libsDir));
        } catch (RuntimeException e) {
            System.out.println("Loader: Libraries not found. Check your classpath! Classpath "
                               + "should point to loader.jar only.\n"
                               + "Was looking in:" + libsDir);
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.out.println("Could not parse URL as URI. Could not load jars"
                               + libURL);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL. Could not load jars" + libURL);
            e.printStackTrace();
        }


        // Try to include possible internal libs of the loader.jar
        // look into jar file if there are additional libs jars 
        // includes
        String jarURL = "";
        try {
            jarURL = "jar:" + baseURL.toExternalForm() + JARFILE_LOADER + "!/";
            JarURLConnection jarConnection = (JarURLConnection) new URL(jarURL)
                                             .openConnection();
            JarFile jar = jarConnection.getJarFile();
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                if ((entry.getName().startsWith("libs/"))
                            && (entry.getName().endsWith(".jar"))) {
                    newCP.add(new URL(jarURL + entry.getName()));
                }
            }
        } catch (ZipException e2) {
            System.out.println("IO excption while unpacking jar" + jarURL);
            e2.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO excption while creating classpath");
            e.printStackTrace();
        }

        URL[] urls = newCP.toArray(new URL[] {  });
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader cl = new URLClassLoader(urls, parent);


        // Load PluginManager with new ClassLoader and invoke
        // PluginManagers static main method with the given 
        // arguments.
        Class<?> pmClass;
        try {
            pmClass = cl.loadClass("de.renew.plugin.PluginManager");
            Method mainMethod = pmClass.getDeclaredMethod("main",
                                                          new Class<?>[] { args
                                                                           .getClass(), URL.class });
            mainMethod.invoke(null, new Object[] { args, baseURL });
        } catch (ClassNotFoundException e) {
            System.out.println("Class PluginManager not found check your classpath");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.out.println("PluginManager has no main method. Wrong version ?");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgument error while invoking PluginManagers main class");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccess error while invoking PluginManagers main class");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("InvocationTarget error while invoking PluginManagers main class");
            e.printStackTrace();
        }
    }

    private static Vector<URL> getJars(File dir) throws MalformedURLException {
        Vector<URL> v = new Vector<URL>();

        File[] files = dir.listFiles();
        for (int x = 0; x < files.length; x++) {
            if (files[x].isFile()) {
                if (files[x].getName().endsWith(".jar")) {
                    v.add(files[x].toURI().toURL());
                }
            } else if (files[x].isDirectory()) {
                v.addAll(getJars(files[x]));
            }
        }

        return v;
    }
}