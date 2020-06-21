package de.renew.dbnets.datalogic;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Impossible;

import java.sql.Connection;

/**
 * The action call's executable. Executes the transition's action call.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ActionCallExecutable implements LateExecutable {

    /**
     * The action call for executing.
     */
    private final ActionCall actionCall;

    /**
     * The transition instance.
     */
    private final DBNetTransitionInstance transitionInstance;

    /**
     * The transition instance's variable mapper. Maps the net's variables' names into their values.
     */
    private final VariableMapper variableMapper;

    /**
     * The database connection instance.
     */
    private final Connection connection;

    /**
     * The action call's executable's constructor.
     *
     * @param actionCall The action call for executing.
     * @param transitionInstance The transition instance.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param connection The database connection instance.
     */
    public ActionCallExecutable(ActionCall actionCall,
                                DBNetTransitionInstance transitionInstance,
                                VariableMapper variableMapper,
                                Connection connection) {
        this.actionCall = actionCall;
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.connection = connection;
    }

    /**
     * Returns the action phase number.
     *
     * @return The action phase number.
     */
    @Override
    public int phase() {
        return ACTION;
    }

    /**
     * Returns that this executable is long since it performs the database operations..
     *
     * @return true.
     */
    @Override
    public boolean isLong() {
        return true;
    }

    /**
     * Executes the action call's executable.
     * Performs the corresponding action.
     *
     * @param stepIdentifier The step identifier instance.
     * @throws Impossible If the error occurred while performing the action.
     */
    @Override
    public void execute(StepIdentifier stepIdentifier) throws Impossible {
        try {
            actionCall.performAction(variableMapper, connection);
        } catch (Impossible e) {
            transitionInstance.setNeedRollback(true);

            throw e;
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
