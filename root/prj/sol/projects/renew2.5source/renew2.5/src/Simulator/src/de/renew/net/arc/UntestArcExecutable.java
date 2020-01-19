package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Untesting;
import de.renew.engine.searcher.LateExecutable;


class UntestArcExecutable implements LateExecutable {
    // Here we store the test arc executable, whose action
    // we are supposed to undo. Untesting arbitrary tokens at
    // arbitrary times is not required.
    TestArcExecutable tester;

    UntestArcExecutable(TestArcExecutable tester) {
        this.tester = tester;
    }

    public int phase() {
        return OUTPUT;
    }

    // We can untest a token quickly.
    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        if (tester.trace) {
            // log activity on net level
            SimulatorEventLogger.log(stepIdentifier,
                                     new Untesting(tester.token,
                                                   tester.pInstance),
                                     tester.pInstance);
        }
        tester.pInstance.lock.lock();
        try {
            tester.pInstance.untestToken(tester.token);
        } finally {
            tester.pInstance.lock.unlock();
        }
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        // Do not keep tested tokens tested.
        execute(stepIdentifier);
    }
}