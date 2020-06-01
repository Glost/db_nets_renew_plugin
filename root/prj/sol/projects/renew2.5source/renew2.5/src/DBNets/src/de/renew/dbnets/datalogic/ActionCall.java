package de.renew.dbnets.datalogic;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Collections;

public class ActionCall implements TransitionInscription {

    private final String actionName;

    private final Collection<String> params;

    public ActionCall(String actionName, Collection<String> params) {
        this.actionName = actionName;
        this.params = params;
    }

    public String getActionName() {
        return actionName;
    }

    public Collection<String> getParams() {
        return params;
    }

    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        // TODO: Check whether occurrences are necessary here or not.
        return Collections.emptySet();
    }
}
