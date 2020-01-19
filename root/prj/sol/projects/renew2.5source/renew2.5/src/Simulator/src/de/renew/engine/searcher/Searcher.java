package de.renew.engine.searcher;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A searcher coordinates the search for an activated binding
 * and keeps track of various possibilities to
 * assign values to variables. To this end, it keeps
 * track of a set of {@link Binder}s that may represent input
 * arcs, synchronous channels, or the like.
 * The searcher also makes sure to inform a
 * {@link Finder} about every acceptable binding that was
 * found.
 * <p>
 * Each run of a searcher may produce multiple possible
 * binding, but at most one of these bindings may be executed.
 */
public class Searcher {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Searcher.class);

    /**
     * The object that is informed about every
     * successfully detected binding.
     **/
    private Finder finder;

    /**
     * the set of current unprocessed {@link Binder}s
     **/
    private Set<Binder> binders;

    /**
     * the set of {@link Occurrence} object (normally
     * transition occurrences) that participate
     * in this search and in the subsequent execution.
     **/
    private Set<Occurrence> occurrences;

    /**
     * the object which is informed about
     **/
    private Triggerable primaryTriggerable;
    private double earliestTime;
    public CalculationChecker calcChecker;
    public StateRecorder recorder;
    private Map<String, DeltaSet> deltaSets = new HashMap<String, DeltaSet>();

    public Searcher() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        binders = new HashSet<Binder>();
        occurrences = new HashSet<Occurrence>();

        calcChecker = new CalculationChecker();
        recorder = new StateRecorder();
    }

    // Returns true, if we should abort the current
    // search for new bindings. This method may only be called
    // by myself or by invoked binders during the search.
    public boolean isCompleted() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return finder.isCompleted();
    }

    private void setPrimaryTriggerable(Triggerable triggerable) {
        // Clear all triggers previously associated to the triggerable.
        if (triggerable != null) {
            triggerable.triggers().clear();
        }

        primaryTriggerable = triggerable;
    }

    public void insertTriggerable(TriggerableCollection triggerables) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // If there is a primary triggerable, we register it
        // at the given collection of triggerables.
        if (primaryTriggerable != null) {
            triggerables.include(primaryTriggerable);
        }
    }

    // Maintain a collection of transition occurrences.
    public void addOccurrence(Occurrence occurrence) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        occurrences.add(occurrence);
    }

    public void removeOccurrence(Occurrence occurrence) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        occurrences.remove(occurrence);
    }

    /**
     * Return the occurrences that are currently registered
     * at this searcher.
     *
     * @see de.renew.engine.searcher.Occurrence
     *
     * @return an enumeration of all occurrences
     */
    public Collection<Occurrence> getOccurrences() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return occurrences;
    }

    /**
     * Retrieve one delta set object for use with this
     * searcher. The delta set is chosen based on the category
     * answered by the argument factory. If no appropriate
     * object is available, create one using the given
     * factory object.
     *
     * @param factory the factory object which is responsible for creating
     *   delta sets of the desired category
     * @return an appropriate delta set
     */
    public DeltaSet getDeltaSet(DeltaSetFactory factory) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        String category = factory.getCategory();
        DeltaSet result = deltaSets.get(category);
        if (result == null) {
            result = factory.createDeltaSet();
            deltaSets.put(category, result);
        }
        return result;
    }

    /**
     * When in a finder called by this searcher,
     * retrieve the earliest possible moment of time
     * where the binding might be enacted.
     *
     * @return the time
     **/
    public double getEarliestTime() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return earliestTime;
    }

    /**
     * Remember the earliest possible time for
     * the currently found binding to be enacted.
     * This value can later on be queried by
     * {@link #getEarliestTime}.
     *
     * @param time the time
     **/
    private void setEarliestTime(double time) {
        earliestTime = time;
    }

    /**
     * Find the binder with the minimum binding badness
     * Return null, if no binder wants to try.
     **/
    private Binder selectBestBinder() {
        Binder bestBinder = null;
        int bestBadness = BindingBadness.max;
        Iterator<Binder> enumeration = binders.iterator();
        while (enumeration.hasNext() && !isCompleted()) {
            Binder binder = enumeration.next();
            int badness = binder.bindingBadness(this);
            if (badness < bestBadness) {
                bestBinder = binder;
                bestBadness = badness;
            }
        }
        return bestBinder;
    }

    /**
     * The central method for coordinating the search process.
     * This method selects the binder with the lowest binding
     * badness, removes it from the set of binders, invokes
     * it, and re-adds it to the set of binders.
     **/
    public void search() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        int checkpoint = recorder.checkpoint();
        if (!binders.isEmpty()) {
            Binder binder = selectBestBinder();
            if (binder != null && !isCompleted()) {
                removeBinder(binder);
                binder.bind(this);
                addBinder(binder);
            }
        } else {
            if (calcChecker.isConsistent()) {
                // Make sure not to fire before all tokens are available.
                double time = 0;
                for (Iterator<DeltaSet> i = deltaSets.values().iterator();
                             i.hasNext();) {
                    DeltaSet deltaSet = i.next();
                    time = Math.max(time, deltaSet.computeEarliestTime());
                }
                setEarliestTime(time);


                // Notify the finder, even if the binding is not yet
                // activated. The finder might want to store the
                // binding for a later time or find out the earliest possible
                // binding.
                finder.found(this);
            }
        }
        recorder.restore(checkpoint);
    }

    /**
     * Add the given occurrence to the set of occurrences,
     * intoduce its binders to the search process and start
     * or continue the search process. At the end of this method
     * the original state of the search is restored.
     *
     * @param occurrence
     */
    public void search(Occurrence occurrence) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        int checkpoint = recorder.checkpoint();
        try {
            Collection<Binder> binders = occurrence.makeBinders(this);
            addOccurrence(occurrence);
            addBinders(binders);
            search();
            removeBinders(binders);
            removeOccurrence(occurrence);
        } catch (Impossible e) {
            // When getting the binders, an exception was thrown.
            // The occurrence cannot be enabled.
        } finally {
            recorder.restore(checkpoint);
        }
    }

    /**
     * Add a new binder that must be processed before
     * this searcher can contact the finder.
     *
     * @param binder the binder to be added
     **/
    public void addBinder(Binder binder) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        binders.add(binder);
    }

    /**
     * Remove a previously added binder.
     *
     * @param binder the binder to be removed
     **/
    public void removeBinder(Binder binder) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        binders.remove(binder);
    }

    /**
     * Convenience method for adding an entire set
     * of binders.
     *
     * @param binders the {@link Binder} objects to be added
     **/
    public void addBinders(Collection<Binder> binders) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.binders.addAll(binders);
    }

    /**
     * Convenience method for removing an entire set
     * of binders.
     *
     * @param binders the {@link Binder} objects to be removed
     **/
    public void removeBinders(Collection<Binder> binders) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.binders.removeAll(binders);
    }

    /**
     * Make sure to clean up before a search.
     **/
    private void startSearch() {
        // Make sure that the current state recorder does not waste
        // space with now obsolete bindings.
        recorder.restore();


        // Ensure that the temporary containers are initially empty.
        // If they are not, something went wrong previously,
        // so we report en error.
        if (!occurrences.isEmpty() || !binders.isEmpty()) {
            throw new RuntimeException("Searcher was not in idle state "
                                       + "at the start of a search.");
        }
    }

    /**
     * Search a given searchable, making sure to register
     * dependencies. The given triggerable might be triggered
     * as soon as this method begins its work. In that case
     * the result of this search should be discarded.
     * The finder will be informed of all bindings, even those
     * bindings that will only become activated in the future.
     *
     * @param finder the finder that will be informed about
     *   possible bindings
     * @param searchable the object to be searched
     * @param triggerable the object that will be notified
     *   when an object changes, if that object is relavant to this
     *   search process
     **/
    public void searchAndRecover(Finder finder, Searchable searchable,
                                 Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Prepare a new search that starts from scratch.
        startSearch();


        // Remember the finder.
        this.finder = finder;

        // Remember the triggerable that is interested in changes
        // of this search result.
        setPrimaryTriggerable(triggerable);


        // Unless we find any further restriction,
        // the next firing can happen immediately.
        earliestTime = 0;

        try {
            // Is this searchable object activated?
            searchable.startSearch(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);


            // We reinsert this searchable only when a binding was found,
            // because otherwise there is no real chance that we
            // could have more luck next time.
        }


        // Reset state to allow garbage collection.
        binders.clear();
        occurrences.clear();

        calcChecker.reset();
        recorder.restore();
        deltaSets.clear();

        setPrimaryTriggerable(null);

        this.finder = null;
    }

    /**
     * Try to find an activated binding by targetting
     * a channel request at a channel target.
     *
     * @param channelTarget the channel target to be searched
     * @param name the name of the channel
     * @param params the channel parameters, typically a tuple
     * @param isOptional if true, the search will succeed even
     *   if the channel target does not declared a channel
     *   of the given name
     * @param finder the finder to be informed about the binding
     * @param triggerable the triggerable to be informed about changes
     **/
    public void initiatedSearch(ChannelTarget channelTarget, String name,
                                Variable params, boolean isOptional,
                                Finder finder, Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Prepare a new search that starts from scratch.
        startSearch();


        // Remember the finder.
        this.finder = finder;


        // Possible register for state change notifications.
        setPrimaryTriggerable(triggerable);

        try {
            // We want to find a transition within the specified
            // net instance that can fire. This method is
            // usually called to generate the very first tokens
            // in a net. In a sense, this resembles the main(args)
            // call of the runtime environment.
            //
            // If the argument isOptional is true, the invoked net
            // need not provide an appropriate channel at all. But if it does,
            // the synchronisation must succeed.
            Variable targetVariable = new Variable(channelTarget, recorder);
            Binder initialBinder = new ChannelBinder(targetVariable, name,
                                                     params, isOptional);
            addBinder(initialBinder);
            search();
            removeBinder(initialBinder);
            recorder.restore();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);


            // We reinsert this searchable only when a binding was found,
            // because otherwise there is no real chance that we
            // could have more luck next time.
        }


        // Reset state to allow garbage collection.
        binders.clear();
        occurrences.clear();

        calcChecker.reset();
        recorder.restore();
        deltaSets.clear();

        setPrimaryTriggerable(null);
        finder = null;
    }
}