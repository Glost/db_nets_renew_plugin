package de.renew.net.inscription;

import de.renew.engine.common.DownlinkOccurrence;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


public class DownlinkInscription implements TransitionInscription {
    public String name;
    public Expression params;
    public Expression callee;
    boolean isOptional;
    Transition transition;

    public DownlinkInscription(String name, Expression params,
                               Expression callee, boolean isOptional,
                               Transition transition) {
        this.name = name;
        this.params = params;
        this.callee = callee;
        this.isOptional = isOptional;
        this.transition = transition;
    }

    // Refactoring
    public int downlinkBeginLine;
    public int downlinkBeginColumn;
    public int downlinkEndLine;
    public int downlinkEndColumn;
    public int nameBeginLine;
    public int nameBeginColumn;
    public int nameEndColumn;
    public int nameEndLine;

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new DownlinkOccurrence(params, callee, name, isOptional,
                                        mapper,
                                        netInstance.getInstance(transition)));
        return coll;
    }
}