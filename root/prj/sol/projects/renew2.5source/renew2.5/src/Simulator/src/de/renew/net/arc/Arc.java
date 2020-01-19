package de.renew.net.arc;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


/**
 * An <code>Arc</code> instance represents an arc from or to a place.
 * <p>
 * Arcs can be of different types distinguished by the public
 * class constants defined in this class.
 * The arc types that are annotated with an asterisk require a
 * time expression.
 * </p>
 **/
public class Arc implements TransitionInscription {

    /** Arc type: input arc (*). **/
    public static final int in = -1;

    /** Arc type: test arc. **/
    public static final int test = 0;

    /** Arc type: output arc (*). **/
    public static final int out = 1;

    /**
     * Arc type: double arc (*) that reserves the token and
     * applies the time to the input token.
     **/
    public static final int both = 2;

    /**
     * Arc type: double arc (*) that releases the token early
     * during the firing.
     **/
    public static final int fastBoth = 3;

    /**
     * Arc type: test arc that releases the token early during
     * the firing.
     **/
    public static final int fastTest = 4;

    /** Arc type: inhibitor arc. **/
    public static final int inhibitor = 5;

    /**
     * Arc type: double arc (*) that reserves the token and
     * applies the time to the output token.
     **/
    public static final int bothOT = 6;
    Place place;
    Transition transition;
    int arcType;
    Expression tokenExpr;
    Expression timeExpr;
    boolean trace;

    public Arc(Place place, Transition transition, int arcType,
               Expression tokenExpr, Expression timeExpr) {
        if (arcType < in || arcType > bothOT) {
            throw new RuntimeException("Bad arc type: " + arcType + ".");
        }
        this.place = place;
        this.transition = transition;
        this.arcType = arcType;
        this.tokenExpr = tokenExpr;
        this.timeExpr = timeExpr;
        trace = true;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean getTrace() {
        return trace;
    }

    public boolean isTestArc() {
        return arcType == test || arcType == fastTest;
    }

    public boolean isUntimedArc() {
        return isTestArc() || arcType == inhibitor;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new ArcOccurrence(this, mapper, netInstance));
        return coll;
    }
}