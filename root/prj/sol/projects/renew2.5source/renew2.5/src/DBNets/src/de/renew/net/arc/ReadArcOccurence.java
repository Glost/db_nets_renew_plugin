package de.renew.net.arc;

import de.renew.dbnets.binder.ReadArcBinder;
import de.renew.dbnets.executable.ReadArcQueryExecutable;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;

public class ReadArcOccurence extends ArcOccurrence {

    private StateRecorder stateRecorder;

    public ReadArcOccurence(ReadArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = new Variable(0, null);
    }

    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        tokenVar = new Variable(arc.tokenExpr.startEvaluation(mapper,
            searcher.recorder,
            searcher.calcChecker),
            searcher.recorder);

        stateRecorder = searcher.recorder;

        return Collections.singleton(new ReadArcBinder(tokenVar, delayVar, placeInstance));
    }

    // TODO: ...
//    @Override
//    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
//        return Collections.singleton(new ReadArcQueryExecutable(placeInstance, tokenVar, stateRecorder));
//    }
}
