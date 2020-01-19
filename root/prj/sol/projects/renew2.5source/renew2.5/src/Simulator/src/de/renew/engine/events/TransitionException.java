/*
 * Created on Nov 22, 2004
 *
 */
package de.renew.engine.events;

import de.renew.net.TransitionInstance;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * @author Sven Offermann
 *
 */
public class TransitionException extends ExceptionEvent {
    private TransitionInstance tInstance;

    public TransitionException(Throwable e) {
        this(null, e);
    }

    public TransitionException(TransitionInstance tInstance, Throwable e) {
        super(e);
        this.tInstance = tInstance;
    }

    public String toString() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        out.println("An exception occured while firing transition " + tInstance);
        getException().printStackTrace(out);

        return writer.toString();
    }

    /**
     * @return Returns the transition instance.
     */
    public TransitionInstance getTransitionInstance() {
        return this.tInstance;
    }
}