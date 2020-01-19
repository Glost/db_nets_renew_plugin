package de.renew.remote;

import de.renew.application.SimulatorPlugin;

import de.renew.plugin.PluginManager;
import de.renew.plugin.PropertyHelper;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;


/**
 * Manages a list of <i>remote</i> renew simulation servers.
 * Net instances of these servers can be viewed and their
 * simulation can be controlled.
 * The registry does not guarantee that all servers listed
 * here are reachable, there is no automated disconnect when
 * failures occur.
 * <p>
 * Remote objects should be published via the registerObject-methods
 * (or the registerObjectsIfPossible-methods if you don't care about
 * the possible exceptions) and subsequently unregistered via
 * <code>unregisterObject(object)</code>.
 * </p>
 * <p>
 * The registry additonally features the computation of related
 * service URLs (via {@link ServerDescriptor#getUrl}) and a
 * listener interface announcing connect and disconnect events
 * (see {@link de.renew.remote.RemoteServerRegistryListener}).
 * Listeners can catch up on connect-events that occured before
 * they registered themselves as listeners, if needed.
 * </p>
 * <p>
 * If you intend to simply compile a list of all remote objects
 * of a give service-description, consider using a
 * {@link de.renew.remote.RemoteObjectHelper}, which hides some
 * of the complexity of a RemoteServerRegistryListener.
 * <p>
 * The <code>RemoteServerRegistry</code> is a singleton
 * class. The one and only instance can be reached via
 * {@link #instance}.
 * </p>
 *
 * @author Timo Carl
 * @author Michael Duvigneau
 * @since Renew 2.0
 * @see Server
 * @see ServerDescriptor
 * @see RemoteServerRegistryListener
 **/
public class RemoteServerRegistry {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(RemoteServerRegistry.class);
    private static RemoteServerRegistry _instance = null;

    /**
     * All currently connected servers
     */
    private Vector<ServerDescriptor> _servers;

    /**
     * All currently subscribed listeners
     */
    private Set<RemoteServerRegistryListener> _listeners;

    /**
     * All remote objects that have been registered through the RemoteServerRegistry
     */
    private Map<Remote, String> _remoteObjects;

    private RemoteServerRegistry() {
        _servers = new Vector<ServerDescriptor>();
        _listeners = new HashSet<RemoteServerRegistryListener>();
        _remoteObjects = new HashMap<Remote, String>();
    }

    /**
     * Get the <code>RemoteServerRegistry</code> singleton instance.
     * It gets created on the first call to this method.
     * @return the singleton instance.
     **/
    public static RemoteServerRegistry instance() {
        if (_instance == null) {
            _instance = new RemoteServerRegistry();
        }
        return _instance;
    }

    /**
     * Connects to the {@link Server} specified by <code>host</code>
     * and <code>name</code> and adds it to the registry.
     * All {@link RemoteServerRegistryListener}s will be notified.
     *
     * @param host the host where the server is located.
     * @param name the name under which the server is announced
     *             at the host's rmi naming service.
     * @return the newly created entry at this registry
     * @exception NotBoundException
     *   if there is no server at the specified <code>host</code>
     *   and <code>name</code>
     * @exception RemoteException
     *   if an RMI failure occurs. In this case, the server will
     *   not have been added to the registry.
     **/
    public ServerDescriptor connectServer(String host, String name)
            throws NotBoundException, RemoteException {
        RemotePlugin.configureInterface();

        Server server = ServerImpl.getInstance(host, name);
        ServerDescriptor desc = new ServerDescriptor(host, name, server);
        _servers.add(desc);
        fireConnectedTo(desc);
        return desc;
    }

    /**
     * Removes the server with the given <code>index</code> from
     * this registry. However, the connection to the server is not
     * really terminated (because there is no termination procedure
     * specified).
     * All {@link RemoteServerRegistryListener}s will be notified.
     *
     * @param index the index of the server to remove in the registry.
     *              Values for this index can be determined from the
     *              array returned by {@link #allServers}.
     **/
    public void removeServer(int index) {
        ServerDescriptor desc = _servers.get(index);
        fireDisconnectedFrom(desc);
        _servers.remove(index);
    }

    /**
     * Returns the registry entry at the given position.
     *
     * @param index the index of the server descriptor to return.
     *              Values for this index can be determined from the
     *              array returned by {@link #allServers}.
     * @return the <code>ServerDescriptor</code> at the given
     *         <code>index</code> in the registry.
     **/
    public ServerDescriptor getServerDescriptor(int index) {
        return _servers.elementAt(index);
    }

