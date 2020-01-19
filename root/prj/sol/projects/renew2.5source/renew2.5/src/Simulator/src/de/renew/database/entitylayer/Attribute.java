package de.renew.database.entitylayer;



/**
 * An attribute represents a database
 * table column. It contains its name
 * as well as its data type.
 */
public class Attribute {

    /**
     * The attribute is of INT (integer) type.
     */
    public static final int TYPE_INT = 1;

    /**
     * The attribute is of REAL (float) type.
     */
    public static final int TYPE_REAL = 2;

    /**
     * The attribute is of CHAR (string) type.
     */
    public static final int TYPE_CHAR = 3;

    /**
     * The name of the attribute.
     */
    private String name;

    /**
     * The type of the attribute.
     */
    private int type;

    /**
     * Creates the attribute.
     * @param name The name of the attribute.
     * @param type The type of the attribute.
     */
    public Attribute(String name, int type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the name of the attribute.
     * @return The name of the attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the attribute.
     * @return The type of the attribute.
     */
    public int getType() {
        return type;
    }
}