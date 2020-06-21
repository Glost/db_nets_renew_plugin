package de.renew.net.arc;

import de.renew.dbnets.binder.ReadArcBinder;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * The read arc's occurrence for the concrete transition firing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ReadArcOccurrence extends ArcOccurrence {

    /**
     * The read arc's binder.
     * Binds the values of the variables retrieved from the persistence layer through the view place's query.
     */
    private ReadArcBinder binder;

    /**
     * The read arc's occurrence's constructor.
     *
     * @param arc The read arc.
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     */
    public ReadArcOccurrence(ReadArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = theNullTimeVar;
    }

    /**
     * Makes the read arc's binder.
     *
     * @param searcher The searcher instance.
     * @return The singleton set with the read arc's binder.
     * @throws Impossible If the error occurred during the token expression evaluation.
     */
    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        if (Objects.nonNull(binder)) {
            return Collections.singleton(binder);
        }

        Object evaluated = arc.tokenExpr.startEvaluation(
                mapper,
                searcher.recorder,
                searcher.calcChecker
        );

        tokenVar = new Variable(evaluated, searcher.recorder);

        Connection connection = ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnection();
        binder = new ReadArcBinder(
                tokenVar,
                delayVar,
                placeInstance,
                mapper,
                searcher.recorder,
                connection
        );

        return Collections.singleton(binder);
    }

    /**
     * Returns the empty set of the read arc's executables since we do not need any of them.
     *
     * @param variableMapperCopier The variable mappers' copier.
     * @return The empty set of the read arc's executables.
     */
    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        return Collections.emptySet();
    }
}
