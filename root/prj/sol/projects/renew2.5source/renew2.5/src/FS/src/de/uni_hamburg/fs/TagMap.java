package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.HashedSet;
import collections.Map;
import collections.UpdatableMap;
import collections.UpdatableSet;


public class TagMap {
    private UpdatableMap tagmap = new HashedMap();
    private UpdatableSet visited = new HashedSet();

    //private UpdatableMap pathmap=new HashedMap();
    private Node root;
    private int tagcounter = 0;

    /** Builds a tag map for a Feature Structure.
       * The tag map provides a Name for every node that is connected
       * with the given root node. If a node is labelled with the empty Name,
       * it has exactly one predescessor and need not be tagged.
       * All other nodes have multiple predecessors and get assigned a unique
       * Name that can be used as a tag number.
       */
    public TagMap(Node node) {
        root = node;
        buildTagMap(root); //Path.EPSILON);
        resetVisited();
    }

    /** Makes a TagMap from a given root node and a Map of tags
     *  that was e.g. the result of parsing a feature structure.
     */
    public TagMap(Node root, EquivRelation er, Map parsedTags) {
        this.root = root;
        CollectionEnumeration ptenumeration = parsedTags.keys();
        while (ptenumeration.hasMoreElements()) {
            Name Tag = (Name) ptenumeration.nextElement();
            try {
                int tag = Integer.parseInt(Tag.name);
                tagcounter = Math.max(tagcounter, tag);
            } catch (NumberFormatException e) {
                // does not have to be a number
            }
            Node node = er.getUnificator((Node) parsedTags.at(Tag));
            tagmap.putAt(node, Tag);
        }
    }

    public Node getRoot() {
        return root;
    }

    /** Sets the Node to "visited" and returns whether it was
     *  visited before.
     */
    public boolean visit(Node node) {
        if (isVisited(node)) { // visited before
            return true;
        } else {
            setVisited(node);
            return false;
        }
    }

    private Name newTag() {
        return new Name(String.valueOf(++tagcounter));
    }

    private void buildTagMap(Node node) { // Path path) {
        if (node instanceof JavaObject
                    || (node instanceof NoFeatureNode
                               && node.getType().isExtensional())) {
            return; // JavaObjects are tagged dynamically,
        }

        // no-feature-nodes with an extensional type are not tagged.
        if (visit(node)) { // visited before
            if (!tagmap.includesKey(node)) { // ...but not yet mapped
                Name Tag = newTag();
                tagmap.putAt(node, Tag); // give node a name
                                         //pathmap.putAt(Tag,path); // remember path of tag


            }
        } else {
            // do this recursively for all nodes:
            CollectionEnumeration features = node.featureNames();
            while (features.hasMoreElements()) {
                Name featureName = (Name) features.nextElement();
                buildTagMap(node.delta(featureName)); // path.append(featureName));
            }
        }
    }

    public void resetVisited() {
        visited = new HashedSet();
    }

    public final static String indent(int depth) {
        StringBuffer space = new StringBuffer(depth + 1);
        for (int i = 0; i < depth; ++i) {
            space.append(' ');
        }
        return space.toString();
    }

    public Name getTag(Node node) {
        if (tagmap.includesKey(node)) {
            return (Name) tagmap.at(node);
        } else if (node instanceof JavaObject) {
            Name Tag = newTag();
            tagmap.putAt(node, Tag);
            return Tag;
        } else {
            return Name.EMPTY;
        }
    }

    public boolean isVisited(Node node) {
        return visited.includes(node);
    }

    public void setVisited(Node node) {
        visited.include(node);
    }

    public void setUnvisited(Node node) {
        visited.removeOneOf(node);
    }
}