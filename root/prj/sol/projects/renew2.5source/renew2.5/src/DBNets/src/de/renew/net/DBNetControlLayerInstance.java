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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DBNetControlLayerInstance extends NetInstanceImpl {

    private DBNetControlLayer net;

    private Map<Object, Object> instanceLookup;

    private Connection connection;

    public DBNetControlLayerInstance() {
    }

    public DBNetControlLayerInstance(DBNetControlLayer net) throws Impossible {
        initNet(net, true);
    }

    public DBNetControlLayerInstance(DBNetControlLayer net, boolean wantInitialTokens) throws Impossible {
        initNet(net, wantInitialTokens);
    }

    @Override
    public DBNetControlLayer getNet() {
        return net;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public Object getInstance(Object netObject) {
        return instanceLookup.get(netObject);
    }

    @Override
    public PlaceInstance getInstance(Place place) {
        return (PlaceInstance) instanceLookup.get(place);
    }

    @Override
    public TransitionInstance getInstance(Transition transition) {
        return (TransitionInstance) instanceLookup.get(transition);
    }

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

    @Override
    public void earlyConfirmationTrace(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";

        net.places().stream()
                .map(this::getInstance)
                .forEach(placeInstance -> placeInstance.earlyConfirmationTrace(stepIdentifier));

        NetInstanceList.add(this);
    }

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

    private void createPersistenceLayer() throws Impossible {
        createDatabaseConnection();
        createDatabaseSchema();
    }

    private void createDatabaseConnection() throws Impossible {
        try {
            connection = DriverManager.getConnection(net.getJdbcConnection().getUrl().trim());
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new Impossible("Error while connecting to the database: " + e.getMessage(), e);
        }
    }

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

//            String seqSql = "CREATE TABLE IF NOT EXISTS dbn_seq (id BIGSERIAL PRIMARY KEY AUTOINCREMENT);";
//
//            Statement statement = connection.createStatement();
//            statement.execute(seqSql);
//
//            String seqInsertSql = "INSERT INTO dbn_seq DEFAULT VALUES;";
//
//            statement = connection.createStatement();
//            statement.execute(seqInsertSql);

            connection.commit();
        } catch (SQLException e) {
            throw new Impossible("Error while creating the database schema: " + e.getMessage(), e);
        }
    }

    private TransitionInstance makeTransitionInstance(Transition transition) {
        if (transition instanceof DBNetTransition) {
            return new DBNetTransitionInstance(this, (DBNetTransition) transition);
        } else {
            return new TransitionInstance(this, transition);
        }
    }
}
