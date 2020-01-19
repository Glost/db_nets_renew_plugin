package de.renew.lola.parser;

public class NetFileParseError extends Exception {
    public NetFileParseError() {
        super();
    }

    public NetFileParseError(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NetFileParseError(Throwable cause) {
        super(cause);
    }

    public NetFileParseError(String msg) {
        super(msg);
    }
}