package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.Triggerable;

import java.util.Collection;


public class SimulatorHelper {

    /**
     * Search a given searchable, making sure to register
     * dependencies. The given triggerable might be triggered
     * as soon as this method begins its work. In that case
     * the result of this search should be discarded.
     * The finder will only be informed of bindings that
     * can be executed without advancing the time.
     *
     * @param finder the finder that will be informed about
     *   possible bindings
     * @param searchable the object to be searched
     * @param triggerable the object that will be notified
     *   when an object changes, if that object is relavant to this
     *   search process
     **/
    public static void searchOnce(Searcher searcher, Finder finder,
                                  Searchable searchable, Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Perform the search.
        searcher.searchAndRecover(new CheckTimeFinder(finder), searchable,
                                  triggerable);
    }

    /**
     * This method checks a searchable for enabledness.
     * A searcher must be provided for this method. The method
     * uses the given triggerable to store a new set of triggers.
     */
    public static boolean isFirable(Searchable searchable,
                                    Triggerable triggerable, Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        EnablednessFinder finder = new EnablednessFinder();
        searchOnce(searcher, finder, searchable, triggerable);
        return finder.isEnabled();
    }

    /**
     * This method checks a searchable for enabledness. It
     * uses the given triggerable to store a new set of triggers.
     */
    public static boolean isFirable(Searchable searchable,
                                    Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Searcher searcher = new Searcher();
        return isFirable(searchable, triggerable, searcher);
    }

    /**
     * This method collects all bindings for a searchable.
     * A searcher must be provided for this method. The method
     * uses the given triggerable to store a new set of triggers.
     * By using triggerables other than those associated with the
     * searchable, it is possible to keep more triggers than
     * with the searchable's triggers. This is required because the
     * searchable might only be interested in its own enabledness,
     * but not in all its bindings.
     *
     * @see de.renew.engine.simulator.Binding
     *
     * @param searchable the searchable whose binding will be determined
     * @param triggerable the triggerable that get notified as soon as
     *   the search result becomes invalid
     * @param searcher the searcher that performs the search
     * @return an enumeration of bindings
     */
    public static Collection<Binding> findAllBindings(Searchable searchable,
                                                      Triggerable triggerable,
                                                      Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        CollectingFinder finder = new CollectingFinder();
        searchOnce(searcher, finder, searchable, triggerable);
        return finder.bindings();
    }

    /**
     * This method collects all bindings for a searchable. It
     * uses the given triggerable to store a new set of triggers.
     */
    public static Collection<Binding> findAllBindings(Searchable searchable,
                                                      Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Searcher searcher = new Searcher();
        return findAllBindings(searchable, triggerable, searcher);
    }
}