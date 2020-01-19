package de.renew.engine.simulator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import de.renew.util.Semaphor;


/**
 * @author Felix Ortmann
 *
 * Testcase for the {@link BlockingSimulationRunnable}.
 *
 */
public class TestBlockingSimulationRunnable {
    Runnable _innerRun1;
    Runnable _innerRun2;
    Runnable _innerRun3;
    BlockingSimulationRunnable _block1;
    BlockingSimulationRunnable _block2;
    BlockingSimulationRunnable _block3;
    Semaphor _lock;
    Thread _thread;

    //since we have no return-statements in the runnables, we will change a String
    String _changeableTestString1;
    String _changeableTestString2;
    String _changeableTestString3;
    SimulationThreadPool _pool;

    /**
     * SetUp for testing the BlockingSimulationRunnable
     * This method is invoked before the execution of each single testmethod
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        _changeableTestString1 = null;
        _changeableTestString2 = null;
        _changeableTestString3 = null;

        _lock = mock(Semaphor.class);
        _thread = Thread.currentThread();

        _pool = SimulationThreadPool.getNew();

        _innerRun1 = new Runnable() {
                @Override
                public void run() {
                    _changeableTestString1 = "1";
                }
            };

        _innerRun2 = new Runnable() {
                @Override
                public void run() {
                    _changeableTestString2 = "2";
                }
            };

        _innerRun3 = new Runnable() {
                @Override
                public void run() {
                    _changeableTestString3 = "3";
                }
            };

        //initialize the blockings under test
        _block1 = new BlockingSimulationRunnable(_innerRun1, _lock, _thread);
        _block2 = new BlockingSimulationRunnable(_innerRun2, _lock, _thread);
        _block3 = new BlockingSimulationRunnable(_innerRun3, _lock, null);

    }


    /**
     * Test method for {@link de.renew.engine.simulator.BlockingSimulationRunnable#getAncestor()}.
     */
    @Test
    public final void testGetAncestor() {
        assertEquals(_thread, _block1.getAncestor());
        assertEquals(_thread, _block2.getAncestor());
        assertNull(_block3.getAncestor());
    }

    /**
     * Test method for {@link de.renew.engine.simulator.BlockingSimulationRunnable#run()}.
     */
    @Test
    public final synchronized void testRun() {
        _pool.execute(_block1);
        _pool.execute(_block2);
        _pool.execute(_block3);

        try {
            System.out.println("Waiting a bit for the Runnable-Execution to finish.");
            wait(1500);
        } catch (InterruptedException e) {
            // we want the pool to have enough time to execute each runnable concurrently
            e.printStackTrace();
        }

        assertEquals("1", _changeableTestString1);
        assertEquals("2", _changeableTestString2);
        assertEquals("3", _changeableTestString3);

        //test the unlocking beavior of the run-methods final statement
        verify(_lock, atLeast(3)).V();
    }

    /**
     * Test method for {@link de.renew.engine.simulator.BlockingSimulationRunnable#abort(de.renew.engine.simulator.SimulationThread)}
     */
    @Test
    public final void testAbort() {
        _block1.abort(null);
        assertNull(_block1.getAncestor());
        verify(_lock, atLeastOnce()).V();

        SimulationThread simThread = new SimulationThread(_thread.getThreadGroup(),
                                                          _innerRun2, "test", 5);

        _block2.abort(simThread);
        assertNull(_block2.getAncestor());
        assertNull(simThread.getAncestor());
        verify(_lock, atLeastOnce()).V();


    }
}