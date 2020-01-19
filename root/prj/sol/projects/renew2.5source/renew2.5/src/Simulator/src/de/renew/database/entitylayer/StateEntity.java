package de.renew.database.entitylayer;

import java.sql.Connection;


/**
 * The entity class for the table STATE.
 */
public class StateEntity extends Entity {
    // Table specifications

    /**
     * The attributes of the entity.
     */
    private static Attribute[] attributes;

    /**
     * The primary key of the entity.
     */
    private static Attribute[] primaryKey;

    static {
        primaryKey = new Attribute[] {  };

        attributes = new Attribute[] { new Attribute("INITED",
                                                     Attribute.TYPE_INT), new Attribute("RUNNING",
                                                                                        Attribute.TYPE_INT) };
    }

    /**
     * Creates the entity.
     * @param connection The connection to
     * be used for entity operations.
     * @param dialect The SQL dialect to
     * be used for entity operations.
     */
    public StateEntity(Connection connection, SQLDialect dialect) {
        super(connection, dialect);
    }

    /**
     * Returns all attributes of the entity as array.
     * @return All attributes as array.
     */
    public Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * Returns the primary key attributes of the entity.
     * @return The primary key attributes.
     */
    public Attribute[] getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Returns the entity's table name.
     * @return The entity's table name.
     */
    public String getTableName() {
        return "STATE";
    }

    // Attribute getter methods


    /**
     * Returns the value of the attribute INITED.
     * @return The value of the attribute INITED.
     */
    public Integer getInited() {
        return (Integer) getValue("INITED");
    }

    /**
     * Returns the value of the attribute RUNNING.
     * @return The value of the attribute RUNNING.
     */
    public Integer getRunning() {
        return (Integer) getValue("RUNNING");
    }

    // Attribute setter methods


    /**
     * Sets the value of the attribute INITED.
     * @param inited The new value for the attribute.
     */
    public void setInited(Integer inited) {
        setValue("INITED", inited);
    }

    /**
     * Sets the value of the attribute RUNNING.
     * @param running The new value for the attribute.
     */
    public void setRunning(Integer running) {
        setValue("RUNNING", running);
    }
}