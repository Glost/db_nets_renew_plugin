package de.renew.formalism.java;

public abstract class Suggestion implements Comparable<Suggestion> {
    protected String name;
    protected String typeName;

    protected Suggestion(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    @Override
    public abstract String toString();

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public int compareTo(Suggestion suggestion) {
        int compareNames = getName().compareTo(suggestion.getName());

        if (compareNames == 0) {
            return suggestion.getName().length() - getName().length();
        } else {
            return compareNames;
        }
    }
}