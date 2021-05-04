package de.renew.dbnets.shadow;

import de.renew.expression.Expression;
import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.DBNetTransition;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;
import de.renew.net.arc.RollbackArc;

/**
 * The factory for the db-net's rollback arcs' instances creating during the net's parsing and compiling.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class RollbackArcFactory extends SimpleArcFactory {

    /**
     * The factory's constructor.
     */
    public RollbackArcFactory() {
        super(Arc.out, false);
    }

    /**
     * Creates the db-net's rollback arc's instance.
     *
     * @param place The net's place which is one of the ends of the rollback arc.
     * @param transition The db-net's transition which is one of the ends of the rollback arc.
     * @param arcType The rollback arc type number.
     * @param expr The rollback arc token inscription expression.
     * @param timeExpr The rollback arc time expression.
     * @return The db-net's rollback arc's instance.
     */
    @Override
    protected RollbackArc getArc(Place place, Transition transition, int arcType, Expression expr, Expression timeExpr) {
        if (!(transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException();
        }

        return new RollbackArc(place, (DBNetTransition) transition, expr, timeExpr);
    }
}
