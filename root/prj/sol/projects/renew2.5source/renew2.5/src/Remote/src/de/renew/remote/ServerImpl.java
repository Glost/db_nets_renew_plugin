package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import java.net.MalformedURLException;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class is the default implementation of the {@link Server}
 * interface, accompanied by static functions useful in the server's
 * lifecycle.
 * <p>
 * The functionality as a stand-alone simulation server (that
 * this class had in Renew 1.6) is no longer available. In the
 * {@link de.renew.application.SimulatorPlugin} of Renew 2.0 the
 * remote server is included as {@link RemoteExtension}. This
 * extension can be enabled by setting one of the
 * <code>de.renew.remote</code> properties (as documented in this
 * package).
 * </p>
 *
 * @author Thomas Jacob, Michael Duvigneau
 * @see de.renew.remote.Server
 * @since Renew 1.6
 **/
public class ServerImpl extends UnicastRemoteObject implements Server {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ServerImpl.class);

    /**
     * Holds this server's current simulator and its environment.
     */
    private SimulationEnvironment environment = null;

    /**
     * Creates a new remote simulation server object that can be
     * registered for public RMI access. This object does not
     * register itself automatically, this has to be done
     * explicitly by the caller.
     * <p>
     * The {@link SocketFactoryDeterminer} is queried whether a
     * special RMI socket factory should be used.
     * </p>
     *
     * @param environment  the <code>SimulationEnvironment</code>
     *                     of the current simulation.
     *
     * @exception RemoteException
     *   if a RMI problem occurred.
     **/
    public ServerImpl(SimulationEnvironment environment)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.environment = environment;
    }

    /* Non-JavaDoc: specified by the Server interface. */
    public SimulatorAccessor getSimulator() throws RemoteException {
        return new SimulatorAccessorImpl(environment);
    }

    /**
     * Binds the given server to the RMI registry and returns its
     * URL.
     *
     * @param server       the <code>Server</code> object to bind.
     *
     * @param serverName   the name to include in the generated
     *                     server URL. if <code>null</code>, the
     *                     default server name will be used.
     *
     * @param allowRebind  if <code>true</code>, the fact that
     *                     the server URL is already bound to
     *                     another server object will be ignored.
     *                     I.e. the old server will be replaced
     *                     by the new one. <br>
     *                     if <code>false</code>, an already
     *                     bound server URL will result in not
     *                     binding this server.
     *
     * @return The URL the server was bound to, if successful.<br>
     *         <code>null</code>, otherwise.
     **/
    public static String bindServer(final Server server,
                                    final String serverName,
                                    final boolean allowRebind) {
        Future<String> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<String>() {
                public String call() throws Exception {
                    String serverUrl = makeLocalServerUrl(serverName);
                    try {
                        try {
                            logger.debug("ServerImpl: Binding server to URL "
                                         + serverUrl + "...");
                            Naming.bind(serverUrl, server);
                            logger.debug("ServerImpl: Bound to " + serverUrl
                                         + ".");
                        } catch (AlreadyBoundException e) {
                            if (allowRebind) {
                                logger.debug("ServerImpl: " + serverUrl
                                             + " already in use. Rebinding ...");
                                Naming.rebind(serverUrl, server);
                            } else {
                                logger.error("ServerImpl: " + serverUrl
                                             + " already in use. Aborting.");
                                serverUrl = null;
                            }
                        }
                    } catch (MalformedURLException e) {
                        logger.error("ServerImpl: " + serverUrl
                                     + " is a malformed URL. Aborting.");
                        serverUrl = null;
                    } catch (RemoteException e) {
                        logger.warn("ServerImpl: Could not bind server to "
                                    + serverUrl + ": " + e);
                        serverUrl = null;
                    }

                    return serverUrl;
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
        return null;

    }

    /**
     * Unbinds the server registered under the given URL.
     * if some error occurs, it will be echoed to the console,
     * There is no feedback to the caller whether the action was
     * successful.
     *
     * @param serverUrl  the URL to deregister.
     */
    public static void unbindServer(String serverUrl) {
        try {
            logger.debug("ServerImpl: Unbinding server from URL " + serverUrl
                         + "...");
            Naming.unbind(serverUrl);
            logger.debug("ServerImpl: Unbound from " + serverUrl + ".");
        } catch (MalformedURLException e) {
            logger.error("ServerImpl: " + serverUrl
                         + " is a malformed URL. Unbinding failed.");
        } catch (NotBoundException e) {
            logger.error("ServerImpl: " + serverUrl
                         + " was not bound. Unbinding failed.");
        } catch (RemoteException e) {
            logger.error("ServerImpl: Unbinding from " + serverUrl
                         + " failed: " + e);
        }
    }


    /**
     * Searches for a server in the RMI registry
     * of a given host and returns its.
     * Use this method for the default server name.
     * @param hostName The host of the RMI registry.
     * @return The server.
     * @exception RemoteException An RMI problem occurred.
     * @exception NotBoundException The server cannot be found.
     */
    public static synchronized Server getInstance(String hostName)
            throws RemoteException, NotBoundException {
        return getInstance(hostName, Server.DEFAULT_SERVER_NAME);
    }

    /**
     * Searches for a server in the RMI registry
     * of a given host and given name and returns its.
     * Use this method if multiple servers exist.
     * @param hostName The host of the RMI registry.
     * @param serverName The name of the server.
     * @return The server.
     * @exception RemoteException An RMI problem occurred.
     * @exception NotBoundException The server cannot be found.
     * @exception IllegalStateException Some internal error occurred.
     * @exception IllegalArgumentException The object found under
     * the given name is not a Renew remote server.
     */
    public static synchronized Server getInstance(String hostName,
                                                  String serverName)
            throws RemoteException, NotBoundException {
        try {
            return (Server) Naming.lookup(makeServerUrl(hostName, serverName));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("An internal error occurred: " + e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(hostName + ":" + serverName
                                               + " is not a renew server");
        }
    }

    /**
     * Returns the local server URL determined from a given
     * server name.
     *
     * @param servername  The name of the server. <br>
     *                    If <code>null</code>, the default name
     *                    will be used.
     *
     * @return the generated server URL.
     */
    public static String makeLocalServerUrl(String servername) {
        return makeServerUrl("localhost", servername);
    }


    /**
     * Builds a default RMI server url pointing to the given
     * host.
     *
     * @param hostName  The host name where the server is
     *                  located.
     *
     * @return the generated server URL.
     */
    public static String makeServerUrl(String hostName) {
        return makeServerUrl(hostName, null);
    }

    /**
     * Builds the a RMI server url out of the given server name
     * on the given host.
     *
     * @param hostName   The host name where the server is
     *                   located.
     *
     * @param serverName The name of the server. <br>
     *                   If <code>null</code>, the default name
     *                   will be used.
     *
     * @return the generated server URL.
     */
    public static String makeServerUrl(String hostName, String serverName) {
        return "//" + hostName + "/" + Server.class.getName() + "."
               + ((serverName == null) ? Server.DEFAULT_SERVER_NAME : serverName);
    }
}