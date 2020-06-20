package de.renew.net;

import de.renew.dbnets.binder.ActionCallValuesBinder;
import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.datalogic.ActionCallExecutable;
import de.renew.engine.common.CompositeOccurrence;
import de.renew.engine.common.TraceExecutable;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.event.FiringEvent;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBNetTransitionOccurrence extends CompositeOccurrence {

    private final VariableMapper mapper;

    private final StateRecorder stateRecorder;

    public DBNetTransitionOccurrence(TransitionInstance transitionInstance,
                                     Variable params,
                                     Searcher searcher) throws Impossible {
        super(transitionInstance);

        // Make a simple variable mapper.
        mapper = new VariableMapper();
        // Make the special variable "this" known to the mapper.
        Variable thisVariable = mapper.map(new LocalVariable("this", false));
        try {
            Unify.unify(thisVariable, transitionInstance.getNetInstance(), null);
        } catch (Impossible e) {
            throw new RuntimeException("Unification failed unexpectedly.");
        }

        Transition transition = transitionInstance.getTransition();
        if (transition.uplink != null) {
            Object up = transition.uplink.params.startEvaluation(mapper,
                    searcher.recorder,
                    searcher.calcChecker);
            Unify.unify(up, params, searcher.recorder);


            // If the unification produced an exception,
            // then no bindings are possible. The caller will
            // handle this situation.
        }

        Iterator<TransitionInscription> iterator = transition.inscriptions
                .iterator();
        while (iterator.hasNext()) {
            TransitionInscription inscription = iterator.next();
            addOccurrences(inscription.makeOccurrences(mapper,
                    transitionInstance
                            .getNetInstance(), searcher));
        }

        stateRecorder = searcher.recorder;
    }

    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();
        Connection connection = ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnection();

        return Stream.concat(
                super.makeBinders(searcher).stream(),
                Stream.of(new ActionCallValuesBinder(
                        actionCall,
                        (DBNetTransitionInstance) getTransition(),
                        mapper,
                        stateRecorder,
                        connection
                ))
        ).collect(Collectors.toList());
    }

    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Collection<Executable> executables = new ArrayList<Executable>();
        executables.addAll(super.makeExecutables(copier));

        // Possibly insert a trace object.
        if (getTransition().getTransition().getTrace()) {
            executables.add(new TraceExecutable("Firing " + getTransition(),
                    getTransition()));
        }


        // Insert notifiers that can report the variable mapping.
        // Both will report the same firing event to the
        // listeners. Event identities are unique so that
        // the listener can associate start and end of a
        // firing.
        FiringEvent event = new FiringEvent(getTransition(),
                copier.makeCopy(mapper));
        executables.add(new FiringStartExecutable(event));
        executables.add(new FiringCompleteExecutable(event));

        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();

        if (Objects.isNull(actionCall)) {
            return executables;
        }

        Connection connection = ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnection();

        executables.add(new ActionCallExecutable(actionCall, copier.makeCopy(mapper), connection));

        return executables;
    }

    @Override
    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new TransitionOccurrenceDescription(getTransition(),
                copier.makeCopy(mapper));
    }

    @Override
    public String toString() {
        return getTransition().toString();
    }
}
