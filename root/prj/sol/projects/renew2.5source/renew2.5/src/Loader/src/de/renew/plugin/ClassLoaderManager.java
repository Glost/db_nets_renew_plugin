package de.renew.plugin;



/**
 * The PluginManager uses a classloader manager to get a system dependent classloader.
 *
 * @author Dominic Dibbern
 * @author Michael Duvigneau
 * @version 1.1
 * @date 11.09.2012
 *
 */
public interface ClassLoaderManager {

    /**
     * Return the ClassLoader instance used to load the plugins.
     * @throws IllegalStateException
     *   if the class loader manager has not been initialized.
     */
    public PluginClassLoader getPluginClassLoader();

    /**
     * Returns the ClassLoader instance used to load the user defined.
     * @throws IllegalStateException
     *   if the class loader manager has not been initialized.
     */
    public ClassLoader getBottomClassLoader();

    /**
     * Returns the system classloader
     * @throws IllegalStateException
     *   if the class loader manager has not been initialized.
     */
    public ClassLoader getSystemClassLoader();

    /**
     * Returns a new ClassLoader instance, which can be used to load user defined content.
     * @throws IllegalStateException
     *   if the class loader manager has not been initialized.
     */
    public ClassLoader getNewBottomClassLoader();

    /**
     * Initializes this manager's class loaders.
     * Queries to all <code>get...()</code> methods throw
     * an <code>IllegalStateException</code> before initialization.
     */
    public void initClassLoaders();
}