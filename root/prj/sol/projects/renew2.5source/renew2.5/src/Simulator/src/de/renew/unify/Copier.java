package de.renew.unify;

import java.util.HashMap;
import java.util.Map;


public class Copier {
    private Map<IdentityWrapper, IdentityWrapper> converted;

    public Copier() {
        converted = new HashMap<IdentityWrapper, IdentityWrapper>();
    }

    // If the object is complete, make a permanent copy.
    // If the object is not complete, make a copy and
    // insert new unknown objects appropriately.
    //
    // This method should not be called from within during
    // a notification callback.
    //
    // This method should not be called while there might be
    // a need to undo any operation on the created
    // copies.
    public Object copy(Object obj) {
        if (obj instanceof Unifiable) {
            if (obj instanceof Variable) {
                return new Variable(copy(((Variable) obj).getValue()), null);
            } else if (obj instanceof SilentlyUnifiable) {
                return ((SilentlyUnifiable) obj).copy(this);
            } else if (obj instanceof Tuple) {
                Tuple tuple = (Tuple) obj;
                return tuple.copy(this);
            } else if (obj instanceof List) {
                List list = (List) obj;
                return list.copy(this);
            } else if (obj instanceof Unknown) {
                return copyUnknown(obj);
            } else if (obj instanceof Calculator) {
                // Calculators are replaced by unknowns.
                // If another calculation is to be registered,
                // it must be done by hand. In Renew the calculation
                // will be actually initiated.
                return copyUnknown(obj);
            } else {
                throw new RuntimeException("An unknown subclass of Unifiable occurred.");
            }
        } else {
            return obj;
        }
    }

    // Extract an unknown from an internal hashtable.
    // This method might be called with an unknown or with
    // a calculator as an argument.
    private Unknown copyUnknown(Object obj) {
        IdentityWrapper wrappedKey = new IdentityWrapper(obj);
        if (converted.containsKey(wrappedKey)) {
            IdentityWrapper wrappedUnk = converted.get(wrappedKey);
            return (Unknown) wrappedUnk.getObject();
        } else {
            Unknown newUnknown = new Unknown();
            converted.put(wrappedKey, new IdentityWrapper(newUnknown));
            return newUnknown;
        }
    }
}