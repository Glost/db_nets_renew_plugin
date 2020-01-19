package de.renew.net.arc;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.Function;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


public class FlexibleArc implements TransitionInscription {
    // Only some of the arc types known from ordinary arcs are implemented
    // for flexible arcs so far.
    public static final int in = Arc.in;
    public static final int out = Arc.out;
    public static final int fastBoth = Arc.fastBoth;
    Place place;
    Transition transition;
    int arcType;

    /**
     * The expression is evaluated and must result in
     * an array, a vector, a list, or (in the case of output arcs)
     * an enumeration.
     *
     * Enumerations are not supported for input arcs, because they
     * can be easily destroyed by reading them once. Because input
     * arcs might have to be checked multiple times, this is unacceptabe.
     *
     * Copying the contents of the enumeration into a temporary
     * vector using an auxillary expression will not work in all cases.
     * Consider the following: One transition has a flexible output arcs
     * inscribed x. x is received through a synchronous channel
     * this:ch(x,y). y is bound to an enumeration in the spontaneous
     * transition. The channel can be satisfied by two transitions,
     * both of which set x=y. Now the first channel is searched,
     * x is let to y and the enumeration is consumed during a
     * copying operation. The search fails, backtracks and tries the
     * second matching channel. Now x is rebound to the enumeration y,
     * but the enumeration is already corrupted and emptied. The search
     * might succeed illegally.
     */
    Expression expression;
    Function forwardFunction;
    Function backwardFunction;
    boolean trace;

    public FlexibleArc(Place place, Transition transition, int arcType,
                       Expression expression, Function forwardFunction,
                       Function backwardFunction) {
        this.place = place;
        this.transition = transition;
        this.arcType = arcType;
        this.expression = expression;
        this.forwardFunction = forwardFunction;
        this.backwardFunction = backwardFunction;
        trace = true;
    }

    boolean isOutputArc() {
        return arcType == out;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean getTrace() {
        return trace;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new FlexibleArcOccurrence(this, mapper, netInstance));
        return coll;
    }
}