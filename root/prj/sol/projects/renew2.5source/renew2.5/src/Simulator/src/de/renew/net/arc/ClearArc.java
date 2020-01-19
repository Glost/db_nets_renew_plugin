package de.renew.net.arc;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;

import java.util.Collection;
import java.util.Vector;


public class ClearArc implements TransitionInscription {
    Place place;
    Transition transition;
    Expression expression;
    boolean trace;

    /**
     * This field is not really transient, but as <code>java.lang.Class
     * </code>is not always serializable, we have to store it by
     * ourselves.
     **/
    transient Class<?> elementType;

    public ClearArc(Place place, Transition transition, Expression expression,
                    Class<?> elementType) {
        this.place = place;
        this.transition = transition;
        this.expression = expression;
        this.elementType = elementType;
        trace = true;
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
        coll.add(new ClearArcOccurrence(this, mapper, netInstance));
        return coll;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient elementType field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeClass(out, elementType);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient elementType field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        elementType = ReflectionSerializer.readClass(in);
    }
}