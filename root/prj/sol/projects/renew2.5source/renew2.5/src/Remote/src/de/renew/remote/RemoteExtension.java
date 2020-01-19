package de.renew.remote;

import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulatorExtension;

import de.renew.plugin.PropertyHelper;

import de.renew.shadow.ShadowLookup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Properties;


/**
 * This simulator extension can provide RMI access to the running
 * simulation. It is always registered within the Renew Simulator
 * plugin. But to be activated, one of the properties
 * <code>de.renew.remote.serverClass</code> or
 * <code>de.renew.remote.enable</code> needs to be set.
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class RemoteExtension implements SimulatorExtension {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(RemoteExtension.class);

    /**
     * A reference to the current simulation environment. Is
     * <code>null</code> whenever no simulation is set up.
     **/
    private SimulationEnvironment currentEnvironment = null;

    /**
     * A reference to the current remote simulation interface server.
     * Is <code>null</code> whenever no simulation with enabled
     * remote access is set up.
     **/
    private Server currentServer = null;

    /**
     * The URL under which the current server had been registered
     * with the RMI naming service.
     **/
    private String currentServerUrl = null;

    /**
     * The name of the property to get the server implementation
     * class from. The name is: {@value}.
     **/
    public static final String CLASS_PROP_NAME = "de.renew.remote.serverClass";

    /**
     * The name of the property to check whether remote access
     * should be enabled. The name is: {@value}.
     **/
    public static final String ENABLE_PROP_NAME = "de.renew.remote.enable";

    /**
     * The name of the property to get the server name under
     * which the remote access is announced. The name is: {@value}.
     **/
    public static final String NAME_PROP_NAME = "de.renew.remote.publicName";

    /**
     * The default server implementation to use, if not
     * configured by a property. The class is: {@link ServerImpl}.
     **/
    public static final Class<?> DEFAULT_SERVER_CLASS = ServerImpl.class;


    /**
     * Describe <code>simulationSetup</code> method here.
     *
     * @param env a <code>SimulationEnvironment</code> value
     */
    public void simulationSetup(SimulationEnvironment env) {
        // The simulator plugin enforces the following conditions:
        assert currentEnvironment == null : "The last simulation has not been cleaned up properly.";
        assert currentServer == null : "The last remote server has not been cleaned up properly.";
        assert currentServerUrl == null : "The last remote server has not been cleaned up properly.";

        this.currentEnvironment = env;

        // Interpret the properties.
        Properties props = env.getProperties();


        // Query for the general enabledness of this extension. It
        // can also be implicitly enabled by any other property.
        boolean enableRemote = PropertyHelper.getBoolProperty(props,
                                                              ENABLE_PROP_NAME);

        if (enableRemote) {
            RemotePlugin.configureInterface();


            // Query for the server implementation class to use.
            Class<?> serverClass = PropertyHelper.getClassProperty(props,
                                                                   CLASS_PROP_NAME,
                                                                   Server.class);

            // Query for the public name the server should use.
            String serverName = props.getProperty(NAME_PROP_NAME);

            // Set the defaults for unset properties.
            if (serverClass == null) {
                serverClass = ServerImpl.class;
            }
            if (serverName == null) {
                serverName = Server.DEFAULT_SERVER_NAME;
            }

            this.currentServer = createServer(serverClass);

            if (currentServer != null) {
                props.setProperty(NAME_PROP_NAME, serverName);
                props.setProperty(CLASS_PROP_NAME, serverClass.getName());
                props.setProperty(ENABLE_PROP_NAME, "true");

                this.currentServerUrl = ServerImpl.bindServer(currentServer,
                                                              serverName, true);
            }
        } else {
            props.remove(NAME_PROP_NAME);
            props.remove(CLASS_PROP_NAME);
            props.setProperty(ENABLE_PROP_NAME, "false");
        }
    }

    /**
     *
     **/
    private Server createServer(Class<?> serverClass) {
        Server server = null;
        logger.debug("RemoteExtension: Using server class "
                     + serverClass.getName() + "...");
        try {
            Constructor<?> constructor = serverClass.getConstructor(new Class<?>[] { SimulationEnvironment.class });
            server = (Server) constructor.newInstance(new Object[] { currentEnvironment });
            logger.debug("RemoteExtension: Created server.");
        } catch (NoSuchMethodException e) {
            logger.error("RemoteExtension: Could not create remote server: "
                         + e);
        } catch (SecurityException e) {
            logger.error("RemoteExtension: Could not create remote server: "
                         + e);
        } catch (IllegalAccessException e) {
            logger.error("RemoteExtension: Could not create remote server: "
                         + e);
        } catch (InstantiationException e) {
            logger.error("RemoteExtension: Could not create remote server: "
                         + e);
        } catch (InvocationTargetException e) {
            logger.error("RemoteExtension: Server initialisation failed: ");
            logger.error(e.getTargetException().getMessage(),
                         e.getTargetException());
        }
        return server;
    }


    /**
     * Describe <code>simulationTerminated</code> method here.
     **/
    public void simulationTerminated() {
        if (currentServerUrl != null) {
            ServerImpl.unbindServer(currentServerUrl);
            logger.debug("RemoteExtension: Server disconnected from registry.");
            currentServerUrl = null;
        }
        currentServer = null;
        currentEnvironment = null;
    }

    public void simulationTerminating() {
        //Nothing to do ?
    }

    /**
     * Does nothing.
     **/
    public void netsCompiled(ShadowLookup lookup) {
    }
}