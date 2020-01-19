package de.renew.engine.searcher;

import de.renew.engine.common.StepIdentifier;


/**
 * This interface describes executable objects that
 * produce irrecoverable side effects.
 * Processing proceeds in certain phases.
 *
 * Here we list all phases that may occur.
 * First of all, the irrevocable start of the
 * transition's firing is announced.
 * Then trace messages are printed. The definite and
 * irrevocable creation of net instances should be
 * confirmed next, so that the net instances can
 * participate in actions later on. Output arcs come
 * next, before the transition's termination is announced.
 * In the case of synchronisation requests there are
 * no output arcs, but the end of the synchronisation needs
 * to be declared.
 */
public interface LateExecutable extends Executable {
    public final int START = -9999;
    public final int TRACE = 0;
    public final int LATECONFIRM = 1;
    public final int ACTION = 2;
    public final int OUTPUT = 3;
    public final int COMPLETION_NOTIFY = OUTPUT;
    public final int END = 9999;

    /**
     * Get the phase during which this executable should execute.
     *
     * The late executables will be executed after all
     * early executables regardless of the phase. The phase
     * is local to late executables.
     **/
    int phase();

    /**
     * Returns true, if this executable might take a long time to
     * execute.
     **/
    boolean isLong();

    /**
     * Execute this executable if an exception occurred
                 * in a previous late executable.
     **/
    void executeAfterException(StepIdentifier stepIdentifier, Throwable t);
}