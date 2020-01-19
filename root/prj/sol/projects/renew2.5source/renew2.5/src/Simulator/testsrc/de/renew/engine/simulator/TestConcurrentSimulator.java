package de.renew.engine.simulator;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import de.renew.engine.simulator.SimulationThreadPool;


/**
 * Tests for deadlock bug #3616.
 *
 * @author Michael Simon
 */
public class TestConcurrentSimulator {
    boolean finished = false;
    ConcurrentSimulator simulator;
    Thread simulatorThread;

    @Before
    public void setUp() throws Exception {
        simulator = new ConcurrentSimulator(true);
        simulator.step();
        System.out.println("Set up");
    }

    @After
    public void tearDown() throws Exception {
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRunTerminateRaceCondition() throws InterruptedException {
        // Find the simulator thread.
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                @Override
                public void run() {
                    Thread[] list = new Thread[1];

                    // Find thread group of the simulator thread.
                    ThreadGroup group = Thread.currentThread().getThreadGroup();

                    // Find the simulator thread.
                    group.enumerate(list);
                    simulatorThread = list[0];
                }
            });

        // Suspend the simulator thread.
        // At this point, the simulator thread is in the AbstractConcurrentSimulator.run() method
        // waiting for desiredMode to change near the end of the outermost while loop.
        simulatorThread.suspend();

        // Issue run request.
        simulator.startRun();
        // Wait for the run request to be processed.
        Thread.sleep(10);

        // Issue termination request in a concurrent thread.
        SimulationThreadPool.getCurrent().execute(new Runnable() {
                @Override
                public void run() {
                    simulator.terminateRun();
                    finished = true;
                }
            });
        // Wait the termination request to reach the point where it is waiting on AbstractConcurrentSimulator.idle.
        Thread.sleep(10);

        // Resume the simulator thread.
        simulatorThread.resume();
        // Wait for the simulator thread to exit the AbstractConcurrentSimulator.run() method.
        Thread.sleep(10);

        // Now both the simulator thread and the termination request should be finished.
        assertTrue(finished);
    }
}