    /**
     * Returns all registry entries as an array. The indices of
     * the array can be used at several other methods to specify
     * one registry entry.
     *
     * @return all registry entries.
     */
    public ServerDescriptor[] allServers() {
        return _servers.toArray(new ServerDescriptor[_servers.size()]);
    }

    /**
     * Returns all net instances known to the server at the given
     * position in the registry. This is a shorthand for calling
     * {@link #getServer getServer(index)}
     * .{@link Server#getSimulator getSimulator()}
     * .{@link SimulatorAccessor#getNetInstances getNetInstances()}.
     *
     * @param index the index of the server to query.
     *              Values for this index can be determined from the
     *              array returned by {@link #allServers}.
     * @return an array of <code>NetInstanceAccessor</code> references
     * @exception RemoteException if an RMI failure occurs
     */
    public NetInstanceAccessor[] allNetInstances(int index)
            throws RemoteException {
        Server server = getServer(index);
        return server.getSimulator().getNetInstances();
    }

    /**
     * Returns the server at the given position in the registry.
     * This is a shorthand for calling
     * {@link #getServerDescriptor getServerDescriptor(index)}
     * .{@link ServerDescriptor#getServer getServer()}.
     *
     * @param index the index of the server to return.
     *              Values for this index can be determined from the
     *              array returned by {@link #allServers}.
     * @return the <code>Server</code> at the given
     *         <code>index</code> in the registry.
     **/
    public Server getServer(int index) {
        return (_servers.elementAt(index)).getServer();
    }

    /**
     * Registers the given listener for updates about addition
     * and removal of registry entries. Duplicate registrations
     * will be ignored.
     *
     * @param listener the listener to register.
     **/
    public void addRegistryListener(RemoteServerRegistryListener listener) {
        _listeners.add(listener);
    }

    /**
     * Lets the listener catch up on all currently connected servers by calling the listener's
     * connectedTo-method for each server.
     * The listener MUST have been registered via addRegistryListener before calling this method.
     *
     * @param listener the listener that wants to catch up on calls to connectedTo
     * @throws IllegalArgumentException if the listener hasn't been registered before
     */
    public void catchUpOnPreviousConnects(RemoteServerRegistryListener listener) {
        if (!_listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener must have been registered before catching up.");
        }

        Iterator<ServerDescriptor> servers = _servers.iterator();
        while (servers.hasNext()) {
            ServerDescriptor server = servers.next();
            listener.connectedTo(server);
        }
    }

    /**
     * Tries to register the object "object" using the pattern defined by serviceName and serverName.
     * During a running simulation (e.g. inside a SimulatorExtension), the activeProperties can be retrieved
     * via <code>environment.getProperties()</code>.
     * When you don't have access to the SimulationEnvironment (e.g. outside a simulation),
     * use the registerObjectIfPossible(String, Remote)-method.
     *
     * @param serviceName Unique name of the service provided by the remote object
     * @param object the object to publish
     * @param activeProperties properties of the currently running simulation.
     * @return true, if the object was properly registered.
     */
    public boolean registerObjectIfPossible(String serviceName, Remote object,
                                            Properties activeProperties) {
        boolean success = false;

        try {
            success = registerObject(serviceName, object, activeProperties);
        } catch (RemoteException e) {
            logger.error("Could not bind " + serviceName
                         + " service to registry: " + e);
        }

        return success;
    }

    /**
     * Tries to register the object "object" using the pattern defined by serviceName and serverName.
     * This method automatically determines the serverName from the current settings.
     * During a running simulation (e.g. inside a SimulatorExtension), the preferred method is
     * <code>registerObjectIfPossible(String, Remote, Properties)</code> to correctly determine the
     * serverName from the SimulationEnvironment.
     *
     * @param serviceName Unique name of the service provided by the remote object
     * @param object the object to publish
     * @return true, if the object was properly registered.
     */
    public boolean registerObjectIfPossible(String serviceName, Remote object) {
        Properties activeProperties = ((SimulatorPlugin) PluginManager.getInstance()
                                                                      .getPluginsProviding("de.renew.simulator")
                                                                      .iterator()
                                                                      .next())
                                          .getProperties();

        return registerObjectIfPossible(serviceName, object, activeProperties);
    }

