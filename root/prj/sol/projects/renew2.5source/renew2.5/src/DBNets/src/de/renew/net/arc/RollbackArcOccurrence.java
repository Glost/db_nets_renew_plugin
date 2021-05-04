package de.renew.net.arc;

import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Copier;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;

/**
 * The rollback arc's occurrence for the concrete transition firing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class RollbackArcOccurrence extends ArcOccurrence {

    /**
     * The rollback arc's occurrence's constructor.
     *
     * @param arc The rollback arc.
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     */
    public RollbackArcOccurrence(RollbackArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = theNullTimeVar;
    }

    /**
     * Makes the rollback arc's executable which moves the token through the rollback arc.
     *
     * @param variableMapperCopier The variable mappers' copier.
     * @return The singleton set with the rollback arc's executable.
     */
    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        Copier copier = variableMapperCopier.getCopier();
        Variable copiedToken = (Variable) copier.copy(tokenVar);
        Variable copiedDelay = (Variable) copier.copy(delayVar);

        return Collections.singleton(new RollbackArcExecutable(
                placeInstance,
                (DBNetTransitionInstance) getTransition(),
                copiedToken,
                copiedDelay,
                arc.getTrace()
        ));
    }
}
