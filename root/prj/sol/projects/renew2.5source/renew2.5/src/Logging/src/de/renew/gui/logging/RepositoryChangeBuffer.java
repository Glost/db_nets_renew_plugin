/**
 *
 */
package de.renew.gui.logging;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.engine.common.StepIdentifier;

import java.awt.EventQueue;

import java.lang.Math;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * The <code>RepositoryChangeBuffer</code> decouples repositories from table
 * models in the sense that all asynchronous update notifications are buffered
 * until the AWT event thread is ready to fetch them as a bulk update.
 * <p>
 * To avoid flooding of the event thread with obsolete events, the buffer
 * automatically discards obsolete change notifications. In fact, for each
 * {@link StepTrace} the buffer retains at most one event:
 * </p>
 * <ul>
 * <li>A {@link #stepTraceAdded} event becomes obsolete, when a
 * {@link #stepTraceRemoved} event arrives for the same {@link StepTrace} the
 * addition event belongs to.</li>
 * <li>A {@link #stepTraceChanged} event immediately becomes obsolete, when an
 * older {@link #stepTraceChanged} or {@link #stepTraceAdded} event already
 * exists in the buffer for the same {@link StepTrace} the new change event
 * belongs to.</li>
 * <li>A {@link #stepTraceRemoved} event immediately becomes obsolete, when the
 * corresponding {@link #stepTraceAdded} event still exists in the queue (has
 * not been delivered yet). Thus the table model will never see any event
 * related to that specific {@link StepTrace}.</li>
 * <li>Events of the kind {@link #stepTraceRemoveRequest} are never buffered or
 * forwarded because they serve for repository-internal communication only.</li>
 * </ul>
 *
 * @author Michael Duvigneau
 * @since Renew 2.4
 * @see AWTSynchronizedUpdate
 */
public class RepositoryChangeBuffer implements RepositoryChangeListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RepositoryChangeBuffer.class);

    /**
     * The minimum delay to wait between scheduled updates in the AWT event
     * queue.
     **/
    private static final long MIN_DELAY = 1000 / 64;

    /**
     * Stores change events. Events are indexed with the step trace they belong
     * to. Events are discarded when they become obsolete.
     **/
    private Map<StepTrace, BufferedEvent> buffer = new HashMap<StepTrace, BufferedEvent>();

    /**
     * Manages the thread decoupling.
     **/
    private AWTSynchronizedUpdate updater;

    /**
     * Stores references to all registered {@link RepositoryChangeListener}
     * instances.
     */
    protected Set<StepTraceChangeListener> listeners = new HashSet<StepTraceChangeListener>();

    /**
     * Creates and initializes a new event buffer. Neither does the buffer
     * register itself as listener at a repository nor does it automatically
     * register some other listener for later update propagation.
     **/
    public RepositoryChangeBuffer() {
        this.updater = new AWTSynchronizedUpdate(MIN_DELAY,
                                                 new Runnable() {
                @Override
                public void run() {
                    fireBulkUpdate();
                }
            });
    }

    // ------------------------------ propagate events to the table model

    /**
     * Dispatch the current buffer contents to all registered listeners. To
     * allow further concurrent incoming events, the buffer is saved and cleared
     * before the dispatch loop begins.
     **/
    protected void fireBulkUpdate() {
        assert EventQueue.isDispatchThread() : "RepositoryChangeBuffer.fireBulkUpdate must be executed in the event thread.";
        final Map<StepTrace, BufferedEvent> bulkbuffer;
        synchronized (this) {
            bulkbuffer = this.buffer;
            buffer = new HashMap<StepTrace, BufferedEvent>();
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + ": preparing bulk update with "
                         + bulkbuffer.size() + " events.");
        }
        Map<StepTrace, BufferedEvent> sortedBuffer = new TreeMap<StepTrace, BufferedEvent>(new StepTraceComparator());
        sortedBuffer.putAll(bulkbuffer);
        for (BufferedEvent bev : sortedBuffer.values()) {
            switch (bev.kind) {
            case ADD:
                fireStepTraceAdded(bev.repository, bev.step);
                break;
            case CHANGE:
                fireStepTraceChanged(bev.step);
                break;
            case REMOVE:
                fireStepTraceRemoved(bev.repository, bev.step);
                break;
            }
        }
    }

    /**
     * Register the given <code>listener</code> so that it will receive future
     * bulk updates. If the <code>listener</code> implements the
     * {@link RepositoryChangeListener} interface, it will also receive
     * the events {@link RepositoryChangeListener#stepTraceAdded} and
     * {@link RepositoryChangeListener#stepTraceRemoved}.
     *
     * @param listener  the listener to register for future notifications
     **/
    public void addStepTraceChangeListener(StepTraceChangeListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Deregister the given <code>listener</code> so that it will no longer
     * receive updates from this buffer.
     *
     * @param listener  the listener to unregister
     **/
    public void removeStepTraceChangeListener(StepTraceChangeListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Inform all currently registered listeners about a step trace change.
     *
     * @param stepTrace the affected step trace.
     * @see StepTraceChangeListener#stepTraceChanged(StepTrace)
     **/
    protected void fireStepTraceChanged(StepTrace stepTrace) {
        StepTraceChangeListener[] l = this.listeners.toArray(new StepTraceChangeListener[] {  });
        if (logger.isTraceEnabled()) {
            logger.trace(this + ": Sending change for step " + stepTrace
                         + " to " + l.length + " listeners.");
        }
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceChanged(stepTrace);
        }
    }

    /**
     * Inform all currently registered listeners about a newly added step trace.
     * Listeners that do not implement {@link RepositoryChangeListener} are
     * skipped.
     *
     * @param stepTrace the new step trace
     * @see RepositoryChangeListener#stepTraceAdded(StepTraceRepository,
     *      StepTrace)
     **/
    protected void fireStepTraceAdded(StepTraceRepository repository,
                                      StepTrace stepTrace) {
        StepTraceChangeListener[] l = this.listeners.toArray(new StepTraceChangeListener[] {  });
        if (logger.isTraceEnabled()) {
            logger.trace(this + ": Sending add for step " + stepTrace + " to "
                         + l.length + " listeners.");
        }
        for (int x = 0; x < l.length; x++) {
            if (l[x] instanceof RepositoryChangeListener) {
                ((RepositoryChangeListener) l[x]).stepTraceAdded(repository,
                                                                 stepTrace);
            }
        }
    }

    /**
     * Inform all currently registered listeners about a removed step trace.
     * Listeners that do not implement {@link RepositoryChangeListener} are
     * skipped.
     *
     * @param stepTrace the former step trace
     * @see RepositoryChangeListener#stepTraceRemoved(StepTraceRepository,
     *      StepTrace)
     **/
    protected void fireStepTraceRemoved(StepTraceRepository repository,
                                        StepTrace stepTrace) {
        StepTraceChangeListener[] l = this.listeners.toArray(new StepTraceChangeListener[] {  });
        if (logger.isTraceEnabled()) {
            logger.trace(this + ": Sending remove for step " + stepTrace
                         + " to " + l.length + " listeners.");
        }
        for (int x = 0; x < l.length; x++) {
            if (l[x] instanceof RepositoryChangeListener) {
                ((RepositoryChangeListener) l[x]).stepTraceRemoved(repository,
                                                                   stepTrace);
            }
        }
    }

    // ------------------------------- receive events from the repository

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceChangeListener#stepTraceChanged(de.renew.gui.logging.StepTrace)
     */
    @Override
    public synchronized void stepTraceChanged(StepTrace stepTrace) {
        if (buffer.containsKey(stepTrace)) {
            // discard event
            if (logger.isTraceEnabled()) {
                logger.trace(this + ": Discarding change " + stepTrace);
            }
        } else {
            buffer.put(stepTrace,
                       new BufferedEvent(BufferedEvent.Kind.CHANGE, stepTrace,
                                         null));
            if (updater != null) {
                updater.scheduleUpdate();
            }
        }
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.RepositoryChangeListener#stepTraceAdded(de.renew.gui.logging.StepTraceRepository, de.renew.gui.logging.StepTrace)
     */
    @Override
    public synchronized void stepTraceAdded(StepTraceRepository repository,
                                            StepTrace stepTrace) {
        if (buffer.containsKey(stepTrace)) {
            // TODO something went wrong.
            // add the event to the buffer anyway, replacing the older event.
            logger.warn(this + ": Add event is not the first event for step "
                        + stepTrace);
        }
        buffer.put(stepTrace,
                   new BufferedEvent(BufferedEvent.Kind.ADD, stepTrace,
                                     repository));
        if (updater != null) {
            updater.scheduleUpdate();
        }
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.RepositoryChangeListener#stepTraceRemoved(de.renew.gui.logging.StepTraceRepository, de.renew.gui.logging.StepTrace)
     */
    @Override
    public synchronized void stepTraceRemoved(StepTraceRepository repository,
                                              StepTrace stepTrace) {
        if (buffer.containsKey(stepTrace)
                    && buffer.get(stepTrace).kind == BufferedEvent.Kind.ADD) {
            // discard all events related to that step trace since it never became visible.
            buffer.remove(stepTrace);
            if (logger.isTraceEnabled()) {
                logger.trace(this + ": Discarding add/remove pair for step "
                             + stepTrace);
            }
        } else {
            buffer.put(stepTrace,
                       new BufferedEvent(BufferedEvent.Kind.REMOVE, stepTrace,
                                         repository));
            if (updater != null) {
                updater.scheduleUpdate();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Events of this kind are always discarded by the
     * <code>RepositoryChangeBuffer</code>.
     * </p>
     **/
    @Override
    public void stepTraceRemoveRequest(StepTraceRemoveRequest request) {
        // Always discard the event
    }

    // ------------------------------------- structure of buffered events

    /**
     * Instances of this class wrap an event type and the associated event data
     * to be kept in the buffer.
     *
     * @author Michael Duvigneau
     */
    private static class BufferedEvent {

        /**
         * Distinguish event types when they are stored in the buffer.
         *
         * @author Michael Duvigneau
         **/
        public static enum Kind {

            /**
             * Denotes step trace change events.
             *
             * @see RepositoryChangeListener#stepTraceChanged(StepTrace)
             */
            ADD,

            /**
             * Denotes step trace change events.
             *
             * @see RepositoryChangeListener#stepTraceChanged(StepTrace)
             */
            CHANGE,

            /**
             * Denotes step trace change events.
             *
             * @see RepositoryChangeListener#stepTraceChanged(StepTrace)
             */
            REMOVE;
        }
        ;

        /**
         * The type of event wrapped by this object.
         **/
        public final Kind kind;

        /**
         * The step the event affects.
         **/
        public final StepTrace step;

        /**
         * The repository that sent the event.
         **/
        public final StepTraceRepository repository;

        /**
         * Create an object that records the event as specified.
         *
         * @param kind the type of event to record.
         * @param step the step affected by the event.
         * @param repository the repository that sent the event.
         */
        public BufferedEvent(Kind kind, StepTrace step,
                             StepTraceRepository repository) {
            this.kind = kind;
            this.step = step;
            this.repository = repository;
        }
    }

    /**
     * Compares {@link StepTrace} instances based on the first
     * <code>stepCount</code> component of their {@link StepIdentifier}.
     *
     * @author Michael Duvigneau
     **/
    private static class StepTraceComparator implements Comparator<StepTrace> {
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(StepTrace o1, StepTrace o2) {
            long stepCount1;
            long stepCount2;
            if ((o1.getStepIdentifier() != null)
                        && (o1.getStepIdentifier().getComponents() != null)
                        && (o1.getStepIdentifier().getComponents().length > 0)) {
                stepCount1 = o1.getStepIdentifier().getComponents()[0];
            } else {
                stepCount1 = 0L;
            }
            if ((o2.getStepIdentifier() != null)
                        && (o2.getStepIdentifier().getComponents() != null)
                        && (o2.getStepIdentifier().getComponents().length > 0)) {
                stepCount2 = o2.getStepIdentifier().getComponents()[0];
            } else {
                stepCount2 = 0L;
            }
            return (int) Math.signum(stepCount1 - stepCount2);
        }
    }

    /**
     * Stops the updating process of AWT. Please invoke this method before destroying this object.
     */
    protected void stopBuffer() {
        this.updater = null;
    }
}