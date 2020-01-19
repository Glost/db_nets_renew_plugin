/*
 * Created on 18.08.2004
 */
package de.renew.engine.common;

import org.apache.log4j.Logger;

import de.renew.engine.events.SimulationEvent;
import de.renew.engine.events.TraceEvent;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;


/**
 * TraceLogger with methods to log the trace output
 * produced by Executables during the simulation of
 * petri nets
 *
 * @author Sven Offermann
 */
public class SimulatorEventLogger {

    /**
     * The prefix used to create names for the loggers
     * to log the simulation output. For example a logger
     * for a net named "untitled" will write the trace messages
     * to the logger named SIM_LOG_PREFIX + ".untitled".
     **/
    public static final String SIM_LOG_PREFIX = "simulation";


    /**
     * static method for debug logging messages produced during the
     * simulation of petri nets
     *
     * @param trace a SimulatorTrace object
     */
    public static void log(SimulatorEvent trace) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        getLogger(trace).debug(trace);
    }

    public static void log(SimulationEvent logObject) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        try {
            log(new SimulatorEvent(logObject));
        } catch (Exception e) {
            // cant create simulator event. 
        }
    }

    public static void log(StepIdentifier stepIdentifier,
                           SimulationEvent logObject) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, logObject));
    }

    public static void log(StepIdentifier stepIdentifier,
                           SimulationEvent logObject, PlaceInstance pInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, logObject, pInstance));
    }

    public static void log(StepIdentifier stepIdentifier,
                           SimulationEvent logObject,
                           TransitionInstance tInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, logObject, tInstance));
    }

    public static void log(StepIdentifier stepIdentifier,
                           SimulationEvent logObject, NetInstance nInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, logObject, nInstance));
    }

    public static void log(String logObject) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        try {
            log(new SimulatorEvent(new TraceEvent(logObject)));
        } catch (Exception e) {
            // cant create simulator event. 
        }
    }

    public static void log(StepIdentifier stepIdentifier, String logObject) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, new TraceEvent(logObject)));
    }

    public static void log(StepIdentifier stepIdentifier, String logObject,
                           PlaceInstance pInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, new TraceEvent(logObject),
                               pInstance));
    }

    public static void log(StepIdentifier stepIdentifier, String logObject,
                           TransitionInstance tInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, new TraceEvent(logObject),
                               tInstance));
    }

    public static void log(StepIdentifier stepIdentifier, String logObject,
                           NetInstance nInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        log(new SimulatorEvent(stepIdentifier, new TraceEvent(logObject),
                               nInstance));
    }


    /**
     * gets the logger for the SimulatorTrace object
     *
     * @param trace a SimulatorTraceObject
     * @return the Logger for the SimulatorTrace object
     */
    private static Logger getLogger(SimulatorEvent trace) {
        // build logger Name
        StringBuffer loggerName = new StringBuffer(SIM_LOG_PREFIX);
        if (trace.getNetInstance() != null) {
            loggerName.append(".");
            loggerName.append(trace.getNetInstance().getNet().getName());

            if (trace.getNetElementInstance() != null) {
                Object element = trace.getNetElementInstance();
                if (element instanceof TransitionInstance) {
                    loggerName.append(".");
                    loggerName.append(((TransitionInstance) element).getTransition()
                                       .toString());
                } else if (element instanceof PlaceInstance) {
                    loggerName.append(".");
                    loggerName.append(((PlaceInstance) element).getPlace()
                                       .toString());
                }
            }
        }

        return Logger.getLogger(loggerName.toString());
    }
}