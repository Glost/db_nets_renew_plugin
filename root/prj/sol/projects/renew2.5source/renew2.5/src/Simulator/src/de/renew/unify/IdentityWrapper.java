package de.renew.unify;

import java.io.Serializable;


// The Identity class restricts the equality of objects
// to their identity. Thereby two distinct, but equal
// objects can be seperated, e.g. in a hashtable.
class IdentityWrapper implements Serializable {
    private final Object o;

    IdentityWrapper(Object o) {
        this.o = o;
    }

    public int hashCode() {
        return System.identityHashCode(o);
    }

    public boolean equals(Object id) {
        if (id instanceof IdentityWrapper) {
            return o == ((IdentityWrapper) id).o;
        } else {
            return false;
        }
    }

    public Object getObject() {
        return o;
    }
}