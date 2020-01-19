package de.renew.shadow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public abstract class ShadowInscribable extends ShadowNetElement {
    static final long serialVersionUID = 1696327514729295533L;

    /**
     * References all inscriptions (text inscriptions, arcs, etc.)
     * of this inscribable shadow net element.
     * <p>
     * This field is not serialized because we have to avoid deep
     * recursion on serialization. (This field would write arcs
     * attached to a node, the arc would write its other adjacent
     * node, the node would write another arc and so on and so on.)
     * Instead, its contents has to be rebuild on deserialization.
     * All inscription net elements have to reregister themselves
     * on deserialization in the same way they do at their creation
     * time.
     * </p>
     **/
    transient Set<ShadowNetElement> elements;

    protected ShadowInscribable(ShadowNet shadowNet) {
        super(shadowNet);
        elements = new HashSet<ShadowNetElement>();
    }

    public void add(ShadowNetElement element) {
        elements.add(element);
    }

    public void remove(ShadowNetElement element) {
        elements.remove(element);
    }

    public Set<ShadowNetElement> elements() {
        return Collections.unmodifiableSet(elements);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method, additionally re-initializes the set of elements.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        elements = new HashSet<ShadowNetElement>();
    }
}