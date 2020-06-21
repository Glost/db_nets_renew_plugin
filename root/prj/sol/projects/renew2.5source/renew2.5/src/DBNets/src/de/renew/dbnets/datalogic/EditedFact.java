package de.renew.dbnets.datalogic;

import java.util.Collections;
import java.util.Map;

/**
 * The action's edited (added/deleted) fact - the rows of the table being added to/deleted from
 * the database during the action performing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
