package de.renew.net.arc;

import de.renew.expression.Expression;
import de.renew.net.DBNetTransition;
import de.renew.net.Place;

public class RollbackArc extends Arc {

    public RollbackArc(Place place,
                       DBNetTransition transition,
                       Expression tokenExpr,
                       Expression timeExpr) {
        super(place, transition, Arc.out, tokenExpr, timeExpr);
    }
}