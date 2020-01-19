package de.renew.database;

import de.renew.database.entitylayer.Entity;
import de.renew.database.entitylayer.NetInstanceEntity;
import de.renew.database.entitylayer.NoSuchEntityException;
import de.renew.database.entitylayer.SQLDialect;
import de.renew.database.entitylayer.StateEntity;
import de.renew.database.entitylayer.TokenEntity;
import de.renew.database.entitylayer.TokenPositionEntity;

import de.renew.util.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Implementation of the TransactionStrategy interface,
 * that encapsulates the code that an application
 * has to execute when the results of a firing transition
 * have to be written to a destination, for example a database
 * or a log file. This implementation writes the results
 * into an SQL database, so that the database holds the
 * current net instance, place instance, token and token
 * position state at any time.
 */
public class DatabaseTransactionStrategy implements TransactionStrategy {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DatabaseTransactionStrategy.class);

    /**
     * The connection for database operations.
     */
    private Connection connection = null;

    /**
     * The SQL dialect for database operations.
     */
    private SQLDialect dialect = null;

    /**
     * Creates the database transaction strategy.
     * @param dbUrl The url to the database to be used.
     * @param dialect The SQL dialect for database operations.
     */
    public DatabaseTransactionStrategy(String dbUrl, SQLDialect dialect)
            throws SQLException {
        connection = DriverManager.getConnection(dbUrl);
        connection.setAutoCommit(false);
        this.dialect = dialect;
    }

    /**
     * Creates the database transaction strategy.
     * @param dbUrl The url to the database to be used.
     * @param dbUser The user for the database to be used.
     * @param dbPassword The password for the database to be used.
     * @param dialect The SQL dialect for database operations.
     */
    public DatabaseTransactionStrategy(String dbUrl, String dbUser,
                                       String dbPassword, SQLDialect dialect)
            throws SQLException {
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        connection.setAutoCommit(false);
        this.dialect = dialect;
    }

    /**
     * Performs the token add actions. Positions are inserted
     * into the database or their quantities are changed, and
     * tokens are inserted, if they don't exist.
     * @adds An array of TokenActions. These tokens
     * are placed into their respective place instances.
     * @exception SQLException The transaction couldn't
     * be performed because of a database problem.
     * @exception IllegalTokenException A token couldn't be
     * added, because it doesn't support a database write.
     * This is most likely if the token is not serializable.
     */
    private void addTokens(TokenAction[] adds)
            throws SQLException, IllegalTokenException {
        for (int addNumber = 0; addNumber < adds.length; addNumber++) {
            TokenAction add = adds[addNumber];

            TokenEntity tokenEntity = new TokenEntity(connection, dialect);
            try {
                tokenEntity.load(add.getTokenID());
            } catch (NoSuchEntityException nsee) {
                Object token = add.getToken();

                try {
                    tokenEntity.setTokenId(Integer.valueOf(add.getTokenID()));
                } catch (NumberFormatException e) {
                    throw new IllegalTokenException("The token with id "
                                                    + add.getTokenID()
                                                    + ", class "
                                                    + token.getClass().getName()
                                                    + " and representation "
                                                    + token.toString()
                                                    + " cannot be saved, because the ID is not parsable to an int."
                                                    + " Currently, only int IDs are supported for tokens.");
                }

                tokenEntity.setClassName(token.getClass().getName());
                byte[] serialisation = null;
                try {
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    new ObjectOutputStream(outStream).writeObject(token);
                    serialisation = outStream.toByteArray();
                } catch (Exception e) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintWriter(stream));
                    throw new IllegalTokenException("The token with id "
                                                    + add.getTokenID()
                                                    + ", class "
                                                    + token.getClass().getName()
                                                    + " and representation "
                                                    + token.toString()
                                                    + " couldn't be serialized, because there was an exception during"
                                                    + " serialisation: "
                                                    + e.toString() + "\n"
                                                    + "Only serializable tokens can be handled"
                                                    + " by the DatabaseTransactionStrategy.");
                }
                tokenEntity.setSerialisation(new String(Base64Coder.encode(serialisation)));
                tokenEntity.save();
            }

            TokenPositionEntity tokenPositionEntity = new TokenPositionEntity(connection,
                                                                              dialect);
            try {
                tokenPositionEntity.load(new Object[] { add.getTokenID(), add
                        .getNetID(), add.getPlaceID().toString() });
                tokenPositionEntity.setQuantity(new Integer(tokenPositionEntity.getQuantity()
                                                                               .intValue()
                                                            + 1));
            } catch (NoSuchEntityException nsee) {
                tokenPositionEntity.setTokenId(Integer.valueOf(add.getTokenID()));

                try {
                    tokenPositionEntity.setNetInstanceId(Integer.valueOf(add
                        .getNetID()));
                } catch (NumberFormatException e) {
                    throw new IllegalTokenException("The net instance ID "
                                                    + add.getNetID()
                                                    + " cannot be saved, because it is not parsable to an int."
                                                    + " Currently, only int IDs are supported for net instances.");
                }

                tokenPositionEntity.setPlaceInstanceId(add.getPlaceID()
                                                          .toString());
                tokenPositionEntity.setQuantity(new Integer(1));
            }
            tokenPositionEntity.save();
        }
    }

    /**
     * Performs the net create actions. The new net instances
     * are inserted into database
     * @creates An array of NetActions. They represent
     * the new nets to be added.
     * @exception SQLException The transaction couldn't
     * be performed because of a database problem.
     */
    private void createNets(NetAction[] creates) throws SQLException {
        for (int createNumber = 0; createNumber < creates.length;
                     createNumber++) {
            NetAction create = creates[createNumber];
            NetInstanceEntity netInstanceEntity = new NetInstanceEntity(connection,
                                                                        dialect);
            try {
                netInstanceEntity.load(create.getNetID());
                if (logger.isDebugEnabled()) {
                    logger.debug(DatabaseTransactionStrategy.class.getSimpleName()
                                 + ": loaded net instance entity with id "
                                 + create.getNetID());
                }
            } catch (NoSuchEntityException nsee) {
                try {
                    netInstanceEntity.setNetInstanceId(Integer.valueOf(create
                        .getNetID()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("The net instance ID "
                                               + create.getNetID()
                                               + " cannot be saved, because it is not parsable to an int."
                                               + " Currently, only int IDs are supported for net instances.");
                }


                if (logger.isDebugEnabled()) {
                    logger.debug(DatabaseTransactionStrategy.class.getSimpleName()
                                 + ": create net instance entity with name "
                                 + create.getName());
                }
                netInstanceEntity.setName(create.getName());
                netInstanceEntity.setDrawingOpen(new Integer(0));
                netInstanceEntity.save();
            }
        }
    }

    /**
     * Performs the net delete actions. Positions are deleted
     * from the database, and tokens are deleted, if they are
     * not used anymore. Finally, the net instance is deleted.
     * @deletes An array of NetActions. They represent
     * the new nets to be removed.
     * @exception SQLException The transaction couldn't
     * be performed because of a database problem.
     */
    private void deleteNets(NetAction[] deletes) throws SQLException {
        for (int deleteNumber = 0; deleteNumber < deletes.length;
                     deleteNumber++) {
            try {
                NetAction delete = deletes[deleteNumber];
                NetInstanceEntity netInstanceEntity = new NetInstanceEntity(connection,
                                                                            dialect);
                netInstanceEntity.load(delete.getNetID());

                TokenPositionEntity tokenPositionEntity = new TokenPositionEntity(connection,
                                                                                  dialect);
                Vector<Entity> tokenPositionEntities = Entity.getEntities(tokenPositionEntity,
                                                                          "NET_INSTANCE_ID='"
                                                                          + delete
                                                                            .getNetID()
                                                                          + "'");
                Enumeration<Entity> tokenPositionEntitiesEnum = tokenPositionEntities
                                                                .elements();
                while (tokenPositionEntitiesEnum.hasMoreElements()) {
                    tokenPositionEntity = (TokenPositionEntity) tokenPositionEntitiesEnum
                                          .nextElement();

                    String tokenId = String.valueOf(tokenPositionEntity
                                         .getTokenId());
                    tokenPositionEntity.delete();

                    if (Entity.getEntities(tokenPositionEntity,
                                                   "TOKEN_ID='" + tokenId + "'")
                                      .size() <= 0) {
                        TokenEntity tokenEntity = new TokenEntity(connection,
                                                                  dialect);
                        tokenEntity.load(tokenId);
                        tokenEntity.delete();
                    }
                }

                netInstanceEntity.delete();
            } catch (NoSuchEntityException e) {
                // The garbage collector was too late.
                // The TransactionStrategy has already deleted this entity.
                // So it is save to ignore this exception.
            }
        }
    }

    /**
     * Closes the connection when the object is collected by the GC.
     * Any open database transaction is rolled back.
     */
    protected void finalize() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.close();
            connection = null;
        }
    }

    /**
     * Notifys the database that a net instance
     * is now not being watched with a drawing anymore.
     * @param netInstanceID The net instance id of which the
     * drawing has been closed.
     * @exception SQLException The notification couldn't
     * be passed to the database because of a database problem.
     */
    public void netInstanceDrawingClosed(String netInstanceID)
            throws SQLException {
        try {
            logger.debug("Closed " + netInstanceID);
            NetInstanceEntity netInstanceEntity = new NetInstanceEntity(connection,
                                                                        dialect);

            netInstanceEntity.load(netInstanceID);
            netInstanceEntity.setDrawingOpen(new Integer(0));
            netInstanceEntity.save();
        } catch (NoSuchEntityException e) {
            // The garbage collector was too late.
            // The TransactionStrategy has already deleted this entity.
            // So it is save to ignore this exception.
        }
    }

    /**
     * Notifys the database that a net instance
     * is now being watched with a drawing.
     * @param netInstanceID The net instance id of which a
     * drawing has been opened.
     * @exception SQLException The notification couldn't
     * be passed to the database because of a database problem.
     */
    public void netInstanceDrawingOpened(String netInstanceID)
            throws SQLException {
        try {
            logger.debug("Opened " + netInstanceID);
            NetInstanceEntity netInstanceEntity = new NetInstanceEntity(connection,
                                                                        dialect);

            netInstanceEntity.load(netInstanceID);
            netInstanceEntity.setDrawingOpen(new Integer(1));
            netInstanceEntity.save();
        } catch (NoSuchEntityException e) {
            // The garbage collector was too late.
            // The TransactionStrategy has already deleted this entity.
            // So it is save to ignore this exception.
        }
    }

    /**
     * Execute one transaction, e.g. by writing to a database.
     * @param createActions an array of net creation actions
     * @param addActions an array of token insertion actions
     * @param removeActions an array of token removal actions
     * @param deleteActions an array of net deletion actions
     * @exception SQLException The transaction couldn't
     * be performed because of a database problem.
     */
    public void perform(NetAction[] createActions, TokenAction[] addActions,
                        TokenAction[] removeActions, NetAction[] deleteActions)
            throws SQLException, IllegalTokenException {
        createNets(createActions);
        addTokens(addActions);
        removeTokens(removeActions);
        deleteNets(deleteActions);

        connection.commit();
    }

    /**
     * Performs the token remove actions. Positions are deleted
     * from the database or their quantities are changed, and
     * tokens are deleted, if they are not used anymore.
     * @removes An array of TokenActions. These tokens
     * are removed from their respective place instances.
     * @exception SQLException The transaction couldn't
     * be performed because of a database problem.
     * @exception IllegalTokenException A token couldn't be
     * removed, because it doesn't support a database write.
     * This is most likely if the token is not serializable.
     */
    private void removeTokens(TokenAction[] removes)
            throws SQLException, IllegalTokenException {
        for (int removeNumber = 0; removeNumber < removes.length;
                     removeNumber++) {
            TokenAction remove = removes[removeNumber];
            Object token = remove.getToken();
            if (!(token instanceof Serializable)) {
                throw new IllegalTokenException("Token " + remove.getTokenID()
                                                + " is not serializable.\n"
                                                + "Only serializable tokens can be handled"
                                                + " by the DatabaseTransactionStrategy.");
            }

            try {
                TokenEntity tokenEntity = new TokenEntity(connection, dialect);
                tokenEntity.load(remove.getTokenID());

                TokenPositionEntity tokenPositionEntity = new TokenPositionEntity(connection,
                                                                                  dialect);
                try {
                    tokenPositionEntity.load(new Object[] { remove.getTokenID(), remove
                                                                                 .getNetID(), remove.getPlaceID()
                                                                                                    .toString() });
                    if (tokenPositionEntity.getQuantity().intValue() <= 1) {
                        tokenPositionEntity.delete();
                    } else {
                        tokenPositionEntity.setQuantity(new Integer(tokenPositionEntity.getQuantity()
                                                                                       .intValue()
                                                                    - 1));
                        tokenPositionEntity.save();
                    }
                } catch (NoSuchEntityException e) {
                    // The garbage collector was too late.
                    // The TransactionStrategy has already deleted this entity.
                    // So it is save to ignore this exception.
                }

                if (Entity.getEntities(tokenPositionEntity,
                                               "TOKEN_ID='"
                                               + remove.getTokenID() + "'")
                                  .size() <= 0) {
                    tokenEntity.delete();
                }
            } catch (NoSuchEntityException e) {
                // The garbage collector was too late.
                // The TransactionStrategy has already deleted this entity.
                // So it is save to ignore this exception.
            }
        }
    }

    /**
     * Notifys the database about simulation state changes.
     * @param inited The simulation is inited (not terminated).
     * @param running The simulation is running.
     * @exception SQLException The notification couldn't
     * be passed to the database because of a database problem.
     */
    public void simulationStateChanged(boolean inited, boolean running)
            throws SQLException {
        StateEntity stateEntity = new StateEntity(connection, dialect);
        try {
            // Object[0] as primary key loads the only entity.
            stateEntity.load(new Object[0]);
        } catch (NoSuchEntityException e) {
            // Since it is okay that there was no simulation state before,
            // and since the entity is in a correct state afterwards,
            // this exception can safely be ignored.
        }

        stateEntity.setInited(new Integer(inited ? 1 : 0));
        stateEntity.setRunning(new Integer(running ? 1 : 0));
        stateEntity.save();

        connection.commit();
    }
}