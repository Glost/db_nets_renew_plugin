package de.renew.engine.searcher;

import de.renew.engine.common.StepIdentifier;

import de.renew.unify.Impossible;

import de.renew.util.OrderedLockable;


/**
 * This interface describes executable objects that may fail,
 * but have no side effects that are irrecoverable.
 * Processing proceeds in certain phases.
 *
 * Here we list all phases that may occur.
 * Blocking of synchronisation requests can be done first. The
 * confirmation messages, if any, should appear before all token movements.
 * We give priority to inhibitor arcs, because they must see the original
 * marking. Ordinary arcs go in fourth position. Test arcs fifth, because they
 * must let the ordinary input arc take the most recent tokens,
 * so that they can reserve and return the older tokens, thus allowing more
 * firing sequences. Clear arcs come last, because they are supposed
 * to absorb those tokens that would otherwise remain.
 */
public interface EarlyExecutable extends Executable, OrderedLockable {
    public final int BLOCK = -6;
    public final int EARLYCONFIRM = -5;
    public final int INHIBIT = -4;
    public final int INPUT = -3;
    public final int TEST = -2;
    public final int CLEAR = -1;

    /**
     * Get the phase during which this executable should execute.
     *
     * The early executables will be executed before all
     * late executables regardless of the phase. The phase
     * is local to early executables.
     *
     * @return the phase as an integer value
     */
    int phase();

    /**
     * Verify that this action is possible. Even if it fails,
     * the executable must still be unlocked, but no rollback
     * is required or allowed.
     *
     * @exception de.renew.unify.Impossible if the verification failed
     */
    void verify(StepIdentifier stepIdentifier) throws Impossible;

    /**
     * Execute this executable, but do not yet unlock the
     * resources. Only called if the verify method succeeded.
     * Unlike the overwritten method from Executable,
     * this method must not throw an exception.
     *
     * Since the verify() method usually has to do not
     * majority of the work, this method simply outputs
     * trace methods, which must not occur if the firing fails.
     *
     */
    void execute(StepIdentifier stepIdentifier);

    /**
     * Undo the actions performed by the verify method.
     * Only called if the verify method succeeded.
     * Never called jointly with the execute method.
     *
     * This method must not under any circumstances
     * transfer control to the transaction mechanism!
     */
    void rollback();
}