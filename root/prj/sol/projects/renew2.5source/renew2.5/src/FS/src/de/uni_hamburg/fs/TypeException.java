package de.uni_hamburg.fs;

public class TypeException extends UnificationFailure {
    public final ConceptImpl concept;
    public final Name featureName;

    public TypeException() {
        this(null, null, null);
    }

    public TypeException(Throwable cause) {
        this(null, null, cause);
    }

    public TypeException(ConceptImpl concept) {
        this(concept, null, null);
    }

    public TypeException(ConceptImpl concept, Name featureName) {
        this(concept, featureName, null);
    }

    public TypeException(ConceptImpl concept, Name featureName, Throwable cause) {
        super((concept == null) ? null
                                : (concept
                                + ((featureName == null) ? "" : ("@"
                                                         + featureName))), cause);
        this.concept = concept;
        this.featureName = featureName;
    }
}