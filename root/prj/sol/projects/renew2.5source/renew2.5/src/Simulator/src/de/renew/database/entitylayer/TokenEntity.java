package de.renew.database.entitylayer;

import java.sql.Connection;


/**
 * The entity class for the table TOKEN.
 */
public class TokenEntity extends Entity {
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
                                                     Attribute.TYPE_INT) };

        attributes = new Attribute[] { new Attribute("TOKEN_ID",
                                                     Attribute.TYPE_INT), new Attribute("CLASS_NAME",
                                                                                        Attribute.TYPE_CHAR), new Attribute("SERIALISATION",
                                                                                                                            Attribute.TYPE_CHAR) };
    }

    /**
     * Creates the entity.
     * @param connection The connection to
     * be used for entity operations.
     * @param dialect The SQL dialect to
     * be used for entity operations.
     */
    public TokenEntity(Connection connection, SQLDialect dialect) {
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
        return "TOKEN";
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
     * Returns the value of the attribute CLASS_NAME.
     * @return The value of the attribute CLASS_NAME.
     */
    public String getClassName() {
        return (String) getValue("CLASS_NAME");
    }

    /**
     * Returns the value of the attribute SERIALISATION.
     * @return The value of the attribute SERIALISATION.
     */
    public String getSerialisation() {
        return (String) getValue("SERIALISATION");
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
     * Sets the value of the attribute CLASS_NAME.
     * @param className The new value for the attribute.
     */
    public void setClassName(String className) {
        setValue("CLASS_NAME", className);
    }

    /**
     * Sets the value of the attribute SERIALISATION.
     * @param serialisation The new value for the attribute.
     */
    public void setSerialisation(String serialisation) {
        setValue("SERIALISATION", serialisation);
    }
}