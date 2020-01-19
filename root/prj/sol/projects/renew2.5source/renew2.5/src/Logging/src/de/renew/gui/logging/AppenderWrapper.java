/*
 * Created on Nov 19, 2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;

import de.renew.engine.common.SimulatorEventLogger;


/**
 * @author Sven Offermann
 *
 */
public class AppenderWrapper {
    private Appender appender = null;
    private String loggerName;

    public AppenderWrapper(Appender appender) {
        this.appender = appender;
    }

    public Appender getAppender() {
        return this.appender;
    }

    public String getLoggerName() {
        String name = null;
        if (loggerName.equals(SimulatorEventLogger.SIM_LOG_PREFIX)) {
            name = "Simulation Root Logger";
        } else if (loggerName.startsWith(SimulatorEventLogger.SIM_LOG_PREFIX
                                                 + ".")) {
            name = loggerName.substring(loggerName.indexOf(".") + 1);
        }

        return name;
    }

    public String getRealLoggerName() {
        return this.loggerName;
    }

    public String toString() {
        return this.getLoggerName();
    }
}