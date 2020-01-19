package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.LinkedList;
import collections.UpdatableSeq;


public class ListNode extends AbstractNode {
    private Node hd = null;
    private Node tl = null;

    public ListNode(ListType listtype) {
        super(listtype);
    }

    public ListNode(ListType listtype, Node hd, Node tl)
            throws TypeException {
        super(listtype);
        Type basetype = getBaseType();
        if (hd.getType().canUnify(basetype)
                    && tl.getType().canUnify(new ListType(basetype))) {
            this.hd = hd;
            this.tl = tl;
        } else {
            throw new TypeException();
        }
    }

    public Type getBaseType() {
        return ((ListType) nodetype).getBaseType();
    }

    public CollectionEnumeration featureNames() {
        UpdatableSeq feats = new LinkedList();
        if (hd != null) {
            feats.insertLast(ListType.HEAD);
        }
        if (tl != null) {
            feats.insertLast(ListType.TAIL);
        }
        return feats.elements();
        //      return nodetype.appropFeatureNames();
    }

    private boolean isHead(Name featureName) {
        return ListType.HEAD.equals(featureName);
    }

    private boolean isTail(Name featureName) {
        return ListType.TAIL.equals(featureName);
    }

    public boolean hasFeature(Name featureName) {
        return (isHead(featureName) && hd != null)
               || (isTail(featureName) && tl != null);
    }

    public Node getHead() {
        if (hd == null) {
            return getBaseType().newNode();
        }
        return hd;
    }

    public Node getTail() {
        if (tl == null) {
            return new ListType(getBaseType()).newNode();
        }
        return tl;
    }

    public Node delta(Name featureName) throws NoSuchFeatureException {
        if (isHead(featureName)) {
            return getHead();
        }
        if (isTail(featureName)) {
            return getTail();
        }
        throw new NoSuchFeatureException(featureName, nodetype);
    }

    /** Sets the value of the feature with the given name.
      *  This method should only be called during construction of
      *  a Node.
      */
    public void setFeature(Name featureName, Node value) {
        if (isHead(featureName)) {
            hd = value;
            return;
        } else if (isTail(featureName)) {
            tl = value;
            return;
        }
        throw new NoSuchFeatureException(featureName, nodetype);
    }

    public Node duplicate() {
        // dont use special constructor as type double checking
        // is not necessary.
        ListNode copy = new ListNode((ListType) nodetype);
        copy.hd = hd;
        copy.tl = tl;
        return copy;
    }
}