/**
 *
 */
package de.renew.engine.simulator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


/**
 * @author JackFrost
 *
 */
public class TestInheritableSimulationThreadLock {
    InheritableSimulationThreadLock _underTest;
    SimulationThread _simThread;
    Thread _normalThread;
    Runnable _dummyTask;
    boolean _currThreadIsSim;
    boolean _testOutcome;
    SimulationThreadPool _pool;
    private Thread _externalThread;
    private boolean _testOutcomeExt;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Thread current = Thread.currentThread();

        _externalThread = new Thread();

        _pool = SimulationThreadPool.getNew();

        _testOutcome = false;
        _currThreadIsSim = false;

        _dummyTask = new Runnable() {
                @Override
                public void run() {
                    // here we invoke the method mayLock which we are testing.
                    // By running this runnable from out a simulation thread, we cause
                    // a different behavior in the test method.

                    // if this is true, the maylock-method enters the elseif block
                    _currThreadIsSim = Thread.currentThread() instanceof SimulationThread;

                    // if this is true, the ancestor of the currentThread is the locking one
                    // note that _normalThread is NOT the currentThread anymore, since this
                    // runnable is executed from out a simulation thread
                    _testOutcome = _underTest.mayLock(_normalThread);

                    // if this is false, some other thread than the ancestor of the currentThread
                    // is holding the lock.
                    _testOutcomeExt = _underTest.mayLock(_externalThread);

                    // tip: to verify / understand this behavior, switch on the sysout statements
                    // in the InheritableSimulationLock#mayLock method.
                }
            };

        _simThread = new SimulationThread(current.getThreadGroup(), _dummyTask,
                                          "test", 5);
        _normalThread = current;

        _underTest = new InheritableSimulationThreadLock();
    }

    /**
     * Test method for {@link de.renew.engine.simulator.InheritableSimulationThreadLock#mayLock(java.lang.Thread)}.
     */
    @Test
    public final void testMayLock() {
        //current Thread may always lock (first if case)
        assertTrue(_underTest.mayLock(_normalThread));

        //though it is a simulation thread, it is not the current 
        assertFalse(_underTest.mayLock(_simThread));

        //make a simulationthread invoke the maylock-method
        _pool.executeAndWait(_simThread);

        // if this is true, the maylock-method has entered the elseif block
        assertTrue(_currThreadIsSim);

        //if this is true, we were able to successfully test all if-cases of mayLock.
        assertTrue(_testOutcome);

        // this tests the else-case in mayLock: although the executing thread
        // is a simulation thread, its ancestor is not the owner of the lock,
        // thus we expect false (denoting disallowance to acquire the lock).
        assertFalse(_testOutcomeExt);
    }
}