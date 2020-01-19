package de.renew.plugin;

import java.io.File;
import java.io.Serializable;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * This ClassLoader is responsible for loading user defined classes.
 * To find classes it searches the locations given in the property
 * <code>de.renew.classPath</code>..
 * To set additional locations it allows adding classpath items
 * dynamically.
 */
public class BottomClassLoader extends URLClassLoader implements Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BottomClassLoader.class);

    /**
     * the name of the property used to set the ClassPath
     */
    public final static String CLASSPATH_PROP_NAME = "de.renew.classPath";


    /**
     * Creates an instance of the BottomClassLoader.
     *
     * @param urls the urls used to find class files
     * @param parent the parent classloader
     */
    public BottomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Creates an instance of the BottomClassLoader. To determine the urls
     * for class loading the value of the property <code>de.renew.classPath</code>
     * will be evaluated.
     *
     * @param parent the parent classloader
     */
    public BottomClassLoader(ClassLoader parent) {
        super(new URL[0], parent);


        // evaluate the value of environment variable de.renew.classPath
        // and add classpath urls
        PluginProperties props = PluginProperties.getUserProperties();
        String classpath = props.getProperty(CLASSPATH_PROP_NAME,
                                             props.getProperty("user.dir"));

        if (classpath != null) {
            String[] parts = classpath.split(new String(new char[] { File.pathSeparatorChar }));
            StringBuffer effectiveClasspath = new StringBuffer();
            boolean firstEffectiveClasspath = true;
            for (int x = 0; x < parts.length; x++) {
                try {
                    // if the part of the classpath is a legal url we can parse
                    // and add it directly.
                    URL url = new URL(parts[x]);
                    addURL(url);
                    if (firstEffectiveClasspath) {
                        firstEffectiveClasspath = false;
                    } else {
                        effectiveClasspath.append(File.pathSeparatorChar);
                    }
                    effectiveClasspath.append(url.toString());
                } catch (MalformedURLException e1) {
                    // The part of the classpath was no legal url. Possibly it is
                    // a path to a local file or directory. So we should try to
                    // interpret it this way. 
                    File path = new File(parts[x]);


                    // if a file or path exists, add the corresponding url to 
                    // the classpath
                    if (path.exists()) {
                        try {
                            addURL(path.toURI().toURL());
                            if (firstEffectiveClasspath) {
                                firstEffectiveClasspath = false;
                            } else {
                                effectiveClasspath.append(File.pathSeparatorChar);
                            }
                            effectiveClasspath.append(path.toString());
                        } catch (MalformedURLException e2) {
                            logger.warn(this + ": could not add " + parts[x]
                                        + " to simulation classpath.");
                            logger.debug("Malformed URL: " + parts[x], e1);
                            logger.debug("Malformed URL for file: " + path, e2);
                        }
                    } else {
                        logger.warn(this + ": could not add " + parts[x]
                                    + " to simulation classpath.");
                        logger.debug("Malformed URL: " + parts[x], e1);
                        logger.debug("Nonexisting file: " + path);
                    }
                }
            }
            props.setProperty(CLASSPATH_PROP_NAME, effectiveClasspath.toString());
        } else {
            logger.warn(this + ": no classpath configured for simulations.");
        }
    }

    /**
     * Add the given URL to the list where classes are sought for.
     */
    public void addURL(URL url) {
        logger.debug(this + " adding URL " + url);
        super.addURL(url);
    }
}