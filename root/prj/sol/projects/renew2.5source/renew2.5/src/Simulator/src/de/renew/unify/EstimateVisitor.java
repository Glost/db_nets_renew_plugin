package de.renew.unify;

import java.util.Collections;
import java.util.Set;


class EstimateVisitor implements TupleIndexVisitor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(EstimateVisitor.class);
    Set<Object> bestSet = null;

    void possibleCollection(Set<Object> set) {
        int size = set.size();
        if (bestSet == null || size < bestSet.size()) {
            if (logger.isTraceEnabled()) {
                logger.trace(" ---> found good enumeration: size " + size);
            }
            bestSet = set;
        }
    }

    public void visitBranch(ComponentBranch branch) {
        // The remainder cannot match any other elements besides those in
        // the element set of the branch.
        if (logger.isTraceEnabled()) {
            logger.trace("EstVis: visiting comp. branch " + branch.all);
        }
        possibleCollection(branch.all);
    }

    public boolean visitIndex(ArityBranch branch, Object remainder) {
        if (logger.isTraceEnabled()) {
            logger.trace("EstVis: visiting arity branch "
                         + branch.hashCodeRelation);
            logger.trace("        with remainder " + remainder);
        }
        if (Unify.isBound(remainder)) {
            // The remainder is well known. We can look up the
            // matching elements. There is no need to proceed
            // down the search tree.
            possibleCollection(branch.hashCodeRelation.elementsAt(TupleIndex
                .theHashCode(remainder)));
            return false;
        } else if (remainder instanceof Aggregate) {
            // The remainder is a non-empty aggregate that is at least partially
            // unbound. It will be processed later.
            return true;
        } else if (remainder instanceof Calculator) {
            // The remainder will be calculated later. Hence the entire
            // object cannot be bound now.
            possibleCollection(Collections.emptySet());
            return false;
        } else {
            // The remainder is an unknown or a variable.
            // All possible elements were already considered
            // in the node above the branch.
            return false;
        }
    }
}