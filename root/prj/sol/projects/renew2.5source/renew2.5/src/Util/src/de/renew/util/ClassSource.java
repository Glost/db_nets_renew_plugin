package de.renew.util;

import de.renew.plugin.PluginManager;

import java.io.IOException;
import java.io.ObjectInput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * The <code>ClassSource</code> is used during simulations to feature
 * reloading of classes. By default (or after <code>setClassLoader(null)</code>
 * has been called), it tries to use the {@link PluginManager}'s class loader.
 * <p>
 * By calling {@link #setClassLoader}, a different class loader can be
 * configured.  Any other method of this class will then use the configured
 * class loader.
 * </p>
 *
 * @author Olaf Kummer
 * @author Michael Duvigneau
 **/
public class ClassSource {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ClassSource.class);
    private static ClassLoader loader = null;
    private static Object deserializer = null;
    private static Method deserializeMethod = null;

    /** This class is completely static. **/
    private ClassSource() {
    }

    /**
     * Reconfigure the <code>ClassSource</code> to use the given class
     * loader.
     *
     * @param newLoader the <code>ClassLoader</code> to use for future
     *                  calls to all other methods of this class.
     *                  If <code>null</code>, the default class loader will
     *                  be used.
     **/
    public static synchronized void setClassLoader(ClassLoader newLoader) {
        loader = newLoader;
        logger.debug("Configured ClassSource with class loader "
                     + newLoader.toString());
        try {
            // Note: This hack only works, if the class
            // ReloadableDeserializerImpl is known to the custom class
            // loader!
            deserializer = classForName("de.renew.util.ReloadableDeserializerImpl")
                               .newInstance();
            deserializeMethod = deserializer.getClass()
                                            .getDeclaredMethod("readObject",
                                                               new Class<?>[] { ObjectInput.class });
            logger.debug("Derived deserializer with class loader "
                         + deserializer.getClass().getClassLoader());
        } catch (Exception e) {
            deserializer = null;
            logger.error("Cannot generate deserializer: " + e, e);
            throw new RuntimeException("Cannot generate deserializer.", e);
        }
    }

    /**
     * Returns the <code>ClassLoader</code> currently established for this
     * class source. Callers should keep in mind that the established class
     * loader can change at any time, so the reference should not be stored
     * for long time.
     *
     * @return the currently configured <code>ClassLoader</code>. Never
     *   returns <code>null</code>. If no special class loader has been
     *   configured, returns the default class loader.
     **/
    public static synchronized ClassLoader getClassLoader() {
        if (loader == null) {
            return getDefaultClassLoader();
        }
        return loader;
    }

    /**
     * Returns the default class loader of this <code>ClassSource</code>.
     * This is either the <code>PluginManager</code>'s bottom class loader or (if
     * not available) the class loader of this class.
     *
     * @return the default class loader.
     **/
    private static ClassLoader getDefaultClassLoader() {
        ClassLoader defaultLoader = PluginManager.getInstance()
                                                 .getBottomClassLoader();
        if (defaultLoader == null) {
            defaultLoader = ClassSource.class.getClassLoader();
        }
        return defaultLoader;
    }

    /**
     * Calls <code>Class.forName(...)</code> with the currently configured
     * <code>ClassLoader</code> and the given name.
     *
     * @param name  the name of the class to load.
     * @return   the loaded class
     * @exception ClassNotFoundException  if the class code could not be found.
     * @see Class#forName
     **/
    public static synchronized Class<?> classForName(String name)
            throws ClassNotFoundException {
        if (loader == null) {
            // There is no ClassLoader set. Use the default BottomClassLoader
            // from the PluginManager. Without a BottomClassLoader the user defined
            // classes from the CLASSPATH are not available. Support for dynamic
            // class reloading can be realised by generating and setting a new
            // instance of a BottomClassLoader using the @link #setClassLoader(ClassLoader newLoader)
            // method.
            logger.debug("Looking up " + name + " in default class source.");
            return Class.forName(name, true,
                                 PluginManager.getInstance()
                                              .getBottomClassLoader());
        }


        // Use class loader to facilitate possible reload.
        logger.debug("Looking up " + name + " in class source " + loader + ".");
        return Class.forName(name, true, loader);
    }

    /**
     * Makes sure to establish a reloaded object on the call stack
     * so that deserialization uses our class loader and not the
     * default class loader.
     *
     * @param input the input stream to use for deserialization.
     * @return the deserialized object.
     **/
    public static Object readObject(ObjectInput input)
            throws ClassNotFoundException, IOException {
        if (deserializer == null) {
            return input.readObject();
        } else {
            try {
                return deserializeMethod.invoke(deserializer,
                                                new Object[] { input });
            } catch (InvocationTargetException ie) {
                Throwable e = ie.getTargetException();
                if (e instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                }
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}