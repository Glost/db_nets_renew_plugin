package de.renew.dbnets.persistence;

import de.renew.dbnets.datalogic.EditedFact;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The database connection instance based on the Wireshark PCAP file with the captured FIX Protocol messages.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class FixPcapJdbcConnectionInstance implements JdbcConnectionInstance {

    /**
     * The regular expression for the FIX PCAP JDBC URL.
     */
    private static final String JDBC_URL_REGEX = "fixpcap:(?<fixPcapFilePath>.+)";

    /**
     * The pattern for the regular expression for the FIX PCAP JDBC URL.
     */
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile(JDBC_URL_REGEX);

    /**
     * The regular expression for the SQL query for retrieving the FIX message of the given msg_type.
     */
    private static final String SQL_QUERY_STRING_REGEX = ".*msg_type\\s+=\\s+(?<messageType>\\w+).*";

    /**
     * The pattern for the regular expression for the SQL query for retrieving the FIX message of the given msg_type.
     */
    private static final Pattern SQL_QUERY_STRING_PATTERN = Pattern.compile(SQL_QUERY_STRING_REGEX);

    /**
     * The regular expression for the SQL query for retrieving the FIX message
     * of the given msg_type and with the given cl_ord_id.
     */
    private static final String SQL_QUERY_WITH_CL_ORD_ID_STRING_REGEX =
        ".*((cl_ord_id\\s+=\\s+(?<clOrdIdL>\\w+).*msg_type\\s+=\\s+(?<messageTypeR>\\w+))|" +
            "(msg_type\\s+=\\s+(?<messageTypeL>\\w+).*cl_ord_id\\s+=\\s+(?<clOrdIdR>\\w+))).*";

    /**
     * The pattern for the regular expression for the SQL query for retrieving the FIX message
     * of the given msg_type and with the given cl_ord_id.
     */
    private static final Pattern SQL_QUERY_WITH_CL_ORD_ID_STRING_PATTERN =
        Pattern.compile(SQL_QUERY_WITH_CL_ORD_ID_STRING_REGEX);

    /**
     * The regular expression for the FIX tag-value pair.
     */
    private static final String FIX_TAG_VALUE_REGEX = "(?<tag>\\d+)=(?<value>\\d)";

    /**
     * The pattern for the regular expression for the FIX tag-value pair.
     */
    private static final Pattern FIX_TAG_VALUE_PATTERN = Pattern.compile(FIX_TAG_VALUE_REGEX);

    /**
     * The delimiter of the FIX tag-value pairs.
     */
    private static final String FIX_TAGS_DELIMITER = "\1";

    /**
     * The number of the FIX message type (MsgType(35)) tag.
     */
    private static final int FIX_MSG_TYPE_TAG = 35;

    /**
     * The number of the FIX client order id (ClOrdId(11)) tag.
     */
    private static final int FIX_CL_ORD_ID_TAG = 11;

    /**
     * The number of the FIX client order id (SendingTime(52)) tag.
     */
    private static final int FIX_SENDING_TIME_TAG = 52;

    /**
     * The stream of lines of the FIX PCAP file.
     */
    private Stream<String> fixPcapFileLinesStream;

    /**
     * The iterator of the stream of lines of the FIX PCAP file.
     */
    private Iterator<String> fixPcapFileLinesStreamIterator;

    /**
     * The mapping from the FIX message's msg_type and cl_ord_id to the FIX message itself.
     */
    private Map<FixMessageKey, Map<Integer, String>> fixMessagesMap;

    /**
     * The mapping from the FIX message's msg_type
     * to the queue of the previously retrieved FIX messages of the corresponding msg_type.
     */
    private Map<String, Queue<Map<Integer, String>>> messageTypeToMessagesQueueMap;

    /**
     * Initializes the database connection instance by the given JDBC URL
     * and creates the database schema using the given DDL query.
     *
     * @param jdbcUrl The JDBC URL for initializing the database connection.
     * @param ddlQueryString The DDL query for creating the database schema.
     * @throws Impossible If the database error occurred during the database connection initialization
     * or the database schema creation.
     */
    @Override
    public void init(String jdbcUrl, String ddlQueryString) throws Impossible {
        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);

        if (!matcher.matches()) {
            throw new Impossible("JDBC URL for the FIX PCAP virtual JDBC connection is unparseable: " + jdbcUrl);
        }

        String fixPcapFilePath = matcher.group("fixPcapFilePath");

        try {
            fixPcapFileLinesStream = Files.lines(Paths.get(fixPcapFilePath));
            fixPcapFileLinesStreamIterator = fixPcapFileLinesStream.iterator();
        } catch (IOException e) {
            throw new Impossible("Cannot open file (" + fixPcapFilePath + "): " + e.getMessage(), e);
        }

        fixMessagesMap = new HashMap<>();
        messageTypeToMessagesQueueMap = new HashMap<>();
    }

    /**
     * Executes the query.
     *
     * @param sqlQueryString The SQL query string.
     * @param stateRecorder The state recorder instance.
     * @return The query results list.
     * @throws Impossible If the database error occurred during the query execution.
     */
    @Override
    public synchronized List<Variable> executeQuery(String sqlQueryString,
                                                    StateRecorder stateRecorder) throws Impossible {
        Matcher sqlQueryWithClOrdIdStringMatcher = SQL_QUERY_WITH_CL_ORD_ID_STRING_PATTERN.matcher(sqlQueryString);

        if (sqlQueryWithClOrdIdStringMatcher.matches()) {
            return getCorrespondingRequestMessage(sqlQueryWithClOrdIdStringMatcher, stateRecorder);
        }

        Matcher sqlQueryStringMatcher = SQL_QUERY_STRING_PATTERN.matcher(sqlQueryString);

        if (!sqlQueryStringMatcher.matches()) {
            throw new Impossible(
                "SQL query string does not contain the valid msg_type equality constraint: " + sqlQueryString
            );
        }

        String queryMessageType = sqlQueryStringMatcher.group("messageType");

        Queue<Map<Integer, String>> messageQueue = messageTypeToMessagesQueueMap.get(queryMessageType);

        Map<Integer, String> message = Optional.ofNullable(messageQueue).map(Queue::poll).orElse(null);

        while (Objects.isNull(message) && fixPcapFileLinesStreamIterator.hasNext()) {
            String line = fixPcapFileLinesStreamIterator.next();

            Map<Integer, String> tagValues = Arrays.stream(line.split(FIX_TAGS_DELIMITER))
                .map(this::parseFixTagValueString)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(FixTagValue::getTag, FixTagValue::getValue));

            String logMessageType = tagValues.get(FIX_MSG_TYPE_TAG);
            String logClOrdId = tagValues.get(FIX_CL_ORD_ID_TAG);

            if (Objects.nonNull(logMessageType)) {
                if (Objects.nonNull(logClOrdId)) {
                    FixMessageKey fixMessageKey = new FixMessageKey(logMessageType, logClOrdId);
                    fixMessagesMap.put(fixMessageKey, tagValues);
                }

                Queue<Map<Integer, String>> messageQueueForAdding =
                    messageTypeToMessagesQueueMap.computeIfAbsent(logMessageType, s -> new ArrayDeque<>());

                messageQueueForAdding.add(tagValues);
            }

            if (queryMessageType.equals(logMessageType)) {
                message = tagValues;
            }
        }

        return getVariablesList(message, stateRecorder);
    }

    /**
     * Performs the action.
     *
     * @param addedFacts The rows of the tables being added to the database during the action performing.
     * @param deletedFacts The rows of the tables being deleted to the database during the action performing.
     * @param paramsValuesMap The map of the values for the edited (added/deleted) facts' params.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     */
    @Override
    public synchronized void performAction(Collection<EditedFact> addedFacts,
                                           Collection<EditedFact> deletedFacts,
                                           Map<String, Object> paramsValuesMap,
                                           VariableMapper variableMapper) {
        // Doing nothing in the case of the FIX PCAP JDBC connection.
    }

    /**
     * Gets the generated value for the param.
     *
     * @param tableName The param usage's table (relation) name.
     * @return The generated value for the param.
     */
    @Override
    public synchronized Variable mapParamToAutoincrementedValue(String tableName,
                                                                StateRecorder stateRecorder) {
        throw new UnsupportedOperationException(
            "Autoincremented values are not supported for the FIX PCAP JDBC connection"
        );
    }

    /**
     * Closes the FIX PCAP file stream.
     */
    @Override
    public void close() {
        if (Objects.nonNull(fixPcapFileLinesStream)) {
            fixPcapFileLinesStream.close();
        }
    }

    /**
     * Closes the FIX PCAP file stream.
     */
    @Override
    protected void finalize() {
        close();
    }

    /**
     * Returns the corresponding request message for the cl_ord_id of the given response message.
     *
     * @param sqlQueryWithClOrdIdStringMatcher The initialized regular expression pattern matcher instance.
     * @param stateRecorder The state recorder instance.
     * @return The corresponding request message for the cl_ord_id of the given response message.
     */
    private List<Variable> getCorrespondingRequestMessage(Matcher sqlQueryWithClOrdIdStringMatcher,
                                                          StateRecorder stateRecorder) {
        String clOrdIdL = sqlQueryWithClOrdIdStringMatcher.group("clOrdIdL");

        String queryClOrdId = Optional.ofNullable(clOrdIdL)
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> sqlQueryWithClOrdIdStringMatcher.group("clOrdIdR"));
        String queryMessageType = Optional.ofNullable(clOrdIdL)
            .filter(s -> !s.isEmpty())
            .map(s -> sqlQueryWithClOrdIdStringMatcher.group("messageTypeR"))
            .orElseGet(() -> sqlQueryWithClOrdIdStringMatcher.group("messageTypeL"));

        FixMessageKey fixMessageKey = new FixMessageKey(queryMessageType, queryClOrdId);

        Map<Integer, String> message = fixMessagesMap.get(fixMessageKey);

        return getVariablesList(message, stateRecorder);
    }

    /**
     * Returns the list of Renew variables with values of the cl_ord_id and msg_type tags of the given FIX message.
     *
     * @param fixTagValueMessage The given FIX message.
     * @param stateRecorder The state recorder instance.
     * @return The list of Renew variables with values of the cl_ord_id and msg_type tags of the given FIX message.
     */
    private List<Variable> getVariablesList(Map<Integer, String> fixTagValueMessage, StateRecorder stateRecorder) {
        if (Objects.isNull(fixTagValueMessage)) {
            return Collections.emptyList();
        }

        return Arrays.asList(
            new Variable(fixTagValueMessage.get(FIX_CL_ORD_ID_TAG), stateRecorder),
            new Variable(fixTagValueMessage.get(FIX_SENDING_TIME_TAG), stateRecorder)
        );
    }

    /**
     * Parses the FIX tag-value pair string.
     *
     * @param fixTagValueString The FIX tag-value pair string for parsing.
     * @return The parsed FIX tag-value pair.
     */
    private FixTagValue parseFixTagValueString(String fixTagValueString) {
        Matcher matcher = FIX_TAG_VALUE_PATTERN.matcher(fixTagValueString);

        if (!matcher.matches()) {
            return null;
        }

        try {
            int tag = Integer.parseInt(matcher.group("tag"));
            String value = matcher.group("value");
            return new FixTagValue(tag, value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * The FIX message's msg_type and cl_ord_id.
     */
    private static final class FixMessageKey {

        /**
         * The FIX message's msg_type.
         */
        private final String messageType;

        /**
         * The FIX message's cl_ord_id.
         */
        private final String clOrdId;

        /**
         * Initializes the instance.
         *
         * @param messageType The FIX message's msg_type.
         * @param clOrdId The FIX message's cl_ord_id.
         */
        public FixMessageKey(String messageType, String clOrdId) {
            this.messageType = messageType;
            this.clOrdId = clOrdId;
        }

        /**
         * Checks the equality of two instances based on their field values.
         *
         * @param o Other instance for checking the equality with the current instance based on field values.
         * @return true if two instances are equal based on their field values, false otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FixMessageKey that = (FixMessageKey) o;
            return Objects.equals(messageType, that.messageType) && Objects.equals(clOrdId, that.clOrdId);
        }

        /**
         * Calculates the hash code of the current instance based on their field values.
         *
         * @return The hash code of the current instance based on their field values.
         */
        @Override
        public int hashCode() {
            return Objects.hash(messageType, clOrdId);
        }
    }

    /**
     * The pair of the FIX tag and its value in the particular message.
     */
    private static final class FixTagValue {

        /**
         * The FIX tag number.
         */
        private final int tag;

        /**
         * The FIX tag's value in the message.
         */
        private final String value;

        /**
         * Initializes the instance.
         *
         * @param tag The FIX tag number.
         * @param value The FIX tag's value in the message.
         */
        public FixTagValue(int tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        /**
         * Returns the FIX tag number.
         *
         * @return The FIX tag number.
         */
        public int getTag() {
            return tag;
        }

        /**
         * Returns the FIX tag's value in the message.
         *
         * @return The FIX tag's value in the message.
         */
        public String getValue() {
            return value;
        }
    }
}
