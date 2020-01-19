package de.uni_hamburg.fs;

public class NoSuchFeatureException extends RuntimeException {
    public NoSuchFeatureException(Name feature, Type type) {
        super("Feature " + feature + " not allowed in type " + type + ".");
    }
}