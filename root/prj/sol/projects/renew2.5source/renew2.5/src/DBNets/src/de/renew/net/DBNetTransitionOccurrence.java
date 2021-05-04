package de.renew.net;

import de.renew.dbnets.binder.ActionCallValuesBinder;
import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.datalogic.ActionCallExecutable;
import de.renew.dbnets.pa.PerformanceAnalysisExecutable;
import de.renew.dbnets.persistence.JdbcConnectionInstance;
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
import de.renew.net.arc.ArcUtils;
import de.renew.net.arc.RollbackArcExecutable;
import de.renew.net.event.FiringEvent;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The db-net's transition's occurrence for the concrete transition firing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DBNetTransitionOccurrence extends CompositeOccurrence {

    /**
     * The transition instance's variable mapper.
     * Maps the net's variables' names into their values.
     */
    private final VariableMapper mapper;

    /**
     * The state recorder instance.
     */
    private final StateRecorder stateRecorder;

    /**
     * The db-net's transition's occurrence's constructor.
     * Based on the {@link CompositeOccurrence#CompositeOccurrence(TransitionInstance)} implementation.
     *
     * @param transitionInstance The db-net's transition's instance.
     * @param params The param variables of the transition.
     * @param searcher The searcher instance.
     * @throws Impossible If the error occurred during the db-net's transition's occurrence initialization.
     */
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

    /**
     * Makes the transition's occurrence's binders - the binders for the transition's arcs as well as
     * the binder for binding the transition's action call's generated values and literals.
     *
     * @param searcher The searcher instance.
     * @return The transition's occurrences' binders.
     * @throws Impossible If the error occurred during the binders making.
     */
    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();
        JdbcConnectionInstance connectionInstance =
            ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnectionInstance();

        return Stream.concat(
                super.makeBinders(searcher).stream(),
                Stream.of(new ActionCallValuesBinder(
                        actionCall,
                        (DBNetTransitionInstance) getTransition(),
                        mapper,
                        stateRecorder,
                        connectionInstance
                ))
        ).collect(Collectors.toList());
    }

    /**
     * Makes the transition's occurrence's executables
     * including the transition's performance analysis and action call executables.
     * Based on the {@link super#makeExecutables(VariableMapperCopier)} implementation.
     *
     * @param copier The variable mappers' copier.
     * @return The transition's occurrence's executables.
     */
    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Collection<Executable> superExecutables = super.makeExecutables(copier);

        RollbackArcExecutable rollbackArcExecutable = superExecutables.stream()
                .filter(executable -> executable instanceof RollbackArcExecutable)
                .map(executable -> (RollbackArcExecutable) executable)
                .findFirst()
                .orElse(null);

        Collection<Executable> executables = superExecutables.stream()
                .map(executable -> ArcUtils.wrapOutputArcExecutable(executable, rollbackArcExecutable))
                .collect(Collectors.toList());

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

        JdbcConnectionInstance connectionInstance =
                ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnectionInstance();

        if (Objects.nonNull(((DBNetTransition) getTransition().getTransition()).getPerformanceAnalysisInfo())) {
            executables.add(new PerformanceAnalysisExecutable(
                    ((DBNetTransitionInstance) getTransition()),
                    copier.makeCopy(mapper),
                    stateRecorder,
                    connectionInstance
            ));
        }

        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();

        if (Objects.isNull(actionCall)) {
            return executables;
        }

        executables.add(new ActionCallExecutable(
                actionCall,
                ((DBNetTransitionInstance) getTransition()),
                copier.makeCopy(mapper),
                connectionInstance
        ));

        return executables;
    }

    /**
     * Makes the occurrence description.
     * Copied from the {@link super#makeOccurrenceDescription(VariableMapperCopier)} implementation.
     *
     * @param copier The variable mappers' copier.
     * @return The occurrence description.
     */
    @Override
    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new TransitionOccurrenceDescription(getTransition(),
                copier.makeCopy(mapper));
    }

    /**
     * Returns the db-net's transition's occurrence's string representation.
     *
     * @return The db-net's transition's occurrence's string representation.
     */
    @Override
    public String toString() {
        return getTransition().toString();
    }
}
