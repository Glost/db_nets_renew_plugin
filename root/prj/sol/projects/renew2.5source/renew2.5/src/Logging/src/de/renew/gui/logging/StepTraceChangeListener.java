/*
 * Created on 19.08.2004
 */
package de.renew.gui.logging;



/**
 * Informs about changes in a StepTrace object, like an added log event message.
 *
 * @author Sven Offermann
 */
public interface StepTraceChangeListener {

    /**
     * Called if a new trace message was added to a StepTrace.
     *
     * @param stepTrace the StepTrace to which a new trace message was added.
     */
    public void stepTraceChanged(StepTrace stepTrace);
}