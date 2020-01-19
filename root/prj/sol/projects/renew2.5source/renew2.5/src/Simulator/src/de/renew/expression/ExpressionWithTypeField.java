package de.renew.expression;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;


/**
 * Superclass for all expression classes which need to remember
 * a targetType.
 * <p>
 * The targetType field is of the type <code>java.lang.Class</code>
 * and needs special treatment on serialization.
 * The common (de-)serialization code is the only reason for the
 * existence of this class.
 * </p>
 * Created: Wed Feb  2  2000
 *
 * @author Michael Duvigneau
 * @see de.renew.util.ReflectionSerializer
 **/
abstract class ExpressionWithTypeField implements Expression {
    static final long serialVersionUID = 8460090091849454249L;

    /**
     * This field is not really transient, but as <code>java.lang.Class
     * </code>is not always serializable, we have to store it by
     * ourselves.
     **/
    transient Class<?> targetType;

    public ExpressionWithTypeField(Class<?> targetType) {
        this.targetType = targetType;
    }

    public Class<?> getType() {
        return targetType;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient targetType field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeClass(out, targetType);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient targetType field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        targetType = ReflectionSerializer.readClass(in);
    }
}