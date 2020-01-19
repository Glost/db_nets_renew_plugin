package de.renew.database;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;

import java.util.Vector;


/**
 * Accept tasks for a transaction and make sure to forward
 * them to the transaction strategy in due time. Additionally,
 * take a note of all token deposits and perform them after the
 * transaction was committed.
 *
 * This enables us to delay the actual token output and hence
 * the firing of further transitions until the firing of
 * the first transition is firmly recorded in the database.
 *
 * For any token that is added or removed, transactions
 * are also responsible for unreserving that token from the
 * place instance once the final fate of the transaction
 * (commit or rollback) has been decided and no more actions are
 * to be performed.
 */
public class Transaction {
    private boolean autoCommit;
    private Vector<NetAction> createActions;
    private Vector<NetAction> deleteActions;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(Transaction.class);

    /**
    * The delayed add actions are those token insertions
    * that have to be performed by the transaction during commit.
    * All delayed add action will also occur as add actions,
    * but not all add actions need to be delayed add actions.
    * Sometimes the simulator takes care of inserting the tokens.
    *
    * This vector contains token actions.
    * @see de.renew.database.TokenAction
    */
    private Vector<TokenAction> delayedAddActions;

    /**
    * This vector contains token actions for all token insertions.
    * @see de.renew.database.TokenAction
    */
    private Vector<TokenAction> addActions;

    /**
    * This vector contains token actions for all token removals.
    * @see de.renew.database.TokenAction
    */
    private Vector<TokenAction> removeActions;

    /**
     * Create a transaction that records all calls and commits them
     * jointly in the end or a transaction that immediately commits each
     * modification.
     *
     * @param autoCommit true, if all actions should be immediately committed
     */
    public Transaction(boolean autoCommit) {
        this.autoCommit = autoCommit;

        createActions = new Vector<NetAction>();
        deleteActions = new Vector<NetAction>();
        delayedAddActions = new Vector<TokenAction>();
        addActions = new Vector<TokenAction>();
        removeActions = new Vector<TokenAction>();
    }

    public synchronized void createNet(NetInstance instance)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(Transaction.class.getSimpleName()
                         + ": creating net action.");
        }
        createActions.addElement(new NetAction(instance));

        if (autoCommit) {
            commit();
        }
    }

    public synchronized void deleteNet(NetInstance instance)
            throws Exception {
        deleteActions.addElement(new NetAction(instance));

        if (autoCommit) {
            commit();
        }
    }

    public synchronized void addToken(PlaceInstance placeInstance,
                                      Object token, double time,
                                      boolean automaticInsertion)
            throws Exception {
        // We have to reserve the token once, so that it is
        // kept until this transaction is completed.
        placeInstance.reserve(token);

        TokenAction action = new TokenAction(placeInstance, token, time);

        if (automaticInsertion) {
            delayedAddActions.addElement(action);
        }


        // The ID is now reserved. We can now build the
        // token action which relies on unchanging IDs.
        addActions.addElement(action);

        if (autoCommit) {
            commit();
        }
    }

    public synchronized void removeToken(PlaceInstance placeInstance,
                                         Object token, double time)
            throws Exception {
        // We have to reserve the token once, so that it is
        // kept until this transaction is completed.
        placeInstance.reserve(token);

        removeActions.addElement(new TokenAction(placeInstance, token, time));

        if (autoCommit) {
            commit();
        }
    }

    private void outputTokens() {
        int n = delayedAddActions.size();
        for (int i = 0; i < n; i++) {
            TokenAction tokenAction = delayedAddActions.elementAt(i);
            PlaceInstance pi = tokenAction.getPlaceInstance();
            Object token = tokenAction.getToken();
            double time = tokenAction.getTime();
            pi.lock.lock();
            try {
                pi.internallyInsertToken(token, time, false);
            } finally {
                pi.lock.unlock();
            }
        }
    }

    private void unreserveTokens(Vector<TokenAction> tokenActions) {
        int n = tokenActions.size();
        for (int i = 0; i < n; i++) {
            TokenAction tokenAction = tokenActions.elementAt(i);
            PlaceInstance pi = tokenAction.getPlaceInstance();
            Object token = tokenAction.getToken();
            pi.lock.lock();
            try {
                pi.unreserve(token);
            } finally {
                pi.lock.unlock();
            }
        }
    }

    private void unreserveOutputTokens() {
        unreserveTokens(addActions);
    }

    private void unreserveInputTokens() {
        unreserveTokens(removeActions);
    }

    private void clear() {
        createActions.removeAllElements();
        delayedAddActions.removeAllElements();
        addActions.removeAllElements();
        removeActions.removeAllElements();
        deleteActions.removeAllElements();
    }

    public synchronized void commit() throws Exception {
        NetAction[] ca = new NetAction[createActions.size()];
        createActions.copyInto(ca);
        TokenAction[] aa = new TokenAction[addActions.size()];
        addActions.copyInto(aa);
        TokenAction[] ra = new TokenAction[removeActions.size()];
        removeActions.copyInto(ra);
        NetAction[] da = new NetAction[deleteActions.size()];
        deleteActions.copyInto(da);

        TransactionSource.perform(ca, aa, ra, da);

        outputTokens();
        unreserveOutputTokens();
        unreserveInputTokens();

        clear();
    }

    public synchronized void rollback() {
        unreserveOutputTokens();
        unreserveInputTokens();
        clear();
    }
}