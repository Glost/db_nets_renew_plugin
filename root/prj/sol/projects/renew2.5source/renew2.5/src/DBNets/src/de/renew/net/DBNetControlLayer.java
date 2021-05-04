package de.renew.net;

import de.renew.dbnets.persistence.DatabaseSchemaDeclaration;
import de.renew.dbnets.persistence.JdbcConnection;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.unify.Impossible;

/**
 * The db-net's control layer.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DBNetControlLayer extends Net {

    /**
     * The db-net's persistence layer's JDBC connection data.
     */
    private JdbcConnection jdbcConnection;

    /**
     * The db-net's persistence layer's relational database schema declaration.
     */
    private DatabaseSchemaDeclaration databaseSchemaDeclaration;

    /**
     * The db-net's control layer's constructor.
     *
     * @param name The db-net's name.
     */
    public DBNetControlLayer(String name) {
        super(name);
    }

    /**
     * Returns the db-net's persistence layer's JDBC connection data.
     *
     * @return The db-net's persistence layer's JDBC connection data.
     */
    public JdbcConnection getJdbcConnection() {
        return jdbcConnection;
    }

    /**
     * Sets the db-net's persistence layer's JDBC connection data.
     *
     * @param jdbcConnection The db-net's persistence layer's JDBC connection data.
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
     * @param databaseSchemaDeclaration The db-net's persistence layer's relational database schema declaration.
     */
    public void setDatabaseSchemaDeclaration(DatabaseSchemaDeclaration databaseSchemaDeclaration) {
        this.databaseSchemaDeclaration = databaseSchemaDeclaration;
    }

    /**
     * Makes the db-net's control layer's instance for the simulation.
     *
     * @return The db-net's control layer's instance for the simulation.
     * @throws Impossible If the error occurred during the db-net's control layer's instance initialization.
     */
    @Override
    public NetInstance makeInstance() throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new DBNetControlLayerInstance(this);
    }

    /**
     * Checks the transition type and adds the transition to the net.
     *
     * @param transition The transition for adding to the net.
     */
    @Override
    void add(Transition transition) {
        checkTransitionType(transition);
        super.add(transition);
    }

    /**
     * Checks the transition type and removes the transition from the net.
     *
     * @param transition The transition for adding from the net.
     */
    @Override
    void remove(Transition transition) {
        checkTransitionType(transition);
        super.remove(transition);
    }

    /**
     * Checks the transition type.
     * It should be the db-net transition.
     *
     * @param transition The transition for checking its type.
     */
    private void checkTransitionType(Transition transition) {
        if (!(transition instanceof DBNetTransition)) {
            throw new IllegalArgumentException("The transition should be instance of DBNetTransition");
        }
    }
}
