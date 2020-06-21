package de.renew.dbnets.datalogic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The db-net's data logic layer's action for modifying the persistence layer's data.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class Action {

    /**
     * The action's name.
     */
    private final String name;

    /**
     * The action's params.
     */
    private final List<String> params;

    /**
     * The added facts - the rows of the tables being added to the database during the action performing.
     */
    private final Collection<EditedFact> addedFacts;

    /**
     * The deleted facts - the rows of the tables being deleted to the database during the action performing.
     */
    private final Collection<EditedFact> deletedFacts;

    /**
     * The action's constructor.
     *
     * @param name The action's name.
     * @param params The action's params.
     * @param addedFacts The rows of the tables being added to the database during the action performing.
     * @param deletedFacts The rows of the tables being deleted to the database during the action performing.
     */
    public Action(String name,
                  List<String> params,
                  Collection<EditedFact> addedFacts,
                  Collection<EditedFact> deletedFacts) {
        this.name = name;
        this.params = params;
        this.addedFacts = addedFacts;
        this.deletedFacts = deletedFacts;
    }

    /**
     * Returns the action's name.
     *
     * @return The action's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the action's params.
     *
     * @return The action's params.
     */
    public List<String> getParams() {
        return Collections.unmodifiableList(params);
    }

    /**
     * Returns the added facts - the rows of the tables being added to the database during the action performing.
     *
     * @return The added facts - the rows of the tables being added to the database during the action performing.
     */
    public Collection<EditedFact> getAddedFacts() {
        return Collections.unmodifiableCollection(addedFacts);
    }

    /**
     * Returns the deleted facts - the rows of the tables being deleted to the database during the action performing.
     *
     * @return The deleted facts - the rows of the tables being deleted to the database during the action performing.
     */
    public Collection<EditedFact> getDeletedFacts() {
        return Collections.unmodifiableCollection(deletedFacts);
    }
}
