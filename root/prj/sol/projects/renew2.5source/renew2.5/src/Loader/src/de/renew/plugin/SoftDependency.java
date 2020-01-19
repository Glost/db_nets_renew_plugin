package de.renew.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;


/**
 * Instances of this class represent a soft dependency between plugins.
 * A {@link SoftDependencyListener} implementation has to be provided by
 * users of this class. The listener is informed about the service
 * availability without redundancies. The listener is also informed if the
 * requested service is available from the very beginning of this dependency's
 * life span.
 * <p>
 * The <code>SoftDependencyListener</code> implementation class has to be
 * given as textual class name because it might depend on classes that are
 * not available when the requested service is not provided. The listener
 * object is instantiated the first time the service becomes available.
 * Due to this decoupling, complaints about listener instantiation cannot
 * go back to the caller, they are reported to the system output only.
 * </p>
 * <p>
 * <b>WARNING:</b>
 * The current implementation of this class assumes that a service is not
 * provided by more than one plugin at the same time. It may report
 * service unavailability although there is another service provider
 * available!
 * </p>
 *
 * @author Michael Duvigneau
 **/
public class SoftDependency implements IPluginManagerListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SoftDependency.class);

    /**
     * The service name to listen for.
     **/
    private String pluginService;

    /**
     * The plugin that provides the service required by this soft
     * dependency.  If <code>null</code>, the service has not been
     * announced as available to the listener.
     **/
    private IPlugin provider;

    /**
     * The class of the listener to inform about service availability. It
     * is used to instantiate the <code>listener</code> at the first time
     * the service is available.
     **/
    private String listenerClass;

    /**
     * The listener object.
     **/
    private SoftDependencyListener listener;

    /**
     * The plugin that wants to establish the soft dependency.
     **/
    private IPlugin caller;

    /**
     * The plugin manager this soft dependency refers to.
     **/
    private PluginManager mgr;

    /**
     * Establishes a <code>SoftDependency</code> from <code>caller</code>
     * plugin to the given <code>pluginService</code>.
     *
     * @param pluginService  the name of the service to listen for
     *
     * @param listenerClass  the class to inform about the service
     *                       availability.  It is resolved via reflection
     *                       to avoid class loading when the service is not
     *                       available.  Uses the bottom class loader of the
     *                       plugin system.
     *
     * @param caller         the plugin that wants to establish the soft
     *                       dependency
     **/
    public SoftDependency(IPlugin caller, String pluginService,
                          String listenerClass) {
        this.caller = caller;
        this.pluginService = pluginService;
        this.listenerClass = listenerClass;
        this.listener = null;
        this.provider = null;
        this.mgr = PluginManager.getInstance();
        logger.debug("Soft dependency from " + caller + " to " + pluginService
                     + " registered, using " + listenerClass + ".");
        mgr.addPluginManagerListener(this);
        checkServiceAvailable();
    }

    /**
     * Removes the soft dependency. Discards the listener reference and
     * disconnects from the plugin system.
     * <p>
     * The listener is informed that the service is no longer available (if
     * it was available before), whatever the current state of the service
     * really is.
     * </p>
     **/
    public synchronized void discard() {
        logger.debug("Unregistering soft dependency from " + caller + " to "
                     + pluginService + ".");
        mgr.removePluginManagerListener(this);
        fireServiceRemoved();
        listener = null;
    }

    /**
     * Returns the <code>listener</code> object.
     * <p>
     * If this is the first call, the listener is instantiated from the
     * <code>listenerClass</code> name.
     * </p>
     * @return the listener object.
     *         Returns <code>null</code> if instantiation failed.
     **/
    public SoftDependencyListener getListener() {
        if (listener == null) {
            try {
                Class<?> clazz = Class.forName(listenerClass, true,
                                               mgr.getBottomClassLoader());
                try {
                    Constructor<?> constructor = clazz.getConstructor(new Class[] { IPlugin.class });
                    listener = (SoftDependencyListener) constructor.newInstance(new Object[] { caller });
                } catch (NoSuchMethodException e) {
                    listener = (SoftDependencyListener) clazz.newInstance();
                    logger.debug("WARNING: Soft dependency from " + caller
                                 + " to " + pluginService
                                 + ": listener is created with"
                                 + " no-arg constructor of " + clazz + ".");
                }
                logger.debug("Soft dependency from " + caller + " to "
                             + pluginService + ": created " + listener + ".");
            } catch (InvocationTargetException e) {
                logger.warn("WARNING: Soft dependency from " + caller + " to "
                            + pluginService + " could not be created: "
                            + e.getTargetException() + ".");
                logger.debug(e.getMessage(), e);
            } catch (Exception e) {
                logger.warn("WARNING: Soft dependency from " + caller + " to "
                            + pluginService + " could not be created: " + e
                            + ".");
                logger.debug(e.getMessage(), e);
            } catch (ExceptionInInitializerError e) {
                logger.warn("WARNING: Soft dependency from " + caller + " to "
                            + pluginService + " could not be created: "
                            + e.getException() + ".");
                logger.debug(e.getMessage(), e);
            }
        }
        return listener;
    }

    /**
     * Calls <code>fireServiceAvailable</code> or <code>fireServiceRemoved</code>
     * based on the current state of the service availability.
     **/
    private void checkServiceAvailable() {
        Collection<IPlugin> providers = mgr.getPluginsProviding(pluginService);
        if (!providers.isEmpty()) {
            fireServiceAvailable(providers.iterator().next());
        } else {
            fireServiceRemoved();
        }
    }

    /**
     * Informs the listener about service availability, if the listener
     * exists (or can be instantiated) unless it has been informed about
     * service availability before.
     *
     * @param provider  a plugin that provides the service.
     **/
    private void fireServiceAvailable(IPlugin provider) {
        if ((this.provider == null) && (getListener() != null)) {
            logger.debug("Soft dependency from " + caller + " to " + provider
                         + " activated.");
            listener.serviceAvailable(provider);
            this.provider = provider;
        }
    }

    /**
     * Informs the listener about service removal, if the listener exists
     * and has been informed about service availability once before.
     **/
    private void fireServiceRemoved() {
        if ((this.provider != null) && (listener != null)) {
            logger.debug("Soft dependency from " + caller + " to " + provider
                         + " deactivated.");
            getListener().serviceRemoved(provider);
            this.provider = null;
        }
    }

    /**
     * To be called by the plugin manager only.
     **/
    public synchronized void serviceAdded(final String service, IPlugin provider) {
        if (service.equals(pluginService)) {
            Collection<IPlugin> providers = mgr.getPluginsProviding(pluginService);
            assert !providers.isEmpty() : "PluginManager misinformed soft dependency from "
            + caller + " to " + service;
            fireServiceAvailable(providers.iterator().next());
        }
    }

    /**
     * To be called by the plugin manager only.
     **/
    public synchronized void serviceRemoved(String service, IPlugin provider) {
        if (service.equals(pluginService)) {
            fireServiceRemoved();
        }
    }
}