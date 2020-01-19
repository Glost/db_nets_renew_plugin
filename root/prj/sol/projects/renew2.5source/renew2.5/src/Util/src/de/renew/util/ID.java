package de.renew.util;



/**
 * An ID, as implemented by this class, is an identifier that
 * consists of a sequence of integers.
 */
public class ID implements java.io.Serializable {
    private int[] seq;
    private int hash;

    private ID(int[] seq) {
        this.seq = seq;


        // Precompute hash code.
        hash = 637;
        for (int i = 0; i < seq.length; i++) {
            hash = hash * 59 + seq[i];
        }
    }

    public static ID create(int num) {
        return new ID(new int[] { num });
    }

    public static ID create(int num1, int num2) {
        return new ID(new int[] { num1, num2 });
    }

    public static ID create(int num1, int num2, int num3) {
        return new ID(new int[] { num1, num2, num3 });
    }

    public static ID create(int[] seq) {
        return new ID(seq.clone());
    }

    public ID appending(int num) {
        int[] newSeq = new int[seq.length + 1];
        for (int i = 0; i < seq.length; i++) {
            newSeq[i] = seq[i];
        }
        newSeq[seq.length] = num;
        return new ID(newSeq);
    }

    public ID appending(ID id) {
        return appending(id.seq);
    }

    public ID appending(int[] app) {
        int[] newSeq = new int[seq.length + app.length];
        for (int i = 0; i < seq.length; i++) {
            newSeq[i] = seq[i];
        }
        for (int i = 0; i < app.length; i++) {
            newSeq[i + seq.length] = seq[i];
        }
        return new ID(newSeq);
    }

    public ID clipped() {
        if (seq.length == 0) {
            throw new RuntimeException("Cannot clip: ID already empty.");
        }
        int[] newSeq = new int[seq.length - 1];
        for (int i = 0; i < seq.length - 1; i++) {
            newSeq[i] = seq[i];
        }
        return new ID(newSeq);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ID) {
            ID that = (ID) obj;
            if (that.seq.length == seq.length) {
                for (int i = 0; i < seq.length; i++) {
                    if (that.seq[i] != seq[i]) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < seq.length; i++) {
            if (i > 0) {
                buf.append(".");
            }
            buf.append(seq[i]);
        }
        return buf.toString();
    }
}