package de.renew.dbnets.pa;

import de.renew.dbnets.persistence.JdbcConnectionInstance;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetControlLayerInstance;
import de.renew.net.DBNetTransition;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The executable for conducting the performance analysis of modeled system on the db-net transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class PerformanceAnalysisExecutable implements LateExecutable {

    /**
     * The message key for the "Sending time field number out of bounds" dialog message.
     */
    private static final String SENDING_TIME_FIELD_NUMBER_OUT_OF_BOUNDS_MESSAGE_KEY =
            "sendingTimeFieldNumberOutOfBounds";

    /**
     * The message key for the "ID in query result not string" dialog message.
     */
    private static final String ID_IN_QUERY_RESULT_NOT_STRING_MESSAGE_KEY = "idInQueryResultNotString";

    /**
     * The message key for the "Sending timestamp not string" dialog message.
     */
    private static final String SENDING_TIMESTAMP_NOT_STRING_MESSAGE_KEY = "sendingTimestampNotString";

    /**
     * The message key for the "Sending timestamp unparseable" dialog message.
     */
    private static final String SENDING_TIMESTAMP_UNPARSEABLE_MESSAGE_KEY = "sendingTimestampUnparseable";

    /**
     * The message key for the "Max delay exceeded" dialog message.
     */
    private static final String MAX_DELAY_EXCEEDED_MESSAGE_KEY = "maxDelayExceeded";

    /**
     * The transition instance.
     */
    private final DBNetTransitionInstance transitionInstance;

    /**
     * The transition instance's variable mapper. Maps the net's variables' names into their values.
     */
    private final VariableMapper variableMapper;

    /**
     * The state recorder instance.
     */
    private final StateRecorder stateRecorder;

    /**
     * The database connection instance.
     */
    private final JdbcConnectionInstance connectionInstance;

    /**
     * The performance analysis executable's constructor.
     *
     * @param transitionInstance The transition instance.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param stateRecorder The state recorder instance.
     * @param connectionInstance The database connection instance.
     */
    public PerformanceAnalysisExecutable(DBNetTransitionInstance transitionInstance,
                                         VariableMapper variableMapper,
                                         StateRecorder stateRecorder,
                                         JdbcConnectionInstance connectionInstance) {
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.stateRecorder = stateRecorder;
        this.connectionInstance = connectionInstance;
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
     * Returns that this executable is long since it performs operations with probably large text file.
     *
     * @return true.
     */
    @Override
    public boolean isLong() {
        return true;
    }

    /**
     * Executes the performance analysis executable.
     * Conducts the performance analysis for the given response message
     * based on the delay between its sending timestamp and its corresponding request message's sending timestamp
     * and the max delay.
     *
     * @param stepIdentifier The step identifier instance.
     * @throws Impossible If the error occurred while conducting the performance analysis.
     */
    @Override
    public void execute(StepIdentifier stepIdentifier) throws Impossible {
        PerformanceAnalysisInfo performanceAnalysisInfo =
                ((DBNetTransition) transitionInstance.getTransition()).getPerformanceAnalysisInfo();

        DBNetControlLayerInstance netInstance = (DBNetControlLayerInstance) transitionInstance.getNetInstance();

        Variable responseMessageIdVariable =
                variableMapper.map(new LocalVariable(performanceAnalysisInfo.getIdFieldName()));
        Variable responseMessageSendingTimestampVariable =
                variableMapper.map(new LocalVariable(performanceAnalysisInfo.getSendingTimeFieldName()));

        if (!(responseMessageIdVariable.getValue() instanceof String)) {
            netInstance.showMessageDialogIfNotShownYet(
                    ID_IN_QUERY_RESULT_NOT_STRING_MESSAGE_KEY,
                    "ID in query result not string",
                    "ID field value in the response message query result was not of the string type " +
                            "or was null."
            );
            return;
        }

        if (!(responseMessageSendingTimestampVariable.getValue() instanceof String)) {
            netInstance.showMessageDialogIfNotShownYet(
                    SENDING_TIMESTAMP_NOT_STRING_MESSAGE_KEY,
                    "Sending timestamp not string",
                    "Sending timestamp was not of the string type or was null for results of the corresponding " +
                            "request and/or response message query(-ies)."
            );
            return;
        }

        List<Variable> requestMessageQueryResult = performanceAnalysisInfo.getRequestMessageQueryCall()
                .executeQuery(connectionInstance, variableMapper, stateRecorder);

        if (Objects.isNull(requestMessageQueryResult) || requestMessageQueryResult.isEmpty()) {
            return;
        }

        if (performanceAnalysisInfo.getSendingTimeFieldNumber() > requestMessageQueryResult.size()) {
            netInstance.showMessageDialogIfNotShownYet(
                    SENDING_TIME_FIELD_NUMBER_OUT_OF_BOUNDS_MESSAGE_KEY,
                    "Sending time field number out of bounds",
                    "Sending time field number (dbn_pa_send_time_num) " +
                            performanceAnalysisInfo.getSendingTimeFieldNumber() +
                            " was out of bounds for results of the corresponding " +
                            "request and/or response message query(-ies)."
            );
            return;
        }

        Variable requestMessageSendingTimestampVariable =
                requestMessageQueryResult.get(performanceAnalysisInfo.getSendingTimeFieldNumber());

        if (!(requestMessageSendingTimestampVariable.getValue() instanceof String)) {
            netInstance.showMessageDialogIfNotShownYet(
                    SENDING_TIMESTAMP_NOT_STRING_MESSAGE_KEY,
                    "Sending timestamp not string",
                    "Sending timestamp was not of the string type or was null for results of the corresponding " +
                            "request and/or response message query(-ies)."
            );
            return;
        }

        String requestMessageSendingTimestampString = (String) requestMessageSendingTimestampVariable.getValue();
        String responseMessageSendingTimestampString = (String) responseMessageSendingTimestampVariable.getValue();

        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(performanceAnalysisInfo.getSendingTimePattern());

        LocalDateTime requestMessageSendingLocalDateTime;
        LocalDateTime responseMessageSendingLocalDateTime;

        try {
            requestMessageSendingLocalDateTime =
                    LocalDateTime.parse(requestMessageSendingTimestampString, dateTimeFormatter);
            responseMessageSendingLocalDateTime =
                    LocalDateTime.parse(responseMessageSendingTimestampString, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            netInstance.showMessageDialogIfNotShownYet(
                    SENDING_TIMESTAMP_UNPARSEABLE_MESSAGE_KEY,
                    "Sending timestamp unparseable",
                    "Sending timestamp was unparseable using the \"" +
                            performanceAnalysisInfo.getSendingTimePattern() +
                            "\" pattern for results of the corresponding " +
                            "request and/or response message query(-ies)."
            );
            return;
        }

        int delay = (int) Duration.between(requestMessageSendingLocalDateTime, responseMessageSendingLocalDateTime)
                .toMillis();

        if (delay > performanceAnalysisInfo.getMaxDelay()) {
            String requestMessageType = performanceAnalysisInfo.getMessageTypeFieldNumber() >= 0 &&
                    performanceAnalysisInfo.getMessageTypeFieldNumber() < requestMessageQueryResult.size() ?
                    Optional.ofNullable(
                            requestMessageQueryResult.get(performanceAnalysisInfo.getMessageTypeFieldNumber())
                                    .getValue()
                    ).filter(value -> value instanceof String).map(value -> (String) value).orElse("") : "";

            netInstance.showMessageDialogIfNotShownYet(
                    MAX_DELAY_EXCEEDED_MESSAGE_KEY,
                    "Max delay exceeded",
                    String.format(
                            "The delay %d ms between request and response messages with id = \"%s\" " +
                                    (requestMessageType.isEmpty() ? "%s" : "and type = \"%s\" ") +
                                    "exceeded the max delay %d ms. " +
                                    "The full info about this and further max delay violations " +
                                    "will be written into the output CSV file in the folder of the processed log file.",
                            delay,
                            responseMessageIdVariable.getValue(),
                            requestMessageType,
                            performanceAnalysisInfo.getMaxDelay()
                    )
            );

            netInstance.writeExceedingDelayIntoOutputCsvFile(
                    (String) responseMessageIdVariable.getValue(),
                    requestMessageType,
                    delay,
                    performanceAnalysisInfo.getMaxDelay()
            );
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
