/*
 * Created on 11.08.2004
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import de.renew.engine.common.SimulatorEvent;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


/**
 * A Log4j {@link Appender} implementation that collects Renew simulator log
 * events and dispatches them to {@link LoggerRepository} instances. The
 * repository instance is determined based on the Log4j logger name and the
 * simulation run id of the logged event.
 * <p>
 * The <code>GuiAppender</code> assumes that it receives only log4j events whose
 * message is of the type {@link SimulatorEvent}. Other events will throw a
 * {@link ClassCastException}.
 * </p>
 * <p>
 * Because the <code>GuiAppender</code> and some related classes generate log4j
 * events in the category <code>"de.renew.gui.logging"</code>, it is not allowed
 * to configure a <code>GuiAppender</code> as destination for events of that log
 * category. Otherwise an endless loop might occur. Fortunately, the log events
 * generated here are not <code>SimulatorEvent</code> instances so that the loop
 * will be broken by a <code>ClassCastException</code>.
 * </p>
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 */
public class GuiAppender implements Appender {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(GuiAppender.class);

    /**
     * The filters of this appender as configured by Log4j.
     **/
    private List<Filter> filters = new Vector<Filter>();

    /**
     * The name of this appender as configured by Log4j.
     **/
    private String name = null;

    /**
     * The capacity currently configured for {@link LoggerRepository} instances
     * related to this appender. The default value is {@value} , but it can be
     * reconfigured anytime via {@link #setPufferSize(int)}.
     **/
    private int stepPufferSize = 20; // by default a GuiAppender stores only the last 20 net simulator log messages 

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#addFilter(org.apache.log4j.spi.Filter)
     */
    @Override
    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#getFilter()
     */
    @Override
    public Filter getFilter() {
        if (filters.isEmpty()) {
            return null;
        }
        return filters.get(0);
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#clearFilters()
     */
    @Override
    public void clearFilters() {
        filters.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>GuiAppender</code> has no <code>ErrorHandler</code> and ignores
     * the given parameter.
     * </p>
     **/
    @Override
    public void setErrorHandler(ErrorHandler eHandler) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>GuiAppender</code> has no <code>ErrorHandler</code>.
     * </p>
     * @return always <code>null</code>
     **/
    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>GuiAppender</code> uses no <code>Layout</code> and ignores
     * the given parameter.
     * </p>
     **/
    @Override
    public void setLayout(Layout layout) {
        // No layout used
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>GuiAppender</code> has no <code>Layout</code>.
     * </p>
     * @return always <code>null</code>
     **/
    @Override
    public Layout getLayout() {
        // no layout used
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>GuiAppender</code> has no <code>Layout</code>.
     * </p>
     * @return always <code>false</code>
     **/
    @Override
    public boolean requiresLayout() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    /**
     * Gets the capacity of the <code>LoggerRepository</code> instances
     * associated with this appender.
     *
     * @return the limit on the number of step traces kept in memory.
     */
    public int getPufferSize() {
        return stepPufferSize;
    }

    /**
     * Sets the capacity of the <code>LoggerRepository</code> instances
     * associated with this appender.
     *
     * @param bufferSize the limit on the number of step traces kept in memory.
     */
    public void setPufferSize(int bufferSize) {
        this.stepPufferSize = bufferSize;
    }

    /**
     * Receive a {@link SimulatorEvent} wrapped in a {@link LoggingEvent} and
     * dispatch it to the appropriate {@link LoggerRepository} instances.
     * <p>
     * The <code>GuiAppender</code> is peckish and accepts only log4j events
     * whose message is of the type {@link SimulatorEvent}.
     * </p>
     *
     * @param event the log event with the {@link SimulatorEvent} message to
     *            collect
     * @throws ClassCastException if the message coming with the event is not of
     *             the type {@link SimulatorEvent}.
     **/
    @Override
    public void doAppend(LoggingEvent event) {
        SimulatorEvent simEvent = (SimulatorEvent) event.getMessage();
        MainRepository mRepository = MainRepositoryManager.getInstance()
                                                          .getRepository(simEvent);
        if (mRepository == null) {
            // cannot log the message, cause i cannot find the repository for this simulation step
            if (logger.isDebugEnabled()) {
                logger.debug(this
                             + ": Discarding event because no matching repository exists: "
                             + simEvent);
            }
            return;
        }

        String[] definedLoggerNames = getDefinedLoggers(event.getLoggerName());
        for (int x = 0; x < definedLoggerNames.length; x++) {
            LoggerRepository repository = mRepository.getLoggerRepository(definedLoggerNames[x],
                                                                          stepPufferSize);
            if (logger.isTraceEnabled()) {
                logger.trace(this + ": Dispatching event to " + repository
                             + ": " + simEvent);
            }
            repository.addEvent(simEvent);
        }
    }

    /**
     * Compute names of all Log4j loggers that are currently configured with a
     * <code>GuiAppender</code> and are covered by the given
     * <code>eventLoggerName</code>.
     *
     * @param eventLoggerName a qualified logger name for which the configured
     *            gui loggers should be computed.
     * @return qualified logger names which are associated with a gui logger.
     */
    private static String[] getDefinedLoggers(String eventLoggerName) {
        Vector<String> loggers = new Vector<String>();
        Category logger = Logger.getLogger(eventLoggerName);
        while (logger != null) {
            Enumeration<?> enumeration = logger.getAllAppenders();
            boolean found = false;
            while (enumeration.hasMoreElements() && !found) {
                Appender appender = (Appender) enumeration.nextElement();
                if (appender instanceof GuiAppender) {
                    found = true;
                    loggers.add(logger.getName());
                }
            }
            logger = logger.getParent();
        }

        return loggers.toArray(new String[] {  });
    }
}