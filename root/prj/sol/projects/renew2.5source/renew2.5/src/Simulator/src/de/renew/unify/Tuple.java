package de.renew.unify;

import java.util.Iterator;


public final class Tuple extends Aggregate {
    public static Tuple NULL = new Tuple(0);

    public Tuple(int arity) {
        super(arity);
    }

    public Tuple(Object[] initValues, StateRecorder recorder) {
        super(initValues, recorder);
    }

    public int getArity() {
        return references.length;
    }

    public Object getComponent(int i) {
        return references[i].value;
    }

    public Iterator<Object> iterator() {
        return new TupleIterator(this);
    }

    public Tuple copy(Copier copier) {
        if (references.length == 0) {
            return NULL;
        } else {
            Object[] dest = new Object[references.length];
            for (int i = 0; i < references.length; i++) {
                dest[i] = copier.copy(references[i].value);
            }
            return new Tuple(dest, null);
        }
    }

    public int hashCode() {
        int result = references.length + 71;

        for (int i = 0; i < references.length; i++) {
            result = result * 41 + refHash(i);
        }

        return result;
    }

    public boolean equals(Object obj) {
        return obj instanceof Tuple && matches((Aggregate) obj);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append('[');
        for (int i = 0; i < references.length; i++) {
            if (i > 0) {
                result.append(',');
            }
            if (references[i].value == null) {
                result.append("null");
            } else {
                result.append(references[i].value.toString());
            }
        }
        result.append(']');
        return result.toString();
    }
}