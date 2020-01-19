/*
 * Created on 19.08.2004
 */
package de.renew.gui.logging;

import de.renew.engine.events.SimulationEvent;

import java.util.Arrays;


/**
 * @author Sven Offermann
 */
public class StepTableModel extends TableModel
        implements StepTraceChangeListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(StepTableModel.class);
    private StepTrace stepTrace;
    private RepositoryChangeBuffer eventBuffer;

    public StepTableModel(StepTrace stepTrace,
                          RepositoryChangeBuffer eventBuffer) {
        super(false);

        this.stepTrace = stepTrace;
        this.eventBuffer = eventBuffer;
        eventBuffer.addStepTraceChangeListener(this);
        stepTrace.addStepTraceChangeListener(eventBuffer);

        SimulationEvent[] messages = stepTrace.getEvents();
        if (logger.isTraceEnabled()) {
            logger.trace(StepTableModel.class.getSimpleName()
                         + ": Initially including " + messages.length
                         + " events for " + stepTrace + ".\n"
                         + Arrays.toString(messages));
        }
        for (int x = 0; x < messages.length; x++) {
            addRow(new Object[] { messages[x] });
        }
    }

    public void stepTraceChanged(StepTrace stepTrace) {
        if (stepTrace == this.stepTrace) {
            // add new trace message to table model
            SimulationEvent[] messages = stepTrace.getEvents();
            for (int x = getRowCount(); x < messages.length; x++) {
                addRow(new Object[] { messages[x] });
            }
            fireTableRowsInserted(getRowCount() - messages.length - 1,
                                  getRowCount() - 1);
        }
    }

    public void dispose() {
        stepTrace.removeStepTraceChangeListener(eventBuffer);
        eventBuffer.removeStepTraceChangeListener(this);
    }
}