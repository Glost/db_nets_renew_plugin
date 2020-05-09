package de.renew.dbnets.shadow;

import de.renew.expression.Expression;
import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.DBNetTransition;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.ViewPlace;
import de.renew.net.arc.Arc;
import de.renew.net.arc.ReadArc;

public class ReadArcFactory extends SimpleArcFactory {

    public ReadArcFactory() {
        super(Arc.in, false);
    }

    @Override
    protected ReadArc getArc(Place place, Transition transition, int arcType, Expression expr, Expression timeExpr) {
        if (!(place instanceof ViewPlace && transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException();
        }

        return new ReadArc((ViewPlace) place, (DBNetTransition) transition, expr, timeExpr);
    }
}
