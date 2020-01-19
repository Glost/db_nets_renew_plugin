package de.renew.plugin;



/**
 * A default classloader manager implementation, which can be used on most systems.
 *
 * @author Dominic Dibbern
 * @version 1.0
 * @date 08.02.2012
 *
 */
public class DefaultClassLoaderManager implements ClassLoaderManager {
    private PluginClassLoader _pluginCL;
    private ClassLoader _systemCL;
    private ClassLoader _bottomCL;

    @Override
    public PluginClassLoader getPluginClassLoader() {
        if (_pluginCL == null) {
            throw new IllegalStateException("DefaultClassLoaderManager is not initialized.");
        }
        return _pluginCL;
    }

    @Override
    public ClassLoader getBottomClassLoader() {
        if (_bottomCL == null) {
            throw new IllegalStateException("DefaultClassLoaderManager is not initialized.");
        }
        return _bottomCL;
    }

    @Override
    public ClassLoader getSystemClassLoader() {
        if (_systemCL == null) {
            throw new IllegalStateException("DefaultClassLoaderManager is not initialized.");
        }
        return _systemCL;
    }

    @Override
    public ClassLoader getNewBottomClassLoader() {
        return new BottomClassLoader(getPluginClassLoader());
    }

    @Override
    public void initClassLoaders() {
        _pluginCL = new PluginClassLoader(getClass().getClassLoader());
        _systemCL = getClass().getClassLoader();
        _bottomCL = new BottomClassLoader(_pluginCL);
    }
}