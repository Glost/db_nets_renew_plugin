package de.renew.engine.simulator;

import de.renew.database.TransactionSource;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.TransitionException;
import de.renew.engine.searcher.EarlyExecutable;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.unify.Copier;
import de.renew.unify.Impossible;

import de.renew.util.LockComparator;
import de.renew.util.Semaphor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Binding implements Runnable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Binding.class);

    // Fields for execution.
    private ArrayList<EarlyExecutable> earlyExecutables;
    private ArrayList<LateExecutable> lateExecutables;

    // Fields for viewing.
    private OccurrenceDescription[] occurrenceDescriptions;
    private String description;

    /**
     * The result variable becomes true, if the transition
     * can be successfully executed.
     */
    private boolean result = false;

    /**
     * The semaphor is increased after the enabledness has
     * been finally determined.
     */
    private Semaphor sem = new Semaphor();

    //We got the current stepIdentifiert bevor but we dont need it
    //as we get it during the execution call anyway
    private StepIdentifier stepIdentifier = null;


    /**
     * Create a new binding based on the current state of the
     * given searcher, which should have reported a valid
     * binding to the finder.
     *
     * @param searcher the searcher that found the binding.
     */
    Binding(Searcher searcher) {
        // Get the executables.
        Copier copier = new Copier();
        VariableMapperCopier variableMapperCopier = new VariableMapperCopier(copier);
        Collection<Executable> executables = new ArrayList<Executable>();
        Collection<Occurrence> occurrences = searcher.getOccurrences();
        {
            for (Occurrence occurrence : occurrences) {
                executables.addAll(occurrence.makeExecutables(variableMapperCopier));
            }
        }

        // Sort the executables into early and late.
        earlyExecutables = new ArrayList<EarlyExecutable>();
        lateExecutables = new ArrayList<LateExecutable>();

        {
            for (Executable executable : executables) {
                if (executable instanceof EarlyExecutable) {
                    earlyExecutables.add((EarlyExecutable) executable);
                } else if (executable instanceof LateExecutable) {
                    lateExecutables.add((LateExecutable) executable);
                } else {
                    throw new RuntimeException("Unknown type of executable detected: "
                                               + executable);
                }
            }
        }

        // Prepare the viewable information.
        int n = occurrences.size();
        occurrenceDescriptions = new OccurrenceDescription[n];
        Iterator<Occurrence> occurrencesIterator = occurrences.iterator();
        for (int i = 0; i < n; i++) {
            Occurrence occurrence = occurrencesIterator.next();
            occurrenceDescriptions[i] = occurrence.makeOccurrenceDescription(variableMapperCopier);
        }
    }

    private String makeText() {
        StringBuffer descr = new StringBuffer();
        for (int i = 0; i < occurrenceDescriptions.length; i++) {
            if (i > 0) {
                descr.append("\n");
            }
            descr.append(occurrenceDescriptions[i].getDescription());
        }
        return descr.toString();
    }

    public String getDescription() {
        if (description == null) {
            description = makeText();
        }
        return description;
    }

    /**
     * Execute a given sequence of executables.
     * This is the only valid way of executing a collection of
     * executables. This method must only be called once.
     *
     * @param asynchronous true, if the executable objects are supposed
     *                     to be run in a different thread and the current
     *                     thread is supposed to be continued as soon as
     *                     any action is done by the transition that
     *                     might possibly block the secondary thread
     */
    public synchronized boolean execute(StepIdentifier stepIdentifier,
                                        final boolean asynchronous) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Check for misuse.
        if (earlyExecutables == null) {
            throw new RuntimeException("Cannot execute binding twice.");
        }

        this.stepIdentifier = stepIdentifier;

        // If asynchronous execution is requested, create
        // a different thread to execute the executables,
        // but block the current thread until either the executables
        // are all done or an executable started a long running task
        // like a method call.
        // 
        // If synchronous execution is requested, just run to completion.
        if (asynchronous) {
            SimulationThreadPool.getCurrent().execute(this);

        } else {
            SimulationThreadPool.getCurrent().executeAndWait(this);
        }

        //Log events after they have happened (previously it was before)
        SimulatorEventLogger.log(stepIdentifier,
                                 "-------- Synchronously --------");

        // Wait for the result. (Just to be sure. It should already
        // be accessible.)
        sem.P();
        return result;
    }

    /**
     * The lock method must be called first. It cannot fail,
     * but it might take a while.
     * Ultimately, this method must be followed by a call to
     * the unlock() method.
     *
     * @see #unlock
     *
     * @param executables the executables that have to be locked
     */
    private void lock() {
        for (EarlyExecutable ee : earlyExecutables) {
            ee.lock();
        }
    }

    /**
     * Unlock a set of executables.
     *
     * @param executables the executables that have to be unlocked
     */
    private void unlock() {
        for (EarlyExecutable ee : earlyExecutables) {
            ee.unlock();
        }
    }

    /**
     * The verify method must be called after the lock method.
     * If it fails, the transition cannot fire. If it succeeds,
     * it must be followed by a call to executeEarly().
     *
     * @see #executeEarly()
     *
     * @param executables the executables that have to be verified
     * @return true, if verification succeeded
     */
    private boolean verify(StepIdentifier stepIdentifier) {
        List<EarlyExecutable> verified = new ArrayList<EarlyExecutable>();

        // Verify all early executables.
        try {
            for (EarlyExecutable executable : earlyExecutables) {
                executable.verify(stepIdentifier);

                // Ok, I got the verification. Now I must make sure to
                // treat this executable correctly: rollback or execute.
                verified.add(executable);
            }
        } catch (Impossible e) {
            // Something failed. Probably a missing token.
            // We make sure to undo all changes to
            // the database (if neccessary).
            TransactionSource.rollback();


            // Now the executables can undo their actions.
            // The database must not be informed about these, because
            // it was not informed about the previous actions
            // in the first place.
            Collections.reverse(verified);
            Iterator<EarlyExecutable> enumeration = verified.iterator();
            while (enumeration.hasNext()) {
                enumeration.next().rollback();
            }


            // I undid all actions (in reverse order, if that matters).
            // Now I have to report failure.
            return false;
        }
        return true;
    }

    /**
     * If all verifications have succeeded, this method can call all early
     * executables in their appropriate order.
     */
    private void executeEarly(StepIdentifier stepIdentifier) {
        for (EarlyExecutable executable : earlyExecutables) {
            executable.execute(stepIdentifier);
        }
    }

    /**
     * If all early executables were correctly executed and
     * the locks were released, we can call all late
     * executables in their appropriate order.
     */
    private void executeLate(StepIdentifier stepIdentifier) {
        // The following throwable becomes non-null when
        // some late executable throws and exception.
        // It is then passed to all following executables.
        Throwable firstThrowable = null;
        for (LateExecutable executable : lateExecutables) {
            if (executable.isLong()) {
                de.renew.util.Detacher.detach();
            }
            try {
                if (firstThrowable == null) {
                    executable.execute(stepIdentifier);
                } else {
                    executable.executeAfterException(stepIdentifier,
                                                     firstThrowable);
                }
            } catch (ThreadDeath death) {
                // We are not supposed to stop a thread death.
                throw death;
            } catch (Throwable t) {
                if (firstThrowable == null) {
                    firstThrowable = t;
                    SimulatorEventLogger.log(stepIdentifier,
                                             new TransitionException(t));
                } else {
                    SimulatorEventLogger.log(stepIdentifier,
                                             new TransitionException(t));
                }
                logger.error(t.getMessage(), t);
            }
        }
    }

    /**
     * This method must not be called directly.
     * It has to be public in order to satisfy the
     * interface Runnable. Use the execute(...) method
     * instead.
     */
    public void run() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Keep a note that this object is now executing.
        BindingList.register(this);


        // Wrap operations in a transaction, if that is
        // required.
        TransactionSource.start();


        // Lock in the required order.
        Collections.sort(earlyExecutables, new LockComparator());
        lock();
        try {
            // The order of execution is
            // usually different from the order of locking.
            Collections.sort(earlyExecutables, new PhaseComparator());

            // Check all early executables. Only those can fail.
            result = verify(stepIdentifier);

            // Execute the executables only if the verification suceeded.
            if (result) {
                // The transition was still activated.
                executeEarly(stepIdentifier);
            }
        } finally {
            // We must make sure to undo every lock even if
            // the execution of some operations failed.
            // The order of the unlock operations does not matter.
            unlock();
        }

        // We can already report success or failure to the calling
        // thread.
        sem.V();

        // Execute the remaining executables only if the verification suceeded.
        if (result) {
            // The remaining steps can now be performed without further problems.
            Collections.sort(lateExecutables, new PhaseComparator());
            executeLate(stepIdentifier);
            try {
                TransactionSource.commit();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }


        // Keep a note that this object is no longer executing.
        BindingList.remove(this);


        // Let's make life easier for the garbage collector.
        earlyExecutables = null;
        lateExecutables = null;
    }
}