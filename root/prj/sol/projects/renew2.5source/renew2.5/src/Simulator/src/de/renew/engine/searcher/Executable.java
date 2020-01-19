package de.renew.engine.searcher;

import de.renew.engine.common.StepIdentifier;

import de.renew.unify.Impossible;


/**
 * Classes should never implement this interface directly, but rather
 * EarlyExecutable or LateExecutable.
 *
 * No other interface except EarlyExecutable and LateExecutable
 * may extend this interface. If another type of executables is
 * added, care must be taken to update the execute method
 * of the simulator helper.
 *
 * @see EarlyExecutable
 * @see LateExecutable
 **/
public interface Executable {

    /**
     * Get the phase during which this executable should execute.
     *
     * The early executables will be executed before all
     * late executables regardless of the phase. Among
     * early executables and late executables the phase
     * is the determining factor.
     **/
    int phase();

    /**
     * Execute this executable.
     **/
    void execute(StepIdentifier stepIdentifier) throws Impossible;
}