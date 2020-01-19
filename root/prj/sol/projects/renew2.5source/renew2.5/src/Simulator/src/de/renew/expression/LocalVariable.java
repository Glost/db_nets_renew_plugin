package de.renew.expression;

import java.io.IOException;
import java.io.Serializable;


public class LocalVariable implements Serializable {
    public String name;
    public boolean isVisible;

    // Refactoring
    public int variableBeginLine;
    public int variableBeginColumn;
    public int variableEndLine;
    public int variableEndColumn;

    public LocalVariable(String name, boolean isVisible) {
        this.name = name.intern();
        this.isVisible = isVisible;
    }

    public LocalVariable(String name) {
        this(name, true);
    }

    public String toString() {
        return "local variable \"" + name + "\"";
    }

    public boolean equals(Object obj) {
        if (obj instanceof LocalVariable) {
            return (name == ((LocalVariable) obj).name);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        name = name.intern();
    }
}