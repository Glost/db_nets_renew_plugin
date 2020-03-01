package de.renew.dbnets.datalogic;

import java.util.Collection;
import java.util.Collections;

public class EditedFact {

    private final String relationName;

    private final Collection<String> paramsValues;

    public EditedFact(String relationName, Collection<String> paramsValues) {
        this.relationName = relationName;
        this.paramsValues = paramsValues;
    }

    public String getRelationName() {
        return relationName;
    }

    public Collection<String> getParamsValues() {
        return Collections.unmodifiableCollection(paramsValues);
    }
}
