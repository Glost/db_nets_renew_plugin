package de.renew.net;

import de.renew.engine.searcher.OccurrenceDescription;

import de.renew.expression.VariableMapper;


/**
 * The description of a transition occurrence.
 */
class TransitionOccurrenceDescription implements OccurrenceDescription {
    private TransitionInstance transitionInstance;
    private VariableMapper variableMapper;

    TransitionOccurrenceDescription(TransitionInstance transitionInstance,
                                    VariableMapper variableMapper) {
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
    }

    /**
     * Describe the transition occurrence by the means of transition
     * instance name and variables.
     */
    public String getDescription() {
        StringBuffer descr = new StringBuffer();
        descr.append(transitionInstance.toString());
        variableMapper.appendBindingsTo(descr);
        return descr.toString();
    }
}