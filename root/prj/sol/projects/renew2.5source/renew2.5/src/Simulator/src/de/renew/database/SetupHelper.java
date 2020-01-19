package de.renew.database;

import de.renew.database.entitylayer.Entity;
import de.renew.database.entitylayer.NetInstanceEntity;
import de.renew.database.entitylayer.SQLDialect;
import de.renew.database.entitylayer.TokenEntity;
import de.renew.database.entitylayer.TokenPositionEntity;

import de.renew.net.IDRegistry;
import de.renew.net.IDSource;
import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;
import de.renew.net.TrivialIDFactory;

import de.renew.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;


/**
 * This class contains some static utility methods that allow
 * the caller to reestablish a simulation state.
 */
public class SetupHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SetupHelper.class);

    /**
     * This class is totally static. One must not create instances
     * of it.
     */
    private SetupHelper() {
    }

    /**
     * This method restores a set of nets from a database source.
     * For the correct usage of this method it is required
     * that no simulator is currently running. Additionally,
     * a transaction source must not yet have been established,
     * otherwise the database might get corrupted.
     *
     * @param source the database source
     *
     * @return A NetInstanceMap of all restored NetInstances.
     *
     * @exception Exception If any exception occurs.
     * These exceptions are thrown by the RestoreSource
     * interface implementations.
     */
    public static NetInstanceMap restoreNetInstances(RestoreSource source)
            throws Exception {
        // Create all net instances with their initial markings.
        String[] netIDs = source.getAllNetIDs();

        // Create arrays for the reestablished nets.
        int n = netIDs.length;
        Net[] nets = new Net[n];
        NetInstance[] instances = new NetInstance[n];
        NetInstanceMap map = new NetInstanceMap();

        // Create all net instances.
        for (int i = 0; i < n; i++) {
            // Create using the initial marking.
            String id = netIDs[i];
            Net net = Net.forName(source.getNetName(id));
            nets[i] = net;
            NetInstance instance = net.buildInstance();
            instance.setID(id);
            instances[i] = instance;
            map.put(id, instance);


            // Remove old tokens and discard them.
            Iterator<Place> places = net.places().iterator();
            while (places.hasNext()) {
                Place place = places.next();
                PlaceInstance pi = instance.getInstance(place);

                pi.lock.lock();
                try {
                    pi.extractAllTokens(new Vector<Object>(),
                                        new Vector<Double>());
                } finally {
                    pi.lock.unlock();
                }
            }
        }

        Hashtable<String, Object> idToToken = source.getTokens(map);


        // Establish all token IDs.
        Enumeration<String> enumeration = idToToken.keys();
        while (enumeration.hasMoreElements()) {
            String id = enumeration.nextElement();
            Object token = idToToken.get(id);
            IDRegistry.getInstance().setAndReserveID(token, id);
        }

        // Change to the saved marking.
        for (int i = 0; i < n; i++) {
            NetInstance instance = instances[i];

            Iterator<Place> places = nets[i].places().iterator();
            while (places.hasNext()) {
                Place place = places.next();
                PlaceInstance pi = instance.getInstance(place);

                pi.lock.lock();
                try {
                    // Add new tokens.
                    Vector<String> ids = new Vector<String>();
                    source.fillinAllTokens(pi, ids);
                    int num = ids.size();
                    for (int j = 0; j < num; j++) {
                        String id = ids.elementAt(j);
                        Object token = idToToken.get(id);

                        // The time does not matter: time=0.
                        pi.insertTokenWithID(token, id, 0);
                    }
                } finally {
                    pi.lock.unlock();
                }
            }
        }


        // Make sure to make token IDs discardable.
        // No token IDs should be discarded right now,
        // but we have to undo one reservation.
        enumeration = idToToken.keys();
        while (enumeration.hasMoreElements()) {
            String id = enumeration.nextElement();
            Object token = idToToken.get(id);
            IDRegistry.getInstance().unreserve(token);
        }

        return map;
    }

    /**
     * This method is one of the main entry points of this class.
     * It simplifies the setup of a simulation
     * from a database with certain net instances and tokens.
     * Despite of the parametered overload, it takes all information
     * from the system properties de.renew.simdb.driver,
     * de.renew.simdb.dialect, de.renew.simdb.url, de.renew.simdb.user and
     * de.renew.password. If they do not exist, no simulation is restored, so
     * you can savely call this method in any case. It also reopens all open
     * net instance drawings, so this method can be used for the CPNApplication
     * database restore.
     * @param properties The properties used for the simulation setup.
     * @return The previous simulation state object grouping the net instances
     * with an open instance drawing and the last run state of the simulation.
     */
    public static SimulationState setup(Properties properties) {
        //    Entity.setMessageStream(System.err);
        String dbDriver = properties.getProperty("de.renew.simdb.driver");
        String dbDialect = properties.getProperty("de.renew.simdb.dialect");
        String dbUrl = properties.getProperty("de.renew.simdb.url");
        String dbUser = properties.getProperty("de.renew.simdb.user");
        String dbPassword = properties.getProperty("de.renew.simdb.password");

        if (dbUrl == null && dbDriver == null) {
            return SimulationState.TERMINATED_STATE;
        }

        if (dbUrl == null || dbDriver == null) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Error: Specify de.renew.simdb.url and"
                         + " de.renew.simdb.driver system properties together"
                         + " to use a simulation database.");
            return SimulationState.TERMINATED_STATE;
        }

        if (dbPassword == null) {
            dbPassword = "";
        }

        if (dbDialect == null) {
            dbDialect = "de.renew.database.entitylayer.SQLDialect";
            logger.warn(SetupHelper.class.getSimpleName()
                        + ": The system property"
                        + " de.renew.simdb.dialect has not been specified. Using"
                        + " default dialect " + dbDialect);
        }

        ClassLoader classLoader = PluginManager.getInstance()
                                               .getBottomClassLoader();
        try {
            Class<?> driverClass = Class.forName(dbDriver, true, classLoader);
            Constructor<?> constructor = driverClass.getConstructor(new Class[0]);
            constructor.newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class " + dbDriver + " not found.");
            return SimulationState.TERMINATED_STATE;
        } catch (IllegalAccessException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class's default constructor"
                         + " is not accessable.");
            return SimulationState.TERMINATED_STATE;
        } catch (IllegalArgumentException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class's default constructor"
                         + " cannot be invoked.");
            return SimulationState.TERMINATED_STATE;
        } catch (InstantiationException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class is abstract or an interface.");
            return SimulationState.TERMINATED_STATE;
        } catch (InvocationTargetException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class's default constructor"
                         + " threw an exception:\n" + e.getTargetException());
            return SimulationState.TERMINATED_STATE;
        } catch (NoSuchMethodException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class has no default constructor.");
            return SimulationState.TERMINATED_STATE;
        } catch (SecurityException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "Database driver class's default constructor"
                         + " is not accessable.");
            return SimulationState.TERMINATED_STATE;
        }

        SQLDialect dialect = null;
        try {
            Class<?> dialectClass = Class.forName(dbDialect, true, classLoader);
            Constructor<?> constructor = dialectClass.getConstructor(new Class[0]);
            dialect = (SQLDialect) constructor.newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class " + dbDialect + " not found.");
            return SimulationState.TERMINATED_STATE;
        } catch (IllegalAccessException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class's default constructor"
                         + " is not accessable.");
            return SimulationState.TERMINATED_STATE;
        } catch (IllegalArgumentException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class's default constructor"
                         + " cannot be invoked.");
            return SimulationState.TERMINATED_STATE;
        } catch (InstantiationException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect is an abstract class or an interface.");
            return SimulationState.TERMINATED_STATE;
        } catch (InvocationTargetException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class's default constructor"
                         + " threw an exception:\n" + e.getTargetException());
            return SimulationState.TERMINATED_STATE;
        } catch (NoSuchMethodException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class has no default constructor.");
            return SimulationState.TERMINATED_STATE;
        } catch (SecurityException e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot set simulation database.\n"
                         + "SQL dialect class's default constructor"
                         + " is not accessable.");
            return SimulationState.TERMINATED_STATE;
        }

        DatabaseRestoreSource restoreSource = null;
        List<NetInstance> netInstances = new ArrayList<NetInstance>();
        Connection connection;
        try {
            if (logger.isInfoEnabled()) {
                logger.info(SetupHelper.class.getSimpleName() + ": "
                            + "Connecting to simulation database " + dbUrl
                            + "...");
            }
            if (dbUser != null) {
                connection = DriverManager.getConnection(dbUrl, dbUser,
                                                         dbPassword);
                connection.setAutoCommit(false);
            } else {
                connection = DriverManager.getConnection(dbUrl);
                connection.setAutoCommit(false);
            }
            restoreSource = new DatabaseRestoreSource(connection, dialect);
            if (logger.isInfoEnabled()) {
                logger.info(SetupHelper.class.getSimpleName() + ": "
                            + "Connected.");
            }


            // We are using the database.
            // Make sure that all nets use a global IDRegistry,
            // so that we can keep a global list of tokens.
            de.renew.net.NetInstanceImpl.useGlobalIDRegistry = true;

            if (restoreSource.wasSimulationInited()) {
                if (logger.isInfoEnabled()) {
                    logger.info(SetupHelper.class.getSimpleName() + ": "
                                + "Loading simulation state...");
                }

                NetInstanceMap map = SetupHelper.restoreNetInstances(restoreSource);
                IDSource.setFactory(new TrivialIDFactory(restoreSource.getLastId()
                                                         + 1));

                String[] viewedNetInstanceIds = restoreSource.getViewedNetIDs();
                for (int i = 0; i < viewedNetInstanceIds.length; i++) {
                    netInstances.add(map.get(viewedNetInstanceIds[i]));
                }

                if (logger.isInfoEnabled()) {
                    logger.info(SetupHelper.class.getSimpleName() + ": "
                                + "Simulation state loaded.");
                }
            } else {
                SetupHelper.clearDatabase(connection, dialect);
                if (logger.isDebugEnabled()) {
                    logger.debug(SetupHelper.class.getSimpleName() + ": "
                                 + "Cleared database.");
                }
            }
            connection.commit();
        } catch (Exception e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot load simulation state.\n"
                         + "There was an exception:");
            logger.error(SetupHelper.class.getSimpleName() + ": "
                         + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(SetupHelper.class.getSimpleName() + ": " + e);
            }
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Simulation database abandonned.");
            return SimulationState.TERMINATED_STATE;
        }

        try {
            if (dbUser != null) {
                TransactionSource.setStrategy(new DatabaseTransactionStrategy(dbUrl,
                                                                              dbUser,
                                                                              dbPassword,
                                                                              dialect));
            } else {
                TransactionSource.setStrategy(new DatabaseTransactionStrategy(dbUrl,
                                                                              dialect));
            }
        } catch (Exception e) {
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Cannot assign simulation database.\n"
                         + "There was an exception:");
            logger.error(SetupHelper.class.getSimpleName() + ": "
                         + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(SetupHelper.class.getSimpleName() + ": " + e);
            }
            logger.error(SetupHelper.class.getSimpleName()
                         + ": Simulation database abandonned.");
            return SimulationState.TERMINATED_STATE;
        }

        return new SimulationState(netInstances, restoreSource);
    }

    /**
     * Clears the simulation database, so that no net instance,
     * token, nor token position is contained. The simulation state
     * is not touched. This is required when the simulation state
     * is not initied and a new simulation is going to get initied,
     * because in this case, the database contains the last aborted
     * simulation.
     * @exception SQLException The clearing couldn't
     * be performed because of a database problem.
     */
    private static void clearDatabase(Connection connection, SQLDialect dialect)
            throws SQLException {
        Entity.deleteEntities(new TokenPositionEntity(connection, dialect));
        Entity.deleteEntities(new TokenEntity(connection, dialect));
        Entity.deleteEntities(new NetInstanceEntity(connection, dialect));
    }

    /**
     * This class groups the net instances with an open instance drawing
     * and the last run state of the simulation.
     * Objects of this class are returned by the setup method.
     */
    public static class SimulationState {

        /**
         * The terminated simulation state. This is the initial
         * state with no open net instances and no initialized simulation.
         */
        public static final SimulationState TERMINATED_STATE = new SimulationState(Collections
                                                                                   .<NetInstance>emptyList(),
                                                                                   null);

        /**
         * A list of net instances that had an open net instance drawing.
         */
        private List<NetInstance> netInstances;

        /**
         * Whether the simulation run state was inited.
         */
        private boolean wasSimulationInitiedFlag;

        /**
         * Whether the simulation run state was running.
         */
        private boolean wasSimulationRunningFlag;

        /**
         * Creates a new simulation state object.
         * @param netInstances A list of net instances that had an
         * open net instance drawing.
         * @param source The restore source used to read the simulation state.
         */
        private SimulationState(List<NetInstance> netInstances,
                                RestoreSource source) {
            this.netInstances = netInstances;
            try {
                wasSimulationInitiedFlag = source != null
                                           && source.wasSimulationInited();
                wasSimulationRunningFlag = source != null
                                           && source.wasSimulationRunning();
            } catch (Exception e) {
                logger.error("Cannot determine simulation run state:");
                logger.error(e.getMessage(), e);
                wasSimulationInitiedFlag = false;
                wasSimulationRunningFlag = false;
            }
        }

        /**
         * A list of net instances that had an open net instance drawing.
         */
        public List<NetInstance> getNetInstances() {
            return netInstances;
        }

        /**
         * Returns whether the simulation run state was inited (not terminated).
         * @return Whether the simulation run state was inited.
         */
        public boolean wasSimulationInited() {
            return wasSimulationInitiedFlag;
        }

        /**
         * Returns whether the simulation run state was running.
         * @return Whether the simulation run state was running.
         */
        public boolean wasSimulationRunning() {
            return wasSimulationRunningFlag;
        }
    }
}