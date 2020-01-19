package de.renew.database.entitylayer;

import java.sql.Connection;


/**
 * The entity class for the table TOKEN_POSITION.
 */
public class TokenPositionEntity extends Entity {
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
        primaryKey = new Attribute[] { new Attribute("TOKEN_ID",
                                                     Attribute.TYPE_INT), new Attribute("NET_INSTANCE_ID",
                                                                                        Attribute.TYPE_INT), new Attribute("PLACE_INSTANCE_ID",
                                                                                                                           Attribute.TYPE_CHAR), };

        attributes = new Attribute[] { new Attribute("TOKEN_ID",
                                                     Attribute.TYPE_INT), new Attribute("NET_INSTANCE_ID",
                                                                                        Attribute.TYPE_INT), new Attribute("PLACE_INSTANCE_ID",
                                                                                                                           Attribute.TYPE_CHAR), new Attribute("QUANTITY",
                                                                                                                                                               Attribute.TYPE_INT) };
    }

    /**
     * Creates the entity.
     * @param connection The connection to
     * be used for entity operations.
     * @param dialect The SQL dialect to
     * be used for entity operations.
     */
    public TokenPositionEntity(Connection connection, SQLDialect dialect) {
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
        return "TOKEN_POSITION";
    }

    // Attribute getter methods


    /**
     * Returns the value of the attribute TOKEN_ID.
     * @return The value of the attribute TOKEN_ID.
     */
    public Integer getTokenId() {
        return (Integer) getValue("TOKEN_ID");
    }

    /**
     * Returns the value of the attribute NET_INSTANCE_ID.
     * @return The value of the attribute NET_INSTANCE_ID.
     */
    public Integer getNetInstanceId() {
        return (Integer) getValue("NET_INSTANCE_ID");
    }

    /**
     * Returns the value of the attribute PLACE_INSTANCE_ID.
     * @return The value of the attribute PLACE_INSTANCE_ID.
     */
    public String getPlaceInstanceId() {
        return (String) getValue("PLACE_INSTANCE_ID");
    }

    /**
     * Returns the value of the attribute QUANTITY.
     * @return The value of the attribute QUANTITY.
     */
    public Integer getQuantity() {
        return (Integer) getValue("QUANTITY");
    }

    // Attribute setter methods


    /**
     * Sets the value of the attribute TOKEN_ID.
     * @param tokenId The new value for the attribute.
     */
    public void setTokenId(Integer tokenId) {
        setValue("TOKEN_ID", tokenId);
    }

    /**
     * Sets the value of the attribute NET_INSTANCE_ID.
     * @param netInstanceId The new value for the attribute.
     */
    public void setNetInstanceId(Integer netInstanceId) {
        setValue("NET_INSTANCE_ID", netInstanceId);
    }

    /**
     * Sets the value of the attribute PLACE_INSTANCE_ID.
     * @param placeInstanceId The new value for the attribute.
     */
    public void setPlaceInstanceId(String placeInstanceId) {
        setValue("PLACE_INSTANCE_ID", placeInstanceId);
    }

    /**
     * Sets the value of the attribute QUANTITY.
     * @param quantity The new value for the attribute.
     */
    public void setQuantity(Integer quantity) {
        setValue("QUANTITY", quantity);
    }
}