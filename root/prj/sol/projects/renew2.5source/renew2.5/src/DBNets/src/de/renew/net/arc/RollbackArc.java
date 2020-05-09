package de.renew.net.arc;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.DBNetTransition;
import de.renew.net.NetInstance;
import de.renew.net.Place;

import java.util.Collection;
import java.util.Collections;

public class RollbackArc extends Arc {

    public RollbackArc(Place place,
                       DBNetTransition transition,
                       Expression tokenExpr,
                       Expression timeExpr) {
        super(place, transition, Arc.out, tokenExpr, timeExpr);
    }

    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            throw new IllegalArgumentException();
        }

        return Collections.singleton(new RollbackArcOccurence(this, mapper, (DBNetControlLayerInstance) netInstance));
    }
}
