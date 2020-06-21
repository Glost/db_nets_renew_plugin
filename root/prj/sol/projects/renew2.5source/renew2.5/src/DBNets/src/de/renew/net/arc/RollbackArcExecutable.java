package de.renew.net.arc;

import de.renew.engine.common.StepIdentifier;
import de.renew.net.DBNetTransitionInstance;
import de.renew.net.PlaceInstance;
import de.renew.unify.Variable;

/**
 * The rollback arc's executable.
 * Moves the token through the rollback arc from the transition to the place.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class RollbackArcExecutable extends OutputArcExecutable {

    /**
     * Stores whether this rollback arc's executable has already been executed or not.
     */
    private boolean isExecuted = false;

    /**
     * The rollback arc's executable's constructor.
     *
     * @param pInstance The instance of the place which is one of the ends of the rollback arc.
     * @param tInstance The instance of the transition which is one of the ends of the rollback arc.
     * @param tokenVar The moved token variable.
     * @param timeVar The time variable.
     * @param trace Stores whether the tracing is requested or not.
     */
    public RollbackArcExecutable(PlaceInstance pInstance,
                                 DBNetTransitionInstance tInstance,
                                 Variable tokenVar,
                                 Variable timeVar,
                                 boolean trace) {
        super(pInstance, tInstance, tokenVar, timeVar, trace);
    }

    /**
     * Returns whether this rollback arc's executable has already been executed or not.
     *
     * @return The true if this rollback arc's executable has already been executed, false otherwise.
     */
    public boolean isExecuted() {
        return isExecuted;
    }

    /**
     * Executes the rollback arc's executable.
     * Moves the token through the rollback arc from the transition to the place if the rollback was requested.
     *
     * @param stepIdentifier The step identifier instance.
     */
    @Override
    public void execute(StepIdentifier stepIdentifier) {
        DBNetTransitionInstance transitionInstance = (DBNetTransitionInstance) tInstance;

        if (((DBNetTransitionInstance) tInstance).needRollback()) {
            super.execute(stepIdentifier);

            isExecuted = true;
        }

        transitionInstance.setNeedRollback(false);
    }

    /**
     * Executes the rollback arc's executable after the exception in one of the previous executables.
     * Moves the token through the rollback arc from the transition to the place if the rollback was requested.
     *
     * @param stepIdentifier The step identifier instance.
     * @param t The throwable thrown in one of the previous executables.
     */
    @Override
    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        execute(stepIdentifier);
    }
}
