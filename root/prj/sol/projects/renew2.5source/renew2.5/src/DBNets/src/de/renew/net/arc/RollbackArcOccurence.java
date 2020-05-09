package de.renew.net.arc;

import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.unify.Variable;

public class RollbackArcOccurence extends ArcOccurrence {

    public RollbackArcOccurence(RollbackArc arc, VariableMapper mapper, DBNetControlLayerInstance netInstance) {
        super(arc, mapper, netInstance);
        delayVar = new Variable(0, null);
    }
}
