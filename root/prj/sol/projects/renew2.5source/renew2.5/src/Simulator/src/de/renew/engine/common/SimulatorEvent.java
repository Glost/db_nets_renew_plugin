/*
 * Created on 17.08.2004
 */
package de.renew.engine.common;

import de.renew.application.SimulatorPlugin;

import de.renew.engine.events.SimulationEvent;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;


/**
 * @author Sven Offermann
 */
public class SimulatorEvent {
    private StepIdentifier stepIdentifier = null;
    private SimulationEvent logObject;
    private NetInstance netInstance = null;

    // can be TransitionInstance, PlaceInstance or null
    private Object netElementInstance = null;

    public SimulatorEvent(SimulationEvent traceObject)
            throws Exception {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.logObject = traceObject;
        if (SimulatorPlugin.getCurrent() == null) {
            throw new Exception("No simulator running. Cant create Simulator Event.");
        }
        this.stepIdentifier = SimulatorPlugin.getCurrent()
                                             .getCurrentEnvironment()
                                             .getSimulator()
                                             .currentStepIdentifier();
    }

    public SimulatorEvent(StepIdentifier stepIdentifier,
                          SimulationEvent traceObject) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.stepIdentifier = stepIdentifier;
        this.logObject = traceObject;
    }

    public SimulatorEvent(StepIdentifier stepIdentifier,
                          SimulationEvent traceObject,
                          TransitionInstance transitionInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.stepIdentifier = stepIdentifier;
        this.logObject = traceObject;
        this.netElementInstance = transitionInstance;
        if (transitionInstance != null) {
            this.netInstance = transitionInstance.getNetInstance();
        }
    }

    public SimulatorEvent(StepIdentifier stepIdentifier,
                          SimulationEvent traceObject,
                          PlaceInstance placeInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.stepIdentifier = stepIdentifier;
        this.logObject = traceObject;
        this.netElementInstance = placeInstance;
        if (placeInstance != null) {
            this.netInstance = placeInstance.getNetInstance();
        }
    }

    public SimulatorEvent(StepIdentifier stepIdentifier,
                          SimulationEvent traceObject, NetInstance netInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.stepIdentifier = stepIdentifier;
        this.logObject = traceObject;
        this.netInstance = netInstance;
    }

    public StepIdentifier getStep() {
        return this.stepIdentifier;
    }

    public SimulationEvent getMessage() {
        return this.logObject;
    }

    public String toString() {
        StringBuffer message = new StringBuffer();
        if (this.stepIdentifier != null
                    && stepIdentifier.getComponents().length > 0) {
            message.append(this.stepIdentifier.toString());
        }
        message.append(logObject.toString());
        return message.toString();
    }

    public Object getNetElementInstance() {
        return this.netElementInstance;
    }

    public NetInstance getNetInstance() {
        return this.netInstance;
    }
}