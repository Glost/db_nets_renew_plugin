package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.TokenEvent;

import de.renew.unify.Impossible;

import de.renew.util.Queue;
import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;

import java.util.Collections;
import java.util.Set;
import java.util.Vector;


/**
 * I will collect tokens for a single place instance. I also
 * keep count of the number of tokens that are currently being tested.
 *
 * Whenever my marking is changed I will notify my triggerables.
 *
 * Anybody who needs to make sure that my marking does not change should
 * lock on me. Synchronization does not help!
 */
public class FIFOPlaceInstance extends PlaceInstance {
    static final long serialVersionUID = 5987006166655815747L;

    /**
     *  <code>tokenTests</code> counts numbers of test
     * on the queue's front token.
     */
    private int tokenTests;

    /**
     *  <code>tokenQueue</code> is the queue for a FIFO place.
     *
     * This field is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.
     */
    private transient Queue<Object> tokenQueue;

    /**
     * Create a new place instance for a given net instance
     * reflecting a certain place. If desired, the constructor
     * will already calculate in the initial marking.
     *
     * @param netInstance the owning net instance
     * @param place the semantic level place
     * @param wantInitialTokens true, if initial marking should be calculated
     */
    FIFOPlaceInstance(NetInstance netInstance, Place place,
                      boolean wantInitialTokens) throws Impossible {
        super(netInstance, place, wantInitialTokens);
    }

    protected void initTokenStorage() {
        // Create Queue for FIFO place.
        tokenQueue = new Queue<Object>();


        // Currently, there are no tokens available.
        tokenTests = 0;
    }

    private Set<Object> getFrontSet() {
        return Collections.singleton(tokenQueue.front());
    }

    public Set<Object> getDistinctTokens() {
        if (tokenTests == 0 && !tokenQueue.isEmpty()) {
            return getFrontSet();
        }
        return Collections.emptySet();
    }

    public Set<Object> getDistinctTokens(Object pattern) {
        return getDistinctTokens();
    }

    public Set<Object> getDistinctTestableTokens() {
        if (!tokenQueue.isEmpty()) {
            return getFrontSet();
        }
        return Collections.emptySet();
    }

    public Set<Object> getDistinctTestableTokens(Object pattern) {
        return getDistinctTestableTokens();
    }

    public int getNumberOfTokens() {
        return (tokenQueue.isEmpty() ? 0 : 1);
    }

    /**
     * Return the number of tokens that are currently tested
     * by a transition, but that reside in this place.
     * We do not count multiple tests on a single token as
     * multiple tokens.
     */
    public int getNumberOfTestedTokens() {
        return (tokenTests == 0 ? 0 : 1);
    }

    // Evtl. zu PlaceInstance
    public boolean isEmpty() {
        return getNumberOfTokens() + getNumberOfTestedTokens() == 0;
    }

    /**
     * <code>containsToken</code> reports whether  <code>Object</code> token
     * equals the queue's front element.
     *
     * @param token an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    boolean containsToken(Object token) {
        return containsTestableToken(token) && (tokenTests == 0);
    }

    /**
     *  <code>getTokenCount</code> is 1 iff token is contained; else 0.
     *
     * @param token an <code>Object</code> value
     * @return an <code>int</code> value
     */
    public int getTokenCount(Object token) {
        return (containsToken(token) ? 1 : 0);
    }

    public TimeSet getFreeTimeSet(Object token) {
        if (containsToken(token)) {
            return TimeSet.ZERO;
        }
        return TimeSet.EMPTY;
    }

    // Returns the delay until a set of untested tokens
    // is available that matches the given delay times.
    public double computeEarliestTime(Object token, TimeSet times) {
        return 0.0;
    }

    // Here consider only already tested tokens.
    public boolean containsTestedToken(Object token) {
        return (containsTestableToken(token) && (tokenTests > 0));
    }

    // Here we consider both kinds of tokens.
    public boolean containsTestableToken(Object token) {
        // nulls will be converted by the called methods.
        return tokenQueue.front().equals(token);
    }

