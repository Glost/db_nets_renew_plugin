package de.renew.net.inscription;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


class CreationOccurrence extends AbstractOccurrence {
    Variable variable;
    Net net;
    NetInstance newNetInstance;

    public CreationOccurrence(Variable variable, Net net,
                              TransitionInstance tInstance) {
        super(tInstance);
        this.variable = variable;
        this.net = net;
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        // Create the new net instance.
        newNetInstance = net.makeInstance();


        // Assign it to the variable.
        Unify.unify(variable, newNetInstance, searcher.recorder);

        // Nothing more to be done.
        return Collections.emptySet();
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        return Arrays.asList(new Executable[] { new EarlyConfirmer(newNetInstance), new LateConfirmer(newNetInstance) });
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}