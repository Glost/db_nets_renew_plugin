package de.renew.engine.searcher;

import de.renew.unify.Variable;


public interface UplinkProvider {
    public void bindChannel(Variable params, Searcher searcher);
}