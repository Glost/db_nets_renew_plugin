package de.renew.net;

import de.renew.database.Transaction;
import de.renew.database.TransactionSource;
import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.NetInstantiation;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.unify.Impossible;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DBNetControlLayerInstance extends NetInstanceImpl {

    private DBNetControlLayer net;

    private Map<Object, Object> instanceLookup;

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
    }

    private TransitionInstance makeTransitionInstance(Transition transition) {
        if (transition instanceof DBNetTransition) {
            return new DBNetTransitionInstance(this, (DBNetTransition) transition);
        } else {
            return new TransitionInstance(this, transition);
        }
    }
}
