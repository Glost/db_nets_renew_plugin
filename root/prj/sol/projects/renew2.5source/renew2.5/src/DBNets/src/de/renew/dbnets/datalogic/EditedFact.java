package de.renew.dbnets.datalogic;

import java.util.Collections;
import java.util.Map;

/**
 * The db-net's data logic layer's action's edited (added/deleted) fact -
 * the rows of the table being added to/deleted from the database during the action performing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class EditedFact {

    /**
     * The database relation's (table's) name.
     */
    private final String relationName;

    /**
     * The mapping of the columns to the params being replaced with values in the action call(-s).
     */
    private final Map<String, Object> columnsToParams;

    /**
     * The action's edited (added/deleted) fact's constructor.
     *
     * @param relationName The database relation's (table's) name.
     * @param columnsToParams The mapping of the columns to the params being replaced with values
     *                        in the action call(-s).
     */
    public EditedFact(String relationName, Map<String, Object> columnsToParams) {
        this.relationName = relationName;
        this.columnsToParams = columnsToParams;
    }

    /**
     * Returns the database relation's (table's) name.
     *
     * @return The database relation's (table's) name.
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * Returns the mapping of the columns to the params being replaced with values in the action call(-s).
     *
     * @return The mapping of the columns to the params being replaced with values in the action call(-s).
     */
    public Map<String, Object> getColumnsToParams() {
        return Collections.unmodifiableMap(columnsToParams);
    }
}
