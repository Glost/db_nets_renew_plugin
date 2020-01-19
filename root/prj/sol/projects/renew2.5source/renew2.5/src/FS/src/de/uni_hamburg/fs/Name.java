package de.uni_hamburg.fs;

public class Name implements java.io.Serializable {
    public static final Name EMPTY = new Name("");
    public String name;

    public Name(String name) {
        this.name = name.intern();
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Name) {
            return name == ((Name) obj).name;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        name = name.intern();
    }
}