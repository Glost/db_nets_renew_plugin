/*
 * Created on 18.08.2004
 */
package de.renew.gui.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import CH.ifa.draw.DrawPlugin;

import de.renew.engine.common.SimulatorEventLogger;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;


/**
 * The controller for the gui to display the simulation log messages.
 * The class LoggingFrame contains the view implementation.
 *
 * @author Sven Offermann
 */
public class LoggingController {
    private LoggingFrame frame;
    private LoggerTableModel currentLoggerTableModel;

    public LoggingController(final Set<LoggingController> _LoggingFrameControllers) {
        String[] loggerNames = findSimulationLoggerNames();

        this.frame = new LoggingFrame(this, loggerNames);
        DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                  .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, frame);

        final LoggingController controller = this;
        _LoggingFrameControllers.add(controller);

        this.frame.addWindowListener(new WindowAdapter() {
                /* (non-Javadoc)
                         * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
                         */
                public void windowClosing(WindowEvent arg0) {
                    DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                              .removeFrame(frame);
                    _LoggingFrameControllers.remove(controller);
                    closeFrame();
                }
            });

        if (loggerNames.length > 0) {
            changeLogger(loggerNames[0]);
        }
    }

    public void setPermanentUpdate(boolean update) {
        // rebuild tabel model when toogled to true
        if (update == true) {
            destruct();
            currentLoggerTableModel = new LoggerTableModel(frame
                                          .getSelectedLoggerName());

            frame.setTableModel(currentLoggerTableModel);
        }

        if (currentLoggerTableModel != null) {
            currentLoggerTableModel.setPermanentUpdate(update);
        }
    }

    public void changeLogger(String loggerName) {
        showLogger(loggerName);
    }

    private String[] findSimulationLoggerNames() {
        Vector<String> loggerNames = new Vector<String>();

        Enumeration<?> ls = LogManager.getCurrentLoggers();
        while (ls.hasMoreElements()) {
            Logger logger = (Logger) ls.nextElement();
            if (logger.getName().startsWith(SimulatorEventLogger.SIM_LOG_PREFIX)) {
                Enumeration<?> as = logger.getAllAppenders();
                while (as.hasMoreElements()) {
                    Object o = as.nextElement();
                    if (o instanceof GuiAppender) {
                        loggerNames.add(logger.getName());
                    }
                }
            }
        }

        return loggerNames.toArray(new String[] {  });
    }

    private void showLogger(String loggerName) {
        // create table model with trace informations
        // if the appender is null an empty table model 
        // will be generated.
        destruct();
        currentLoggerTableModel = new LoggerTableModel(loggerName);

        frame.setTableModel(currentLoggerTableModel);
    }

    /**
     * Closes the visible logging frame. Afterwards, all asociated logging functionality will halt.
     */
    public void closeFrame() {
        this.frame.setVisible(false);
        this.frame.dispose();
        destruct();

    }

    /**
     * Destroys the currently registered {@link LoggerTableModel} and all asociated components
     * to the logging environment.
     */
    private void destruct() {
        if (currentLoggerTableModel != null) {
            currentLoggerTableModel.removeAll();
            currentLoggerTableModel = null;
        }
    }
}