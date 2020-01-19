package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.PlaceInstance;
import de.renew.net.TimeSet;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;


/**
 * Implements the MarkingAccessor interface, representing the marking of a place
 * instance as a snapshot. Its contents will not change even if transitions
 * fire.
 *
 * @author Thomas Jacob
 */
public class MarkingAccessorImpl extends UnicastRemoteObject
        implements MarkingAccessor {

    /**
     * The simulation environment this object is situated in.
     */
    private SimulationEnvironment environment;

    /**
     * The current time that was used for the time set.
     */
    private double currentTime;

    /**
     * The number of distinct tokens.
     */
    private int distinctTokenCount;

    /**
     * The number of free tokens.
     */
    private int freeTokenCount;

    /**
     * The number of tested tokens.
     */
    private int testedTokenCount;

    /**
     * The token free count array. Its length equals distinctTokenCount.
     */
    private int[] tokenFreeCounts;

    /**
     * The token accessors array. Its length equals distinctTokenCount.
     */
    private ObjectAccessor[] tokens;

    /**
     * The tokens string array. Its length equals distinctTokenCount.
     */
    private String[] tokenStrings;

    /**
     * The token is tested array. Its length equals distinctTokenCount.
     */
    private boolean[] tokenTesteds;

    /**
     * The token time arrays array. Its outer length equals distinctTokenCount.
     */
    private double[][] tokenTimeArrays;

    /**
     * The token time multiplicities arrays array. Its outer length equals
     * distinctTokenCount.
     */
    private int[][] tokenTimeMultiplicitiesArrays;

    /**
     * Creates a new MarkingAccessorImpl.
     *
     * @param placeInstance
     *            The place instance for the marking accessor.
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public MarkingAccessorImpl(final PlaceInstance placeInstance,
                               SimulationEnvironment env)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.environment = env;

        SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    Vector<Object> tokenVector = new Vector<Object>();
                    placeInstance.lock.lock();
                    try {
                        Iterator<Object> iterator = placeInstance.getDistinctTestableTokens()
                                                                 .iterator();

                        while (iterator.hasNext()) {
                            tokenVector.addElement(iterator.next());
                        }

                        distinctTokenCount = tokenVector.size();
                        tokenFreeCounts = new int[distinctTokenCount];
                        tokens = new ObjectAccessor[distinctTokenCount];
                        tokenStrings = new String[distinctTokenCount];
                        tokenTesteds = new boolean[distinctTokenCount];
                        tokenTimeArrays = new double[distinctTokenCount][];
                        tokenTimeMultiplicitiesArrays = new int[distinctTokenCount][];

                        currentTime = SearchQueue.getTime();

                        freeTokenCount = 0;
                        testedTokenCount = 0;
                        for (int i = 0; i < distinctTokenCount; i++) {
                            Object token = tokenVector.elementAt(i);
                            tokens[i] = ObjectAccessorImpl.createObjectAccessor(token,
                                                                                environment);
                            tokenStrings[i] = token == null ? "null"
                                                            : token.toString();
                            TimeSet timeSet = placeInstance.getFreeTimeSet(token);
                            TimeSet extendedTimeSet = timeSet;

                            if (placeInstance.containsTestedToken(token)) {
                                tokenTesteds[i] = true;
                                testedTokenCount += 1;
                                extendedTimeSet = timeSet.including(currentTime);
                            } else {
                                tokenTesteds[i] = false;
                            }
                            tokenFreeCounts[i] = placeInstance.getTokenCount(token);
                            freeTokenCount += tokenFreeCounts[i];

                            tokenTimeArrays[i] = extendedTimeSet.asUniqueArray();
                            tokenTimeMultiplicitiesArrays[i] = new int[tokenTimeArrays[i].length];
                            for (int j = 0;
                                         j < tokenTimeMultiplicitiesArrays[i].length;
                                         j++) {
                                tokenTimeMultiplicitiesArrays[i][j] = timeSet
                                    .multiplicity(tokenTimeArrays[i][j]);
                            }
                        }
                    } finally {
                        placeInstance.lock.unlock();
                    }
                    return null;
                }
            });
    }

    /**
     * Return the number of distinct token values in this marking. As long as
     * two objects in the place marking are equal, they are reported as one
     * distinct token.
     *
     * @return the number of distinct tokens
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public int getDistinctTokenCount() throws RemoteException {
        return distinctTokenCount;
    }

    /**
     * Return the number of free tokens in this marking. Even if two tokens are
     * equal, they are both counted individually. Currently tested tokens are
     * not included in this count.
     *
     * @return the number of free tokens
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public int getFreeTokenCount() throws RemoteException {
        return freeTokenCount;
    }

    /**
     * Return the number of currently tested tokens in this marking.
     *
     * @return the number of tested tokens
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public int getTestedTokenCount() throws RemoteException {
        return testedTokenCount;
    }

    /**
     * Return the string representation of each object in the set of tokens. The
     * strings are generated by the <code>toString()</code> methods.
     *
     * This method produces the same result as like a call to
     * <code>getTokenString</code> for each token position and putting the
     * results into an array would do.
     *
     * @return string representations for each object
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     *
     * @see #getTokenString(int)
     */
    public String[] getAllTokenStrings() throws RemoteException {
        return tokenStrings;
    }

    /**
     * Return the numbers of free tokens that equal each object in the set of
     * tokens.
     *
     * This method produces the same result as like a call to
     * <code>getTokenFreeCount</code> for each token position and putting the
     * results into an array would do.
     *
     * @return free token counts for each object
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     *
     * @see #getTokenFreeCount(int)
     */
    public int[] getAllTokenFreeCounts() throws RemoteException {
        return tokenFreeCounts;
    }

    /**
     * Return whether the objects in the set of tokens are currently being
     * tested.
     *
     * This method produces the same result as like a call to
     * <code>getTokenTested</code> for each token position and putting the
     * results into an array would do.
     *
     * @return array of booleans (see <code>getTokenTested</code> for an
     *         explanation)
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     *
     * @see #getTokenTested(int)
     */
    public boolean[] getAllTokenTested() throws RemoteException {
        return tokenTesteds;
    }

    /**
     * Return the number of free tokens that equal the object at the given index
     * position.
     *
     * @param i
     *            the index of the token that has to be counted, must be between
     *            0 and getDistinctTokenCount()-1.
     * @return the number of free tokens that equal the token
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public int getTokenFreeCount(int i) throws RemoteException {
        return tokenFreeCounts[i];
    }

    /**
     * Return whether the object at the given index position is currently being
     * tested.
     *
     * @param i
     *            the index of the token that might be tested, must be between 0
     *            and getDistinctTokenCount()-1.
     * @return <code>true</code>, if the token is being tested -
     *         <code>false</code>, otherwise
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public boolean getTokenTested(int i) throws RemoteException {
        return tokenTesteds[i];
    }

    /**
     * Return the string representation of an object at a given index position.
     * The string is generated by the toString() method.
     *
     * @param i
     *            the index of the token to describe, must be between 0 and
     *            getDistinctTokenCount()-1.
     * @return the string representation
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public String getTokenString(int i) throws RemoteException {
        return tokenStrings[i];
    }

    /**
     * Returns the free time array an object at a given index position.
     *
     * @param i
     *            the index of the token to describe, must be between 0 and
     *            getDistinctTokenCount()-1.
     * @return the free time array.
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public double[] getTokenTimes(int i) throws RemoteException {
        return tokenTimeArrays[i];
    }

    /**
     * Returns the free time multiplicity array an object at a given index
     * position.
     *
     * @param i
     *            the index of the token to describe, must be between 0 and
     *            getDistinctTokenCount()-1.
     * @return the free time multiplicity array.
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public int[] getTokenTimeMultiplicities(int i) throws RemoteException {
        return tokenTimeMultiplicitiesArrays[i];
    }

    /**
     * Return an object that describes the token at a given index position.
     *
     * @param i
     *            the index of the token to return, must be between 0 and
     *            getDistinctTokenCount()-1.
     * @return the object accessor
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public ObjectAccessor getToken(int i) throws RemoteException {
        return tokens[i];
    }

    /**
     * Returns the current time that was used for the time set.
     *
     * @return The former current time.
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public double getCurrentTime() throws RemoteException {
        return currentTime;
    }

    /**
     * Try to remove one of the tokens at a given index. If concurrent actions
     * have already removed the token, return false. The given index must be
     * between 0 and getDistinctTokenCount()-1.
     *
     * <p>
     * To add a token to a place, use the <code>addSerializableToken</code>
     * method of the place accessor from which this marking accessor was
     * obtained. The change will not be reflected in this marking accessor
     * instance, naturally.
     * </p>
     *
     * @see PlaceInstanceAccessor#addSerializableToken
     *
     * @param i
     *            the index of the token to be removed
     * @return true, if the token was successfully removed
     * @exception java.rmi.RemoteException
     *                if an RMI failure occured.
     */
    public boolean removeOneOf(int i) throws RemoteException {
        throw new RuntimeException("Not yet implemented");
    }
}