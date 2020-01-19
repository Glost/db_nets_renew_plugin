package de.renew.database.entitylayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Hashtable;


/**
 * The SQL dialect for the Oracle database system.
 */
public class OracleDialect extends SQLDialect {

    /**
     * Doesn't do anything, since the primary key
     * value is known before the INSERT statement.
     * @param connection The connection to determine
     * the primary key from (if necessary).
     * @param entity The entity to set the primary key into.
     * @param values The values hashtable of the entity.
     * @see #setUniquePrimaryKeyValueBeforeInsert(Connection connection,
     * Entity entity, Hashtable values)
     */
    protected void setUniquePrimaryKeyValueAfterInsert(Connection connection,
                                                       Entity entity,
                                                       Hashtable<String, Object> values) {
    }

    /**
     * Sets the primary key value before a save
     * method's INSERT statement.
     * @param connection The connection to determine
     * the primary key from.
     * @param entity The entity to set the primary key into.
     * @param values The values hashtable of the entity.
     * @see #setUniquePrimaryKeyValueAfterInsert(Connection connection,
     * Entity entity, Hashtable values)
     */
    protected void setUniquePrimaryKeyValueBeforeInsert(Connection connection,
                                                        Entity entity,
                                                        Hashtable<String, Object> values)
            throws SQLException {
        Attribute[] primaryKey = entity.getPrimaryKey();
        if (primaryKey.length != 1) {
            throw new IllegalStateException("Auto-set primary key works only"
                                            + " on tables with exactly one primary key attribute.\n"
                                            + entity.getTableName()
                                            + " is not such a table.");
        }
        synchronized (entity.getClass()) {
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                StringBuffer query = new StringBuffer();
                query.append("select ");
                query.append(entity.getTableName());
                query.append("_seq.nextval from DUAL");
                statement = connection.createStatement();
                resultSet = statement.executeQuery(query.toString());

                if (resultSet.next()) {
                    entity.setValue(primaryKey[0].getName(),
                                    new Integer(resultSet.getInt(1) + 1));
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                    resultSet = null;
                }

                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }
    }
}