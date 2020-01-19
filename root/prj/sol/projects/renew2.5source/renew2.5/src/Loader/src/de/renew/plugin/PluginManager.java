package de.renew.plugin;

import org.apache.log4j.Logger;

import de.renew.plugin.DependencyCheckList.DependencyElement;
import de.renew.plugin.command.CLCommand;
import de.renew.plugin.command.ExitCommand;
import de.renew.plugin.command.GCCommand;
import de.renew.plugin.command.GetPropertyCommand;
import de.renew.plugin.command.InfoCommand;
import de.renew.plugin.command.ListCommand;
import de.renew.plugin.command.LoadCommand;
import de.renew.plugin.command.NoOpCommand;
import de.renew.plugin.command.ScriptCommand;
import de.renew.plugin.command.SetPropertyCommand;
import de.renew.plugin.command.SleepCommand;
import de.renew.plugin.command.UnloadCommand;
import de.renew.plugin.di.Container;
import de.renew.plugin.di.MissingDependencyException;
import de.renew.plugin.di.ServiceContainer;
import de.renew.plugin.load.DIPluginLoader;
import de.renew.plugin.load.PluginLoaderComposition;
import de.renew.plugin.load.SimplePluginLoader;
import de.renew.plugin.locate.PluginJarLocationFinder;
import de.renew.plugin.locate.PluginLocationFinders;
import de.renew.plugin.locate.PluginSubDirFinder;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


/**
 * This class is the central management facility for providing Plugins with
 * access to Renew. When the Singleton instance of this class is created, the
 * statical plugins will be loaded. Furthermore, this class will be the
 * interface for dynamical plugins that can log into it at run-time.
 */
public class PluginManager implements Serializable, CommandsProvider {
    public static final String COMMAND_SEPERATOR = "---";
    public static Logger logger = Logger.getLogger(PluginManager.class);

    /**
     * The timeout for cleaning up a plugin, in microseconds. The value is fixed
     * to {@value} microseconds.
     **/
    public static final int CLEANUP_TIMEOUT = 20000;

    /**
     * The name of the system property, which define the path for the plugins.
     */
    public static final String PLUGIN_LOCATIONS_PROPERTY = "pluginLocations";

    /** Singleton member instance. **/
    protected static PluginManager _instance;
    /** The list managing the dependencies among the plugins. **/
    DependencyCheckList<IPlugin> _dependencyList = new DependencyCheckList<IPlugin>();

    /**
     * The map of recognized commands and their associated executors. Maps from
     * <code>String</code> to {@link CLCommand} objects.
     **/
    private Map<String, CLCommand> _commands = Collections.synchronizedMap(new TreeMap<String, CLCommand>());

    /**
     * Contains the listeners that register for PluginManager events.
     */
    private Set<IPluginManagerListener> _managerListener;

    /**
     * The set of plugins preventing the system from automatical termination.
     **/
    private Set<IPlugin> _blockers = Collections.synchronizedSet(new HashSet<IPlugin>());

    /**
     * This flag indicates that the system termination thread is running. It is
     * set by the {@link #stopSynchronized()} method only.
     **/
    private boolean _Terminating = false;
    protected PluginLocationFinders _locationFinder = PluginLocationFinders
                                                          .getInstance();
    protected PluginLoaderComposition _loader = new PluginLoaderComposition();
    private final ClassLoaderManager _classLoaderManager;

    /**
     * The location of loader.jar
     */
    protected static URL _loaderLocation;
    private LogStrategy _logStrategy;

    // Location of the preferences (expected in home directory).
    // use with method getPreferenceLocation.
    private static final String PREF_DIR = ".renew";
    private ArrayList<CommandsListener> commandsListener = new ArrayList<CommandsListener>();
    private final ServiceContainer _container;

    /**
     * Get the location of loader.jar
     *
     * @return url of loader.jar
     */
    public static URL getLoaderLocation() {
        if (_loaderLocation == null) { // to prevent NullPointerException
            _loaderLocation = getDefaultLoaderLocation();
        }
        return _loaderLocation;
    }

