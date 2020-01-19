package de.renew.watch;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;

import de.renew.unify.Copier;
import de.renew.unify.Variable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ParamFinder implements Finder {
    private Variable early;
    private Set<Object> values = new HashSet<Object>();

    public ParamFinder(Variable early) {
        this.early = early;
    }

    // Ok, let's record the settings of the
    // parameters which led to a successful binding.
    public void found(Searcher searcher) {
        // Copy the final form of the parameters
        // and insert them into the set.
        // The variable is supposed to be evaluated early,
        // so that it should be fully bound.
        values.add(new Copier().copy(early.getValue()));
    }

    // These finders cannot be interrupted.
    public boolean isCompleted() {
        return false;
    }

    public Iterator<Object> iterator() {
        return values.iterator();
    }
}