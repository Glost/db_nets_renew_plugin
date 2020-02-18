package de.renew.dbnets.controllayer;

import de.renew.expression.Expression;
import de.renew.net.Place;
import de.renew.net.arc.Arc;

public class RollbackArc extends Arc {

    public RollbackArc(Place place,
                       DBNetTransition transition,
                       Expression tokenExpr,
                       Expression timeExpr) {
        super(place, transition, Arc.out, tokenExpr, timeExpr);
    }
}
