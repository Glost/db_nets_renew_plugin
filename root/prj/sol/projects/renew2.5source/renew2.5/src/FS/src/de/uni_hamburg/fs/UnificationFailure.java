package de.uni_hamburg.fs;

public class UnificationFailure extends Exception {
    public UnificationFailure() {
        super();
    }

    public UnificationFailure(Throwable cause) {
        super(cause);
    }

    public UnificationFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public UnificationFailure(Type t1, Type t2) {
        super("Types " + t1 + " and " + t2 + " are not compatible.");
    }
}