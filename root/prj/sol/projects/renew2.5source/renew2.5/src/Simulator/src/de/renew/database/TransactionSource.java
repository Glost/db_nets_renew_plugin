package de.renew.database;

import java.util.Hashtable;


/**
 * Here we provide methods to access to the database that
 * stores the net marking information. This is done
 * by the means of static methods.
 *
 * Transactions are grouped according to the current thread
 * in a similar way as done by JTA. If a transaction is
 * explicitly started, all subsequent calls within the same thread
 * that request the current transaction will return the same
 * transaction object. Only after an explicit commit in this
 * thread will the transaction be freed.
 *
 * If the current transaction is queried without having started
 * a transaction, a special transaction is provided that performs
 * an immediate commit after each operation.
 */
public class TransactionSource {
    private static TransactionStrategy strategy = null;
    private static Hashtable<Thread, Transaction> transactions = new Hashtable<Thread, Transaction>();

    /**
     * The hashtable lock controls the access to the
     * transactions hashtable for means of creating or removing
     * transactions.
     */
    private static Object hashtableLock = new Object();

    /**
     * The strategy lock ensure that no two transactions
     * are executed by the strategy object at the same time.
     *
     * We cannot use one lock object for both tasks, because
     * the current transaction must be requested while a place
     * is locked and the palce must locked while a transaction is
     * executing.
     */
    private static Object strategyLock = new Object();

    /**
     * This class is totally static. One must not create instances
     * of it.
     */
    private TransactionSource() {
    }

    /**
     * Set the transaction strategy. This allows an application
     * to configure how the database is accessed. The transaction strategy
     * will be called once for each transaction commit.
     *
     * If this method has not been called or if the strategy has
     * been set to null explicitly, no database accesses will be performed
     * in the case of marking changes. This can be useful, if certain
     * actions have to be performed so that the database and the nets get
     * in sync.
     *
     * @param newStrategy the strategy that is to be called for all
     *   subsequent commit operations
     */
    public static void setStrategy(TransactionStrategy newStrategy) {
        synchronized (strategyLock) {
            strategy = newStrategy;
        }
    }

    /**
     * Execute one transaction by delegating it to the
     * strategy object and make sure that no two transactions
     * are executed at the same time.
     * @param createActions an array of net creation actions
     * @param addActions an array of token insertion actions
     * @param removeActions an array of token removal actions
     * @param deleteActions an array of net deletion actions
     * @exception Exception If any exception has occurred.
     */
    public static void perform(NetAction[] createActions,
                               TokenAction[] addActions,
                               TokenAction[] removeActions,
                               NetAction[] deleteActions)
            throws Exception {
        synchronized (strategyLock) {
            if (strategy != null) {
                strategy.perform(createActions, addActions, removeActions,
                                 deleteActions);
            }
        }
    }

    /**
     * Notifies the TransactionSource that a net instance
     * is now being watched with a drawing.
     * @param netInstanceID the ID of the net instance of which a
     *   drawing has been opened, as per {@link de.renew.net.NetInstance#getID}
     * @exception Exception If any exception has occurred.
     */
    public static void netInstanceDrawingOpened(String netInstanceID)
            throws Exception {
        synchronized (strategyLock) {
            if (strategy != null) {
                strategy.netInstanceDrawingOpened(netInstanceID);
            }
        }
    }

    /**
     * Notifies the TransactionStrategy that a net instance
     * is now not being watched with a drawing anymore.
     * @param netInstanceID the ID of the net instance of which the
     * drawing has been closed, as per {@link de.renew.net.NetInstance#getID}
     * @exception Exception If any exception has occurred.
     */
    public static void netInstanceDrawingClosed(String netInstanceID)
            throws Exception {
        synchronized (strategyLock) {
            if (strategy != null) {
                strategy.netInstanceDrawingClosed(netInstanceID);
            }
        }
    }

    /**
     * Notifies the TransactionStrategy about simulation
     * state changes.
     * @param inited The simulation is inited (not terminated).
     * @param running The simulation is running.
     * @exception Exception If any exception has occurred.
     */
    public static void simulationStateChanged(boolean inited, boolean running)
            throws Exception {
        synchronized (strategyLock) {
            if (strategy != null) {
                strategy.simulationStateChanged(inited, running);
            }
        }
    }

    /**
     * Prepare a transaction for the current thread. All subsequent
     * get operations within this thread will return this transaction
     * until either commit or rollback is performed.
     */
    public static void start() {
        synchronized (hashtableLock) {
            Thread current = Thread.currentThread();
            if (transactions.containsKey(current)) {
                throw new RuntimeException("Nested transactions not supported.");
            }
            transactions.put(Thread.currentThread(), new Transaction(false));
        }
    }

    /**
     * Acquire the current transaction. If no transaction
     * has already been started, a special transaction object will
     * be generated that commits on each operation. It this case, no
     * explicit commit is required or even allowed.
     *
     * @return a transaction object
     */
    public static Transaction get() {
        synchronized (hashtableLock) {
            Thread current = Thread.currentThread();
            Transaction transaction = transactions.get(current);
            if (transaction != null) {
                // A transaction is already active.
                return transaction;
            }


            // No transaction has been explicitly started.
            //
            // We create a transaction that will use an individual
            // subtransaction every time that it has to perform a
            // change. This way the changes are propagated to the
            // database, but not handled in a group.
            return new Transaction(true);
        }
    }

    /**
     * Get the current transaction based on the current thread,
     * remove it from the hashtable and return it for commit
     * or rollback.
     *
     * @return the only remaining reference to the current transaction
     */
    private static Transaction extractCurrentTransaction() {
        synchronized (hashtableLock) {
            // Is a transaction running?
            Thread current = Thread.currentThread();
            Transaction transaction = transactions.get(current);
            if (transaction != null) {
                // We do not need this transaction any more.
                transactions.remove(current);
            }
            return transaction;
        }
    }

    /**
     * All the changes made to the net database since the current
     * transaction's creation time are permanently recorded.
     * Changes to the net database which are NOT followed
     * by commit are not permanently recorded and are not available in a
     * subsequent session with the net database.
     *
     * If not transaction was previously started, this call is
     * ignored.
     */
    public static void commit() throws Exception {
        // The call to extractCurrentTransaction() handles the synchronisation
        // that avoids a possible corruption of the transactions hashtable.
        Transaction transaction = extractCurrentTransaction();
        if (transaction != null) {
            transaction.commit();
        }
    }

    /**
     * All the changes made to the net database since the current
     * transaction's creation time are discarded.
     */
    public static void rollback() {
        // See comment in commit() about synchronisation.
        Transaction transaction = extractCurrentTransaction();
        if (transaction != null) {
            transaction.rollback();
        }
    }
}