package de.renew.expression;

import de.renew.unify.Impossible;

import java.io.Serializable;


public interface Function extends Serializable {
    public Object function(Object arg) throws Impossible;
}