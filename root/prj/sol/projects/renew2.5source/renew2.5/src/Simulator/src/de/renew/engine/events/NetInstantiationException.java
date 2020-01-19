/*
 * Created on Nov 22, 2004
 *
 */
package de.renew.engine.events;

import de.renew.net.Net;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * @author Sven Offermann
 *
 */
public class NetInstantiationException extends ExceptionEvent {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetInstantiationException.class);
    private Net net = null;

    public NetInstantiationException(Net net, Throwable exception) {
        super(exception);

        this.net = net;
    }

    public String toString() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        out.println("An exception occured durring the instantiation of net "
                    + net.getName());
        getException().printStackTrace(out);

        return writer.toString();
    }
}