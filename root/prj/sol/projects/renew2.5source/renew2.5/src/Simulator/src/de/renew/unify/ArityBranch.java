package de.renew.unify;

import de.renew.util.HashedRelation;

import java.io.Serializable;


public class ArityBranch implements Serializable {
    /**
     * This relation maps hash codes to sets
     * of tokens that exhibit the hashcode at
     * the component currently under investigation.
     **/
    HashedRelation<Integer, Object> hashCodeRelation = new HashedRelation<Integer, Object>();

    // This array stores the branches in the following way:
    // 0: list pairs
    // 1: 1-tuples
    // 2: 2-tuples
    // 3: 3-tuples
    // and so on. This simplifies some procedures.
    private ComponentBranch[] branches = new ComponentBranch[0];

    public ArityBranch() {
    }

    private void setCapacity(int cap) {
        ComponentBranch[] newBranches = new ComponentBranch[cap];
        System.arraycopy(branches, 0, newBranches, 0,
                         Math.min(cap, branches.length));
        branches = newBranches;
    }

    private void ensureCapacity(int cap) {
        if (branches.length < cap) {
            setCapacity(cap);
        }
    }

    private void trim() {
        int cap = branches.length;
        while (cap > 0 && branches[cap - 1] == null) {
            cap--;
        }
        if (cap < branches.length) {
            setCapacity(cap);
        }
    }

    void traverse(TupleIndexVisitor visitor, Object remainder) {
        if (visitor.visitIndex(this, remainder)) {
            Aggregate aggregate = null;
            int arity;
            int index;

            if (remainder instanceof Aggregate) {
                aggregate = (Aggregate) remainder;
                arity = aggregate.references.length;
                if (arity == 0) {
                    // No need to traverse deeper into the search tree.
                    // The object in question is a nullary tuple or an empty list.
                    // During the search it is either known and
                    // indexed in the relation or unknown and
                    // indexed in the parent component branch.
                    return;
                } else if (remainder instanceof Tuple) {
                    // It is a non-empty tuple.
                    // Non-empty tuples are stored in branch arity.
                    index = arity;
                } else {
                    // It is a list pair.
                    // List pairs are stored in branch 0.
                    index = 0;
                }
            } else {
                // No need to traverse deeper into the search tree.
                // The object in question is an ordinary Java object.
                // During the search it is either known and
                // indexed in the relation or unknown and
                // indexed in the parent component branch.
                return;
            }


            // Make sure to have something traversable.
            ensureCapacity(index + 1);
            if (branches[index] == null) {
                branches[index] = new ComponentBranch(arity);
            }


            // Traverse.
            branches[index].traverse(visitor, aggregate);

            // Make sure to clean up, if nothing stored.
            if (branches[index].size == 0) {
                branches[index] = null;
                trim();
            }
        }
    }
}