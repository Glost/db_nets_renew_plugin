package de.renew.engine.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.renew.util.Semaphor;

import java.util.concurrent.Callable;


/**
 *
 * @author Felix Ortmann
 *
 * TestCase for the {@link BlockingSimulationCallable}
 *
 */
public class TestBlockingSimulationCallable {
    BlockingSimulationCallable<Object> _underTest1;
    BlockingSimulationCallable<String> _underTest2;
    BlockingSimulationCallable<?> _underTest3;
    SimulationThreadPool _pool;
    Callable<Object> _innerCallable1;
    Callable<String> _innerCallable2;
    Object _retValO;
    String _retValS;
    Semaphor _lock;
    SimulationThread _thread;
    Runnable _run;

    @Before
    public void setUp() throws Exception {
        _retValO = new Object();
        _retValS = "a String to experiment with";

        _pool = SimulationThreadPool.getNew();

        _lock = mock(Semaphor.class);
        _run = new Runnable() {
                @Override
                public void run() {
                    try {
                        assertEquals(_retValO, _underTest1.call());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

        _thread = new SimulationThread(Thread.currentThread().getThreadGroup(),
                                       _run, "testthread", 5);

        _innerCallable1 = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //here we do compute something, what we want to return via the call method
                    return _retValO;
                }
            };

        _underTest1 = new BlockingSimulationCallable<Object>(_innerCallable1,
                                                             _lock, _thread);

        _innerCallable2 = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    //here we do compute something, what we want to return via the call method
                    return _retValS;
                }
            };

        _underTest2 = new BlockingSimulationCallable<String>(_innerCallable2,
                                                             _lock, _thread);

        _underTest3 = new BlockingSimulationCallable<Object>(_innerCallable1,
                                                             _lock, null);

    }

    @Test
    public final void testGetAncestor() {
        assertEquals(_thread, _underTest1.getAncestor());
        assertEquals(_thread, _underTest2.getAncestor());
        assertNull(_underTest3.getAncestor());
    }

    @Test
    public final synchronized void testCall() {
        System.out.println("Trying to get the values by calling the inner callables call() methods. No Exception should be thrown here.");

        //for the first test, everythign is prepared form the setUp()
        _pool.execute(_thread);

        _run = new Runnable() {
                @Override
                public void run() {
                    try {
                        assertEquals(_retValS, _underTest2.call());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


        //prepare for testing the second callable
        _thread = new SimulationThread(Thread.currentThread().getThreadGroup(),
                                       _run, "testthread", 5);

        _pool.execute(_thread);

        _run = new Runnable() {
                @Override
                public void run() {
                    try {
                        assertEquals(_retValO, _underTest3.call());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


        //prepare for testing the second callable
        _thread = new SimulationThread(Thread.currentThread().getThreadGroup(),
                                       _run, "testthread", 5);

        _pool.execute(_thread);
        //even in the case of an exception, the finally part of the callables call method 
        //should have been executed at least three times (this is the abort in the semaphor)

        //since the pool executes the threads concurrently we want it to have enough timt
        try {
            System.out.println("Waiting for SimluationThreadPool to execute.");
            wait(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(_lock, atLeast(3)).V();
    }

    @Test
    public final void testAbort() {
        _thread.setAncestor(Thread.currentThread());
        assertNotNull(_thread.getAncestor());
        _underTest1.abort(_thread);
        verify(_lock, atLeastOnce()).V(); // the abort method should unlock the semaphor
        assertNull(_thread.getAncestor());
    }
}