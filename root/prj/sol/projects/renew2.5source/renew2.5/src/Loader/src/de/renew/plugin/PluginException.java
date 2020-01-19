/*
 * (c) Copyright 2001 Joern Schumacher
 */
package de.renew.plugin;



/**
 * This class is a wrapper class for any exceptions thrown
 * while the framework is dealing with plugins.
 * There is a method that lets you extract the causing
 * exception if any (<code>getCause</code>).
 */
public class PluginException extends Exception {
    public PluginException() {
        super();
    }

    public PluginException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>PluginException</code> wrapping the
     * given <code>Throwable</code>. The message of the causing
     * exception is copied to this instance.
     **/
    public PluginException(Throwable ex) {
        this(ex.getMessage(), ex);
    }

    public PluginException(String message, Throwable ex) {
        super(message, ex);
    }
}