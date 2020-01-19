package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Use this class to access net instances and
 * other simulation elements remotely. Instances of this class
 * can be announced to a RMI registry by the static methods
 * provided in the {@link ServerImpl} class.
 * <p>
 * In order to be usable by the {@link RemoteExtension} simulator
 * extension, implementations of this class must provide a
 * constructor with the following signature:
 * <pre>
 * public AServerImplementation(SimulationEnvironment environment) {
 *     super(0, SocketFactoryDeterminer.getInstance(),
 *           SocketFactoryDeterminer.getInstance());
 *     // Implementation-specific initialisation goes here...
 * }
 * </pre>
 * The {@link de.renew.application.SimulationEnvironment} is passed to the server
 * implementation to inform in about the simulation it gets
 * attached to.<br>
 * Like all classes of the de.renew.remote package, the server
 * implementation should use the {@link SocketFactoryDeterminer}
 * in its constructor to allow specialised RMI connections.
 * </p>
 *
 * @author Olaf Kummer, Michael Duvigneau, Thomas Jacob
 * @since Renew 1.6
 **/
public interface Server extends Remote {

    /**
     * The name of the default server.
     */
    public static final String DEFAULT_SERVER_NAME = "default";

    /**
     * Returns the current simulator of this server as an accessor.
     * @return The simulator as an accessor.
     * @exception RemoteException An RMI problem occurred.
     */
    public SimulatorAccessor getSimulator() throws RemoteException;
}