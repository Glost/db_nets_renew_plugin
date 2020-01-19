package de.renew.unify;

public class Impossible extends Exception {
    public Impossible() {
    }

    public Impossible(String msg) {
        super(msg);
    }

    public Impossible(Throwable cause) {
        super(cause);
    }

    public Impossible(String msg, Throwable cause) {
        super(msg, cause);
    }

    public static void THROW() throws Impossible {
        throw new Impossible();
    }
}