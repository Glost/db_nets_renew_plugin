package de.renew.remote;

import de.renew.application.NoSimulationException;
import de.renew.application.SimulatorPlugin;

import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.concurrent.Callable;


/**
 * @author Dominic Dibbern
 * @date Jan 23, 2012
 * @version 0.1
 */
public class RemotePlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimulatorPlugin.class);

    /**
     * The name of the property to get the host address.
     * The name is: {@value}.
     **/
    private static final String RMI_HOST_NAME = "de.renew.remote.rmi-host-name";
    private static RemotePlugin _instance;

    public RemotePlugin(URL url) throws PluginException {
        super(url);
        setInstance();
        setup();
    }

    public RemotePlugin(PluginProperties props) {
        super(props);
        setInstance();
        setup();
    }

    private void setup() {
        SimulatorPlugin.getCurrent().addExtension(new RemoteExtension());
    }

    private void setInstance() {
        if (_instance == null) {
            _instance = this;
        }
    }

    /**
     * Wraps a local <code>NetInstance</code> reference into a could-be-remote
     * <code>NetInstanceAccessor</code> reference.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link SimulatorPlugin#lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param instance
     *            the net instance to wrap.
     *
     * @return the resulting net instance accessor. Returns <code>null</code>,
     *         if <code>instance</code> was <code>null</code>.
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @see #lock
     */
    public NetInstanceAccessor wrapInstance(final NetInstance instance)
            throws NoSimulationException {
        final SimulatorPlugin sim = SimulatorPlugin.getCurrent();
        try {
            return sim.submitAndWait(new Callable<NetInstanceAccessor>() {
                    public NetInstanceAccessor call()
                            throws NoSimulationException {
                        NetInstanceAccessor returnValue = null;

                        try {
                            returnValue = (instance == null) ? null
                                                             : new NetInstanceAccessorImpl(instance,
                                                                                           sim
                                                                                           .getCurrentEnvironment());
                        } catch (RemoteException e) {
                            logger.error("Wrapping of net instance failed: "
                                         + e);
                        }

                        return returnValue;
                    }
                });
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Wraps a local <code>PlaceInstance</code> reference into a could-be-remote
     * <code>PlaceInstanceAccessor</code> reference.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link SimulatorPlugin#lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param instance
     *            the place instance to wrap.
     *
     * @return the resulting place instance accessor. Returns <code>null</code>,
     *         if <code>instance</code> was <code>null</code>.
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @see #lock
     */
    public PlaceInstanceAccessor wrapInstance(final PlaceInstance instance) {
        final SimulatorPlugin sim = SimulatorPlugin.getCurrent();
        try {
            return sim.submitAndWait(new Callable<PlaceInstanceAccessor>() {
                    public PlaceInstanceAccessor call()
                            throws NoSimulationException {
                        PlaceInstanceAccessor returnValue = null;
                        try {
                            returnValue = (instance == null) ? null
                                                             : new PlaceInstanceAccessorImpl(instance,
                                                                                             sim
                                                                                             .getCurrentEnvironment());
                        } catch (RemoteException e) {
                            logger.error("Wrapping of place instance failed: "
                                         + e);

                        }

                        return returnValue;
                    }
                });
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Wraps a local <code>TransitionInstance</code> reference into a
     * could-be-remote <code>TransitionInstanceAccessor</code> reference.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link SimulatorPlugin#lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param instance
     *            the transition instance to wrap.
     *
     * @return the resulting transition instance accessor. Returns
     *         <code>null</code>, if <code>instance</code> was <code>null</code>
     *         .
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @see #lock
     */
    public TransitionInstanceAccessor wrapInstance(final TransitionInstance instance) {
        final SimulatorPlugin sim = SimulatorPlugin.getCurrent();

        try {
            return sim.submitAndWait(new Callable<TransitionInstanceAccessor>() {
                    public TransitionInstanceAccessor call()
                            throws NoSimulationException {
                        TransitionInstanceAccessor returnValue = null;
                        try {
                            returnValue = (instance == null) ? null
                                                             : new TransitionInstanceAccessorImpl(instance,
                                                                                                  sim
                                                                                                  .getCurrentEnvironment());
                        } catch (RemoteException e) {
                            logger.error("Wrapping of transition instance failed: "
                                         + e);
                        }

                        return returnValue;
                    }
                });
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Wraps a local <code>Net</code> reference into a could-be-remote
     * <code>NetAccessor</code> reference.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link SimulatorPlugin#lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param net
     *            the net to wrap.
     *
     * @return the resulting net accessor. Returns <code>null</code>, if
     *         <code>net</code> was <code>null</code>.
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @see #lock
     */
    public NetAccessor wrapNet(final Net net) {
        final SimulatorPlugin sim = SimulatorPlugin.getCurrent();

        try {
            return sim.submitAndWait(new Callable<NetAccessor>() {
                    public NetAccessor call() throws NoSimulationException {
                        NetAccessorImpl returnValue = null;
                        try {
                            returnValue = (net == null) ? null
                                                        : new NetAccessorImpl(net,
                                                                              sim
                                                                              .getCurrentEnvironment());
                        } catch (RemoteException e) {
                            logger.error("Wrapping of net failed: " + e);

                        }

                        return returnValue;
                    }
                });
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    public static RemotePlugin getInstance() {
        return _instance;
    }

    /**
     * Configures the RMI Interface, because the host address has to be set in some cases.
     */
    public static void configureInterface() {
        //RMI need the current ip-address. It can be set in the properties. 
        String hostAddress = getInstance().getProperties()
                                 .getProperty(RMI_HOST_NAME);

        if (hostAddress != null) {
            System.setProperty("java.rmi.server.hostname", hostAddress);
            if (logger.isInfoEnabled()) {
                logger.info(RemoteExtension.class.getSimpleName()
                            + ": RMI host address set to: " + hostAddress);
            }
        }
    }
}