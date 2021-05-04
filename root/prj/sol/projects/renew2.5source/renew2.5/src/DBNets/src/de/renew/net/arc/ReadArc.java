package de.renew.net.arc;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.DBNetTransition;
import de.renew.net.NetInstance;
import de.renew.net.ViewPlace;

import java.util.Collection;
import java.util.Collections;

/**
 * The db-net's control layer's read arc.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ReadArc extends Arc {

    /**
     * The db-net's control layer's read arc's constructor.
     *
     * @param place The view place which is one of the ends of the read arc.
     * @param transition The transition which is one of the ends of the read arc.
     * @param tokenExpr The read arc token inscription expression.
     * @param timeExpr The read arc time expression.
     */
    public ReadArc(ViewPlace place,
                   DBNetTransition transition,
                   Expression tokenExpr,
                   Expression timeExpr) {
        super(place, transition, Arc.in, tokenExpr, timeExpr);
    }

    /**
     * Returns that the read arc is the untimed arc.
     *
     * @return true.
     */
    @Override
    public boolean isUntimedArc() {
        return true;
    }

    /**
     * Makes the read arc's occurrence.
     *
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     * @param searcher The searcher instance.
     * @return The singleton set with the read arc's occurrence.
     */
    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            throw new IllegalArgumentException();
        }

        return Collections.singleton(new ReadArcOccurrence(this, mapper, (DBNetControlLayerInstance) netInstance));
    }
}
