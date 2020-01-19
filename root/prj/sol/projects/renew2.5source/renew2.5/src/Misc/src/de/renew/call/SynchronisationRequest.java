package de.renew.call;

import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.ChannelBinder;
import de.renew.engine.searcher.ChannelTarget;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.TriggerCollection;
import de.renew.engine.searcher.Triggerable;
import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;
import de.renew.unify.Variable;

import de.renew.util.Lock;
import de.renew.util.Semaphor;


public class SynchronisationRequest implements Searchable, Triggerable {
    private ChannelTarget channelTarget;
    private String channelName;
    Tuple parameters;
    Tuple resultParameters;
    Semaphor resultSemaphor;
    boolean completed;
    final long lockOrder;
    final Lock lock;
    private TriggerCollection triggers = new TriggerCollection(this);

    public SynchronisationRequest(ChannelTarget channelTarget,
                                  String channelName, Tuple parameters) {
        this.channelTarget = channelTarget;
        this.channelName = channelName;

        this.parameters = parameters;
        resultParameters = null;
        resultSemaphor = new Semaphor();

        completed = false;
        lockOrder = de.renew.util.Orderer.getTicket();
        lock = new Lock();
    }

    // This is a convenience wrapper that allows the most simple call
    // to issue a synchronisation request.
    public static Tuple synchronize(ChannelTarget channelTarget,
                                    String channelName, Tuple parameters) {
        SynchronisationRequest request = new SynchronisationRequest(channelTarget,
                                                                    channelName,
                                                                    parameters);
        request.proposeSearch();
        return request.getResult();
    }

    public TriggerCollection triggers() {
        return triggers;
    }

    public void proposeSearch() {
        SearchQueue.includeNow(this);
    }

    public Tuple getResult() {
        resultSemaphor.P();
        return resultParameters;
    }

    // I assume that I won't be searched concurrently multiple times.
    // Is that safe?
    public synchronized void startSearch(final Searcher searcher) {
        final SynchronisationRequest object = this;
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    // Has my synchronisation been done already?
                    if (completed) {
                        // Yes, no need to keep me in the search queue.
                        return;
                    }

                    // Require completeness of the parameters.
                    Variable paramVariable = new Variable(parameters,
                                                          searcher.recorder);
                    try {
                        searcher.calcChecker.addLateVariable(paramVariable,
                                                             searcher.recorder);
                    } catch (Impossible e) { //NOTICEthrows
                        throw new RuntimeException("Calculation checker refused to make "
                                                   + "fresh variable late.");
                    }


                    // We want to find a transition within the specified
                    // channel target that can fire.
                    Variable targetVariable = new Variable(channelTarget,
                                                           searcher.recorder);


                    // Create a binder that tries to find an appropriate channel.
                    // This synchronisation is not optional.
                    Binder initialBinder = new ChannelBinder(targetVariable,
                                                             channelName,
                                                             paramVariable,
                                                             false);
                    searcher.addBinder(initialBinder);


                    // Create an occurrence that will inhibit multiple executions
                    // of this request and that will notify me upon execution.
                    Occurrence occurrence = new SynchronisationOccurrence(object);
                    searcher.addOccurrence(occurrence);


                    // Start the search.
                    searcher.search();


                    // Undo changes to searcher state.
                    searcher.removeOccurrence(occurrence);
                    searcher.removeBinder(initialBinder);
                    searcher.recorder.restore();
                }
            });
    }
}