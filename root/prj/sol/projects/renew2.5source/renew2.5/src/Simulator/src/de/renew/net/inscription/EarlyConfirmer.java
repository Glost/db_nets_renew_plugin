package de.renew.net.inscription;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.EarlyExecutable;

import de.renew.net.NetInstance;


class EarlyConfirmer implements EarlyExecutable {
    NetInstance netInstance;

    EarlyConfirmer(NetInstance netInstance) {
        this.netInstance = netInstance;
    }

    public long lockPriority() {
        return 0;
    }

    public void lock() {
        // Nothing to do.
    }

    public int phase() {
        return EARLYCONFIRM;
    }

    // This will not take long.
    public boolean isLong() {
        return false;
    }

    public void verify(StepIdentifier stepIdentifier) {
        netInstance.earlyConfirmation();
    }

    public void execute(StepIdentifier stepIdentifier) {
        netInstance.earlyConfirmationTrace(stepIdentifier);
    }

    public void rollback() {
        // Nothing to do.
    }

    public void unlock() {
        // Nothing to do.
    }
}