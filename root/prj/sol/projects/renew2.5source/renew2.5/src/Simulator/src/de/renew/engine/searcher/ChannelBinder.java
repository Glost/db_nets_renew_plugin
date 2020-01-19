package de.renew.engine.searcher;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class ChannelBinder implements Binder {
    // Try bindings with all the transitions possible
    // for the given net. Usually there will be very few
    // possible transitions.
    private Variable targetVariable;
    private String name;
    private Variable params;

    // If true, indicates that this channel need not be used if
    // the called net has no appropriate channels at all.
    private boolean isOptional;

    public ChannelBinder(Variable targetVariable, String name, Variable params,
                         boolean isOptional) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.targetVariable = targetVariable;
        this.name = name;
        this.params = params;
        this.isOptional = isOptional;
    }

    public int bindingBadness(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Object value = targetVariable.getValue();
        if (value instanceof ChannelTarget) {
            ChannelTarget channelTarget = (ChannelTarget) value;
            Collection<UplinkProvider> uplinkProviders = channelTarget
                                                             .getUplinkProviders(name);
            return BindingBadness.clip(uplinkProviders.size());
        } else {
            return BindingBadness.max;
        }
    }

    public void bind(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        ChannelTarget channelTarget = (ChannelTarget) targetVariable.getValue();
        List<UplinkProvider> providers = new ArrayList<UplinkProvider>(channelTarget
                                                                       .getUplinkProviders(name));
        Collections.shuffle(providers);
        Iterator<UplinkProvider> uplinkProviders = providers.iterator();

        if (isOptional && !uplinkProviders.hasNext()) {
            // This channel is optional and no channel is offered at
            // the invoked net. This is too easy.
            searcher.search();
        } else {
            // Either the channel is not optional, or a suitable
            // channel is provided.
            while (uplinkProviders.hasNext() && !searcher.isCompleted()) {
                UplinkProvider uplinkProvider = uplinkProviders.next();
                uplinkProvider.bindChannel(params, searcher);
            }
        }
    }
}