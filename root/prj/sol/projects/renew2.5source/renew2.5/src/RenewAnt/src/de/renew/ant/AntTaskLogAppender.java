package de.renew.ant;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


/**
 * These appender forwards a log message delivered by the
 * log4j framework to the log method of a ant given task.
 * This Appender is needed to when an ant task uses some
 * methods of the renew runtime classes to redirect produced
 * log4j log messages.
 *
 * @author Sven Offermann
 */
public class AntTaskLogAppender extends AppenderSkeleton {

    /**
     * To avoid repetitions of each Renew log message in the ant log, at
     * most one instance of this class should be used.
     **/
    private static AntTaskLogAppender instance = null;

    /**
     * The ant task to which the log messages should be redirected.
     */
    private Task logTask;

    private AntTaskLogAppender() {
    }

    private void setLogTask(Task logTask) {
        this.logTask = logTask;
    }

    /**
     * Creates a new AntTaskLogAppender for redirecting log4j log messages
     * to the log method of an ant task.  If an appender instance already
     * exists, it will be reused.
     *
     * @param logTask The ant task to which the log messages should be redirected.
     */
    public static synchronized AntTaskLogAppender getInstance(Task logTask) {
        if (instance == null) {
            instance = new AntTaskLogAppender();
        }
        instance.setLogTask(logTask);
        if (logTask != null) {
            logTask.log("Attaching Renew->Ant log forwarder to task: "
                        + logTask, Project.MSG_VERBOSE);
        }
        return instance;
    }

    public void close() {
        // nothing to do
    }

    public void append(LoggingEvent event) {
        doAppend(event);
    }

    public boolean requiresLayout() {
        return false;
    }

    /**
     * writes the log message delivered by log4j to the
     * log method of the given task.
     */
    public synchronized void doAppend(LoggingEvent event) {
        // map the log4j log levels to the message levels defined for
        // logging in ant tasks.
        int msgLevel = Project.MSG_VERBOSE;

        if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            msgLevel = Project.MSG_ERR;
        } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            msgLevel = Project.MSG_WARN;
        } else if (event.getLevel().isGreaterOrEqual(Level.INFO)) {
            msgLevel = Project.MSG_INFO;
        }

        if (logTask != null) {
            logTask.log(event.getMessage().toString(), msgLevel);
        }
    }
}