package de.renew.database.entitylayer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Hashtable;


/**
 * An SQLDialect defines the SQL dialect specific
 * strings, statements and behaviors.
 * This superclass implements a default dialect.
 */
public class SQLDialect {

    /**
     * Converts a string into an SQL
     * statement save from. This means any
     * character translation and escaping.
     * @param string The original string.
     * @return The converted string.
     */
    protected String convertStringForDB(String string) {
        int pos;

        pos = string.indexOf("\\");
        while (pos >= 0) {
            string = string.substring(0, pos) + "\\\\"
                     + string.substring(pos + 1);
            pos = string.indexOf("\\", pos + 2);
        }

        pos = string.indexOf("'");
        while (pos >= 0) {
            string = string.substring(0, pos) + "\\'"
                     + string.substring(pos + 1);
            pos = string.indexOf("'", pos + 2);
        }

        return string;
    }

    /**
     * Returns the SQL delete statement for a given
     * table name, primary key name and primary key value.
     * @param tableName The table name.
     * @param primaryKey The primary key attribute.
     * @param primaryKeyValue The primary key value.
     * @return The delete statement.
     */
    protected String getDeleteString(String tableName, Attribute[] primaryKey,
                                     Object[] primaryKeyValue) {
        return getDeleteString(tableName,
                               getStringOfAndedAttributesAndValues(primaryKey,
                                                                   primaryKeyValue));
    }

    /**
     * Returns the SQL delete statement for a given table name
     * that deletes some of the data sets.
     * @param tableName The table name.
     * @param condition The condition for the data sets to be
     * deleted. This is the string after the SQL WHERE symbol.
     * @return The delete statement.
     */
    protected String getDeleteString(String tableName, String condition) {
        StringBuffer delete = new StringBuffer();

        delete.append("delete from ");
        delete.append(tableName);
        if (!condition.equals("")) {
            delete.append(" where ");
            delete.append(condition);
        }

        return delete.toString();
    }

    /**
     * Returns the SQL insert statement
     * for a given table name, attributes
     * vector, values and primary key attribute.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @param values The values as Hashtable of
     * name/value pairs.
     * @param primaryKey The primary key attribute.
     * @return The insert statement.
     */
    protected String getInsertString(String tableName, Attribute[] attributes,
                                     Hashtable<String, Object> values,
                                     Attribute[] primaryKey) {
        StringBuffer update = new StringBuffer();

        update.append("insert into ");
        update.append(tableName);
        update.append(" (");
        update.append(getStringOfAttributes(attributes));
        update.append(") values (");
        update.append(getStringOfValues(attributes, values));
        update.append(")");

        return update.toString();
    }

    /**
     * Returns the SQL select statement
     * for a given table name and attributes.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @return The select statement.
     */
    protected String getSelectString(String tableName, Attribute[] attributes) {
        return getSelectString(tableName, attributes, null, null);
    }

    /**
     * Returns the SQL select statement
     * for a given table name, attributes
     * and condition string.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @param condition The condition string.
     * @return The select statement.
     */
    protected String getSelectString(String tableName, Attribute[] attributes,
                                     String condition) {
        return getSelectString(tableName, attributes, condition, null);
    }

    /**
     * Returns the SQL select statement
     * for a given table name, attributes,
     * condition string and order string.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @param condition The condition string.
     * May be null to be left out.
     * @param order The order string.
     * May be null to be left out.
     * @return The select statement.
     */
    protected String getSelectString(String tableName, Attribute[] attributes,
                                     String condition, String order) {
        StringBuffer query = new StringBuffer();

        query.append("select ");
        query.append(getStringOfAttributes(attributes));
        query.append(" from ");
        query.append(tableName);
        if (condition != null && !condition.equals("")) {
            query.append(" where ");
            query.append(condition);
        }
        if (order != null && !order.equals("")) {
            query.append(" order by ");
            query.append(order);
        }

        return query.toString();
    }

    /**
     * Returns the SQL select statement
     * for a given table name, attributes
     * vector, values, primary key attribute and
     * primary key value.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @param primaryKey The primary key attribute.
     * @param primaryKeyValue The primary key value.
     * @return The select statement.
     */
    protected String getSelectStringWithPrimaryKey(String tableName,
                                                   Attribute[] attributes,
                                                   Attribute[] primaryKey,
                                                   Object[] primaryKeyValue) {
        return getSelectString(tableName, attributes,
                               getStringOfAndedAttributesAndValues(primaryKey,
                                                                   primaryKeyValue));
    }

