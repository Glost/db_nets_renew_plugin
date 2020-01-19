package de.renew.application;

import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.plugin.PluginProperties;

import de.renew.util.Semaphor;


public class TestSimulatorPluginRaceConditions {
    SimulatorPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new SimulatorPlugin(PluginProperties.getUserProperties());
        System.out.println("Set up");
    }

    @After
    public void tearDown() throws Exception {
        plugin.cleanup();
        plugin = null;
    }

    /**
     * This scenario is only possible because the test implementation
     * does not conform to the lock-contract of the simulator plug-in.
     */
    @Test
    public void testSetupRaceCondition() {
        StarterThread starterA = new StarterThread("starterA");
        SimulationThreadPool.getNew().execute(starterA);
        starterA.initialized.P();

        StarterThread starterC = new StarterThread("starterC");
        SimulationThreadPool.getNew().execute(starterC);
        starterC.initialized.P();

        starterA.setup.V();
        starterA.setupReady.P();
        assertTrue(SimulationThreadPool.getCurrent().isMyThread(starterA.thread));
        starterA.setup.V();

        StopperThread stopperB = new StopperThread("stopperB");
        SimulationThreadPool.getCurrent().execute(stopperB);
        stopperB.initialized.P();
        assertTrue(SimulationThreadPool.getCurrent().isMyThread(stopperB.thread));
        stopperB.stop.V();
        stopperB.stopReady.P();

        //StarterThread starterC = new StarterThread("starterC");
        //SimulationThreadPool.getNew().execute(starterC);
        //starterC.initialized.P();
        starterC.setup.V();
        starterC.setupReady.P();
        assertFalse(SimulationThreadPool.getCurrent().isMyThread(starterC.thread));
        starterC.setup.V();
    }

    @Ignore
    private class StarterThread implements Runnable {
        public final Semaphor initialized = new Semaphor();
        public final Semaphor setup = new Semaphor();
        public final Semaphor setupReady = new Semaphor();
        public Thread thread;
        private String name;

        public StarterThread(String name) {
            this.name = name;
        }

        public void run() {
            thread = Thread.currentThread();
            initialized.V();
            System.out.println(name + " initialized");
            setup.P();
            plugin.setupSimulation(null);
            setupReady.V();
            setup.P();
            System.out.println(name + " finished");
        }
    }

    @Ignore
    private class StopperThread implements Runnable {
        public final Semaphor initialized = new Semaphor();
        public final Semaphor stop = new Semaphor();
        public final Semaphor stopReady = new Semaphor();
        public Thread thread;
        private String name;

        public StopperThread(String name) {
            this.name = name;
        }

        public void run() {
            thread = Thread.currentThread();
            initialized.V();
            System.out.println(name + " initialized");
            stop.P();
            plugin.terminateSimulation();
            stopReady.V();
            System.out.println(name + " finished");
        }
    }
}