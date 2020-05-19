package de.renew.net.arc;

import de.renew.dbnets.binder.ReadArcBinder;
import de.renew.dbnets.executable.ReadArcQueryExecutable;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.ViewPlaceInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.UnifyUtils;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ReadArcOccurence extends ArcOccurrence {

    private ReadArcBinder binder;

    private StateRecorder stateRecorder;

    public ReadArcOccurence(ReadArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = new Variable(0, null);
    }

    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        if (Objects.nonNull(binder)) {
            return Collections.singleton(binder);
        }

        Object evaluated = arc.tokenExpr.startEvaluation(mapper,
                searcher.recorder,
                searcher.calcChecker);

        if (UnifyUtils.isInstanceOfUnknown(evaluated)) {
            tokenVar = new Variable(evaluated, searcher.recorder);
        } else {
            tokenVar = new Variable();
        }

        stateRecorder = searcher.recorder;

        binder = new ReadArcBinder(tokenVar, delayVar, placeInstance);

        return Collections.singleton(binder);
    }

    // TODO: check downcast.
    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        return Collections.singleton(
                new ReadArcQueryExecutable((ViewPlaceInstance) placeInstance, tokenVar, stateRecorder)
        );
    }
}
