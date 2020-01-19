package de.renew.unify;

import java.util.Iterator;


public final class List extends Aggregate {
    public static List NULL = new List(0);

    public List(int arity) {
        super(arity);
        if (arity != 2 && arity != 0) {
            throw new RuntimeException("Illegal list element size.");
        }
    }

    public List(Object head, Object tail, StateRecorder recorder) {
        super(new Object[] { head, tail }, recorder);
    }

    public boolean isNull() {
        return references.length == 0;
    }

    public Object head() {
        return references[0].value;
    }

    public Object tail() {
        return references[1].value;
    }

    public Iterator<Object> iterator() {
        return new ListIterator(this);
    }

    public int length() {
        int length = 0;
        List current = this;
        while (!current.isNull()) {
            ++length;
            current = (List) current.tail();


            // Throws ClassCastException if tail is not a list.
            // We should better convert this Exception to CorruptedList or something.
        }
        return length;
    }

    public List append(List that) {
        if (this.isNull()) {
            return that;
        }
        if (tail() instanceof List) {
            return new List(head(), ((List) tail()).append(that), null);
        } else {
            // Ignore remainder of ill-formed list.
            return new List(head(), that, null);
        }
    }

    public List copy(Copier copier) {
        if (references.length == 0) {
            return NULL;
        } else {
            return new List(copier.copy(references[0].value),
                            copier.copy(references[1].value), null);
        }
    }

    public int hashCode() {
        if (references.length == 0) {
            return 73;
        } else {
            return refHash(0) + 43 * refHash(1);
        }
    }

    public boolean equals(Object obj) {
        return obj instanceof List && matches((Aggregate) obj);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append('{');

        List that = this;
        boolean prependComma = false;
        while (that.references.length != 0) {
            if (that.references.length == 1) {
                // This should not happen. Somehow the references
                // reduction did not work.
                throw new RuntimeException("Somebody took the hashcode of a delegator.");
            }

            if (prependComma) {
                result.append(',');
            } else {
                prependComma = true;
            }

            if (that.references[0].value == null) {
                result.append("null");
            } else {
                result.append(that.references[0].value.toString());
            }

            Object next = that.references[1].value;
            if (next instanceof List) {
                that = (List) next;
            } else {
                result.append(':');
                if (that.references[1].value == null) {
                    result.append("null");
                } else {
                    result.append(that.references[1].value.toString());
                }
                that = NULL;
            }
        }
        result.append('}');
        return result.toString();
    }
}