package de.renew.net.inscription;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;

import de.renew.net.NetInstance;


class LateConfirmer implements LateExecutable {
    NetInstance netInstance;

    LateConfirmer(NetInstance netInstance) {
        this.netInstance = netInstance;
    }

    public int phase() {
        // This is a tricky question. When should we confirm
        // the creation of a net instance? If we confirm during
        // phase 2, it might be safer. But at phase 1 it is
        // already clear that the net must be created and so we might
        // introduce the net to avoid some deadlocks. Furthermore,
        // all information is present during phase 1, so there is no
        // problem either.
        //
        // The translation formalism for low-level reference nets
        // must take care of this issue.
        return LATECONFIRM;
    }

    // This will not take long.
    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        netInstance.lateConfirmation(stepIdentifier);
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
    }
}