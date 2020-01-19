package de.renew.refactoring.renamevariable;



/**
 * Simple representation of a variable with a name and a type.
 *
 * @author 2mfriedr
 */
final class Variable implements Comparable<Variable> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(Variable.class);
    final String _name;
    final Class<?> _type;

    Variable(String name, Class<?> type) {
        _name = name;
        _type = type;
    }

    String getName() {
        return _name;
    }

    String getType() {
        return _type.getName();
    }

    @Override
    public String toString() {
        return getName() + ": " + getType();
    }

    @Override
    public int compareTo(Variable other) {
        // sort by type
        return getType().compareTo(other.getType());
    }

    @Override
    public int hashCode() {
        return getName().hashCode() ^ getType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable other = (Variable) obj;
            return getName().equals(other.getName())
                   && getType().equals(other.getType());
        }
        return false;
    }
}