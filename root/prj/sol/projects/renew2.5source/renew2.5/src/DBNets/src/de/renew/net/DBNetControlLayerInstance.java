package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;
import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.NetInstantiation;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.unify.Impossible;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The db-net's control layer's instance for the simulation.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetControlLayerInstance extends NetInstanceImpl {

    /**
     * The mapping from the connections' JDBC urls to the connections themselves.
     */
    private static final Map<String, Connection> connectionsMap = new HashMap<>();

    /**
     * The db-net's control layer.
     */
    private DBNetControlLayer net;

    /**
     * The mapping from the net's components to their instances.
     */
    private Map<Object, Object> instanceLookup;

    /**
     * The database connection instance.
     */
    private Connection connection;

    /**
     * The db-net's control layer's instance's constructor.
     *
     * @param net The db-net's control layer.
     * @throws Impossible If the error occurred during the db-net's control layer's instance initialization.
     */
    public DBNetControlLayerInstance(DBNetControlLayer net) throws Impossible {
        initNet(net, true);
    }

    /**
     * Closes the database connection instance if there is any.
     *
     * @throws Throwable If any error occured.
     */
    @Override
    protected void finalize() throws Throwable {
        if (Objects.nonNull(connection)) {
            connection.close();
        }
    }

    /**
     * Returns the db-net's control layer.
     *
     * @return The db-net's control layer.
     */
    @Override
    public DBNetControlLayer getNet() {
        return net;
    }

    /**
     * Returns the database connection instance.
     *
     * @return The database connection instance.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the instance for the given net's component.
     *
     * @param netObject The net's component for returning its instance.
     * @return The instance for the given net's component.
     */
    @Override
    public Object getInstance(Object netObject) {
        return instanceLookup.get(netObject);
    }

    /**
     * Returns the place instance for the given place.
     *
     * @param place The place for returning its instance.
     * @return The place instance for the given place.
     */
    @Override
    public PlaceInstance getInstance(Place place) {
        return (PlaceInstance) instanceLookup.get(place);
    }

    /**
     * Returns the transition instance for the given transition.
     *
     * @param transition The transition for returning its instance.
     * @return The transition instance for the given transition.
     */
    @Override
    public TransitionInstance getInstance(Transition transition) {
        return (TransitionInstance) instanceLookup.get(transition);
    }

    /**
     * Based on the {@link super#earlyConfirmation()} implementation.
     */
    @Override
    public void earlyConfirmation() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";

        Transaction transaction = TransactionSource.get();

        try {
            transaction.createNet(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        net.places().stream().map(this::getInstance).forEach(PlaceInstance::earlyConfirmation);
    }

    /**
     * Based on the {@link super#earlyConfirmationTrace(StepIdentifier)} implementation.
     *
     * @param stepIdentifier The step identifier instance.
     */
    @Override
    public void earlyConfirmationTrace(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";

        net.places().stream()
                .map(this::getInstance)
                .forEach(placeInstance -> placeInstance.earlyConfirmationTrace(stepIdentifier));

        NetInstanceList.add(this);
    }

    /**
     * Based on the {@link super#lateConfirmation(StepIdentifier)} implementation.
     *
     * @param stepIdentifier The step identifier instance.
     */
    @Override
    public void lateConfirmation(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";

        SimulatorEventLogger.log(stepIdentifier, new NetInstantiation(this),this);

        net.places().stream()
                .map(this::getInstance)
                .forEach(placeInstance -> placeInstance.lateConfirmation(stepIdentifier));

        net.transitions().stream()
                .map(this::getInstance)
                .forEach(TransitionInstance::createConfirmation);
    }

    /**
     * Initializes the db-net's control layer's instance.
     * Based on the {@link super#initNet(Net, boolean)} implementation.
     *
     * @param net The db-net's control layer.
     * @param wantInitialTokens Not used, always true.
     * @throws Impossible If the error occurred during the db-net's control layer's instance initialization.
     */
    @Override
    protected void initNet(Net net, boolean wantInitialTokens) throws Impossible {
        if (Objects.isNull(net)) {
            Impossible.THROW();
        }

        if (!(net instanceof DBNetControlLayer)) {
            Impossible.THROW();
        }

        this.net = (DBNetControlLayer) net;

        setID(IDSource.createID());

        IDRegistry registry = useGlobalIDRegistry ? IDRegistry.getInstance() : new IDRegistry();

        try {
            reassignField(registry);
        } catch (IOException e) {
            throw new Impossible(e.getMessage(), e);
        }

        instanceLookup = new ConcurrentHashMap<>();

        for (Place place : net.places()) {
            instanceLookup.put(place, place.makeInstance(this, wantInitialTokens));
        }

        net.transitions().forEach(transition -> instanceLookup.put(transition, makeTransitionInstance(transition)));

        createPersistenceLayer();
    }

    /**
     * Creates the persistence layer - the database connection and schema.
     *
     * @throws Impossible If the SQL error occurred during the persistence layer creation.
     */
    private void createPersistenceLayer() throws Impossible {
        createDatabaseConnection();
        createDatabaseSchema();
    }

    /**
     * Creates the persistence layer's database connection.
     *
     * @throws Impossible If the SQL error occurred during the database connection creation
     * or while closing the previous connection to the database.
     */
    private void createDatabaseConnection() throws Impossible {
        String jdbcUrl = net.getJdbcConnection().getUrl().trim();

        Connection previousConnection = connectionsMap.get(jdbcUrl);

        if (Objects.nonNull(previousConnection)) {
            try {
                previousConnection.close();
                connectionsMap.remove(jdbcUrl);
            } catch (SQLException e) {
                throw new Impossible("Error while closing previous connection to the database: " + e.getMessage(), e);
            }
        }

        try {
            connection = DriverManager.getConnection(jdbcUrl);
            connectionsMap.put(jdbcUrl, connection);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new Impossible("Error while connecting to the database: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the persistence layer's database schema.
     *
     * @throws Impossible If the SQL error occurred during the database schema creation.
     */
    private void createDatabaseSchema() throws Impossible {
        try {
            String[] sqls = net.getDatabaseSchemaDeclaration().getDdlQueryString().split(";");

            for (String sql : sqls) {
                if (sql.trim().isEmpty()) {
                    continue;
                }

                Statement statement = connection.createStatement();
                statement.execute(sql);
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();

                throw new Impossible("Error while creating the database schema, it is rollbacked: " +
                        e.getMessage(), e);
            } catch (SQLException rollbackEx) {
                throw new Impossible("The database error occurred during performing the rollback: " +
                        rollbackEx.getMessage() + " after the error creating the database schema: " +
                        e.getMessage(), rollbackEx);
            }
        }
    }

    /**
     * Makes the transition instance for the given db-net transition.
     * Throws IllegalArgumentException if the given transition is not the db-net transition.
     *
     * @param transition The db-net transition.
     * @return The transition instance for the given db-net transition.
     */
    private TransitionInstance makeTransitionInstance(Transition transition) {
        if (transition instanceof DBNetTransition) {
            return new DBNetTransitionInstance(this, (DBNetTransition) transition);
        } else {
            throw new IllegalArgumentException("The transition should be instance of DBNetTransition");
        }
    }
}
