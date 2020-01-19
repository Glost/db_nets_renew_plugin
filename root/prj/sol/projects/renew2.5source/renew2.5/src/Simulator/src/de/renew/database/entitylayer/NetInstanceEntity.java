package de.renew.database.entitylayer;

import java.sql.Connection;


/**
 * The entity class for the table NET_INSTANCE.
 */
public class NetInstanceEntity extends Entity {
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
        primaryKey = new Attribute[] { new Attribute("NET_INSTANCE_ID",
                                                     Attribute.TYPE_INT) };

        attributes = new Attribute[] { new Attribute("NET_INSTANCE_ID",
                                                     Attribute.TYPE_INT), new Attribute("NAME",
                                                                                        Attribute.TYPE_CHAR), new Attribute("DRAWING_OPEN",
                                                                                                                            Attribute.TYPE_INT) };
    }

    /**
     * Creates the entity.
     * @param connection The connection to
     * be used for entity operations.
     * @param dialect The SQL dialect to
     * be used for entity operations.
     */
    public NetInstanceEntity(Connection connection, SQLDialect dialect) {
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
        return "NET_INSTANCE";
    }

    // Attribute getter methods


    /**
     * Returns the value of the attribute NET_INSTANCE_ID.
     * @return The value of the attribute NET_INSTANCE_ID.
     */
    public Integer getNetInstanceId() {
        return (Integer) getValue("NET_INSTANCE_ID");
    }

    /**
     * Returns the value of the attribute NAME.
     * @return The value of the attribute NAME.
     */
    public String getName() {
        return (String) getValue("NAME");
    }

    /**
     * Returns the value of the attribute DRAWING_OPEN.
     * @return The value of the attribute DRAWING_OPEN.
     */
    public Integer getDrawingOpen() {
        return (Integer) getValue("DRAWING_OPEN");
    }

    // Attribute setter methods


    /**
     * Sets the value of the attribute NET_INSTANCE_ID.
     * @param netInstanceId The new value for the attribute.
     */
    public void setNetInstanceId(Integer netInstanceId) {
        setValue("NET_INSTANCE_ID", netInstanceId);
    }

    /**
     * Sets the value of the attribute NAME.
     * @param name The new value for the attribute.
     */
    public void setName(String name) {
        setValue("NAME", name);
    }

    /**
     * Sets the value of the attribute DRAWING_OPEN.
     * @param drawingOpen The new value for the attribute.
     */
    public void setDrawingOpen(Integer drawingOpen) {
        setValue("DRAWING_OPEN", drawingOpen);
    }
}