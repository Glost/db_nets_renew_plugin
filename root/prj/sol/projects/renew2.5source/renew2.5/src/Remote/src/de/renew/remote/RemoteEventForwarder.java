package de.renew.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * This class fowards remote events to a listener that is
 * not a RMI server.
 * <p>
 * As RMI server classes are easier to implement when
 * extending <code>UnicastRemoteObject</code> and listeners
 * may already extend a different class, this remote event
 * forwarder can be used to listen to remote events without
 * the the need to act as a RMI server.
 * <br>
 * The use of remote forwarders does not protect the
 * listener against <code>RemoteException</code>s when
 * registering or unregistering the forwarder at an event
 * producer.
 * </p>
 * <p>
 * Typical use:
 * <blockquote><pre>
 * PlaceInstanceAccessor accessor;
 * ...
 * RemoteEventForwarder forwarder=new RemoteEventForwarder(this);
 * accessor.addRemoteEventListener(forwarder);
 * ...
 * accessor.removeRemoteEventListener(forwarder);
 * </pre></blockquote>
 * </p>
 *
 * @author Olaf Kummer, Michael Duvigneau
 *
 * @see RemoteEventListener
 */
public class RemoteEventForwarder extends UnicastRemoteObject
        implements RemoteEventListener {
    private EventListener listener;

    public RemoteEventForwarder(EventListener listener)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.listener = listener;
    }

    /**
     * Receives the event message.
     *
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public void update() throws RemoteException {
        listener.update();
    }
}