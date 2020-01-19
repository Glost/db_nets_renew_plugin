package de.renew.remote;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.PlaceInstance;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Implements the TokenCountsAccessor interface,
 * representing the token counts of a place instance as a snapshot.
 * Its contents will not change even if transitions fire.
 *
 * @author Thomas Jacob
 */
public class TokenCountsAccessorImpl extends UnicastRemoteObject
        implements TokenCountsAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TokenCountsAccessorImpl.class);

    /**
     * The number of free tokens.
     */
    private int freeTokenCount;

    /**
     * The number of tested tokens.
     */
    private int testedTokenCount;

    /**
     * Creates a new TokenCountsAccessorImpl.
     * @param placeInstance The place instance for the marking accessor.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TokenCountsAccessorImpl(final PlaceInstance placeInstance)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());

        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    placeInstance.lock.lock();
                    try {
                        freeTokenCount = placeInstance.getNumberOfTokens();
                        testedTokenCount = placeInstance.getNumberOfTestedTokens();
                    } finally {
                        placeInstance.lock.unlock();
                    }
                    return null;
                }
            });
        try {
            future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }
    }

    /**
     * Return the number of free tokens in this marking.
     * Even if two tokens are equal, they are both counted individually.
     * Currently tested tokens are not included in this count.
     *
     * @return the number of free tokens
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public int getFreeTokenCount() throws RemoteException {
        return freeTokenCount;
    }

    /**
     * Return the number of currently tested tokens in this marking.
     *
     * @return the number of tested tokens
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public int getTestedTokenCount() throws RemoteException {
        return testedTokenCount;
    }

    /**
     * Returns whether the place instance has no tokens.
     *
     * @return Whether the place instance is empty.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean isEmpty() throws RemoteException {
        return freeTokenCount + testedTokenCount <= 0;
    }
}