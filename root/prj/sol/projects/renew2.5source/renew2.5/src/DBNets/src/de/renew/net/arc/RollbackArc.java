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

/**
 * The db-net's control layer's rollback arc.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class RollbackArc extends Arc {

    /**
     * The db-net's control layer's rollback arc's constructor.
     *
     * @param place The place which is one of the ends of the rollback arc.
     * @param transition The transition which is one of the ends of the rollback arc.
     * @param tokenExpr The rollback arc token inscription expression.
     * @param timeExpr The rollback arc time expression.
     */
    public RollbackArc(Place place,
                       DBNetTransition transition,
                       Expression tokenExpr,
                       Expression timeExpr) {
        super(place, transition, Arc.out, tokenExpr, timeExpr);
    }

    /**
     * Returns that the rollback arc is the untimed arc.
     *
     * @return true.
     */
    @Override
    public boolean isUntimedArc() {
        return true;
    }

    /**
     * Makes the rollback arc's occurrence.
     *
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     * @param searcher The searcher instance.
     * @return The singleton set with the rollback arc's occurrence.
     */
    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            throw new IllegalArgumentException();
        }

        return Collections.singleton(new RollbackArcOccurrence(this, mapper, (DBNetControlLayerInstance) netInstance));
    }
}
