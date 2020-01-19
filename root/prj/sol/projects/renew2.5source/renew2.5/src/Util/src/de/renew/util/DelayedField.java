package de.renew.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Wrapper to store a field and the object containing the field.
 *
 * Created: Tue Feb  8  2000
 *
 * @author Michael Duvigneau
 */
class DelayedField implements Serializable {
    static final long serialVersionUID = -6231461074991131516L;
    private Object field;
    private DelayedFieldOwner owner;

    DelayedField(Object field, DelayedFieldOwner owner) {
        this.field = field;
        this.owner = owner;
    }

    void reassign() throws IOException {
        owner.reassignField(field);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        RenewObjectOutputStream rout = null;
        if (out instanceof RenewObjectOutputStream) {
            rout = (RenewObjectOutputStream) out;
        }
        if (rout != null) {
            rout.beginDomain(owner);
        }
        out.defaultWriteObject();
        if (rout != null) {
            rout.endDomain(owner);
        }
    }

    public String toString() {
        return "DelayedField(owner:" + owner + ", value:" + field + ")";
    }
}