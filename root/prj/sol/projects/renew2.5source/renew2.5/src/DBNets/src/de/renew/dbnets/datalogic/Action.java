package de.renew.dbnets.datalogic;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;

public class Action {

    private static final Logger logger = Logger.getLogger(Action.class);

    private final String name;

    private final Collection<String> params;

    private final Collection<EditedFact> addedFacts;

    private final Collection<EditedFact> deletedFacts;

    public Action(String name,
                  Collection<String> params,
                  Collection<EditedFact> addedFacts,
                  Collection<EditedFact> deletedFacts) {
        this.name = name;
        this.params = params;
        this.addedFacts = addedFacts;
        this.deletedFacts = deletedFacts;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getParams() {
        return Collections.unmodifiableCollection(params);
    }

    public Collection<EditedFact> getAddedFacts() {
        return Collections.unmodifiableCollection(addedFacts);
    }

    public Collection<EditedFact> getDeletedFacts() {
        return Collections.unmodifiableCollection(deletedFacts);
    }

    public void performAction() {
        // TODO: implement the method.
        logger.info("Performing action: " + name);
    }

    public void rollbackAction() {
        // TODO: implement the method.
        logger.info("Rollbacking action: " + name);
    }
}
