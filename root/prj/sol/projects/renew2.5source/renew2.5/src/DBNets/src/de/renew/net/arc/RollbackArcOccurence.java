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

public class RollbackArcOccurence extends ArcOccurrence {

    public RollbackArcOccurence(RollbackArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = new Variable(0, null);
    }

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
