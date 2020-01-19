package de.uni_hamburg.fs;

import java.util.Enumeration;


public abstract class AbstractNode implements Node {
    protected Type nodetype;

    protected AbstractNode(Type nodetype) {
        this.nodetype = nodetype;
    }

    protected AbstractNode() {
        this(Type.ANY);
    }

    protected AbstractNode(String nodetype) throws UnificationFailure {
        this(ConjunctiveType.getType(nodetype));
    }

    public Type getType() {
        return nodetype;
    }

    /** Returns the Node at the given Path.
     *  The empty path returns the Node itself.
     *  The exception is thrown if at any point, the feature given
     *  by the path does not exist in the current Node.
     */
    public Node delta(Path path) throws NoSuchFeatureException {
        Enumeration<?> featenumeration = path.features();
        Node curr = this;
        while (featenumeration.hasMoreElements()) {
            curr = curr.delta((Name) featenumeration.nextElement());
        }
        return curr;
    }
}