package de.renew.dbnets.pa;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Collections;

/**
 * The metadata for performance analysis of modeled system on the db-net transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class PerformanceAnalysisInfo implements TransitionInscription {

    /**
     * The name of the field with id which is equal in the pair of request and response messages.
     */
    private final String idFieldName;

    /**
     * The name of the field with sending time of the message.
     */
    private final String sendingTimeFieldName;

    /**
     * The number of the field with sending time of the message in the results of the queries for retrieving messages.
     */
    private final int sendingTimeFieldNumber;

    /**
     * The pattern for parsing the sending timestamp of the message.
     */
    private final String sendingTimePattern;

    /**
     * The number of the field with the type of the message in the results of the queries for retrieving messages.
     */
    private final int messageTypeFieldNumber;

    /**
     * The query call for retrieving the request message corresponding to the given response message.
     */
    private final QueryCall requestMessageQueryCall;

    /**
     * A maximum acceptable delay between request and response messages (in msec).
     */
    private final int maxDelay;

    /**
     * The constructor of the metadata for performance analysis of modeled system on the db-net transition.
     *
     * @param idFieldName The name of the field with id which is equal in the pair of request and response messages.
     * @param sendingTimeFieldName The name of the field with sending time of the message.
     * @param sendingTimeFieldNumber The number of the field with sending time of the message
     *                               in the results of the queries for retrieving messages.
     * @param sendingTimePattern The pattern for parsing the sending timestamp of the message.
     * @param messageTypeFieldNumber The number of the field with the type of the message
     *                               in the results of the queries for retrieving messages.
     * @param requestMessageQueryCall The query call for retrieving the request message
     *                                corresponding to the given response message.
     * @param maxDelay A maximum acceptable delay between request and response messages (in msec).
     */
    public PerformanceAnalysisInfo(String idFieldName,
                                   String sendingTimeFieldName,
                                   int sendingTimeFieldNumber,
                                   String sendingTimePattern,
                                   int messageTypeFieldNumber,
                                   QueryCall requestMessageQueryCall,
                                   int maxDelay) {
        this.idFieldName = idFieldName;
        this.sendingTimeFieldName = sendingTimeFieldName;
        this.sendingTimeFieldNumber = sendingTimeFieldNumber;
        this.sendingTimePattern = sendingTimePattern;
        this.messageTypeFieldNumber = messageTypeFieldNumber;
        this.requestMessageQueryCall = requestMessageQueryCall;
        this.maxDelay = maxDelay;
    }

    /**
     * Returns the name of the field with id which is equal in the pair of request and response messages.
     *
     * @return The name of the field with id which is equal in the pair of request and response messages.
     */
    public String getIdFieldName() {
        return idFieldName;
    }

    /**
     * Returns the name of the field with sending time of the message.
     *
     * @return The name of the field with sending time of the message.
     */
    public String getSendingTimeFieldName() {
        return sendingTimeFieldName;
    }

    /**
     * Returns the number of the field with sending time of the message
     * in the results of the queries for retrieving messages.
     *
     * @return The number of the field with sending time of the message
     * in the results of the queries for retrieving messages.
     */
    public int getSendingTimeFieldNumber() {
        return sendingTimeFieldNumber;
    }

    /**
     * Returns the pattern for parsing the sending timestamp of the message.
     *
     * @return The pattern for parsing the sending timestamp of the message.
     */
    public String getSendingTimePattern() {
        return sendingTimePattern;
    }

    /**
     * Returns the number of the field with the type of the message
     * in the results of the queries for retrieving messages.
     *
     * @return The number of the field with the type of the message
     * in the results of the queries for retrieving messages.
     */
    public int getMessageTypeFieldNumber() {
        return messageTypeFieldNumber;
    }

    /**
     * Returns the query call for retrieving the request message corresponding to the given response message.
     *
     * @return The query call for retrieving the request message corresponding to the given response message.
     */
    public QueryCall getRequestMessageQueryCall() {
        return requestMessageQueryCall;
    }

    /**
     * Returns a maximum acceptable delay between request and response messages (in msec).
     *
     * @return A maximum acceptable delay between request and response messages (in msec).
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * Returns the empty list of the performance analysis info's occurrences since we do not need any of them.
     *
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     * @param searcher The searcher instance.
     * @return The empty list of the performance analysis info's occurrences.
     */
    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        return Collections.emptyList();
    }
}
