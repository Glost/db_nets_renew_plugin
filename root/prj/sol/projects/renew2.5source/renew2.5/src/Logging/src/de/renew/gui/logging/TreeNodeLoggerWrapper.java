/*
 * Created on Oct 25, 2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Logger;

import de.renew.engine.common.SimulatorEventLogger;


/**
 * @author Sven Offermann
 *
 */
public class TreeNodeLoggerWrapper {
    private Logger logger;

    public TreeNodeLoggerWrapper(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String toString() {
        String loggerName = logger.getName();
        if (loggerName.equals(SimulatorEventLogger.SIM_LOG_PREFIX)) {
            loggerName = "Simulation Root Logger";
        } else if (loggerName.startsWith(SimulatorEventLogger.SIM_LOG_PREFIX
                                                 + ".")) {
            loggerName = loggerName.substring(loggerName.indexOf(".") + 1);
        }

        return loggerName;
    }
}