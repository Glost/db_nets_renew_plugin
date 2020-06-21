package de.renew.dbnets.shadow;

import de.renew.expression.Expression;
import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.DBNetTransition;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.ViewPlace;
import de.renew.net.arc.Arc;
import de.renew.net.arc.ReadArc;

/**
 * The factory for the db-net's read arcs' instances creating during the net's parsing and compiling.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ReadArcFactory extends SimpleArcFactory {

    /**
     * The factory's constructor.
     */
    public ReadArcFactory() {
        super(Arc.in, false);
    }

    /**
     * Creates the db-net's read arc's instance.
     *
     * @param place The db-net's view place which is one of the ends of the read arc.
     * @param transition The db-net's transition which is one of the ends of the read arc.
     * @param arcType The read arc type number.
     * @param expr The read arc token inscription expression.
     * @param timeExpr The read arc time expression.
     * @return The db-net's read arc's instance.
     */
    @Override
    protected ReadArc getArc(Place place, Transition transition, int arcType, Expression expr, Expression timeExpr) {
        if (!(place instanceof ViewPlace && transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException();
        }

        return new ReadArc((ViewPlace) place, (DBNetTransition) transition, expr, timeExpr);
    }
}
