package de.renew.database.entitylayer;

import java.io.PrintStream;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Hashtable;
import java.util.Vector;


/**
 * An entity represents a dataset in
 * a database table. It contains attributes (one or
 * more of them are called the primary keys) and values.
 * An entity abstracts from the difference
 * between INSERT and UPDATE, so that you can
 * just load and save the entities, and from
 * primary keys, so that they are automatically
 * assigned.
 */
public abstract class Entity implements Cloneable {

    /**
     * The entity contains persistent data
     * that has not been modified.
     */
    public static final byte NOT_MODIFIED = 0;

    /**
     * The entity contains persistent data
     * that has been modified.
     */
    public static final byte MODIFIED = 1;

    /**
     * The entity contains new data.
     */
    public static final byte NEW = 2;

    /**
     * The PrintStream to log all database accesses to.
     * If set to null (default), logging is disabled.
     */
    private static PrintStream messageStream = null;

    /**
     * The connection for entity operations.
     */
    private Connection connection;

    /**
     * The SQL dialect for entity operations.
     */
    private SQLDialect dialect;

    /**
     * The current modification state.
     */
    private byte state;

    /**
     * The values of the attributes.
     */
    private Hashtable<String, Object> values;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(Entity.class);

    /**
     * Creates the entity.
     * @param connection The connection to
     * be used for entity operations.
     * @param dialect The SQL dialect to
     * be used for entity operations.
     */
    public Entity(Connection connection, SQLDialect dialect) {
        this.connection = connection;
        this.dialect = dialect;
        clear();
    }

    /**
     * Clears all attributes' values
     * and sets the entity state to NEW.
     */
    public void clear() {
        values = new Hashtable<String, Object>();
        state = NEW;
    }

    /**
     * Clones the entity. All attributes' values
     * are copied, but the primary key is released,
     * so that the new entity has NEW state.
     * @return The cloned entity.
     * @exception CloneNotSupportedException The object
     * couldn't be cloned. This should never happen.
     */
    public Object clone() throws CloneNotSupportedException {
        Entity newEntity = (Entity) super.clone();

        newEntity.values = new Hashtable<String, Object>();
        Attribute[] attributes = getAttributes();
        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            Object oldValue = getValue(attributes[attributeNumber].getName());
            if (oldValue != null) {
                newEntity.values.put(attributes[attributeNumber].getName(),
                                     oldValue);
            }
        }

        Attribute[] primaryKey = getPrimaryKey();
        for (int attributeNumber = 0; attributeNumber < primaryKey.length;
                     attributeNumber++) {
            newEntity.values.remove(attributes[attributeNumber].getName());
        }
        newEntity.state = NEW;

