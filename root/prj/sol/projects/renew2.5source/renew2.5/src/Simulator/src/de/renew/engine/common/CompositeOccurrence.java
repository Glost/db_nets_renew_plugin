package de.renew.engine.common;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * The base class for occurrences that derive their semantics from
 * a number of other occurrences.
 */
public class CompositeOccurrence extends AbstractOccurrence {
    private List<Occurrence> occurrences = new ArrayList<Occurrence>();

    public CompositeOccurrence(TransitionInstance tInstance) {
        super(tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    public void addOccurrence(Occurrence occurrence) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        occurrences.add(occurrence);
    }

    public void addOccurrences(Collection<Occurrence> newOccurrences) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        occurrences.addAll(newOccurrences);
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        List<Binder> binders = new ArrayList<Binder>();
        Iterator<Occurrence> iterator = occurrences.iterator();
        while (iterator.hasNext()) {
            binders.addAll(iterator.next().makeBinders(searcher));
        }
        return binders;
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Collection<Executable> executables = new ArrayList<Executable>();
        Iterator<Occurrence> iterator = occurrences.iterator();
        while (iterator.hasNext()) {
            executables.addAll(iterator.next()
                                       .makeExecutables(variableMapperCopier));
        }
        return executables;
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return null;
    }
}