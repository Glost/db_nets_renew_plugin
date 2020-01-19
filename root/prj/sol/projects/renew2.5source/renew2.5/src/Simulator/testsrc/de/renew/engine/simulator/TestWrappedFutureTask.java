/**
 *
 */
package de.renew.engine.simulator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import de.renew.util.Semaphor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author Felix Ortmann
 *
 * TestCase for the {@link WrappedFutureTask}.
 *
 */
public class TestWrappedFutureTask {
    private BlockingSimulationCallable<Object> _sampleBlock1;
    private BlockingSimulationCallable<String> _sampleBlock2;
    private BlockingSimulationCallable<Object> _sampleBlock3;
    private Semaphor _lock;
    private Callable<Object> _sampleCallable1;
    private Callable<String> _sampleCallable2;
    private Callable<Object> _sampleCallable3;
    private Object _returnVal1;
    private String _returnVal2;
    private WrappedFutureTask<Object> _underTest1;
    private WrappedFutureTask<String> _underTest2;
    private WrappedFutureTask<Object> _underTest3;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        _returnVal1 = new Object();
        _returnVal2 = "some String to be returned...";

        _sampleCallable1 = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //we do something and then return what we computed
                    return _returnVal1;
                }
            };

        _sampleCallable2 = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    //we do something and then return what we computed
                    return _returnVal2;
                }
            };

        _sampleCallable3 = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //we do something and then return what we computed
                    //here we wait a bit ;)
                    Thread.sleep(2000); //2 seconds
                    return _returnVal2; //strings are also objects
                }
            };

        _lock = new Semaphor();

        _sampleBlock1 = new BlockingSimulationCallable<Object>(_sampleCallable1,
                                                               _lock,
                                                               Thread
                            .currentThread());
        _sampleBlock2 = new BlockingSimulationCallable<String>(_sampleCallable2,
                                                               _lock,
                                                               Thread
                            .currentThread());
        _sampleBlock3 = new BlockingSimulationCallable<Object>(_sampleCallable3,
                                                               _lock,
                                                               Thread
                            .currentThread());

        _underTest1 = new WrappedFutureTask<Object>(_sampleBlock1);
        _underTest2 = new WrappedFutureTask<String>(_sampleBlock2);
        _underTest3 = new WrappedFutureTask<Object>(_sampleBlock3);

    }


    /**
     * Test method for {@link de.renew.engine.simulator.WrappedFutureTask#get()}.
     */
    @Test
    public final void testGetNormal() {
        //first we assume that everything is ok, we just want the returnvalues.
        System.out.println("Execute the test tasks to be able to use the get() method.");
        SimulationThreadPool pool = SimulationThreadPool.getNew();
        pool.execute(_underTest1);
        pool.execute(_underTest2);
        pool.execute(_underTest3);
        try {
            System.out.println("After this line there should no exception be thrown! \nTrying to get the return values.");
            assertEquals(_returnVal1, _underTest1.get());
            assertEquals(_returnVal2, _underTest2.get());
            assertEquals(_returnVal2, _underTest3.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.WrappedFutureTask#get()}.
     */
    @Test
    public final void testGetWithInterrupt() {
        // now we voluntarily enforce an interrupt, to check the deblocking function of
        // get in case of exceptions.
        _sampleCallable1 = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    throw new InterruptedException();
                }
            };

        _sampleCallable2 = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    throw new ExecutionException(null);
                }
            };

        //now we need Semaphor mocks to test if methods were invoked on them
        _lock = mock(Semaphor.class);

        //wrap faulty callables into the blockingCallables
        _sampleBlock1 = new BlockingSimulationCallable<Object>(_sampleBlock1,
                                                               _lock,
                                                               Thread
                            .currentThread());
        _sampleBlock2 = new BlockingSimulationCallable<String>(_sampleBlock2,
                                                               _lock,
                                                               Thread
                            .currentThread());

        //create new WrappedFutureTasks to test their behaviour under exceptions...
        _underTest1 = new WrappedFutureTask<Object>(_sampleBlock1);
        _underTest2 = new WrappedFutureTask<String>(_sampleBlock2);

        System.out.println("Execute the test tasks to be able to use the get() method.");
        SimulationThreadPool pool = SimulationThreadPool.getNew();
        pool.execute(_underTest1);
        pool.execute(_underTest2);
        pool.execute(_underTest3);

        Object ret = null;
        try {
            System.out.println("After this line an exception should be thrown, invisible on the console.");
            ret = _underTest1.get(); //should not set a value
        } catch (InterruptedException e) {
            // when we got here, the wrapedFutureTask Exceptionhandling should have made
            // sure, that the lock is unblocked by now (V() was called). This is what we test for now.
            assertTrue(ret == null);
            verify(_lock, atLeastOnce()).V(); //at least once the semaphor was unlocked.
                                              //this has to be, cause wrappedFutureTask shall unlock in case of exception!


        } catch (ExecutionException e) {
            // when we got here, the wrapedFutureTask Exceptionhandling should have made
            // sure, that the lock is unblocked by now (V() was called). This is what we test for now.
            assertTrue(ret == null);
            verify(_lock, atLeastOnce()).V(); //at least once the semaphor was unlocked.
                                              //this has to be, cause wrappedFutureTask shall unlock in case of exception!


        }

        try {
            System.out.println("After this line an exception should be thrown, invisible on the console.");
            ret = _underTest2.get(); //should not set a value
        } catch (InterruptedException e) {
            // when we got here, the wrapedFutureTask Exceptionhandling should have made
            // sure, that the lock is unblocked by now (V() was called). This is what we test for now.
            assertTrue(ret == null);
            verify(_lock, atLeastOnce()).V(); //at least once the semaphor was unlocked.
                                              //this has to be, cause wrappedFutureTask shall unlock in case of exception!


        } catch (ExecutionException e) {
            // when we got here, the wrapedFutureTask Exceptionhandling should have made
            // sure, that the lock is unblocked by now (V() was called). This is what we test for now.
            assertTrue(ret == null);
            verify(_lock, atLeastOnce()).V(); //at least once the semaphor was unlocked.
                                              //this has to be, cause wrappedFutureTask shall unlock in case of exception!


        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.WrappedFutureTask#get(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testGetLongTimeUnit() {
        //now we wait a given amount of time for the result to be computed.
        //first we assume that everything is ok, we just want the returnvalues.
        System.out.println("Execute the test tasks to be able to use the get() method.");
        SimulationThreadPool pool = SimulationThreadPool.getNew();
        pool.execute(_underTest1);
        pool.execute(_underTest2);
        pool.execute(_underTest3);
        try {
            System.out.println("After this line there should no exception be thrown! \nTrying to get the return values.");
            assertEquals(_returnVal1,
                         _underTest1.get(1000, TimeUnit.MILLISECONDS)); //max. wait a sec.
            assertEquals(_returnVal2,
                         _underTest2.get(1000, TimeUnit.MILLISECONDS)); //max. wait a sec.
            assertEquals(_returnVal2,
                         _underTest3.get(2500, TimeUnit.MILLISECONDS)); //max. wait 2.5 secs.
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link de.renew.engine.simulator.WrappedFutureTask#get(long, java.util.concurrent.TimeUnit)}.
     */
    @Test
    public final void testGetLongTimeUnitWithTimeout() {
        //now we wait a given amount of time for the result to be computed.

        //first setup a mock Semaphore
        _lock = mock(Semaphor.class);

        _sampleBlock3 = new BlockingSimulationCallable<Object>(_sampleCallable3,
                                                               _lock,
                                                               Thread
                            .currentThread());
        _underTest3 = new WrappedFutureTask<Object>(_sampleBlock3);

        System.out.println("Execute the test tasks to be able to use the get() method.");
        SimulationThreadPool pool = SimulationThreadPool.getNew();
        pool.execute(_underTest1);
        pool.execute(_underTest2);
        pool.execute(_underTest3);

        Object ret = null;
        try {
            System.out.println("After this line an exception should be thrown, which is invisible on the console.");
            System.out.println("If actually a visible exception is thrown, the test has failed!");
            //now we wait too short cause this callable needs at least 2 seconds to finish.
            ret = _underTest3.get(1000, TimeUnit.MILLISECONDS); //max. wait a sec.
        } catch (InterruptedException e) {
            e.printStackTrace();
            //if we break here, the test fails - see stacktrace
        } catch (ExecutionException e) {
            e.printStackTrace();
            //if we break here, the test fails - see stacktrace
        } catch (TimeoutException e) {
            assertNull(ret);
            verify(_lock, atLeastOnce()).V(); // since we timed out, we want to verify the 
                                              //exceptionhandling of wrappedFutureTask and test, wether the semaphor is unlocked now.


        }
    }
}