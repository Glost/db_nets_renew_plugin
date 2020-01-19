/*
 * Created on Oct 25, 2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;


/**
 * @author Sven Offermann
 *
 */
public class TreeNodeAppenderWrapper {
    private Logger logger;
    private Appender appender;

    public TreeNodeAppenderWrapper(Logger logger, Appender appender) {
        this.logger = logger;
        this.appender = appender;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Appender getAppender() {
        return this.appender;
    }

    public String toString() {
        return this.appender.getClass().getName();
    }
}