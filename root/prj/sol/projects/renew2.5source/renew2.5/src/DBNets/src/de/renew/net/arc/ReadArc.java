package de.renew.net.arc;

import de.renew.expression.Expression;
import de.renew.net.DBNetTransition;
import de.renew.net.ViewPlace;

public class ReadArc extends Arc {

    public ReadArc(ViewPlace place,
                   DBNetTransition transition,
                   Expression tokenExpr,
                   Expression timeExpr) {
        super(place, transition, Arc.in, tokenExpr, timeExpr);
    }
}
