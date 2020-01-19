package de.renew.database;

import de.renew.database.entitylayer.Entity;
import de.renew.database.entitylayer.NetInstanceEntity;
import de.renew.database.entitylayer.NoSuchEntityException;
import de.renew.database.entitylayer.SQLDialect;
import de.renew.database.entitylayer.StateEntity;
import de.renew.database.entitylayer.TokenEntity;
import de.renew.database.entitylayer.TokenPositionEntity;

import de.renew.net.PlaceInstance;

import de.renew.util.Base64Coder;
import de.renew.util.ClassSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class DatabaseRestoreSource implements RestoreSource {

    /**
     * The connection for database operations.
     */
    private Connection connection = null;

    /**
     * The SQL dialect for database operations.
     */
    private SQLDialect dialect = null;

    /**
     * Creates the database restore source based on an existing connection.
     * @param connection The connection to the database to be used.
     * @param dialect The SQL dialect for database operations.
     */
    public DatabaseRestoreSource(Connection connection, SQLDialect dialect) {
        this.connection = connection;
        this.dialect = dialect;
    }

    /**
     * Closes the connection when the object is collected by the gc.
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
     * Adds all tokens, that lie in a given place instance,
     * and their ids to vectors.
     * @param placeInstance The place instance to
     * look up the tokens of. Also contains the netInstance.
     * @param ids The vector to append all token ids lying
     * in the place instance to.
     * @exception SQLException If any SQL exception occurs.
     */
    public void fillinAllTokens(PlaceInstance placeInstance, Vector<String> ids)
            throws SQLException {
        TokenPositionEntity tokenPositionEntity = new TokenPositionEntity(connection,
                                                                          dialect);
        Vector<Entity> tokenPositionEntities = Entity.getEntities(tokenPositionEntity,
                                                                  "NET_INSTANCE_ID='"
                                                                  + placeInstance.getNetInstance()
                                                                                 .getID()
                                                                  + "'"
                                                                  + " and PLACE_INSTANCE_ID='"
                                                                  + placeInstance.getPlace()
                                                                                 .getID()
                                                                  + "'");
        Enumeration<Entity> tokenPositionEntitiesEnum = tokenPositionEntities
                                                            .elements();
        while (tokenPositionEntitiesEnum.hasMoreElements()) {
            tokenPositionEntity = (TokenPositionEntity) tokenPositionEntitiesEnum
                                  .nextElement();
            for (int i = tokenPositionEntity.getQuantity().intValue(); i > 0;
                         i--) {
                ids.addElement(String.valueOf(tokenPositionEntity.getTokenId()));
            }
        }
    }

    /**
     * Returns all net instance ids in the
     * DatabaseRestoreSource's database.
     * @return The net instance ids as an array.
     * @exception SQLException If any SQL exception occurs.
     */
    public String[] getAllNetIDs() throws SQLException {
        Vector<Entity> entities = Entity.getEntities(new NetInstanceEntity(connection,
                                                                           dialect));
        String[] netIds = new String[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            netIds[i] = ((NetInstanceEntity) entities.elementAt(i)).getNetInstanceId()
                         .toString();
        }
        return netIds;
    }

    /**
     * Returns the last used net instance or token id.
     * @return The last used net instance or token id.
     * @exception SQLException If any SQL exception occurs.
     */
    public int getLastId() throws SQLException {
        int lastNetInstanceId = 0;
        NetInstanceEntity netInstanceEntity = new NetInstanceEntity(connection,
                                                                    dialect);
        Vector<Entity> entities = Entity.getEntities(netInstanceEntity, null,
                                                     "NET_INSTANCE_ID");
        if (entities.size() > 0) {
            try {
                lastNetInstanceId = ((NetInstanceEntity) entities.lastElement()).getNetInstanceId()
                                     .intValue();
            } catch (NumberFormatException e) {
                // lastNetInstanceId automatically stays 0,
                // what is intended.
            }
        }

        int lastTokenId = 0;
        TokenEntity tokenEntity = new TokenEntity(connection, dialect);
        entities = Entity.getEntities(tokenEntity, null, "TOKEN_ID");
        if (entities.size() > 0) {
            try {
                lastTokenId = ((TokenEntity) entities.lastElement()).getTokenId()
                               .intValue();
            } catch (NumberFormatException e) {
                // lastTokenId automatically stays 0,
                // what is intended.
            }
        }

        return lastTokenId > lastNetInstanceId ? lastTokenId : lastNetInstanceId;
    }

    /**
     * Returns the net instance name for a given net instance id.
     * @param netID The net instance id.
     * @return The net instance name.
     * @exception SQLException If any SQL exception occurs.
     */
    public String getNetName(String netID) throws SQLException {
        NetInstanceEntity entity = new NetInstanceEntity(connection, dialect);
        entity.load(netID);
        return entity.getName();
    }

    /**
     * Returns a hashtable containing all tokens as values
     * with their respective ids as keys.
     * @param map The net instance map to fetch a net instance by its id.
     * @return The token hashtable.
     * @exception SQLException If any SQL exception occurs.
     * @exception IllegalTokenException If any of the
     * tokens in the database is not instanciatable,
     * deserializable or causes any other problem.
     */
    public Hashtable<String, Object> getTokens(NetInstanceMap map)
            throws SQLException, IllegalTokenException {
        TokenEntity tokenEntity = new TokenEntity(connection, dialect);
        Vector<Entity> tokenEntities = Entity.getEntities(tokenEntity);

        Hashtable<String, Object> tokens = new Hashtable<String, Object>();
        Enumeration<Entity> tokenEntitiesEnum = tokenEntities.elements();
        while (tokenEntitiesEnum.hasMoreElements()) {
            tokenEntity = (TokenEntity) tokenEntitiesEnum.nextElement();

            Class<?> tokenClass = null;
            ByteArrayInputStream inStream = null;
            NetInstanceResolutionInputStream netStream = null;
            try {
                tokenClass = ClassSource.classForName(tokenEntity.getClassName());

                inStream = new ByteArrayInputStream(Base64Coder.decode(tokenEntity
                                                                       .getSerialisation()));
                netStream = new NetInstanceResolutionInputStream(inStream, map);
                Object token = netStream.readObject();
                tokens.put(String.valueOf(tokenEntity.getTokenId()), token);
            } catch (InvalidClassException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n" + "Class "
                                                + (tokenClass != null
                                                   ? tokenClass.getName() : null) //NOTICEnull
                                                + " is no valid class for deserialisation, because:\n"
                                                + e.toString());
            } catch (StreamCorruptedException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n"
                                                + "It has invalid serialisation data: "
                                                + "Control information corrupt.");
            } catch (OptionalDataException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n"
                                                + "It has invalid serialisation data: "
                                                + "Serialisation not recognized.");
            } catch (NotSerializableException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n"
                                                + "It has invalid serialisation data or the class "
                                                + (tokenClass != null
                                                   ? tokenClass.getName() : null) //NOTICEnull
                                                + " is not serializable.");
            } catch (IOException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n"
                                                + "I/O exception during deserialisation:\n"
                                                + e.toString());
            } catch (ClassNotFoundException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n" + "Class "
                                                + (tokenClass != null
                                                   ? tokenClass.getName() : null) //NOTICEnull
                                                + " cannot be found.");
            } catch (IllegalArgumentException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid. "
                                                + "It has not a valid base 64 coded serialisation.");
            } catch (SecurityException e) {
                throw new IllegalTokenException("The database's token with id "
                                                + tokenEntity.getTokenId()
                                                + " is invalid.\n"
                                                + "The default constructor of class "
                                                + (tokenClass != null
                                                   ? tokenClass.getName() : null) //NOTICEnull
                                                + " is not accessable.");
            } finally {
                try {
                    if (netStream != null) {
                        netStream.close();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tokens;
    }

    /**
     * Returns all net instance ids in the
     * DatabaseRestoreSource's database that
     * had a corresponding open drawing.
     * @return The net instance ids as an array.
     * @exception SQLException If any SQL exception occurs.
     */
    public String[] getViewedNetIDs() throws Exception {
        Vector<Entity> entities = Entity.getEntities(new NetInstanceEntity(connection,
                                                                           dialect),
                                                     "DRAWING_OPEN=1");
        String[] netIds = new String[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            netIds[i] = ((NetInstanceEntity) entities.elementAt(i)).getNetInstanceId()
                         .toString();
        }
        return netIds;
    }

    /**
     * Returns if the simulation was inited (not terminated).
     * @return If the simulation was inited.
     * @exception SQLException If any SQL exception occurs.
     */
    public boolean wasSimulationInited() throws SQLException {
        StateEntity stateEntity = new StateEntity(connection, dialect);

        try {
            // Object[0] as primary key loads the only entity.
            stateEntity.load(new Object[0]);
        } catch (NoSuchEntityException e) {
            // A missing entity is interpreted as a fresh database.
            // This means of course that there was no simulation inited.
            return false;
        }

        return stateEntity.getInited().intValue() == 1;
    }

    /**
     * Returns if the simulation was running.
     * @return If the simulation was running.
     * @exception SQLException If any SQL exception occurs.
     */
    public boolean wasSimulationRunning() throws SQLException {
        StateEntity stateEntity = new StateEntity(connection, dialect);

        try {
            // Object[0] as primary key loads the only entity.
            stateEntity.load(new Object[0]);
        } catch (NoSuchEntityException e) {
            // A missing entity is interpreted as a fresh database.
            // This means of course that there was no simulation running.
            return false;
        }

        return stateEntity.getRunning().intValue() == 1;
    }
}