package de.renew.net.arc;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.net.DBNetTransitionInstance;

import java.util.Objects;

/**
 * The wrapper for the obvious output arc's executable.
 * Allows to NOT move the token through the obvious output arc when the rollback arc is activated.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class SimpleOutputArcExecutable implements LateExecutable {

    /**
     * The wrapped obvious output arc's executable.
     */
    private final OutputArcExecutable outputArcExecutable;

    /**
     * The db-net transition's rollback arc's executable.
     * May be null, if the transition has no rollback arcs.
     */
    private final RollbackArcExecutable rollbackArcExecutable;

    /**
     * The constructor of the wrapper for the obvious output arc's executable.
     *
     * @param outputArcExecutable The wrapped obvious output arc's executable.
     * @param rollbackArcExecutable The db-net transition's rollback arc's executable.
     *                              May be null, if the transition has no rollback arcs.
     */
    public SimpleOutputArcExecutable(OutputArcExecutable outputArcExecutable,
                                     RollbackArcExecutable rollbackArcExecutable) {
        this.outputArcExecutable = outputArcExecutable;
        this.rollbackArcExecutable = rollbackArcExecutable;
    }

    /**
     * Returns the output phase number.
     *
     * @return The output phase number.
     */
    @Override
    public int phase() {
        return OUTPUT;
    }

    /**
     * Returns that this executable is not long since it does not require any long actions.
     *
     * @return false.
     */
    @Override
    public boolean isLong() {
        return false;
    }

    /**
     * Execute the wrapped output arc executable, if the rollback is not requested or executed.
     *
     * @param stepIdentifier The step identifier instance.
     */
    @Override
    public void execute(StepIdentifier stepIdentifier) {
        if (Objects.isNull(rollbackArcExecutable) ||
                (!((DBNetTransitionInstance) outputArcExecutable.tInstance).needRollback() &&
                        !rollbackArcExecutable.isExecuted())) {
            outputArcExecutable.execute(stepIdentifier);
        }
    }

    /**
     * We should not do anything here.
     */
    @Override
    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        // We should not do anything here.
    }
}
