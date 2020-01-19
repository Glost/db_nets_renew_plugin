package de.uni_hamburg.fs;

public class NotInstantiableException extends Exception {
    public NotInstantiableException(FeatureStructure fs) {
        super("Feature Structure  is not instantiable:" + fs);
    }

    public NotInstantiableException(String msg) {
        super(msg);
    }
}