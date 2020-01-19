package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Putting;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.searchqueue.SearchQueue;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Variable;

import de.renew.util.Value;


class OutputArcExecutable implements LateExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Variable tokenVar;
    Variable timeVar;
    boolean trace;

    OutputArcExecutable(PlaceInstance pInstance, TransitionInstance tInstance,
                        Variable tokenVar, Variable timeVar, boolean trace) {
        this.pInstance = pInstance;
        this.tInstance = tInstance;
        this.tokenVar = tokenVar;
        this.timeVar = timeVar;
        this.trace = trace;
    }

    public int phase() {
        return OUTPUT;
    }

    // We can put a token into an output place quickly.
    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        double time = SearchQueue.getTime();
        if (timeVar != null) {
            time += ((Value) timeVar.getValue()).doubleValue();
        }

        if (trace) {
            // log activities on net level
            SimulatorEventLogger.log(stepIdentifier,
                                     new Putting(tokenVar.getValue(),
                                                 pInstance, time), pInstance);

        }
        pInstance.insertToken(tokenVar.getValue(), time);
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        // Do not output any ordinary tokens.
        // The tokens might not even be available.
    }
}