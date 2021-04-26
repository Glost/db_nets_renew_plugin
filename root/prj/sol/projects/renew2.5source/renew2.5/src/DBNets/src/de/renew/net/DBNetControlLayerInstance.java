package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;
import de.renew.dbnets.persistence.JdbcConnectionInstance;
import de.renew.dbnets.persistence.SQLiteJdbcConnectionInstance;
import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.NetInstantiation;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.unify.Impossible;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The db-net's control layer's instance for the simulation.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DBNetControlLayerInstance extends NetInstanceImpl {

    /**
     * The mapping from the connections' JDBC urls to the connections themselves.
     */
    private static final Map<String, JdbcConnectionInstance> connectionsMap = new HashMap<>();

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
    private JdbcConnectionInstance connectionInstance;

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
     * @throws Throwable If any error occurred.
     */
    @Override
    protected void finalize() throws Throwable {
        if (Objects.nonNull(connectionInstance)) {
            connectionInstance.close();
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
    public JdbcConnectionInstance getConnectionInstance() {
        return connectionInstance;
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
     * @throws Impossible If the SQL error occurred during the persistence layer creation
     * or during closing the previous open database connection for the same JDBC URL.
     */
    private void createPersistenceLayer() throws Impossible {
        String jdbcUrl = net.getJdbcConnection().getUrl().trim();
        String ddlQueryString = net.getDatabaseSchemaDeclaration().getDdlQueryString();

        closePreviousConnection(jdbcUrl);

        // TODO: Selecting necessary implementation of the JdbcConnectionInstance interface.
        connectionInstance = new SQLiteJdbcConnectionInstance();
        connectionInstance.init(jdbcUrl, ddlQueryString);

        connectionsMap.put(jdbcUrl, connectionInstance);
    }

    /**
     * Closes the previous open database connection for the same JDBC URL if exists.
     *
     * @param jdbcUrl The JDBC URL for which the previous open database connection should be closed.
     * @throws Impossible If the SQL error occurred during closing the previous open database connection.
     */
    private void closePreviousConnection(String jdbcUrl) throws Impossible {
        JdbcConnectionInstance previousConnection = connectionsMap.get(jdbcUrl);

        if (Objects.nonNull(previousConnection)) {
            try {
                previousConnection.close();
                connectionsMap.remove(jdbcUrl);
            } catch (SQLException e) {
                throw new Impossible("Error while closing previous connection to the database: " + e.getMessage(), e);
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
