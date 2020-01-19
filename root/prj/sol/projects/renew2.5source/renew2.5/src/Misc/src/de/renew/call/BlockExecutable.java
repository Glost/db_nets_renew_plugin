package de.renew.call;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.EarlyExecutable;

import de.renew.unify.Impossible;


class BlockExecutable implements EarlyExecutable {
    SynchronisationRequest synchronisation;

    BlockExecutable(SynchronisationRequest synchronisation) {
        this.synchronisation = synchronisation;
    }

    public int phase() {
        return BLOCK;
    }

    public long lockPriority() {
        return synchronisation.lockOrder;
    }

    /**
     * Locks the <code>SynchronisationRequest</code> associated with this arc.
     * @see SynchronisationRequest#lock
     **/
    public void lock() {
        synchronisation.lock.lock();
    }

    public void verify(StepIdentifier stepIdentifier) throws Impossible {
        if (synchronisation.completed) {
            throw new Impossible();
        }
    }

    public void execute(StepIdentifier stepIdentifier) {
        synchronisation.completed = true;
    }

    public void rollback() {
        // Nothing to do.
    }

    /**
     * Unlocks the <code>SynchronisationRequest</code> associated with this arc.
     * @see SynchronisationRequest#lock
     **/
    public void unlock() {
        synchronisation.lock.unlock();
    }
}