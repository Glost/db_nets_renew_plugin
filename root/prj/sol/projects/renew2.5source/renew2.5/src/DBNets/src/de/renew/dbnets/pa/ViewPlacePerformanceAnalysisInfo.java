package de.renew.dbnets.pa;

/**
 * The metadata for performance analysis of modeled system on the view place.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Concurrent Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ViewPlacePerformanceAnalysisInfo {

    /**
     * The name of column in the log table with id which is equal in the pair of request and response messages.
     */
    private final String idColumnName;

    /**
     * The name of column in the log table with sending time of the message.
     */
    private final String sendingTimeColumnName;

    /**
     * The name of query for retrieving the request message corresponding to the given response message.
     */
    private final String requestMessageQueryName;

    /**
     * A maximum acceptable delay between request and response messages (in msec).
     */
    private final int maxDelay;

    /**
     * The constructor of the metadata for performance analysis of modeled system on the view place.
     *
     * @param idColumnName The name of column in the log table with id which is equal
     *                     in the pair of request and response messages.
     * @param sendingTimeColumnName The name of column in the log table with sending time of the message.
     * @param requestMessageQueryName The name of query for retrieving the request message
     *                                corresponding to the given response message.
     * @param maxDelay A maximum acceptable delay between request and response messages (in msec).
     */
    public ViewPlacePerformanceAnalysisInfo(String idColumnName,
                                            String sendingTimeColumnName,
                                            String requestMessageQueryName,
                                            int maxDelay) {
        this.idColumnName = idColumnName;
        this.sendingTimeColumnName = sendingTimeColumnName;
        this.requestMessageQueryName = requestMessageQueryName;
        this.maxDelay = maxDelay;
    }

    /**
     * Returns the name of column in the log table with id which is equal in the pair of request and response messages.
     *
     * @return The name of column in the log table with id which is equal in the pair of request and response messages.
     */
    public String getIdColumnName() {
        return idColumnName;
    }

    /**
     * Returns the name of column in the log table with sending time of the message.
     *
     * @return The name of column in the log table with sending time of the message.
     */
    public String getSendingTimeColumnName() {
        return sendingTimeColumnName;
    }

    /**
     * Returns the name of query for retrieving the request message corresponding to the given response message.
     *
     * @return The name of query for retrieving the request message corresponding to the given response message.
     */
    public String getRequestMessageQueryName() {
        return requestMessageQueryName;
    }

    /**
     * Returns a maximum acceptable delay between request and response messages (in msec).
     *
     * @return A maximum acceptable delay between request and response messages (in msec).
     */
    public int getMaxDelay() {
        return maxDelay;
    }
}
