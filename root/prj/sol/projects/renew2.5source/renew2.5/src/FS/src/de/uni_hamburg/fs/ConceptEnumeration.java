package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public class ConceptEnumeration implements CollectionEnumeration {
    private CollectionEnumeration enumeration;

    public ConceptEnumeration(ConceptSet concepts) {
        enumeration = concepts.elements();
    }

    public ConceptEnumeration(CollectionEnumeration enumeration) {
        this.enumeration = enumeration;
    }

    public boolean hasMoreElements() {
        return enumeration.hasMoreElements();
    }

    public Object nextElement() {
        return enumeration.nextElement();
    }

    public Concept nextConcept() {
        return (Concept) enumeration.nextElement();
    }

    public int numberOfRemainingElements() {
        return enumeration.numberOfRemainingElements();
    }

    public boolean corrupted() {
        return enumeration.corrupted();
    }
}