    /**
     * Tries to register the object "object" using the pattern defined by serviceName and serverName.
     * Any RemoteException is forwarded to the caller.
     * During a running simulation (e.g. inside a SimulatorExtension), the activeProperties can be retrieved
     * via <code>environment.getProperties()</code>.
     * When you don't have access to the SimulationEnvironment (e.g. outside a simulation),
     * use the registerObject(String, Remote)-method.
     *
     * @param serviceName Unique name of the service provided by the remote object
     * @param object the object to publish
     * @param activeProperties properties of the currently running simulation.
     * @return true, if the object was properly registered.
     * @throws RemoteException
     */
    public boolean registerObject(String serviceName, Remote object,
                                  Properties activeProperties)
            throws RemoteException {
        boolean success = false;

        if (PropertyHelper.getBoolProperty(activeProperties,
                                                   RemoteExtension.ENABLE_PROP_NAME)) {
            String serverName = activeProperties.getProperty(RemoteExtension.NAME_PROP_NAME);
            String serverURL = "";
            if (serverName == null) {
                serverName = Server.DEFAULT_SERVER_NAME;
            }

            try {
                serverURL = "//localhost/" + serviceName + "." + serverName;
                Naming.rebind(serverURL, object);
                logger.info(serviceName + " service bound to " + serverURL
                            + ".");
                _remoteObjects.put(object, serverURL);
                success = true;
            } catch (MalformedURLException e) {
                logger.error("Could not bind " + serviceName + " service to "
                             + serverURL + ": " + e);
                serverURL = null;
            }
        }

        return success;
    }

    /**
     * Tries to register the object "object" using the pattern defined by serviceName and serverName.
     * Any RemoteException is forwarded to the caller.
     * This method automatically determines the serverName from the current settings.
     * During a running simulation (e.g. inside a SimulatorExtension), the preferred method is
     * <code>registerObject(String, Remote, Properties)</code> to correctly determine the
     * serverName from the SimulationEnvironment.
     *
     * @param serviceName Unique name of the service provided by the remote object
     * @param object the object to publish
     * @return true, if the object was properly registered.
     * @throws RemoteException
     */
    public boolean registerObject(String serviceName, Remote object)
            throws RemoteException {
        Properties activeProperties = ((SimulatorPlugin) PluginManager.getInstance()
                                                                      .getPluginsProviding("de.renew.simulator")
                                                                      .iterator()
                                                                      .next())
                                          .getProperties();

        return registerObject(serviceName, object, activeProperties);
    }

    /**
     * Unregisters an object that was previously registered via registerObject or registerObjectIfPossible.
     * It doesn't matter whether these methods actually succeeded in registering them at the registry.
     *
     * @param object the object to unregister
     */
    public void unregisterObject(Remote object) {
        String serverURL = _remoteObjects.get(object);
        if (serverURL != null) {
            try {
                Naming.unbind(serverURL);
            } catch (MalformedURLException e) {
                logger.error("Could not unbind object: " + e);
            } catch (RemoteException e) {
                logger.error("Could not unbind object: " + e);
            } catch (NotBoundException e) {
                // Funny. But that's what we wanted to achieve, anyway.
            }
            serverURL = null;
        }
    }

    /**
     * Deregisters the given listener from updates about registry
     * entries, it will no longer be informed. The method call will
     * be safely ignored if the listener was not registered.
     *
     * @param listener the listener to deregister.
     **/
    public void removeRegistryListener(RemoteServerRegistryListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Inform all registered listeners that a new server has been
     * added to the registry.
     *
     * @param desc the new registry entry
     **/
    private void fireConnectedTo(ServerDescriptor desc) {
        Iterator<RemoteServerRegistryListener> listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            (listeners.next()).connectedTo(desc);
        }
    }

    /**
     * Inform all registered listeners that a server has been
     * removed from the registry.
     *
     * @param desc the former registry entry
     **/
    private void fireDisconnectedFrom(ServerDescriptor desc) {
        Iterator<RemoteServerRegistryListener> listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            (listeners.next()).disconnectedFrom(desc);
        }
    }

    /**
     * Objects of this class comprise all information stored in
     * one entry of the {@link RemoteServerRegistry}.
     **/
    public class ServerDescriptor {
        private String _host;
        private String _name;
        private Server _server;

        /**
         * Creates a new entry object for the given <code>server</code>
         * located at the given <code>host</code> and published
         * under the name <code>name</code>.
         **/
        public ServerDescriptor(String host, String name, Server server) {
            _host = host;
            _name = name;
            _server = server;
        }

        /**
         * Computes an URL suitable for {@link java.rmi.Naming} requests
         * for the given service. The URL is based on the <code>host</code>
         * and <code>name</code> information stored in this entry.
         *
         * @param service the <code>String</code> to use as
         *                service part in the URL.
         * @return the computed URL. It is not guaranteed to be
         *         well formed, because the <code>service</code>
         *         parameter is not checked for validity.
         **/
        public String getUrl(String service) {
            return "//" + _host + "/" + service + "." + _name;
        }

        public String getHost() {
            return _host;
        }

        public String getName() {
            return _name;
        }

        public Server getServer() {
            return _server;
        }

        public String toString() {
            return "//" + getHost() + "/" + getName();
        }
    }
}