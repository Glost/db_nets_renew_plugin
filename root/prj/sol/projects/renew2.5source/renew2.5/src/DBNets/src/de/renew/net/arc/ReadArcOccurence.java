package de.renew.net.arc;

import de.renew.dbnets.binder.ReadArcBinder;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ReadArcOccurence extends ArcOccurrence {

    private ReadArcBinder binder;

    public ReadArcOccurence(ReadArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = new Variable(0, null);
    }

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
                (DBNetTransitionInstance) getTransition(),
                mapper,
                searcher.recorder,
                connection
        );

        return Collections.singleton(binder);
    }

    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        return Collections.emptySet();
    }
}