    private static URL getDefaultLoaderLocation() {
        URL url = Loader.class.getProtectionDomain().getCodeSource()
                              .getLocation();
        String base = url.toExternalForm();
        base = base.substring(0, base.lastIndexOf("/"));
        try {
            return new URL(base + "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static LogStrategy getDefaultLogStrategy() {
        return new DefaultLogStrategy();
    }

    private static ClassLoaderManager getDefaultClassLoaderManager() {
        return new DefaultClassLoaderManager();
    }

    protected PluginManager(URL url, LogStrategy logStrategy,
                            ClassLoaderManager classLoaderManager) {
        if (logStrategy == null) {
            logStrategy = getDefaultLogStrategy();
        }
        _logStrategy = logStrategy;
        if (classLoaderManager == null) {
            classLoaderManager = getDefaultClassLoaderManager();
        }
        _classLoaderManager = classLoaderManager;

        _logStrategy.configureLogging();
        _classLoaderManager.initClassLoaders();

        _managerListener = new HashSet<IPluginManagerListener>();

        initLocationFinders();

        _container = new Container();
        _container.set(PluginManager.class, this);
        _container.set(ServiceContainer.class, _container);
        _container.set(ClassLoaderManager.class, _classLoaderManager);

        // initialize the loaders
        _loader = new PluginLoaderComposition();
        final PluginClassLoader loader = getPluginClassLoader();
        _loader.addLoader(new SimplePluginLoader(loader, _container));
        _loader.addLoader(new DIPluginLoader(loader, _container));

        _commands.put("", new NoOpCommand());
        _commands.put("help", new HelpCommand());
        _commands.put("exit", new ExitCommand());
        _commands.put("load", new LoadCommand());
        _commands.put("list", new ListCommand());
        _commands.put("unload", new UnloadCommand());
        _commands.put("info", new InfoCommand());
        _commands.put("script", new ScriptCommand());
        _commands.put("set", new SetPropertyCommand());
        _commands.put("get", new GetPropertyCommand());
        _commands.put("gc", new GCCommand());
        _commands.put("packageCount",
                      _classLoaderManager.getPluginClassLoader().new PackageCountCommand());
        _commands.put(SleepCommand.CMD, new SleepCommand());
    }

    public static synchronized PluginManager getInstance() {
        if (_instance == null) {
            createInstance(getDefaultLoaderLocation(), getDefaultLogStrategy(),
                           getDefaultClassLoaderManager());
        }

        return _instance;
    }

    private static synchronized PluginManager createInstance(URL url,
                                                             LogStrategy logStrategy,
                                                             ClassLoaderManager classLoaderManager) {
        if (_instance != null) {
            throw new IllegalStateException("Cannot create PluginManager singleton, is already there.");
        }
        _instance = new PluginManager(url, logStrategy, classLoaderManager);
        _instance.initPlugins();
        return _instance;
    }

    /**
     * Adds a Listener to the PluginManager
     *
     * @param l
     *            the listener
     */
    public void addPluginManagerListener(IPluginManagerListener l) {
        _managerListener.add(l);
    }

    /**
     * Removes a Listener from the PluginManager
     *
     * @param l
     *            the listener
     */
    public void removePluginManagerListener(IPluginManagerListener l) {
        _managerListener.remove(l);
    }

    public ServiceContainer getServiceContainer() {
        return _container;
    }

    private void serviceAdded(Collection<String> services, IPlugin provider) {
        for (IPluginManagerListener listener : _managerListener) {
            for (String service : services) {
                listener.serviceAdded(service, provider);
            }
        }
    }

    private void serviceRemoved(Collection<String> services, IPlugin provider) {
        for (IPluginManagerListener listener : _managerListener) {
            for (String service : services) {
                listener.serviceRemoved(service, provider);
            }
        }
    }

    public void addPlugin(IPlugin plugin)
            throws DependencyNotFulfilledException {
        if (!checkDependenciesFulfilled(plugin)) {
            throw new DependencyNotFulfilledException("Cannot add " + plugin);
        }

        try {
            logger.debug("************ INITIALIZING " + plugin.getName()
                         + " ********");
            plugin.init();
            _dependencyList.addElement(DependencyElement.create(plugin));
            serviceAdded(plugin.getProperties().getProvisions(), plugin);
        } catch (RuntimeException e) {
            logger.error("PluginManager: adding of " + plugin + " failed: " + e
                         + "\n Plugin location: "
                         + plugin.getProperties().getURL());
            logger.debug(e.toString(), e);
        } catch (LinkageError e) {
            logger.error("PluginManager: adding of " + plugin + " failed: " + e
                         + "\n Plugin location: "
                         + plugin.getProperties().getURL());
            logger.debug(e.toString(), e);
        }
    }

    /**
     * Checks whether the dependencies specified by the given
     * <code>PluginProperties</code> object are fulfilled by the current set of
     * loaded plugins.
     *
     * @param props
     *            the properties that specify the dependencies.
     *
     * @return <code>true</code>, if a plugin with the given <code>props</code>
     *         could be added to the set of plugins. Returns <code>false</code>,
     *         if some dependency of the plugin would not be fulfilled.
     **/
    public synchronized boolean checkDependenciesFulfilled(PluginProperties props) {
        return _dependencyList.dependencyFulfilled(DependencyElement.create(props));
    }

    /**
     * Checks whether the dependencies specified by the given
     * <code>IPlugin</code> object are fulfilled by the current set of loaded
     * plugins.
     *
     * @param plugin
     *            the plugin that specifies the dependencies.
     *
     * @return <code>true</code>, if the given <code>plugin</code> could be
     *         added to the set of plugins. Returns <code>false</code>, if some
     *         dependency of the plugin would not be fulfilled.
     **/
    public synchronized boolean checkDependenciesFulfilled(IPlugin plugin) {
        return _dependencyList.dependencyFulfilled(DependencyElement.create(plugin));
    }

    /**
     * Returns the plugin with the given name, null if no such plugin is
     * present.
     */
    public IPlugin getPluginByName(String pluginName) {
        IPlugin found = null;
        pluginName = pluginName.replaceAll("_", " ").trim();
        logger.debug("PluginManager looking for " + pluginName);
        List<IPlugin> plugins = getPlugins();
        for (int i = 0; i < plugins.size(); i++) {
            IPlugin current = plugins.get(i);
            if (current.getName().equals(pluginName)) {
                found = current;
                break;
            }
        }
        if (found == null) {
            found = getPluginByAlias(pluginName);
        }
        return found;
    }

    /**
     * Returns the plugin of the given class, null if no such plugin is
     * present.
     */
    public IPlugin getPluginByClass(Class<?> clazz) {
        logger.debug("PluginManager looking for " + clazz.getName());
        try {
            final Object o = _container.get(clazz);

            if (o instanceof IPlugin) {
                return (IPlugin) o;
            }

            return null;
        } catch (MissingDependencyException e) {
            return null;
        }
    }

    /**
     * Returns the plugin with the given alias, null if no such plugin is
     * present.
     */
    public IPlugin getPluginByAlias(String pluginAlias) {
        IPlugin found = null;
        logger.debug("PluginManager looking for " + pluginAlias);
        List<IPlugin> plugins = getPlugins();
        for (int i = 0; i < plugins.size(); i++) {
            IPlugin current = plugins.get(i);

            if (pluginAlias.equals(current.getAlias())) {
                found = current;
                break;
            }
        }
        return found;
    }

    /**
     * Returns a Collection containing the Plugins that provide the given
     * service.
     */
    public Collection<IPlugin> getPluginsProviding(String service) {
        Collection<IPlugin> result = new Vector<IPlugin>();
        for (IPlugin plugin : getPlugins()) {
            if (plugin.getProperties().getProvisions().contains(service)) {
                result.add(plugin);
            }
        }
        return result;
    }

    /**
     * Returns a collection of plug-ins which require the given <b>service</b>.<br>
     * <span style="color:red;">NOT safe for concurrent access.</span>
     *
     * @param service
     *            The required service
     * @return Collection&lt;IPlugin&gt;
     *
     * @author Eva Mueller
     * @modified Nov 14, 2010
     * @version 0.1
     */
    public Collection<IPlugin> getPluginsRequiring(String service) {
        Collection<IPlugin> result = new Vector<IPlugin>();
        Iterator<IPlugin> it = getPlugins().iterator();
        while (it.hasNext()) {
            IPlugin p = it.next();
            if (p.getProperties().getRequirements().contains(service)) {
                result.add(p);
                continue;
            }
            Iterator<String> requirements = p.getProperties().getRequirements()
                                             .iterator();
            while (requirements.hasNext()) {
                String requirement = requirements.next();
                if (requirement.startsWith(service)) {
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Iterate through all plugins and initialize them.
     */
    public void initPlugins() {
        Iterator<IPlugin> plugins = getPlugins().iterator();
        while (plugins.hasNext()) {
            IPlugin p = plugins.next();
            logger.debug("initializing " + p.getName() + ", loaded from "
                         + p.getClass().getClassLoader());
            logger.debug("************ INITIALIZING " + p.getName()
                         + " ********");
            p.init();
            serviceAdded(p.getProperties().getProvisions(), p);
        }
    }

    /**
     * Return the ClassLoader instance used to load the plugins.
     */
    public PluginClassLoader getPluginClassLoader() {
        return _classLoaderManager.getPluginClassLoader();
    }

    /**
     * Returns the ClassLoader instance used to load the user defined .
     */
    public ClassLoader getBottomClassLoader() {
        return _classLoaderManager.getBottomClassLoader();
    }

    /**
     * Returns a new ClassLoader instance, which can be used to load user defined content.
     */
    public ClassLoader getNewBottomClassLoader() {
        return _classLoaderManager.getNewBottomClassLoader();
    }

    /**
     * Returns the system classloader
     */
    public ClassLoader getSystemClassLoader() {
        return _classLoaderManager.getSystemClassLoader();
    }

    /**
     * Get the PluginLoader used to create plugin instances
     */
    PluginLoaderComposition getPluginLoader() {
        return _loader;
    }

    /**
     * Finds all candidate locations and tries to load a plugin from each
     * location. The currently configured <code>PluginLoader</code> is used for
     * the job.
     **/
    public synchronized void loadPlugins() {
        _loader.loadPlugins();
    }

    /**
     * Loads a plugin from the given URL, if possible. The currently configured
     * <code>PluginLoader</code> is used for the job.
     *
     * @param url
     *            the plugin's location as <code>URL</code>.
     **/
    public synchronized IPlugin loadPlugin(URL url) {
        return _loader.loadPluginFromURL(url);
    }

    /**
     * Return a list of all loaded Plugins.
     *
     * @return List&lt;{@link IPlugin}&gt;
     *
     * @author Eva Mueller
     * @modified Nov 14, 2010
     * @version 0.1
     */
    public List<IPlugin> getPlugins() {
        return _dependencyList.getFulfilledObjects();
    }

    /**
     * Register the given command. It will be called when the user types the
     * given String into the console. It is not checked whether the String
     * already exists, so an existing command can be overridden!
     *
     * @param command
     *            The String identifying
     * @param clcommand
     *            The command to add
     */
    public void addCLCommand(String command, CLCommand clcommand) {
        _commands.put(command, clcommand);
        notifyCommandAdded(command, clcommand);
    }

    /**
     * Unregister the command identified by the given String.
     */
    public void removeCLCommand(String command) {
        _commands.remove(command);
        notifyCommandRemoved(command);
    }

    /**
     * Return a Map containing all commands as values, with the identifying
     * Strings as keys.
     */
    public Map<String, CLCommand> getCLCommands() {
        return _commands;
    }

    /**
     * Adds the given plugin to the set of exit blockers. If the exit blocker
     * set becomes non-empty, the plugin system will not be automatically
     * terminated.
     *
     * @param blocker
     *            the plugin that prevents automatic termination of the plugin
     *            system.
     **/
    public void blockExit(IPlugin blocker) {
        if (blocker != null) {
            logger.debug("PluginManager: registering exit blocker " + blocker);
            _blockers.add(blocker);
        }
    }

    /**
     * Removes the given plugin from the set of exit blockers. If the exit
     * blocker set becomes empty, the plugin system is automatically terminated.
     *
     * @param blocker
     *            the plugin that should not any longer prevent automatic
     *            termination of the plugin system. If this plugin object is not
     *            included in the set of exit blockers, the set will not be
     *            modified. Nevertheless, the set will be checked for emptyness.
     **/
    public void exitOk(IPlugin blocker) {
        synchronized (_blockers) {
            if (blocker != null) {
                logger.debug("PluginManager: unregistering exit blocker "
                             + blocker);
                _blockers.remove(blocker);
            }
            checkExit();
        }
    }

    /**
     * Checks whether there are any exit blockers registered, and shuts down the
     * plugin system, if not.
     **/
    public boolean checkExit() {
        synchronized (_blockers) {
            if (_blockers.isEmpty()) {
                logger.debug("PluginManager: no active plugins, shutting down.");

                // Since there might still be AWT threads running,
                // shut down the Java VM manually.
                // The asynchronous stop method is used to decouple the system
                // termination from the plugin that unknowingly initiated the
                // process. This also unrolls the endless loop when a plugin on
                // termination deregisters itself as exit blocker.
                stop();
                return true;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Active plugins blocking exit: "
                             + CollectionLister.toString(_blockers));
            }
        }
        return false;
    }

    /**
     * Terminates all plugins and the whole Java system. Whenever the
     * termination of any plugin fails, the termination process is cancelled,
     * too.
     * <p>
     * This method works asynchronously, it returns to the caller immediately
     * after initiating the termination process. Plugins are then terminated in
     * sequence (with respect to plugin dependencies), but concurrently to the
     * calling thread. Multiple concurrent calls to this method will be
     * sequentialized because each termination process is synchronized on the
     * PluginManager object.
     * </p>
     * There is no feedback to the calling thread, instead messages will be
     * printed to <code>System.out</code>.
     **/
    public void stop() {
        logger.debug("Initiating PluginManager termination.");
        Thread terminationThread = new Thread() {
            public void run() {
                if (stopSynchronized()) {
                    System.exit(0);
                }
            }
        };
        terminationThread.start();
    }

    /**
     * Stops the given plugin.
     * <p>
     * This method works asynchronously, it returns to the caller immediately
     * after initiating the termination process. The plugin is then terminated
     * (with respect to plugin dependencies) concurrently to the calling thread.
     * Multiple concurrent calls to this method will be sequentialized because
     * each termination process is synchronized on the PluginManager object.
     * </p>
     * There is no feedback to the calling thread, instead messages will be
     * printed to <code>System.out</code>.
     *
     * @param p
     *            the plugin to remove from the plugin system.
     **/
    public void stop(final IPlugin p) {
        logger.debug("Initiating termination of " + p + ".");
        Thread terminationThread = new Thread() {
            public void run() {
                stopSynchronized(p);
            }
        };
        terminationThread.start();
    }

    /**
     * Stops the given plugins. Works through the list <em>from back to
     * front</em>. If any plugin cannot be stopped due to some other dependent
     * plugin (it does not matter whether it is or is not included in the list
     * in front of the plugin it depends on), the list is not further processed.
     * <p>
     * Example: Assume that plugin B depends on plugin A. A call with the list
     * <code>[A, B]</code> will succeed. But a call with the list
     * <code>[B, A]</code> will fail because plugin A cannot be terminated due
     * to the dependent plugin B.
     * </p>
     * <p>
     * The reason for the backward processing is that lists of recursively
     * dependent plugins are easier to generate with the most basic plugin at
     * first position.
     * </p>
     * <p>
     * This method works asynchronously, it returns to the caller immediately
     * after initiating the termination process. The plugins are then terminated
     * (with respect to plugin dependencies) concurrently to the calling thread.
     * Multiple concurrent calls to this method will be sequentialized because
     * each termination process is synchronized on the PluginManager object.
     * </p>
     * There is no feedback to the calling thread, instead messages will be
     * printed to <code>System.out</code>.
     *
     * @param plugins
     *            the plugins to remove from the plugin system.
     **/
    public void stop(final List<IPlugin> plugins) {
        logger.debug("Initiating termination of " + plugins + ".");
        Thread terminationThread = new Thread() {
            public void run() {
                stopSynchronized(plugins);
            }
        };
        terminationThread.start();
    }

    /**
     * Tells whether the plugin system is about to terminate. This method only
     * returns true, if the {@link #stop()} method has been used to trigger the
     * system termination.
     * <p>
     * Please be aware that this method's result may be out of date before you
     * are able to interpret it. It is intended to be used by terminated plugins
     * that want to know whether it's just them or the whole system...
     * </p>
     *
     * @return <code>true</code> if the concurrent system termination thread is
     *         running.
     **/
    public boolean isStopping() {
        return _Terminating;
    }

    /**
     * Terminates all plugins. This method is called internally by
     * {@link #stop()}.
     **/
    public synchronized boolean stopSynchronized() {
        logger.debug("Stopping plugin system asynchronously.");
        _Terminating = true;
        try {
            List<IPlugin> pls = _dependencyList.getFulfilledObjects();
            return stopSynchronized(pls);
        } finally {
            _Terminating = false;
        }
    }

    /**
     * Terminates all plugins in the given list. This method is called
     * internally by {@link #stopSynchronized()} and {@link #stop(List)}.
     **/
    private synchronized boolean stopSynchronized(List<IPlugin> pls) {
        // int limit = pls.size();
        for (int i = pls.size() - 1; i >= 0; i--) {
            // ask all plugins if they can be stopped.
            final IPlugin toStop = pls.get(i);
            Object sync = new Object();
            if (logger.isDebugEnabled()) {
                logger.debug("Preparing stop of " + toStop);
            }
            int waitSeconds = toStop.getProperties()
                                    .getIntProperty("cleanupTimeout",
                                                    CLEANUP_TIMEOUT);
            int firstWait = waitSeconds;
            int secondWait = 1;
            if (waitSeconds > 10) {
                firstWait = waitSeconds / 10;
                secondWait = waitSeconds - firstWait;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("canShutDown timeout is " + waitSeconds
                             + " milliseconds (" + firstWait + "/" + secondWait
                             + ").");
            }
            SynchronizedThread ct = new SynchronizedThread(new Command() {
                    public boolean execute() {
                        return toStop.canShutDown();
                    }
                }, sync);
            try {
                ct.start();
                if (logger.isTraceEnabled()) {
                    logger.trace("Started canShutDown thread, synching on "
                                 + sync);
                }
                synchronized (sync) {
                    // has thread already been finished?
                    if (ct.didFinish()) {
                        // do nothing
                    } else if (waitSeconds == -1) {
                        // stay a while...
                        // stay... forever!
                        if (logger.isTraceEnabled()) {
                            logger.trace("Waiting (unlimited) for canShutDown thread to finish, synched on "
                                         + sync);
                        }
                        sync.wait();
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Waiting (first part) for canShutDown thread to finish, synched on "
                                         + sync);
                        }
                        sync.wait(firstWait);
                        if (!ct.didFinish()) {
                            logger.info("Waiting for " + toStop
                                        + " to confirm termination...");
                            sync.wait(secondWait);
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace("Done waiting, canShutDown thread "
                                         + (ct.didFinish() ? "finished"
                                                           : "did not finish")
                                         + ", synched on " + sync);
                        }
                    }
                }
            } catch (InterruptedException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Interrupted timeout waiting on " + sync);
                }
            }
            if (!ct.hadSuccess()) {
                logger.warn(toStop + " did not confirm termination in time.");
                return false;
            } else if (!ct.hadSuccess()) {
                logger.warn(toStop + " said it cannot shut down.");
                return false;
            } else {
                logger.debug(toStop + " said it can shut down.");
            }
        }
        for (int i = pls.size() - 1; i >= 0; i--) {
            IPlugin toStop = pls.get(i);
            try {
                _dependencyList.removeElement(toStop);
            } catch (DependencyNotFulfilledException e) {
                logger.error(e.getMessage());
                logger.error("list of dependent plugins:");
                logger.error(CollectionLister.toString(e.getElements()));
                return false;
            }

            if (!stopSynchronized(toStop)) {
                logger.warn("stop cancelled by plugin " + toStop);
                _dependencyList.addElement(DependencyElement.create(toStop));
                return false;
            }
        }
        return true;
    }

    /**
     * Stops the given plugin. This method is called internally by
     * {@link #stop(IPlugin)} and {@link #stopSynchronized(List)}.
     **/
    private synchronized boolean stopSynchronized(final IPlugin p) {
        try {
            synchronized (_dependencyList) {
                _dependencyList.removeElement(p);
            }
        } catch (DependencyNotFulfilledException e) {
            logger.error(e.getMessage());
            logger.error("list of dependent plugins:");
            logger.error(CollectionLister.toString(e.getElements()));
            return false;
        }

        logger.debug("stopping " + p);
        boolean result;
        Object sync = new Object();
        int waitSeconds = p.getProperties()
                           .getIntProperty("cleanupTimeout", CLEANUP_TIMEOUT);
        if (logger.isTraceEnabled()) {
            logger.trace("Cleanup timeout is " + waitSeconds + " milliseconds.");
        }
        SynchronizedThread ct = new SynchronizedThread(new Command() {
                public boolean execute() {
                    return p.cleanup();
                }
            }, sync);

        // prevent stop from being called by the manager because
        // it just shut down the last plugin with an exit block.
        IPlugin blockLock = new PluginAdapter(PluginProperties.getUserProperties());
        _blockers.add(blockLock);
        try {
            ct.start();
            if (logger.isTraceEnabled()) {
                logger.trace("Started cleanup thread, synching on " + sync);
            }
            synchronized (sync) {
                if (!ct.didFinish()) {
                    // stay a while...
                    if (waitSeconds == -1) {
                        // stay... forever!
                        if (logger.isTraceEnabled()) {
                            logger.trace("Waiting (unlimited) for cleanup thread to finish, synched on "
                                         + sync);
                        }
                        sync.wait();
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Waiting for cleanup thread to finish, synched on "
                                         + sync);
                        }
                        sync.wait(waitSeconds);
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Done waiting, cleanup thread "
                                 + (ct.didFinish() ? "finished" : "did not finish")
                                 + ", synched on " + sync);
                }
            }
        } catch (InterruptedException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Interrupted timeout waiting on " + sync);
            }
        }
        _blockers.remove(blockLock);
        logger.debug(p + " stopped");
        result = ct.hadSuccess();
        if (!result) {
            synchronized (_dependencyList) {
                _dependencyList.addElement(DependencyElement.create(p));
            }
        } else {
            // success:
            serviceRemoved(p.getProperties().getProvisions(), p);
        }
        return result;
    }

    /**
     * Start the Renew Plugin System
     *
     * @param args
     *            the command line parameters
     * @param url
     *            the url of the Loader.class needs a trailing slash
     * @param logStrategy
     *                           logStrategy for configuring log4j
     * @param classLoaderManager
     *                           classLoaderManager to provide own classloaders
     */
    public static void main(String[] args) {
        main(args, null);
    }

    /**
     * Start the Renew Plugin System
     *
     * @param args
     *            the command line parameters
     * @param url
     *            the url of the Loader.class needs a trailing slash
     */
    public static void main(String[] args, URL url) {
        main(args, url, null, null);
    }

    /**
     * Start the Renew Plugin System. Call {@link #main(String[], URL)} instead
     * of this method.
     */
    public static void main(String[] args, URL url, LogStrategy logStrategy,
                            ClassLoaderManager classLoaderManager) {
        for (String s : args) {
            System.out.println(s);
        }


        if (url != null) {
            _loaderLocation = url;
        }
        PluginManager pm = createInstance(url, logStrategy, classLoaderManager);

        Thread.currentThread().setContextClassLoader(pm.getBottomClassLoader());

        pm.loadPlugins();
        for (IPlugin plugin : pm.getPlugins()) {
            plugin.startUpComplete();
            logger.info("loaded plugin: " + plugin.getName());
        }

        if (args.length > 0) {
            ArrayList<List<String>> cmds = new ArrayList<List<String>>();
            cmds.add(new ArrayList<String>());

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                if (arg.equals(COMMAND_SEPERATOR)) {
                    cmds.add(new ArrayList<String>());
                    continue;
                } else if (arg.matches("^\\s*$")) {
                    continue;
                }

                while (arg.contains(COMMAND_SEPERATOR)) {
                    int indexOf = arg.indexOf(COMMAND_SEPERATOR);
                    String left = arg.substring(0, indexOf);
                    String right = arg.substring(indexOf
                                                 + COMMAND_SEPERATOR.length());
                    cmds.get(cmds.size() - 1).add(left);
                    cmds.add(new ArrayList<String>());
                    arg = right;
                }
                cmds.get(cmds.size() - 1).add(arg);
            }

            for (List<String> cmd : cmds) {
                if (cmd.size() == 0) {
                    continue;
                }
                CLCommand c = pm._commands.get(cmd.get(0));
                if (c == null) {
                    for (String s : cmd) {
                        System.out.println("#" + s + "#");
                    }

                    logger.warn("Unknown initial command (ignored): " + cmd);
                } else {
                    cmd.remove(0);
                    String[] nc = new String[cmd.size()];
                    nc = cmd.toArray(nc);
                    logger.debug("Executing initial command: " + cmd);
                    c.execute(nc, System.out);
                }
            }
        }
        pm.checkExit();
    }

    /**
     * This is a shortcut to configure the logging environment
     * without initializing the complete plug-in management system.
     *
     */
    public static synchronized void configureLogging() {
        if (_instance != null) {
            _instance._logStrategy.configureLogging();
        } else {
            getDefaultLogStrategy().configureLogging();
        }
    }

    private void initLocationFinders() {
        URL url = getLoaderLocation(); // getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            url = new URL(url, "plugins/");
        } catch (MalformedURLException e) {
            logger.error("Could not deduce plugins directory near plugin loader: "
                         + e);
            try {
                url = new URL(new File(System.getProperty("user.dir")).toURI()
                                                                      .toURL(),
                              "plugins/");
            } catch (MalformedURLException e2) {
                logger.error("Could not deduce plugins directory near current directory: "
                             + e2);
                return;
            }
        }
        _locationFinder.addLocationFinder(new PluginSubDirFinder(url));
        _locationFinder.addLocationFinder(new PluginJarLocationFinder(url));
        Iterator<URL> fromFile = getLocations();
        while (fromFile.hasNext()) {
            url = fromFile.next();
            _locationFinder.addLocationFinder(new PluginSubDirFinder(url));
            _locationFinder.addLocationFinder(new PluginJarLocationFinder(url));
        }
    }

    /*
     * return a list of URLs to be added to the location finder
     */
    private Iterator<URL> getLocations() {
        Vector<URL> result = new Vector<URL>();
        Properties userProps = PluginProperties.getUserProperties();
        if (userProps.getProperty(PLUGIN_LOCATIONS_PROPERTY) == null) {
            logger.info("no additional plugin locations set.");
        }
        Collection<String> locations = PropertyHelper.parsePathListString(userProps
                                                                          .getProperty(PLUGIN_LOCATIONS_PROPERTY,
                                                                                       ""));
        Iterator<String> files = locations.iterator();
        while (files.hasNext()) {
            String file = files.next();
            logger.debug("location: " + file);
            URL url = createURLfromString(file);
            if (url != null) {
                logger.debug("as URL: " + url);
                result.add(url);
            }
        }
        return result.iterator();
    }

    private URL createURLfromString(String str) {
        URL url = null;

        // first check if str is a filename of an existing file
        File file = new File(str);
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
                return url;
            } catch (MalformedURLException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // then, try to create a url brute force
        try {
            url = new URI(str).toURL();
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Neither file nor url: " + str + "(" + e + ")", e);
            } else {
                logger.warn("Neither file nor url: " + str);
            }
        } catch (NullPointerException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Neither file nor url: " + str + "(" + e + ")", e);
            } else {
                logger.warn("Neither file nor url: " + str);
            }
        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Neither file nor url: " + str + "(" + e + ")", e);
            } else {
                logger.warn("Neither file nor url: " + str);
            }
        } catch (MalformedURLException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Neither file nor url: " + str + "(" + e + ")", e);
            } else {
                logger.warn("Neither file nor url: " + str);
            }
        }
        return url;
    }

    /**
     * This command prints a list of all available commands.
     */
    public class HelpCommand implements CLCommand {
        /*
         * print all available commands
         */
        public void execute(String[] args, PrintStream response) {
            response.println("usage: {command {args}* " + COMMAND_SEPERATOR
                             + "}*");
            response.println("available commands:");

            int largestKeySize = 0;
            for (String key : _commands.keySet()) {
                largestKeySize = Math.max(largestKeySize, key.length());
            }

            String space = stringRepeat(" ", (largestKeySize + 5));

            for (String key : _commands.keySet()) {
                CLCommand command = _commands.get(key);
                String additionalSpace = stringRepeat(" ",
                                                      largestKeySize
                                                      - key.length() + 2);
                response.print(key + additionalSpace + "-  ");
                String description = command.getDescription();
                description = description.replaceAll("\n", "\n" + space);
                response.println(description);
            }
        }

        /**
         * Repeat a string a specified number of times.
         */
        private String stringRepeat(String str, int times) {
            String result = "";
            if (times > 0) {
                for (int i = 0; i < times; i++) {
                    result += str;
                }
            }
            return result;
        }

        public String getDescription() {
            return "print a list of all commands";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return null;
        }
    }

    /**
     * This class represents a timeout when trying to clean up a plugin. An
     * object must be given to synchronize on; this will be notified when the
     * cleanup worked.
     */
    private static class SynchronizedThread extends Thread {
        private boolean _success = false;
        private boolean _finished = false;
        private Object _toNotify;
        private Command _toExecute;

        public SynchronizedThread(Command toExecute, Object toNotify) {
            _toExecute = toExecute;
            _toNotify = toNotify;
        }

        public void run() {
            _success = _toExecute.execute();
            if (logger.isTraceEnabled()) {
                logger.trace("Execution finished: 1) synching on " + _toNotify);
            }
            synchronized (_toNotify) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Execution finished: 2) notifying on "
                                 + _toNotify);
                }
                _finished = true;
                _toNotify.notify();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Execution finished: 3) done on " + _toNotify);
            }
        }

        public boolean hadSuccess() {
            return _success;
        }

        public boolean didFinish() {
            return _finished;
        }
    }

    private static interface Command {
        public boolean execute();
    }

    public URL[] getLibs() {
        URLClassLoader urlCL;
        try {
            urlCL = (URLClassLoader) getSystemClassLoader();
        } catch (ClassCastException e) {
            logger.warn("Could not extract URL from SystemClassLoader, no URLClassLoader.",
                        e);
            return null;
        }
        return urlCL.getURLs();
    }

    /**
     * User preferences location is usually set to ~/.renew.
     *
     * @return folder of user preferences location
     */
    public static File getPreferencesLocation() {
        //FIXME: fallback, if home folder does not exist (temp dir?)
        File dir = new File(System.getProperty("user.home") + File.separator
                            + PREF_DIR);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                return null;
            }
        }
        return dir;
    }

    @Override
    public void addCommandListener(CommandsListener listener) {
        commandsListener.add(listener);
    }

    @Override
    public void removeCommandListener(CommandsListener listener) {
        commandsListener.remove(listener);
    }

    @Override
    public void notifyCommandAdded(String name, CLCommand command) {
        for (CommandsListener clistener : commandsListener) {
            clistener.commandAdded(name, command);
        }
    }

    @Override
    public void notifyCommandRemoved(String name) {
        for (CommandsListener clistener : commandsListener) {
            clistener.commandRemoved(name);
        }
    }
}