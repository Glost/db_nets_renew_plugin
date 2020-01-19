package de.renew.database;

import de.renew.database.entitylayer.Entity;
import de.renew.database.entitylayer.NetInstanceEntity;
import de.renew.database.entitylayer.SQLDialect;
import de.renew.database.entitylayer.StateEntity;
import de.renew.database.entitylayer.TokenEntity;
import de.renew.database.entitylayer.TokenPositionEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Enumeration;
import java.util.Vector;


// Parameters example:
// jdbc:mysql://localhost/SIMULATION
// org.gjt.mm.mysql.Driver
// de.renew.database.entitylayer.MySqlSQLDialect
public class SimulationStateDumper {
    private static void dumpTable(Entity entityTemplate)
            throws SQLException {
        System.out.println(entityTemplate.getTableName() + ":");

        Vector<Entity> entities = Entity.getEntities(entityTemplate);
        Enumeration<Entity> entitiesEnum = entities.elements();
        while (entitiesEnum.hasMoreElements()) {
            Entity entity = entitiesEnum.nextElement();
            System.out.println("   " + entity.toString());
        }

        System.out.println("");
    }

    public static void dumpSimulationState(Connection connection,
                                           SQLDialect dialect)
            throws SQLException {
        dumpTable(new StateEntity(connection, dialect));
        dumpTable(new NetInstanceEntity(connection, dialect));
        dumpTable(new TokenEntity(connection, dialect));
        dumpTable(new TokenPositionEntity(connection, dialect));
    }

    public static void main(String[] args) {
        if (args.length != 3 || args[0].charAt(0) == '-') {
            System.out.println("SimulationStateDumper.\n"
                               + "Displays the current simulation state in the database.\n"
                               + "This includes all nets, places, tokens and token positions.\n"
                               + "Parameters: <database url> <database driver class>"
                               + " <database dialect class>\n"
                               + "Database dialect may be generic, oracle, msql or mysql.");
            System.exit(0);
        }

        try {
            Class<?> driverClass = Class.forName(args[1]);
            Constructor<?> constructor = driverClass.getConstructor(new Class[0]);
            constructor.newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Database driver class not found.");
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.out.println("Error: Database driver's default constructor"
                               + " is not accessable.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Database driver's default constructor"
                               + " cannot be invoked.");
            System.exit(1);
        } catch (InstantiationException e) {
            System.out.println("Error: Database driver is abstract or"
                               + " an interface.");
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.out.println("Error: Database driver's default constructor"
                               + " threw an exception:\n"
                               + e.getTargetException());
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.out.println("Error: Database driver has no"
                               + " default constructor.");
            System.exit(1);
        } catch (SecurityException e) {
            System.out.println("Error: Database driver's default constructor"
                               + " is not accessable.");
            System.exit(1);
        }

        SQLDialect dialect = null;
        try {
            Class<?> dialectClass = Class.forName(args[2]);
            Constructor<?> constructor = dialectClass.getConstructor(new Class<?>[0]);
            dialect = (SQLDialect) constructor.newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Error: SQL dialect class not found.");
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.out.println("Error: SQL dialect's default constructor"
                               + " is not accessable.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: SQL dialect's default constructor"
                               + " cannot be invoked.");
            System.exit(1);
        } catch (InstantiationException e) {
            System.out.println("Error: SQL dialect is abstract or"
                               + " an interface.");
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.out.println("Error: SQL dialect's default constructor"
                               + " threw an exception:\n"
                               + e.getTargetException());
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.out.println("Error: SQL dialect has no"
                               + " default constructor.");
            System.exit(1);
        } catch (SecurityException e) {
            System.out.println("Error: SQL dialect's default constructor"
                               + " is not accessable.");
            System.exit(1);
        }

        Connection connection = null;
        try {
            try {
                connection = DriverManager.getConnection(args[0]);
            } catch (SQLException e) {
                System.out.println("Error: Cannot connect to database:\n"
                                   + e.toString());
                System.exit(1);
            }

            try {
                dumpSimulationState(connection, dialect);
            } catch (SQLException e) {
                System.out.println("Error: Exception while dumping:\n"
                                   + e.toString());
                System.exit(1);
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
                connection = null;
            }
        }
    }
}