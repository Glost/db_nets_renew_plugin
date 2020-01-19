/*
 * @(#)FigureAttributes.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.util.Storable;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;

import java.io.IOException;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A container for a figure's attributes. The attributes are stored
 * as key/value pairs.
 *
 * @see CH.ifa.draw.framework.Figure
 */
public class FigureAttributes extends Object implements Cloneable, Serializable {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FigureAttributes.class);
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -6886355144423666716L;
    private Hashtable<String, Object> fMap;
    @SuppressWarnings("unused")
    private int figureAttributesSerializedDataVersion = 1;

    /**
     * Constructs the FigureAttributes.
     */
    public FigureAttributes() {
        fMap = new Hashtable<String, Object>();
    }

    /**
     * Gets the attribute with the given name.
     * @return attribute or null if the key is not defined
     */
    public Object get(String name) {
        return fMap.get(name);
    }

    /**
     * Sets the attribute with the given name and
     * overwrites its previous value.
     * If the value is <code>null</code>, the attribute
     * is removed from the container (ergo undefined).
     */
    public void set(String name, Object value) {
        if (value == null) {
            fMap.remove(name);
        } else {
            fMap.put(name, value);
        }
    }

    /**
     * Tests if an attribute is defined.
     */
    public boolean hasDefined(String name) {
        return fMap.containsKey(name);
    }

    /**
     * Returns an enumeration of all attribute keys.
     */
    public Enumeration<String> definedAttributes() {
        return fMap.keys();
    }

    /**
     * Clones the attributes.
     */
    public Object clone() {
        try {
            FigureAttributes a = (FigureAttributes) super.clone();
            a.fMap = new Hashtable<String, Object>(fMap);
            return a;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Reads the attributes from a StorableInput.
     * FigureAttributes store the following types directly:
     * Color, Boolean, String, Int. Other attribute types
     * have to implement the Storable interface or they
     * have to be wrapped by an object that implements Storable.
     * @see Storable
     * @see #write
     */
    public void read(StorableInput dr) throws IOException {
        String s = dr.readString();
        if (!s.toLowerCase().equals("attributes")) {
            throw new IOException("Attributes expected");
        }
        int version = dr.getVersion();

        //fMap = new Hashtable();
        int size = dr.readInt();
        for (int i = 0; i < size; i++) {
            String key = dr.readString();
            String valtype = dr.readString();
            Object val = null;
            if (valtype.equals("Color")) {
                //Alpha values are not saved befor version 11
                if (version < 11) {
                    val = new Color(dr.readInt(), dr.readInt(), dr.readInt());
                } else {
                    val = new Color(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
                }
            } else if (valtype.equals("Boolean")) {
                val = new Boolean(dr.readString());
            } else if (valtype.equals("String")) {
                val = dr.readString();
            } else if (valtype.equals("Int")) {
                val = new Integer(dr.readInt());
            } else if (valtype.equals("Storable")) {
                val = dr.readStorable();
            } else if (valtype.equals("UNKNOWN")) {
                continue;
            }

            fMap.put(key, val);
        }
    }

    /**
     * Writes the attributes to a StorableInput.
     * FigureAttributes store the following types directly:
     * Color, Boolean, String, Int. Other attribute types
     * have to implement the Storable interface or they
     * have to be wrapped by an object that implements Storable.
     * @see Storable
     * @see #write
     */
    public void write(StorableOutput dw) {
        dw.writeString("attributes");

        dw.writeInt(fMap.size()); // number of attributes
        Enumeration<String> k = fMap.keys();
        while (k.hasMoreElements()) {
            String s = k.nextElement();
            dw.writeString(s);
            Object v = fMap.get(s);
            if (v instanceof String) {
                dw.writeString("String");
                dw.writeString((String) v);
            } else if (v instanceof Color) {
                dw.writeString("Color");
                dw.writeInt(((Color) v).getRed());
                dw.writeInt(((Color) v).getGreen());
                dw.writeInt(((Color) v).getBlue());
                dw.writeInt(((Color) v).getAlpha());
            } else if (v instanceof Boolean) {
                dw.writeString("Boolean");
                if (((Boolean) v).booleanValue()) {
                    dw.writeString("TRUE");
                } else {
                    dw.writeString("FALSE");
                }
            } else if (v instanceof Integer) {
                dw.writeString("Int");
                dw.writeInt(((Integer) v).intValue());
            } else if (v instanceof Storable) {
                dw.writeString("Storable");
                dw.writeStorable((Storable) v);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(FigureAttributes.class.getSimpleName()
                                 + ": Unknown attribute type: " + v);
                }
                dw.writeString("UNKNOWN");
            }
        }
    }
}