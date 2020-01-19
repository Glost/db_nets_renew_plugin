package de.renew.unify;

public interface TupleIndexVisitor {

    /**
     * Visit a component branch.
     **/
    public void visitBranch(ComponentBranch branch);

    /**
     * Visit an arity branch.
     * Return true, if traversal should proceed to deeper nodes.
     * This is the default.
     **/
    public boolean visitIndex(ArityBranch branch, Object remainder);
}