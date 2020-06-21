package de.renew.dbnets.shadow;

import de.renew.dbnets.datalogic.Action;
import de.renew.dbnets.datalogic.Query;
import de.renew.dbnets.persistence.DatabaseSchemaDeclaration;
import de.renew.dbnets.persistence.JdbcConnection;
import de.renew.formalism.java.ParsedDeclarationNode;

import java.util.HashMap;
import java.util.Map;

/**
 * The parsed db-net's declaration node.
 * Includes the db-net's persistence layer's JDBC connection URL, database schema DDL/DML declarations,
 * data logic layer's queries and actions as well as the net's imports and variables declarations.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ParsedDBNetDeclarationNode extends ParsedDeclarationNode {

    /**
     * The mapping of the declared db-net's queries (from their names to themselves).
     */
    private final Map<String, Query> queries = new HashMap<>();

    /**
     * The mapping of the declared db-net's actions (from their names to themselves).
     */
    private final Map<String, Action> actions = new HashMap<>();

    /**
     * The declared db-net's persistence layer's JDBC connection data.
     */
    private JdbcConnection jdbcConnection;

    /**
     * The db-net's persistence layer's relational database schema declaration.
     */
    private DatabaseSchemaDeclaration databaseSchemaDeclaration;

    /**
     * Returns the declared db-net's persistence layer's JDBC connection data.
     *
     * @return The declared db-net's persistence layer's JDBC connection data.
     */
    public JdbcConnection getJdbcConnection() {
        return jdbcConnection;
    }

    /**
     * Sets the declared db-net's persistence layer's JDBC connection data.
     *
     * @param jdbcConnection The declared db-net's persistence layer's JDBC connection data.
     */
    public void setJdbcConnection(JdbcConnection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    /**
     * Returns the db-net's persistence layer's relational database schema declaration.
     *
     * @return The db-net's persistence layer's relational database schema declaration.
     */
    public DatabaseSchemaDeclaration getDatabaseSchemaDeclaration() {
        return databaseSchemaDeclaration;
    }

    /**
     * Sets the db-net's persistence layer's relational database schema declaration.
     *
     * @param databaseSchemaDeclaration The the db-net's persistence layer's relational database schema declaration.
     */
    public void setDatabaseSchemaDeclaration(DatabaseSchemaDeclaration databaseSchemaDeclaration) {
        this.databaseSchemaDeclaration = databaseSchemaDeclaration;
    }

    /**
     * Adds the declared query to the parsed db-net's declaration node.
     *
     * @param query The declared query.
     */
    public void addQuery(Query query) {
        queries.put(query.getName(), query);
    }

    /**
     * Returns the declared query for the given query name.
     *
     * @param name The query name.
     * @return The declared query for the given query name.
     */
    public Query getQueryByName(String name) {
        return queries.get(name);
    }

    /**
     * Adds the declared action to the parsed db-net's declaration node.
     *
     * @param action The declared action.
     */
    public void addAction(Action action) {
        actions.put(action.getName(), action);
    }

    /**
     * Returns the declared action for the given action name.
     *
     * @param name The action name.
     * @return The declared action for the given action name.
     */
    public Action getActionByName(String name) {
        return actions.get(name);
    }
}
