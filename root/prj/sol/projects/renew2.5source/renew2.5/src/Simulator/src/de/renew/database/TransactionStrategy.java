package de.renew.database;



/**
 * This interface encapsulates the code that an application
 * has to execute when the results of a firing transition
 * have to be written to a destination, for example a database
 * or a log file.
 */
public interface TransactionStrategy {

    /**
     * Notifys the TransactionStrategy that a net instance
     * is now being watched with a drawing.
     * @param netInstanceID The net instance id of which a
     * drawing has been opened.
     * @exception Exception As an interface, TransactionStrategy
     * expects any exception to be thrown.
     */
    public void netInstanceDrawingOpened(String netInstanceID)
            throws Exception;

    /**
     * Notifys the TransactionStrategy that a net instance
     * is now not being watched with a drawing anymore.
     * @param netInstanceID The net instance id of which the
     * drawing has been closed.
     * @exception Exception As an interface, TransactionStrategy
     * expects any exception to be thrown.
     */
    public void netInstanceDrawingClosed(String netInstanceID)
            throws Exception;

    /**
     * Execute one transaction, e.g. by writing to a database.
     *
     * A typical database might feature these tables:
     * <dl>
     * <dt>NET_INSTANCE</dt><dd>NET_INSTANCE_ID: String,
     * NAME: String</dd>
     * <dt>PLACE_INSTANCE</dt><dd>PLACE_INSTANCE_ID: int,
     * NET_INSTANCE_ID: String, NAME: String</dd>
     * <dt>TOKEN</dt><dd>TOKEN_ID: String,
     * CLASS_NAME: String, SERIALISATION: String</dd>
     * <dt>TOKEN_POSITION</dt><dd>TOKEN_ID: String,
     * PLACE_INSTANCE_ID: int, QUANTITY: int</dd>
     * </dl>
     * All ID columns are used to decide equality of the stored
     * objects. the SERIALISATION column contains the serialized
     * versions of the tokens.
     *
     * It is suggested that the actions are executed in the
     * order of their appearance in the method header.
     * Especially, it is possible that tokens are added in a
     * newly created net, that newly deposited tokens are
     * removed immediately, and that tokens are removed before
     * a net is finally deleted.
     * The action objects may be queried for all relevant
     * information about net creations and token moves.
     *
     * @param createActions an array of net creation actions
     * @param addActions an array of token insertion actions
     * @param removeActions an array of token removal actions
     * @param deleteActions an array of net deletion actions
     *
     * @exception Exception As an interface, TransactionStrategy
     * expects any exception to be thrown.
     */
    public void perform(NetAction[] createActions, TokenAction[] addActions,
                        TokenAction[] removeActions, NetAction[] deleteActions)
            throws Exception;

    /**
     * Notifys the TransactionStrategy about simulation
     * state changes.
     * @param inited The simulation is inited (not terminated).
     * @param running The simulation is running.
     * @exception Exception As an interface, TransactionStrategy
     * expects any exception to be thrown.
     */
    public void simulationStateChanged(boolean inited, boolean running)
            throws Exception;
}