    /**
     * Returns SQL string for an anded attribute/value
     * equation term, used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @param values The values as Hashtable.
     * @return The SQL string.
     */
    protected String getStringOfAndedAttributesAndValues(Attribute[] attributes,
                                                         Hashtable<String, Object> values) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(" and ");
            }

            result.append(getStringOfAttributeAndValue(attributes[attributeNumber],
                                                       values.get(attributes[attributeNumber]
                                                                  .getName())));
        }

        return result.toString();
    }

    /**
     * Returns SQL string for an anded attribute/value
     * equation term, used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @param values The values as array.
     * @return The SQL string.
     */
    protected String getStringOfAndedAttributesAndValues(Attribute[] attributes,
                                                         Object[] values) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(" and ");
            }

            result.append(getStringOfAttributeAndValue(attributes[attributeNumber],
                                                       values[attributeNumber]));
        }

        return result.toString();
    }

    /**
     * Returns SQL string for an attribute/value equation
     * used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attribute The attribute.
     * @param value The value.
     * @return The SQL string.
     */
    protected String getStringOfAttributeAndValue(Attribute attribute,
                                                  Object value) {
        return attribute.getName() + "=" + getStringOfValue(attribute, value);
    }

    /**
     * Returns SQL string for an attribute list
     * used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @return The SQL string.
     */
    protected String getStringOfAttributes(Attribute[] attributes) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(", ");
            }

            result.append(attributes[attributeNumber].getName());
        }

        return result.toString();
    }

    /**
     * Returns SQL string for an attribute/value
     * equation list, used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @param values The values as Hashtable.
     * @return The SQL string.
     */
    protected String getStringOfAttributesAndValues(Attribute[] attributes,
                                                    Hashtable<String, Object> values) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(", ");
            }

            result.append(getStringOfAttributeAndValue(attributes[attributeNumber],
                                                       values.get(attributes[attributeNumber]
                                                                  .getName())));
        }

        return result.toString();
    }

    /**
     * Returns SQL string for an attribute/value
     * equation list, used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @param values The values as array.
     * @return The SQL string.
     */
    protected String getStringOfAttributesAndValues(Attribute[] attributes,
                                                    Object[] values) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(", ");
            }

            result.append(getStringOfAttributeAndValue(attributes[attributeNumber],
                                                       values[attributeNumber]));
        }

        return result.toString();
    }

    /**
     * Returns SQL string for an attribute/value
     * equation list with the primary key left out,
     * used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array.
     * @param values The values.
     * @param primaryKey The primary key attribute.
     * @return The SQL string.
     */
    protected String getStringOfAttributesAndValuesExceptPrimaryKey(Attribute[] attributes,
                                                                    Hashtable<String, Object> values,
                                                                    Attribute[] primaryKey) {
        StringBuffer result = new StringBuffer();

        boolean firstAttribute = true;
        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            int primaryKeyAttributeNumber;
            for (primaryKeyAttributeNumber = 0;
                         primaryKeyAttributeNumber < primaryKey.length;
                         primaryKeyAttributeNumber++) {
                if (attributes[attributeNumber].getName()
                                                       .equals(primaryKey[primaryKeyAttributeNumber]
                                                                       .getName())) {
                    break;
                }
            }

            if (primaryKeyAttributeNumber >= primaryKey.length) {
                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    result.append(", ");
                }

                result.append(getStringOfAttributeAndValue(attributes[attributeNumber],
                                                           values.get(attributes[attributeNumber]
                                                                      .getName())));
            }
        }

        return result.toString();
    }

    /**
     * Returns SQL string for a value
     * used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attribute The attribute of the value,
     * to determine the value's type.
     * @param value The value.
     * @return The SQL string.
     */
    protected String getStringOfValue(Attribute attribute, Object value) {
        if (value == null) {
            return "NULL";
        }

        String stringValue;
        if (value instanceof byte[]) {
            stringValue = new String((byte[]) value);
        } else {
            stringValue = value.toString();
        }

        switch (attribute.getType()) {
        case Attribute.TYPE_INT:
        case Attribute.TYPE_REAL:
            return stringValue;
        case Attribute.TYPE_CHAR:
            return "'" + convertStringForDB(stringValue) + "'";
        default:
            throw new IllegalStateException("Unknown attribute type");
        }
    }

    /**
     * Returns SQL string for a value list
     * used by most of the SQL dialects.
     * Use this method in the derived dialects.
     * @param attributes The attributes as array
     * to determine the values' types.
     * @param values The values.
     * @return The SQL string.
     */
    protected String getStringOfValues(Attribute[] attributes,
                                       Hashtable<String, Object> values) {
        StringBuffer result = new StringBuffer();

        for (int attributeNumber = 0; attributeNumber < attributes.length;
                     attributeNumber++) {
            if (attributeNumber > 0) {
                result.append(", ");
            }

            result.append(getStringOfValue(attributes[attributeNumber],
                                           values.get(attributes[attributeNumber]
                                                      .getName())));
        }

        return result.toString();
    }

    /**
     * Returns the SQL update statement
     * for a given table name, attributes
     * vector, values and primary key attribute.
     * @param tableName The table name.
     * @param attributes The attributes as array.
     * @param values The values as Hashtable of
     * name/value pairs.
     * @param primaryKey The primary key attribute.
     * @return The update statement.
     */
    protected String getUpdateString(String tableName, Attribute[] attributes,
                                     Hashtable<String, Object> values,
                                     Attribute[] primaryKey) {
        StringBuffer update = new StringBuffer();

        update.append("update ");
        update.append(tableName);
        update.append(" set ");
        update.append(getStringOfAttributesAndValuesExceptPrimaryKey(attributes,
                                                                     values,
                                                                     primaryKey));
        String condition = getStringOfAndedAttributesAndValues(primaryKey,
                                                               values);
        if (!condition.equals("")) {
            update.append(" where ");
            update.append(condition);
        }

        return update.toString();
    }

    /**
     * Sets the primary key value after a save
     * method's INSERT statement. Override this method
     * if the primary key value is only known after
     * the insert (for example, in a last insert variable).
     * The default behavior is to do nothing, since the
     * primary key is determined before the INSERT by default.
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
     * method's INSERT statement. Override this method
     * if the primary key value is known before
     * the insert (for example, as a sequence value).
     * The default behavior is to determine the primary
     * key by the highest existing primary key. This
     * is neither transaction-save nor save for removed
     * and reinserted rows, so you should override
     * this method.
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
        synchronized (entity.getClass()) {
            for (int attributeNumber = 0; attributeNumber < primaryKey.length;
                         attributeNumber++) {
                Statement statement = null;
                ResultSet resultSet = null;

                try {
                    StringBuffer query = new StringBuffer();
                    query.append("select max(");
                    query.append(primaryKey[attributeNumber].getName());
                    query.append(") from ");
                    query.append(entity.getTableName());
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(query.toString());

                    if (resultSet.next()) {
                        entity.setValue(primaryKey[attributeNumber].getName(),
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
}