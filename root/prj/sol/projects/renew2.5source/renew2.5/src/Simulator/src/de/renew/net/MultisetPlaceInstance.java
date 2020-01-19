package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.TokenEvent;

import de.renew.unify.Impossible;
import de.renew.unify.TupleIndex;

import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * I will collect tokens for a single place instance. I also
 * keep count of the number of tokens that are currently being tested.
 *
 * Whenever my marking is changed I will notify my triggerables.
 *
 * Anybody who needs to make sure that my marking does not change should
 * lock on me. Synchronisation does not help!
 */
public class MultisetPlaceInstance extends PlaceInstance {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MultisetPlaceInstance.class);
    static final long serialVersionUID = -1174662340665947999L;
    private int freeTokenCount;
    private int testedTokenCount;

    /**
     * This field is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.
     **/
    transient private TokenBag freeTokens;

    /**
     * This field is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.
     **/
    transient private TestTokenBag testedTokens;

    /**
     * All elements contained in this index must also
     * appear in <code>testIndex</code>.
     *
     * This field is transient and will be recomputed
     * on deserialization.
     **/
    transient private TupleIndex freeIndex;

    /**
     * Must contain at least all elements
     * contained in <code>freeIndex</code>.
     *
     * This field is transient and will be recomputed
     * on deserialization.
     **/
    transient private TupleIndex testIndex;

    /**
     * Create a new place instance for a given net instance
     * reflecting a certain place. If desired, the constructor
     * will already calculate in the initial marking.
     *
     * @param netInstance the owning net instance
     * @param place the semantic level place
     * @param wantInitialTokens true, if initial marking should be calculated
     */
    MultisetPlaceInstance(NetInstance netInstance, Place place,
                          boolean wantInitialTokens) throws Impossible {
        super(netInstance, place, wantInitialTokens);
    }

    protected void initTokenStorage() {
        freeTokens = new TokenBag();
        testedTokens = new TestTokenBag();
        freeTokenCount = 0;
        testedTokenCount = 0;

        freeIndex = new TupleIndex();
        testIndex = new TupleIndex();
    }

    /**
     * Returns an enumeration without repetitions
     * of currently untested tokens.
     *
     * @return a <code>CollectionEnumeration</code> value
     */
    public Set<Object> getDistinctTokens() {
        return freeIndex.getAllElements();
    }

    /**
     * Returns an enumeration without repetitions
     * of currently untested tokens.
     * At least those tokens which are unifiable with pattern
     * should be included.
     *
     * @param pattern an <code>Object</code> value
     * @return a <code>CollectionEnumeration</code> value
     */
    public Set<Object> getDistinctTokens(Object pattern) {
        return freeIndex.getPossibleMatches(pattern);
    }

    public Set<Object> getDistinctTestableTokens() {
        return testIndex.getAllElements();
    }

    public Set<Object> getDistinctTestableTokens(Object pattern) {
        return testIndex.getPossibleMatches(pattern);
    }

    public int getNumberOfTokens() {
        return freeTokenCount;
    }

    /**
    * Return the number of tokens that are currently tested
    * by a transition, but that reside in this place.
    * We do not count multiple tests on a single token as
    * multiple tokens.
    */
    public int getNumberOfTestedTokens() {
        return testedTokenCount;
    }

    public boolean isEmpty() {
        return getNumberOfTokens() + getNumberOfTestedTokens() == 0;
    }

    boolean containsToken(Object token) {
        return freeTokens.includesAnytime(token);
    }

    public int getTokenCount(Object token) {
        return freeTokens.getMultiplicity(token);
    }

    public TimeSet getFreeTimeSet(Object token) {
        return freeTokens.getTimeSet(token);
    }

    // Returns the delay until a set of untested tokens
    // is available that matches the given delay times.
    public double computeEarliestTime(Object token, TimeSet times) {
        return freeTokens.computeEarliestTime(token, times);
    }

    // Here consider only already tested tokens.
    public boolean containsTestedToken(Object token) {
        return testedTokens.includesTested(token);
    }

    // Here we consider both kinds of tokens.
    public boolean containsTestableToken(Object token) {
        // nulls will be converted by the called methods.
        return containsToken(token) || containsTestedToken(token);
    }

    // Try to remove the token. If no token is available at
    // the given time, an exception is thrown.
    public double removeToken(Object token, double delay)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        double removedTime;
        lock.lock();
        try {
            removedTime = 0;
            try {
                removedTime = freeTokens.removeWithDelay(token, delay);
            } catch (Exception e) {
                throw new Impossible(e);
            }

            freeTokenCount--;

            if (!freeTokens.includesAnytime(token)) {
                freeIndex.remove(token);
                if (!testedTokens.includesTested(token)) {
                    testIndex.remove(token);
                }
            }


            // All changes to the place instance state have been performed.
            // It is now safe to notify the current transaction,
            // the triggerables and the listeners (if any) of the change.
            //
            // Up to now the token has still kept its ID, because concurrent
            // accesses to the place instances are stopped by the lock.
            Transaction transaction = TransactionSource.get();
            try {
                transaction.removeToken(this, token, removedTime);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            triggerables.proposeSearch();

            // Notify all listeners.
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
            removedTime = SearchQueue.getTime();


            // Do we need to remove a token explicitly?
            // It seems so. This does not really matter, because testing is
            // not really visible in the timed (an hence sequential)
            // formalism.
            if (!testedTokens.includesTested(token)) {
                try {
                    // Remove a token that is already in the place.
                    removedTime = freeTokens.removeWithDelay(token, 0);
                } catch (Exception e) {
                    throw new Impossible(e);
                }


                // Fix the token counters. Only required if a
                // token was actually converted from free to tested.
                testedTokenCount++;
                freeTokenCount--;

                if (!freeTokens.includesAnytime(token)) {
                    freeIndex.remove(token);
                }

                // We must notify the listeners only if a free token has
                // become tested. If a token was already tested, the new
                // test simply delays the time span until a possible
                // activation.
                wantNotify = true;
            }

            testedTokens.addTested(token, removedTime);


            // No need to touch IDRegistry, because token is
            // still kept in the place.
            if (wantNotify) {
                triggerables.proposeSearch();

                // Notify all listeners.
                TokenEvent te = new TokenEvent(this, token);
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
            // In the following, I do not remove tested token.
            List<Object> uniqueValues = new ArrayList<Object>(getDistinctTokens());

            while (!uniqueValues.isEmpty()) {
                Object token = uniqueValues.remove(uniqueValues.size() - 1);


                // The following loop might be optimized, but
                // currently this is the safest solution.
                while (containsToken(token)) {
                    double time = 0;
                    try {
                        time = removeToken(token, Double.NEGATIVE_INFINITY);
                    } catch (Impossible e) {
                        throw new RuntimeException("Token was not removable.", e);
                    }
                    if (tokens != null) {
                        tokens.addElement(token);
                    }
                    if (timeStamps != null) {
                        timeStamps.addElement(new Double(time));
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void internallyInsertToken(Object token, double time,
                                      boolean alreadyRegistered) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            // Insert the tokens.
            if (!freeTokens.includesAnytime(token)) {
                freeIndex.insert(token);
                // Maybe there is an appropriate token being tested?
                if (!testedTokens.includesTested(token)) {
                    // No, remember the new token.
                    testIndex.insert(token);
                }
            }

            freeTokens.add(token, time);
            freeTokenCount++;

            if (!alreadyRegistered) {
                reserve(token);
            }

            // Now inform triggerables and listeners.
            // By now they might find out by chance, so we should better
            // tell them directly.
            triggerables.proposeSearch();

            // Notify all listeners.
            TokenEvent te = new TokenEvent(this, token);
            listeners.tokenAdded(te);

            // Allow the master place to distribute the event, too.
            place.getListenerSet().tokenAdded(te);
        } finally {
            lock.unlock();
        }
    }

    // Undo a testToken call. Once the last token is returned,
    // a token is reinserted into the bag of free tokens.
    //
    // The time is used as the time stamp of the returned token,
    // if the current test is the last. Otherwise, the argument is
    // ignored.
    public void untestToken(Object token) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            double time = testedTokens.removeTested(token);


            // No need to change the test index, because a tested token
            // will become free or an equivalent token will remain tested.
            // No need to touch IDRegistry, because token is
            // still kept in the place.
            if (!testedTokens.includesTested(token)) {
                if (!freeTokens.includesAnytime(token)) {
                    freeIndex.insert(token);
                }

                // Reinsert the token as a free token at the given time.
                freeTokens.add(token, time);

                // Fix the token count.
                freeTokenCount++;
                testedTokenCount--;

                // We must notify the listeners only if a tested token has
                // become free. If a token is still tested, the
                // untest simply shortens the time span until a possible
                // activation.
                triggerables.proposeSearch();

                // Notify all listeners.
                TokenEvent te = new TokenEvent(this, token);
                listeners.tokenUntested(te);

                // Allow the master place to distribute the event, too.
                place.getListenerSet().tokenUntested(te);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except storing the not-really-transient fields
     * freeTokens, testedTokens and triggerables.
     * If the stream used is a RenewObjectOutputStream, these
     * fields are delayed to cut down recursion depth.
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
            rOut.delayedWriteObject(freeTokens, this);
            rOut.delayedWriteObject(testedTokens, this);
            rOut.endDomain(this);
        } else {
            out.writeObject(freeTokens);
            out.writeObject(testedTokens);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     * The method also restores the not-really-transient fields
     * freeTokens and testedTokens, <b>if</b> the
     * stream used is <b>not</b> a RenewObjectInputStream.
     * In that case it also recomputes the transient fields
     * freeIndex and testedIndex.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        in.defaultReadObject();

        if (in instanceof RenewObjectInputStream) {
            // Do nothing, the fields will be
            // reassigned by the stream soon.
        } else {
            // Free tokens must be written first, so that
            // the indexes can be recomputed after reading the
            // tested tokens.
            freeTokens = (TokenBag) in.readObject();
            testedTokens = (TestTokenBag) in.readObject();
            recomputeIndexes();
        }
    }

    /**
     * Method used on deserialization by RenewObjectInputStream.
     * Reassigns values to the not-really-transient fields
     * freeTokens and testedTokens, one at a time.
     * Also recomputes freeIndex and testIndex after both
     * TokenBags got reassigned.
     **/
    public void reassignField(Object value) throws java.io.IOException {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (tryReassignField(value)) {
            // do nothing because the superclass used the value
        } else if (value instanceof TokenBag) {
            freeTokens = (TokenBag) value;
        } else if (value instanceof TestTokenBag) {
            testedTokens = (TestTokenBag) value;
            recomputeIndexes();
        } else {
            throw new java.io.NotSerializableException("Value of unexpected type given to MultiSetPlaceInstance.reassignField():"
                                                       + value.getClass()
                                                              .getName() + ".");
        }
    }

    /**
     * Rebuilds both TupleIndex fields (freeIndex and
     * testIndex) from scratch, using the elements of
     * the TokenBag fields freeTokens and testedTokens.
     * Needed by the deserialization methods readObject()
     * and reassignField() to update the transient
     * TupleIndex fields.
     **/
    private void recomputeIndexes() {
        freeIndex = new TupleIndex();
        addToIndex(freeIndex, freeTokens.uniqueElements());
        testIndex = new TupleIndex();
        addToIndex(testIndex, freeTokens.uniqueElements());
        addToIndex(testIndex, testedTokens.uniqueElements());
    }

    /**
     * Inserts all elements of the given TokenBag into
     * the given TupleIndex. Used by recomputeIndexes().
     **/
    private void addToIndex(TupleIndex index, Collection<Object> tokenList) {
        Iterator<Object> iterator = tokenList.iterator();
        while (iterator.hasNext()) {
            Object token = iterator.next();
            index.insert(token);
        }
    }

    protected void finalize() throws Throwable {
        // Deregister all tokens at the ID registry.
        for (Object token : testIndex.getAllElements()) {
            unreserve(token);
        }
        super.finalize();
    }
}