    /**
     * Try to remove the token. If no token is available at
     * the given time, an exception is thrown.
     */
    public double removeToken(Object token, double delay)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        double removedTime;
        lock.lock();
        try {
            removedTime = 0.0;

            if (containsToken(token)) {
                tokenQueue.dequeue();
            } else {
                throw new Impossible();
            }

            // Transaction???
            triggerables.proposeSearch();

            // Inform all listeners.
            TokenEvent te = new TokenEvent(this, token);
            listeners.tokenRemoved(te);

            // Allow the master place to distribute the event, too.
            place.getListenerSet().tokenRemoved(te);

            // Now all actions are performed. The place instance
            // uses one less reference to the token now.
            unreserve(token);
        } finally {
            lock.unlock();
        }
        return removedTime;
    }

    // Multiple tests on the same token do not remove
    // the token multiple times. Instead, the token is
    // removed once, and the number of removals is recorded
    // by a multiple insertion into the bag of tested tokens.
    public double testToken(Object token) throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        double removedTime;
        lock.lock();
        try {
            boolean wantNotify = false;

            removedTime = 0.0; // SearchQueue.getTime();

            if (containsToken(token)) {
                if (tokenTests == 0) {
                    // We must notify the listeners only if a free token has
                    // become tested. If a token was already tested, the new
                    // test simply delays the time span until a possible
                    // activation.
                    wantNotify = true;
                }
                tokenTests++;
            } else {
                throw new Impossible();
            }


            // No need to touch IDRegistry, because token is
            // still kept in the place.
            if (wantNotify) {
                triggerables.proposeSearch();

                TokenEvent te = new TokenEvent(this, token);

                // Inform all listeners.
                listeners.tokenTested(te);

                // Allow the master place to distribute the event, too.
                place.getListenerSet().tokenTested(te);
            }
        } finally {
            lock.unlock();
        }
        return removedTime;
    }

    /**
     * This method removes all tokens from the place and
     * puts them into a vector that is provided as an argument.
     * The method should be used when the simulator is at rest
     * or at least the place is locked and no firings are
     * testing tokens. Otherwise some tested token may not
     * be removable.
     *
     * For each token, the original time stamp is recorded
     * in a second vector. The same number of elements
     * is added to each vector, one entry for each token in
     * the place. Multiple identical tokens result in multiple
     * identical elements in the vector.
     *
     * @param tokens vector where removed tokens will be placed
     *   or null, if the tokens should be discarded.
     * @param timeStamps vector where time stamps will be placed
     *   or null, if the tokens should be discarded.
     */
    public void extractAllTokens(Vector<Object> tokens,
                                 Vector<Double> timeStamps) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            if (tokenTests == 0) {
                while (!tokenQueue.isEmpty()) {
                    Object token = tokenQueue.dequeue(); //FIXME: we should call remove() to establish all side effects
                    if (tokens != null) {
                        tokens.addElement(token);
                    }
                    if (timeStamps != null) {
                        timeStamps.addElement(new Double(0.0));
                    }
                }
            }

            // if front element is tested, I do not remove any token.
        } finally {
            lock.unlock();
        }
    }

    public void insertToken(Object token, double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        internallyInsertToken(token, time, true);
    }

    public void internallyInsertToken(Object token, double time,
                                      boolean alreadyRegistered) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            tokenQueue.enqueue(token);

            if (!alreadyRegistered) {
                reserve(token);
            }

            // Now inform triggerables and listeners.
            // By now they might find out by chance, so we should better
            // tell them directly.
            triggerables.proposeSearch();

            // Inform all listeners.
            TokenEvent te = new TokenEvent(this, token);
            listeners.tokenAdded(te);

            // Allow the master place to distribute the event, too.
            place.getListenerSet().tokenAdded(te);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Undo a testToken call. Once the last token is returned,
     * a token is reinserted into the bag of free tokens.
     *
     * The time is used as the time stamp of the returned token,
     * if the current test is the last. Otherwise, the argument is ignored.
     */
    public void untestToken(Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            // No need to change the test index, because a tested token
            // will become free or an equivalent token will remain tested.
            // No need to touch IDRegistry, because token is
            // still kept in the place.
            if (containsTestedToken(token)) {
                tokenTests--;
                if (tokenTests == 0) {
                    triggerables.proposeSearch();

                    // Inform all listeners.
                    TokenEvent te = new TokenEvent(this, token);
                    listeners.tokenUntested(te);


                    // Allow the master place to distribute the event, too.
                    place.getListenerSet().tokenUntested(te);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except storing the not-really-transient field
     * <code>tokenQueue</code>.
     * If the stream used is a RenewObjectOutputStream, this
     * field is delayed to cut down recursion depth.
     * The domain trace feature of this special stream is also
     * used.
     * @see de.renew.util.RenewObjectOutputStream
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        out.defaultWriteObject();
        if (rOut != null) {
            rOut.delayedWriteObject(tokenQueue, this);
            rOut.endDomain(this);
        } else {
            out.writeObject(tokenQueue);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     * The method also restores the not-really-transient field
     * <code>tokenQueue</code>, <b>if</b> the
     * stream used is <b>not</b> a RenewObjectInputStream.
     **/
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();

        if (in instanceof RenewObjectInputStream) {
            // Do nothing, the fields will be
            // reassigned by the stream soon.
        } else {
            tokenQueue = (Queue<Object>) in.readObject();
        }
    }

    /**
     * Method used on deserialization by RenewObjectInputStream.
     * Reassigns a value to the not-really-transient field
     * <code>tokenQueue</code>.
     **/
    @SuppressWarnings("unchecked")
    public void reassignField(Object value) throws java.io.IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (tryReassignField(value)) {
            // Do nothing because the superclass used the value
        } else if (value instanceof Queue) {
            tokenQueue = (Queue<Object>) value;
        } else {
            throw new java.io.NotSerializableException("Value of unexpected type given to FIFOPlaceInstance.reassign():"
                                                       + value.getClass()
                                                              .getName() + ".");
        }
    }

    protected void finalize() throws Throwable {
        // Deregister all tokens at the ID registry.
        while (!tokenQueue.isEmpty()) {
            Object token = tokenQueue.dequeue();
            unreserve(token);
        }
        super.finalize();
    }
}