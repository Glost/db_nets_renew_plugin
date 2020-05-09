package de.renew.dbnets.shadow;

import de.renew.expression.Expression;
import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.DBNetTransition;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;
import de.renew.net.arc.RollbackArc;

public class RollbackArcFactory extends SimpleArcFactory {

    public RollbackArcFactory() {
        super(Arc.out, false);
    }

    @Override
    protected Arc getArc(Place place, Transition transition, int arcType, Expression expr, Expression timeExpr) {
        if (!(transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException();
        }

        return new RollbackArc(place, (DBNetTransition) transition, expr, timeExpr);
    }
}
