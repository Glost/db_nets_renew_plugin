/**
 *
 */
package de.renew.engine.simulator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import de.renew.util.Semaphor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


/**
 * @author Felix Ortmann
 * Test for the {@link SimulationThreadPool}, especially the freshly changed
 * methods submit and submitAndWait. Both this methods use {@link BlockingSimulationCallable}s
 * as well as {@link WrappedFutureTask}s.
 *
 */
public class TestSimulationThreadPool {
    Object _returnVal1;
    Callable<Object> _sampleCallable;
    Callable<String> _sampleCallableWithWait;
    WrappedFutureTask<Object> _sampleFTask;
    BlockingSimulationCallable<Object> _sampleBlock;
    Runnable _sampleRun;
    String _checkvalue;
    SimulationThreadPool _pool;

    /**
     * @throws java.lang.Exception
     * Sets up the test callables before each single test.
     */
    @Before
    public void setUp() throws Exception {
        _returnVal1 = new Object();
        _checkvalue = "";

        _pool = SimulationThreadPool.getNew();

        _sampleCallable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // when the call mehtod is invoked, we want something to be computed here
                    // which is kept simple for this test
                    return _returnVal1;
                }
            };

        _sampleCallableWithWait = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    // when the call mehtod is invoked, we want something to be computed here
                    // which is kept simple for this test
                    String s = "this is some string";
                    wait(5); //we wait a sec.
                    return s;
                }
            };

        // Blocks the thread in which this test is executed. Calls _sampleCallable
        _sampleBlock = new BlockingSimulationCallable<Object>(_sampleCallable,
                                                              new Semaphor(),
                                                              Thread
                           .currentThread());

        _sampleFTask = new WrappedFutureTask<Object>(_sampleBlock);

        // we needed to define an extra String (or any other datatype) to verify executive 
        // behaviuor within an runnable.
        _sampleRun = new Runnable() {
                @Override
                public void run() {
                    _checkvalue = "checked";
                }
            };
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#submitAndWait(java.util.concurrent.Callable)}.
     */
    @Test
    public final void testSubmitAndWaitNormal() {
        FutureTask<Object> compareTask = new FutureTask<Object>(_sampleCallable);
        _pool.execute(compareTask);
        System.out.println("submitAndWait setup with normal Callable successfull");
        try {
            System.out.println("Wait complete, compare results.");
            assertEquals(compareTask.get(),
                         _pool.submitAndWait(_sampleCallable).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#submitAndWait(java.util.concurrent.Callable)}.
     */
    @Test
    public final void testSubmitAndWaitBlockingSimulationCallable() {
        FutureTask<Object> compareTask = new FutureTask<Object>(_sampleBlock);
        _pool.execute(compareTask);
        System.out.println("submitAndWait setup with BlockingSimulationCallable successfull");
        try {
            System.out.println("Wait complete, compare results.");
            assertEquals(compareTask.get(),
                         _pool.submitAndWait(_sampleBlock).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#submit(java.util.concurrent.Callable)}.
     */
    @Test
    public final void testSubmitNormal() {
        // submit a normal Callable
        FutureTask<Object> compareTask = new FutureTask<Object>(_sampleCallable);
        _pool.execute(compareTask);
        System.out.println("submit setup with normal Callable successfull");
        try {
            System.out.println("try to compare submit result with expected.");
            assertEquals(compareTask.get(), _pool.submit(_sampleCallable).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#submit(java.util.concurrent.Callable)}.
     */
    @Test
    public final void testSubmitBlockingSimulationCallable() {
        //submit with BlockingSimulationCallable
        FutureTask<Object> compareTask = new FutureTask<Object>(_sampleBlock);
        _pool.execute(compareTask);
        System.out.println("submit setup with BlockingSimulationCallable successfull");

        try {
            System.out.println("try to compare submit result with expected.");
            assertEquals(compareTask.get(), _pool.submit(_sampleBlock).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#getNew()}.
     */
    @Test
    public final void testGetNew() {
        assertNotSame(SimulationThreadPool.getNew(),
                      SimulationThreadPool.getCurrent());
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#getCurrent()}.
     */
    @Test
    public final void testGetCurrent() {
        // this is quite difficult to test. 
        //TODO An appropriate solution has to be worked out.
        assertEquals(SimulationThreadPool.getCurrent(),
                     SimulationThreadPool.getCurrent());
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#getSimulationThreadPool()}.
     */
    @Test
    public final void testGetSimulationThreadPool() {
        assertEquals(SimulationThreadPool.getCurrent(),
                     SimulationThreadPool.getSimulationThreadPool());
    }


    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#isMyThread()}.
     */
    @Test
    public final void testIsMyThread() {
        // Test only negative case, since this JUnit thread is not a simulation thread.
        // cannot execute this test within a simulation thread.
        assertFalse(_pool.isMyThread());
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#isMyThread(java.lang.Thread)}.
     */
    @Test
    public final void testIsMyThreadThread() {
        // Test only negative case, since this JUnit thread is not a simulation thread.
        // cannot execute this test within a simulation thread.
        assertFalse(_pool.isMyThread(Thread.currentThread()));
    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#cleanup()}.
     */
    @Test
    public final void testCleanup() {
        // difficult to test. method returns always true (why not void?) 
        SimulationThreadPool oldPool = SimulationThreadPool.getCurrent();
        assertTrue(SimulationThreadPool.cleanup());
        assertNotSame(oldPool, SimulationThreadPool.getCurrent());
    }

    /*
     * NON JAVA DOC
     *
     * FIXME
     *
     * Unable to test discardNew()
     *
     * Unable to test isSimulationThread()
     *
     * Unclear how to test ExecuteAndWait() since no return value exists.
     *
     */


    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#executeAndWait(Runnable)}.
     */
    @Test
    public final synchronized void testExecuteAndWait() {
        _checkvalue = ""; //just to be sure.

        // test case 1: invoke the method from within an simulationthread.
        Runnable invoker = new Runnable() {
            @Override
            public void run() {
                // we call SimulationThreadPool#executeAndWait from within this runnable,
                // which will be executed from inside a simulationThread.
                SimulationThreadPool.getNew().executeAndWait(_sampleRun);
            }
        };

        SimulationThread simThread = new SimulationThread(_pool.getThreadFactory()
                                                               .newThread(_sampleRun)
                                                               .getThreadGroup(),
                                                          invoker,
                                                          "SimulationThread", 5);
        _pool.execute(simThread);
        // now we have concurrent behaviour: the pool executes another thread, wich waites for the execution of
        // sampleRun - meanwhile the junit controlflow carries on. So if we compare the computational result now,
        // it is unclear which result we get, cause the above code may not have already been executed. 
        // Thus we wait a little (no perfect solution!)
        try {
            System.out.println("Waiting 5 sec. for a concurrent thread to finish.");
            wait(3000); // 3 sec. should be enough.
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals("checked", _checkvalue);

        _checkvalue = ""; //clear the val before we start the next test.


        //test case 2: the calling thread is different from the simulationthreadgroup 
        // (the calling thread in fact is the junit thread)
        // we know, that internally a blockingSimulationRunnable will be build, so we enforce this testcase.
        _pool.executeAndWait(_sampleRun);
        // we do not need to check any threadblock-behaviour - this is the wait. If the wait implementation
        // would be faulty, the assertstatement would simply fail (sometimes) due to concurrency.
        assertEquals("checked", _checkvalue);

        _checkvalue = ""; // even though its done in setUp, clear the value.

    }


    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#setMaxPriority(int)}.
     */
    @Test
    public final void testSetMaxPriority() {
        //test all accepted values (from 1 to 10)
        for (int i = 1; i < 11; ++i) {
            _pool.setMaxPriority(i);
            assertEquals(i, _pool.getMaxPriority());
        }
        //test the outer extrema
        _pool.setMaxPriority(Integer.MAX_VALUE);
        assertEquals(10, _pool.getMaxPriority()); //should have no effect
        _pool.setMaxPriority(Integer.MIN_VALUE);
        assertEquals(10, _pool.getMaxPriority()); //should have no effect

    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#getMaxPriority()}.
     */
    @Test
    public final void testGetMaxPriority() {
        //known default Priorities are 1, 5, 10. These should be returned
        assertNotSame(_pool.getMaxPriority(), null);
        _pool.setMaxPriority(1);
        assertEquals(1, _pool.getMaxPriority());
        _pool.setMaxPriority(5);
        assertEquals(5, _pool.getMaxPriority());
        _pool.setMaxPriority(10);
        assertEquals(10, _pool.getMaxPriority());

        //higher / lower values should be ignored
        _pool.setMaxPriority(-1);
        assertEquals(10, _pool.getMaxPriority());
        _pool.setMaxPriority(11);
        assertEquals(10, _pool.getMaxPriority());

    }

    /**
     * Test method for {@link de.renew.engine.simulator.SimulationThreadPool#beforeExecute(java.lang.Thread, java.lang.Runnable)}.
     */
    @Test
    public final void testBeforeExecuteThreadRunnable() {
        // test with a WrappedFutureTask as runnable
        SimulationThread compareThread = new SimulationThread(_pool.getThreadFactory()
                                                                   .newThread(_sampleFTask)
                                                                   .getThreadGroup(),
                                                              _sampleFTask,
                                                              "TestThread", 10);
        assertNull(compareThread.getAncestor()); //no one is expected

        _pool.beforeExecute(compareThread, _sampleFTask);
        // now compareThread should have an ancestor
        assertNotNull(compareThread.getAncestor());

        //do it again with an BlockingSimulationRunnable
        compareThread = new SimulationThread(_pool.getThreadFactory()
                                                  .newThread(_sampleFTask)
                                                  .getThreadGroup(),
                                             _sampleFTask, "TestThread", 10);
        assertNull(compareThread.getAncestor()); //no one is expected

        BlockingSimulationRunnable run = new BlockingSimulationRunnable(new Runnable() {
                @Override
                public void run() {
                    //just dont anything
                }
            }, new Semaphor(), Thread.currentThread()); // the JUnit Thread is ancestor of "run"

        _pool.beforeExecute(compareThread, run);
        assertEquals(Thread.currentThread(), compareThread.getAncestor());
    }

    /**
     * This test tests that the complementary pair of methods
     * {@link SimulationThreadPool#beforeExecute(Thread, Runnable)} and
     * {@link BlockingSimulationCallable#abort(SimulationThread)} form a complete
     * lifecycle with setting and deleting the thread ancestor relation.
     *
     * This method is no direct testcase for the {@link SimulationThreadPool}-method
     * but it is a joined-functionality test.
     */
    @Test
    public final void ancestorLifecycleTest() {
        SimulationThread compareThread = new SimulationThread(_pool.getThreadFactory()
                                                                   .newThread(_sampleFTask)
                                                                   .getThreadGroup(),
                                                              _sampleFTask,
                                                              "TestThread", 10);
        assertNull(compareThread.getAncestor());
        _pool.beforeExecute(compareThread, _sampleFTask); //sets an ancestor
        assertNotNull(compareThread.getAncestor());

        _sampleBlock.abort(compareThread); //delete the ancestor
        assertNull(compareThread.getAncestor());
    }
}