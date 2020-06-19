package de.renew.dbnets.datalogic;

import java.util.Collections;
import java.util.Map;

public class EditedFact {

    private final String relationName;

    private final Map<String, Object> columnsToParams;

    public EditedFact(String relationName, Map<String, Object> columnsToParams) {
        this.relationName = relationName;
        this.columnsToParams = columnsToParams;
    }

    public String getRelationName() {
        return relationName;
    }

    public Map<String, Object> getColumnsToParams() {
        return Collections.unmodifiableMap(columnsToParams);
    }
}
