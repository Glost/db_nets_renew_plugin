/*
 * Created on Oct 25, 2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;


/**
 *        A Appender builder is used to create new initialized instances of
 *  the different types of appenders, which can be used for logging.
 *
 *  * @author Sven Offermann
 */
public interface AppenderBuilder {

    /**
     * Creates new instance of a appender.
     *
     * @return the new instance of the appender
     */
    public Appender newInstance() throws InstantiationException;
}