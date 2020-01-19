package de.renew.net;

import de.renew.expression.VariableMapper;

import de.renew.unify.Impossible;

import java.io.Serializable;


public interface TokenSource extends Serializable {
    Object createToken(VariableMapper mapper) throws Impossible;
}