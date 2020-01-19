package de.renew.net.event;

import de.renew.expression.VariableMapper;

import de.renew.net.TransitionInstance;


/**
 * This event occurs when a TransitionInstance starts a new
 * Occurrence or completes a running Occurrence.
 */
public class FiringEvent extends TransitionEvent {

    /** The variable binding of the Occurrence. */
    protected VariableMapper mapper;

    /** Constructs a new FiringEvent using the given TransitionInstance
     *  and the given Binding.
     */
    public FiringEvent(TransitionInstance instance, VariableMapper mapper) {
        super(instance);
        this.mapper = mapper;
    }

    /** Returns the variable mapper of the Occurrence. */
    public VariableMapper getVariableMapper() {
        return mapper;
    }
}