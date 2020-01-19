package de.renew.expression;

import de.renew.unify.Impossible;

import java.io.Serializable;


public interface NoArgFunction extends Serializable {
    public Object function() throws Impossible;
}