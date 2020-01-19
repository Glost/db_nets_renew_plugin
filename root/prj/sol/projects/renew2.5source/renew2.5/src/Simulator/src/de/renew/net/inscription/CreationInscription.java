package de.renew.net.inscription;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;

import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


public class CreationInscription implements TransitionInscription {
    private Net net;
    private LocalVariable createVariable;
    private Transition transition;

    public CreationInscription(Net net, LocalVariable createVariable,
                               Transition transition) {
        this.net = net;
        this.createVariable = createVariable;
        this.transition = transition;
    }

    public LocalVariable getVariable() {
        return createVariable;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new CreationOccurrence(mapper.map(createVariable), net,
                                        netInstance.getInstance(transition)));
        return coll;
    }
}