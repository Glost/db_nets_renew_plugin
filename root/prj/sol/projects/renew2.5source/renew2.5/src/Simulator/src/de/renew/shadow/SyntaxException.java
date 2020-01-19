package de.renew.shadow;

import java.util.Vector;


public class SyntaxException extends Exception {
    public final String[] detailed;
    public final int line;
    public final int column;
    public final Vector<Object> errorObjects;
    private Object o;

    public SyntaxException(String msg, String[] detailed, int line, int column,
                           Throwable cause) {
        super(msg, cause);
        this.detailed = detailed;
        this.line = line;
        this.column = column;
        errorObjects = new Vector<Object>();
    }

    public SyntaxException(String msg, String[] detailed, int line, int column) {
        this(msg, detailed, line, column, null);
    }

    public SyntaxException(String msg, String[] detailed) {
        this(msg, detailed, 0, 0, null);
    }

    public SyntaxException(String msg, Throwable cause) {
        this(msg, null, 0, 0, cause);
    }

    public SyntaxException(String msg) {
        this(msg, null, 0, 0, null);
    }

    public SyntaxException addObject(Object graphicObject) {
        errorObjects.addElement(graphicObject);
        return this;
    }

    public SyntaxException(String msg, String[] detailed, int beginLine,
                           int beginColumn, Throwable cause, Object o) {
        this(msg, detailed, beginLine, beginColumn, cause);
        this.o = o;
    }

    public Object getProblemSpecificInformation() {
        return this.o;
    }
}