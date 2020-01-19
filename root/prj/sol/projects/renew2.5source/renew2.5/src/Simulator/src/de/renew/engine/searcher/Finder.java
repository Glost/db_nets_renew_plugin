package de.renew.engine.searcher;

public interface Finder {

    /**
     * Called whenever a valid binding is found. This method must implement
     * the desired actions: e.g. counting the number of
     * enabled bindings, record all enabled bindings,
     * record the first binding for later firing,
     * wait for an appropriate binding, etc.
     *
     * @param searcher the searcher that found the binding
     */
    public void found(Searcher searcher);

    /**
     * Return true if no other bindings should be tried.
     * After returning true once, the method must return true forever.
     */
    public boolean isCompleted();
}