package de.renew.dbnets.controllayer;

import de.renew.expression.Expression;
import de.renew.net.arc.Arc;

public class ReadArc extends Arc {

    public ReadArc(ViewPlace place,
                   DBNetTransition transition,
                   Expression tokenExpr,
                   Expression timeExpr) {
        super(place, transition, Arc.in, tokenExpr, timeExpr);
    }
}