        return newEntity;
    }

    /**
     * Deletes the entity from the database,
     * clears all attributes' values and
     * sets the entity state to NEW.
     * If the entity already has NEW state,
     * only the values are cleared.
     * @exception SQLException If any SQL problem occurred.
     */
    public void delete() throws SQLException {
        synchronized (getClass()) {
            Statement statement = null;

            try {
                if (state != NEW) {
                    String update = dialect.getDeleteString(getTableName(),
                                                            getPrimaryKey(),
                                                            getPrimaryKeyValue());

                    if (messageStream != null) {
                        messageStream.println("Entity: Executing " + update);
                    }

                    statement = connection.createStatement();
                    if (logger.isTraceEnabled()) {
                        logger.trace(Entity.class.getSimpleName()
                                     + ": executing sql statement (delete): "
                                     + update);
                    }
                    statement.executeUpdate(update);

                    if (messageStream != null) {
                        messageStream.println("Entity: Done.");
                    }
                }

                clear();
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }
    }

    /**
     * Deletes all entities of a given table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @exception SQLException If any SQL problem occurred.
     */
    public static void deleteEntities(Entity entityTemplate)
            throws SQLException {
        deleteEntities(entityTemplate, "");
    }

    /**
     * Deletes some of the entities of a given table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @param condition The condition for the entities to be
     * deleted. This is the string after the SQL WHERE symbol.
     * @exception SQLException If any SQL problem occurred.
     */
    public static void deleteEntities(Entity entityTemplate, String condition)
            throws SQLException {
        synchronized (entityTemplate.getClass()) {
            Statement statement = null;

            try {
                String update = entityTemplate.getDialect()
                                              .getDeleteString(entityTemplate
                                    .getTableName(), condition);

                if (messageStream != null) {
                    messageStream.println("Entity: Executing " + update);
                }

                statement = entityTemplate.getConnection().createStatement();
                if (logger.isTraceEnabled()) {
                    logger.trace(Entity.class.getSimpleName()
                                 + ": executing sql statement (deleteEntities): "
                                 + update);
                }
                statement.executeUpdate(update);

                if (messageStream != null) {
                    messageStream.println("Entity: Done.");
                }
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }
    }

    /**
     * Returns all attributes of the entity as array.
     * Implement this method in the derived actual entities.
     * @return All attributes as array.
     */
    public abstract Attribute[] getAttributes();

    /**
     * Returns the connection for entity operations.
     * @return The connection for entity operations.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the SQL dialect for entity operations.
     * @return The SQL dialect for entity operations.
     */
    public SQLDialect getDialect() {
        return dialect;
    }

    /**
     * Returns all entities of a given table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @return All entities of the table.
     * @exception SQLException If any SQL problem occurred.
     */
    public static Vector<Entity> getEntities(Entity entityTemplate)
            throws SQLException {
        return getEntitiesBySelectString(entityTemplate,
                                         entityTemplate.getDialect()
                                                       .getSelectString(entityTemplate
                                                                        .getTableName(),
                                                                        entityTemplate
                                                                        .getAttributes()));
    }

    /**
     * Returns some of the entities of a given table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @param condition The condition for the entities
     * to be added to the Vector. This is the string
     * after the SQL WHERE symbol.
     * @return Some of the entities of the table.
     * @exception SQLException If any SQL problem occurred.
     */
    public static Vector<Entity> getEntities(Entity entityTemplate,
                                             String condition)
            throws SQLException {
        return getEntitiesBySelectString(entityTemplate,
                                         entityTemplate.getDialect()
                                                       .getSelectString(entityTemplate
                                                                        .getTableName(),
                                                                        entityTemplate
                                                                        .getAttributes(),
                                                                        condition));
    }

    /**
     * Returns a sorted Vector of some of the entities
     * of a given table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @param condition The condition for the entities
     * to be added to the Vector. This is the string
     * after the SQL WHERE symbol.
     * @param order The order for the Vector. This
     * is the string after the SQL ORDER BY symbol.
     * @return Some of the entities of the table.
     * @exception SQLException If any SQL problem occurred.
     */
    public static Vector<Entity> getEntities(Entity entityTemplate,
                                             String condition, String order)
            throws SQLException {
        return getEntitiesBySelectString(entityTemplate,
                                         entityTemplate.getDialect()
                                                       .getSelectString(entityTemplate
                                                                        .getTableName(),
                                                                        entityTemplate
                                                                        .getAttributes(),
                                                                        condition,
                                                                        order));
    }

    /**
     * Internal method to get a Vector of entities
     * of a table.
     * @param entityTemplate An instance of the table's
     * entity. This object is used for the table name,
     * the connection and the SQL dialect.
     * @param query The SQL statement to get the
     * result set for the Vector construction.
     * @return Some of the entities of the table.
     * @exception SQLException If any SQL problem occurred.
     */
    private static Vector<Entity> getEntitiesBySelectString(Entity entityTemplate,
                                                            String query)
            throws SQLException {
        synchronized (entityTemplate.getClass()) {
            Attribute[] attributes = entityTemplate.getAttributes();
            Vector<Entity> entities = new Vector<Entity>();

            Statement statement = null;
            ResultSet resultSet = null;

            try {
                if (messageStream != null) {
                    messageStream.println("Entity: Executing " + query);
                }

                statement = entityTemplate.getConnection().createStatement();
                resultSet = statement.executeQuery(query);
                if (logger.isTraceEnabled()) {
                    logger.trace(Entity.class.getSimpleName()
                                 + ": executing sql statement (getEntitiesBySelectString): "
                                 + query);
                }

                if (messageStream != null) {
                    messageStream.println("Entity: Done.");
                }

                while (resultSet.next()) {
                    Entity entity;
                    try {
                        entity = (Entity) entityTemplate.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalArgumentException("Clone of object template not possible.\n"
                                                           + "Possibly illegal attribute values?");
                    }
                    entity.clear();

                    for (int attributeNumber = 0;
                                 attributeNumber < attributes.length;
                                 attributeNumber++) {
                        Object value = resultSet.getObject(attributeNumber + 1);
                        if (value != null) {
                            if (value instanceof BigDecimal) {
                                entity.setValue(attributes[attributeNumber]
                                    .getName(),
                                                new Integer(((Number) value)
                                    .intValue()));
                            } else {
                                entity.setValue(attributes[attributeNumber]
                                    .getName(), value);
                            }
                        }
                    }

                    entity.state = NOT_MODIFIED;
                    entities.addElement(entity);
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                    resultSet = null;
                }

                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }

            return entities;
        }
    }

    /**
     * Returns the primary key attributes of the entity.
     * Implement this method in the derived actual entities.
     * @return The primary key attributes.
     */
    public abstract Attribute[] getPrimaryKey();

    /**
     * Returns the current value of the primary
     * key attribute.
     * @return The current primary key value.
     */
    public Object[] getPrimaryKeyValue() {
        Attribute[] primaryKey = getPrimaryKey();
        Object[] primaryKeyValue = new Object[primaryKey.length];
        for (int attributeNumber = 0; attributeNumber < primaryKey.length;
                     attributeNumber++) {
            primaryKeyValue[attributeNumber] = values.get(primaryKey[attributeNumber]
                                                          .getName());
        }

        return primaryKeyValue;
    }

    /**
     * Returns the current modification state.
     * @return The current modification state.
     */
    public byte getState() {
        return state;
    }

    /**
     * Returns the entity's table name.
     * Implement this method in the derived actual entities.
     * @return The entity's table name.
     */
    public abstract String getTableName();

    /**
     * Returns an attribute's current value.
     * @param name The name of the attribute.
     * @return The current value of the attribute.
     */
    public Object getValue(String name) {
        Object value = values.get(name);
        if (value != null && (value instanceof byte[])) {
            return new String((byte[]) value);
        } else {
            return value;
        }
    }

    /**
     * Loads an entity from the database into the
     * current object and sets its state to NOT_MODIFIED.
     * @param primaryKeyValue The primary key
     * attribute's value to match the entity in
     * the database's table, if the table has exactly
     * one primary key attribute.
     * @exception SQLException If any SQL problem
     * occurred, as well as if the primary key
     * value couldn't be found.
     */
    public void load(Object primaryKeyValue) throws SQLException {
        load(new Object[] { primaryKeyValue });
    }

    /**
     * Loads an entity from the database into the
     * current object and sets its state to NOT_MODIFIED.
     * @param primaryKeyValue The primary key
     * attributes' values to match the entity in
     * the database's table.
     * @exception SQLException If any SQL problem
     * occurred, as well as if the primary key
     * value couldn't be found.
     */
    public void load(Object[] primaryKeyValue) throws SQLException {
        synchronized (getClass()) {
            Attribute[] attributes = getAttributes();
            Attribute[] primaryKey = getPrimaryKey();

            if (primaryKeyValue.length != primaryKey.length) {
                throw new IllegalArgumentException("Wrong number of"
                                                   + " primary key values supplied.");
            }

            Statement statement = null;
            ResultSet resultSet = null;

            try {
                String query = dialect.getSelectStringWithPrimaryKey(getTableName(),
                                                                     attributes,
                                                                     primaryKey,
                                                                     primaryKeyValue);

                if (messageStream != null) {
                    messageStream.println("Entity: Executing " + query);
                }

                statement = connection.createStatement();
                resultSet = statement.executeQuery(query);
                if (logger.isTraceEnabled()) {
                    logger.trace(Entity.class.getSimpleName()
                                 + ": executing sql statement (load): " + query);
                }

                if (messageStream != null) {
                    messageStream.println("Entity: Done.");
                }

                if (!resultSet.next()) {
                    throw new NoSuchEntityException("No entity with primary key "
                                                    + dialect
                                                        .getStringOfAttributesAndValues(primaryKey,
                                                                                        primaryKeyValue)
                                                    + " in table "
                                                    + getTableName() + ".");
                }

                clear();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int attributeNumber = 0; attributeNumber < columnCount;
                             attributeNumber++) {
                    Object value = resultSet.getObject(attributeNumber + 1);
                    if (value != null) {
                        if (value instanceof BigDecimal) {
                            values.put(attributes[attributeNumber].getName(),
                                       new Integer(((Number) value).intValue()));
                        } else {
                            values.put(attributes[attributeNumber].getName(),
                                       value);
                        }
                    }
                }

                if (resultSet.next()) {
                    throw new PrimaryKeyNotUniqueException("Duplicate entity with"
                                                           + " primary key "
                                                           + dialect
                                                               .getStringOfAttributesAndValues(primaryKey,
                                                                                               primaryKeyValue)
                                                           + " in table "
                                                           + getTableName()
                                                           + ".");
                }

                state = NOT_MODIFIED;
            } catch (SQLException e) {
                clear();
                throw e;
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                    resultSet = null;
                }

                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }
    }

    /**
     * Saves the entity into the database and sets
     * its state to NOT_MODIFIED. If the entity was
     * new (state is NEW, is has not been loaded),
     * this is done with an INSERT statement. If
     * the state was MODIFIED (is has been loaded
     * and modified), this is done with an UPDATE.
     * If the entity has not been modified, it is not
     * saved. If you require this, use the touch method.
     * @exception SQLException If any SQL problem
     * occurred.
     */
    public void save() throws SQLException {
        synchronized (getClass()) {
            Statement statement = null;
            Attribute[] attributes = getAttributes();
            Attribute[] primaryKey = getPrimaryKey();

            try {
                String update;
                switch (state) {
                case NEW:
                    int setPrimaryKeyValueCount = 0;
                    for (int attributeNumber = 0;
                                 attributeNumber < primaryKey.length;
                                 attributeNumber++) {
                        if (values.get(primaryKey[attributeNumber].getName()) != null) {
                            setPrimaryKeyValueCount++;
                        }
                    }

                    if (setPrimaryKeyValueCount > 0
                                && setPrimaryKeyValueCount < primaryKey.length) {
                        throw new IllegalStateException("The primary key value"
                                                        + " is only partially set.\nSet it completely to"
                                                        + " define its value or set it not at all to"
                                                        + " let the save method auto-set it.");
                    }

                    if (setPrimaryKeyValueCount < primaryKey.length) {
                        dialect.setUniquePrimaryKeyValueBeforeInsert(connection,
                                                                     this,
                                                                     values);
                    }

                    update = dialect.getInsertString(getTableName(),
                                                     attributes, values,
                                                     primaryKey);

                    if (setPrimaryKeyValueCount < primaryKey.length) {
                        dialect.setUniquePrimaryKeyValueAfterInsert(connection,
                                                                    this, values);
                    }

                    if (messageStream != null) {
                        messageStream.println("Entity: Executing " + update);
                    }

                    statement = connection.createStatement();
                    if (logger.isTraceEnabled()) {
                        logger.trace(Entity.class.getSimpleName()
                                     + ": executing sql statement (save): "
                                     + update);
                    }
                    statement.executeUpdate(update);

                    if (messageStream != null) {
                        messageStream.println("Entity: Done.");
                    }
                    break;
                case MODIFIED:
                    update = dialect.getUpdateString(getTableName(),
                                                     attributes, values,
                                                     primaryKey);
                    if (messageStream != null) {
                        messageStream.println("Entity: Executing " + update);
                    }

                    statement = connection.createStatement();
                    if (logger.isTraceEnabled()) {
                        logger.trace(Entity.class.getSimpleName()
                                     + ": executing sql statement (save): "
                                     + update);
                    }
                    statement.executeUpdate(update);

                    if (messageStream != null) {
                        messageStream.println("Entity: Done.");
                    }
                    break;
                }

                state = NOT_MODIFIED;
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }
    }

    /**
     * Sets the PrintStream to log all database accesses to.
     * @param messageStream The new PrintStream to log all database
     * accesses to. If set to null, logging is disabled.
     */
    public static void setMessageStream(PrintStream messageStream) {
        Entity.messageStream = messageStream;
    }

    /**
     * Sets an attribute's value.
     * @param name The name of the attribute.
     * @param value The new value for the attribute.
     */
    public void setValue(String name, Object value) {
        if (state != NEW) {
            Attribute[] primaryKey = getPrimaryKey();
            for (int attributeNumber = 0; attributeNumber < primaryKey.length;
                         attributeNumber++) {
                if (name.equals(primaryKey[attributeNumber].getName())) {
                    throw new IllegalArgumentException("It is illegal to change"
                                                       + " the primary key value of an existing entity.");
                }
            }
        }

        if (value == null) {
            values.remove(name);
        } else {
            values.put(name, value);
        }

        if (state == NOT_MODIFIED) {
            state = MODIFIED;
        }
    }

    /**
     * Returns a string representation of the
     * entity for debugging or displaying purposes.
     * @return A string representation.
     */
    public String toString() {
        return getClass().getName() + "{"
               + dialect.getStringOfAttributesAndValues(getAttributes(), values)
               + "}";
    }

    /**
     * Touches the entity. This means, that the
     * entity's modification state is set to
     * MODIFIED, if it was NOT_MODIFIED. The
     * method has no effect on NEW entities.
     * Use this method if you require an entity
     * to be updated in the database, even if
     * you haven't changed it.
     */
    public void touch() {
        if (state == NOT_MODIFIED) {
            state = MODIFIED;
        }
    }
}