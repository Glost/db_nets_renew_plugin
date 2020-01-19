package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.TraceEvent;
import de.renew.engine.searcher.TriggerableCollection;
import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;

import de.renew.net.event.PlaceEventListener;
import de.renew.net.event.PlaceEventListenerSet;
import de.renew.net.event.PlaceEventProducer;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import de.renew.util.DelayedFieldOwner;
import de.renew.util.Lock;
import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
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
public abstract class PlaceInstance implements PlaceEventProducer, Serializable,
                                               DelayedFieldOwner {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PlaceInstance.class);
    static final long serialVersionUID = 562364463914372791L;
    protected NetInstance netInstance;
    protected Place place;

    /**
     * Here we store tokens that constitute the initial marking.
     * These tokens are computed in the constructor, but their
     * insertion must be traced later on and maybe they must even
     * be inserted into the place instance.
     **/
    protected List<Object> initTokens;

    /**
     * This flag denotes whether the initial tokens have been put
     * into the place instance.
     */
    protected boolean earlyTokens;

    /**
     * This field contains those objects that might have to be triggered once after
     * a change to the current marking.
     *
     * It is not really transient, but as we want
     * to cut down the serialization recursion depth, it
     * is serialized manually.
     **/
    transient protected TriggerableCollection triggerables;

    /**
     * The order in which locks are requested is governed by
     * this number, which is unique within a single simulation.
     *
     * lockOrder and lock were originally final, but to allow the
     * creation of new values on deserialization the modifier
     * had to be removed.
     */
    public transient long lockOrder;

    /**
     * The lock that controls access to this place instance.
     */
    public transient Lock lock;

    /**
     * The listeners that want to be informed about every update
     * of the current marking.
     **/
    protected transient PlaceEventListenerSet listeners = new PlaceEventListenerSet();

    /**
     * Create a new place instance for a given net instance
     * reflecting a certain place. If desired, the constructor
     * will already calculate in the initial marking.
     *
     * @param netInstance the owning net instance
     * @param place the semantic level place
     * @param wantInitialTokens true, if initial marking should be calculated
     */
    PlaceInstance(NetInstance netInstance, Place place,
                  boolean wantInitialTokens) throws Impossible {
        this.netInstance = netInstance;
        this.place = place;

        lockOrder = de.renew.util.Orderer.getTicket();
        lock = new Lock();

        triggerables = new TriggerableCollection();

        initTokenStorage();


        // Copy the early token flag. Maybe this flag changes
        // later on. (Well, it shouldn't, but let's be safe.)
        earlyTokens = netInstance.getNet().earlyTokens;


        // Let's calculate the initial bag of tokens.
        initTokens = new ArrayList<Object>();
        for (TokenSource tokenSource : place.inscriptions) {
            VariableMapper mapper = new VariableMapper();
            Variable thisVariable = mapper.map(new LocalVariable("this", false));
            Unify.unify(thisVariable, netInstance, null);

            Object token = tokenSource.createToken(mapper);
            initTokens.add(token);

            if (earlyTokens) {
                // I am requested to insert the tokens now.
                internallyInsertToken(token, SearchQueue.getTime(), false);
            }
        }
    }

    protected abstract void initTokenStorage();

    public String toString() {
        return netInstance.toString() + "." + place.toString();
    }

    public NetInstance getNetInstance() {
        return netInstance;
    }

    public Place getPlace() {
        return place;
    }

    // Access the triggerables. We do not lock the place instance during
    // accesses to the triggerables contrary to earlier implementations.
    // The triggerables have to take care of locking themselves.
    public TriggerableCollection triggerables() {
        return triggerables;
    }

    public void addPlaceEventListener(PlaceEventListener listener) {
        listeners.addPlaceEventListener(listener);
    }

    public void removePlaceEventListener(PlaceEventListener listener) {
        listeners.removePlaceEventListener(listener);
    }

    /**
     * Returns the set of currently untested tokens.
     */
    public abstract Set<Object> getDistinctTokens();

    /**
     * Returns the set of currently untested tokens.
     * At least those tokens which are unifiable with pattern
     * should be included.
     */
    public abstract Set<Object> getDistinctTokens(Object pattern);

    public abstract Set<Object> getDistinctTestableTokens();

    public abstract Set<Object> getDistinctTestableTokens(Object pattern);

    public abstract int getNumberOfTokens();

    /**
     * Return the number of tokens that are currently tested
     * by a transition, but that reside in this place.
     * We do not count multiple tests on a single token as
     * multiple tokens.
     */
    public abstract int getNumberOfTestedTokens();

    public abstract boolean isEmpty();

    abstract boolean containsToken(Object token);

    public abstract int getTokenCount(Object token);

    public abstract TimeSet getFreeTimeSet(Object token);

    // Returns the delay until a set of untested tokens
    // is available that matches the given delay times.
    public abstract double computeEarliestTime(Object token, TimeSet times);

    // Here consider only already tested tokens.
    public abstract boolean containsTestedToken(Object token);

    // Here we consider both kinds of tokens.
    public abstract boolean containsTestableToken(Object token);

    protected IDRegistry registry() {
        return netInstance.getRegistry();
    }

    // Make sure a token keeps its ID. Only while the
    // token is reserved or contained in the place
    // it is guaranteed that it keeps its ID.
    public void reserve(Object token) {
        // Increase the number of registrations.
        registry().reserve(token);
    }

    public String getTokenID(Object token) {
        return registry().getID(token);
    }

    public void unreserve(Object token) {
        // Decrease the number of registrations.
        registry().unreserve(token);
    }

    // Try to remove the token. If no token is available at
    // the given time, an exception is thrown.
    public abstract double removeToken(Object token, double delay)
            throws Impossible;

    // Multiple tests on the same token do not remove
    // the token multiple times. Instead, the token is
    // removed once, and the number of removals is recorded
    // by a multiple insertion into the bag of tested tokens.
    public abstract double testToken(Object token) throws Impossible;

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
    public abstract void extractAllTokens(Vector<Object> tokens,
                                          Vector<Double> timeStamps);

    /**
     * This method inserts a token into the place instance and
     * and tries to assign the given ID to it. It is an error,
     * if the token is already registered in the place
     * instance with a different ID.
     *
     * This method should only be called when restoring a
     * simulation state from a database. Only in that case
     * there is any reason to assign a particular ID to a token.
     *
     * @param token the token to be inserted
     * @param id the token's ID
     * @param time the time stamp of the token
     * @exception java.lang.RuntimeException if the object was already
     *   registered with a different ID
     */
    public void insertTokenWithID(Object token, String id, double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        lock.lock();
        try {
            registry().setAndReserveID(token, id);
            internallyInsertToken(token, time, true);
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method inserts tokens into the place instance with notifications
     * and index updates, although it will not invoke the transaction
     * mechanism.
     *
     * This method must be used with care. It should only be called
     * in those cases where an invocation of the transaction
     * mechanism would be absolutely inappropriate, e.g., within
     * the transaction mechanims itself or if the transaction
     * is called explicitly or if a net instance is restored
     * from a database or if a previous token removal that did not reach the
     * database is undone.
     *
     * This method will take care of locking the place instance
     * automatically. It is however, allowed that the current thread
     * has already locked it, if this is required due to
     * deadlock prevention.
     *
     * @param token the token to be inserted
     * @param time the time stamp of the token
     * @param alreadyRegistered true, if the caller has already
     *   registered the new token at the IDRegistry.
     */
    public abstract void internallyInsertToken(Object token, double time,
                                               boolean alreadyRegistered);

    /**
     * This method records the effect of a token insertion
     * in a transaction object. If automatic insertion is requested,
     * then the token is actually inserted after the transaction has
     * been committed.
     *
     * @param token the token to be inserted
     * @param time the time stamp of the token
     * @param automaticInsertion true, if the transaction should care about
     *   the insertion of the token.
     */
    public void transactionInsertToken(Object token, double time,
                                       boolean automaticInsertion) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Transaction transaction = TransactionSource.get();
        try {
            transaction.addToken(this, token, time, automaticInsertion);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Insert a token into the place instance at a specified time.
     * The token deposit is written to the currently active transaction.
     * Only after that transaction has been committed, the token
     * will occur in the place instance. If not transaction is active,
     * the deposit takes place immediately.
     *
     * This is the standard method call that applications should
     * use to get tokens into a net. It will not print trace messages,
     * but it will ultimately notify the listeners and triggerables
     * of the change.
     *
     * This method will take care of locking the place instance
     * automatically. It is however, allowed that the current thread
     * has already locked the place instance, if this is required
     * due to deadlock prevention.
     *
     * @param token the token to be inserted
     * @param time the time stamp of the token
     */
    public void insertToken(Object token, double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        transactionInsertToken(token, time, true);
    }

    // Undo a testToken call. Once the last token is returned,
    // a token is reinserted into the bag of free tokens.
    //
    // The time is used as the time stamp of the returned token,
    // if the current test is the last. Otherwise, the argument is
    // ignored.
    public abstract void untestToken(Object token);

    /**
     * Print trace messages for the insertion of initial tokens.
     *
     */
    private void traceInitialTokens(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Iterator<Object> iterator = initTokens.iterator();

        // Anything to do?
        while (iterator.hasNext()) {
            Object token = iterator.next();
            if (place.getTrace()) {
                // log activity on net level
                SimulatorEventLogger.log(stepIdentifier,
                                         new TraceEvent("Initializing " + token
                                                        + " into " + this), this);
            }
        }
    }

    /**
     * The associated net instance will call this method while
     * its creation is confirmed and trace messages are printed.
     *
     * I will notify the database about all tokens that I kept during
     * my initialisation, if the tokens must be available early.
     *
     * You must call this method at most once.
     *
     * In fact, this method is rather a kludge in the sense
     * that transaction support should not be activated
     * at all if a net requests early confirmation.
     */
    void earlyConfirmation() {
        if (earlyTokens) {
            Iterator<Object> enumeration = initTokens.iterator();

            // Record each token individually in the database.
            while (enumeration.hasNext()) {
                Object token = enumeration.next();
                double time = SearchQueue.getTime();


                // Do not keep a note to insert to tokens
                // at commit time. They were added already during the
                // creation of this net instance.
                transactionInsertToken(token, time, false);
            }
        }
    }

    /**
     * My net will call this method while its creation is traced.
     *
     */
    void earlyConfirmationTrace(StepIdentifier stepIdentifier) {
        if (earlyTokens) {
            traceInitialTokens(stepIdentifier);
        }
    }

    /**
     * My net will call this method if its creation is confirmed.
     * I will free all tokens that I kept during my initialisation.
     *
     * You must call this method at most once.
     *
     */
    void lateConfirmation(StepIdentifier stepIdentifier) {
        if (!earlyTokens) {
            traceInitialTokens(stepIdentifier);

            Iterator<Object> iterator = initTokens.iterator();

            // Anything to do?
            while (iterator.hasNext()) {
                Object token = iterator.next();
                insertToken(token, SearchQueue.getTime());
            }
        }


        // Null the field to allow garbage collection.
        initTokens = null;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except storing the not-really-transient field
     * triggerables.
     * If the stream used is a RenewObjectOutputStream, this
     * field is delayed to cut down recursion depth.
     * The domain trace feature of this special stream is also
     * used.
     * @see de.renew.util.RenewObjectOutputStream
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        RenewObjectOutputStream rOut = null;
        if (out instanceof RenewObjectOutputStream) {
            rOut = (RenewObjectOutputStream) out;
        }
        if (rOut != null) {
            rOut.beginDomain(this);
        }
        out.defaultWriteObject();
        if (rOut != null) {
            rOut.delayedWriteObject(triggerables, this);
            rOut.endDomain(this);
        } else {
            out.writeObject(triggerables);
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     * Creates new lock object and gets a new lock order ticket.
     * The method also restores the not-really-transient field
     * <code>triggerables</code>, <b>if</b> the
     * stream used is <b>not</b> a RenewObjectInputStream.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lockOrder = de.renew.util.Orderer.getTicket();
        lock = new Lock();
        listeners = new PlaceEventListenerSet();

        if (in instanceof RenewObjectInputStream) {
            // Do nothing, the fields will be
            // reassigned by the stream soon.
        } else {
            triggerables = (TriggerableCollection) in.readObject();
        }
    }

    /**
     * Method used on deserialization by RenewObjectInputStream.
     * Reassigns values to the not-really-transient fields,
     * one at a time.
     * <p>
     * Subclasses must implement this method, and the first
     * statement should be a call to {@link #tryReassignField}.
     * Example:
     * <pre>
     *   public void reassignField(Object value) throws java.io.IOException {
     *     if (!tryReassignField(value)) {
     *       // Subclass field assignment goes here...
     *     }
     *   }
     * </pre>
     * </p>
     **/
    public abstract void reassignField(Object value) throws java.io.IOException;

    /**
     * Reassigns the value to the not-really-transient field
     * <code>triggerables</code>.
     * <p>
     * This method must be called by subclasses at the beginning
     * of their {@link #reassignField} implementation.
     * If the method returns <code>false</code>, the value
     * was not consumed and can be used by the subclass.
     * </p><p>
     * If a subclass wants to override this method, a <code>super</code>
     * call and correct interpretation of the return value is mandatory.
     * </p>
     * @param value The value to be reassigned to a field.
     * @return <code>true</code>  if the value was consumed <br>
     *         <code>false</code> if the value didn't fit to
     *                            any delayed field of this class
     **/
    protected boolean tryReassignField(Object value) throws java.io.IOException { //NOTICEthrows
        if (value instanceof TriggerableCollection) {
            triggerables = (TriggerableCollection) value;
            return true;
        } else {
            return false;
        }
    }
}