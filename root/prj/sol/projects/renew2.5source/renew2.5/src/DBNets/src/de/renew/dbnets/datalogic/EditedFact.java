package de.renew.dbnets.datalogic;

import java.util.Collections;
import java.util.List;

public class EditedFact {

    private final String relationName;

    private final List<String> paramsValues;

    public EditedFact(String relationName, List<String> paramsValues) {
        this.relationName = relationName;
        this.paramsValues = paramsValues;
    }

    public String getRelationName() {
        return relationName;
    }

    public List<String> getParamsValues() {
        return Collections.unmodifiableList(paramsValues);
    }
}
