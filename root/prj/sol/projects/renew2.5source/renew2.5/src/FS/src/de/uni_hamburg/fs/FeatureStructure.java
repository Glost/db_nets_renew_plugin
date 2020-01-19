package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.HashedSet;
import collections.Set;
import collections.UpdatableMap;
import collections.UpdatableSet;

import java.util.Enumeration;
import java.util.Vector;


/** The class FeatureStructure defines all methods that are needed
  * to construct, access and unify Feature Structures.
  * A FeatureStructure mainly wraps a Node and provides
  * customized access to the Node's type, features, etc.
  */
public class FeatureStructure implements java.io.Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FeatureStructure.class);
    private Node node;
    protected int hashCode;

    // as the hashCode is a finicky computation, it is being cached
    public FeatureStructure(Node node) {
        this(node, true);
    }

    public FeatureStructure(Node node, boolean mayNeedReduction) {
        this.node = node;
        if (mayNeedReduction) {
            reduce();
        }
        computeHashCode(node, new HashedSet());
    }

    public FeatureStructure(Type type) {
        this(type.newNode(), false);
    }

    private void computeHashCode(Node node, UpdatableSet visited) {
        if (!visited.includes(node)) {
            visited.include(node);
            Type type = node.getType();
            hashCode += type.hashCode();
            if (!(type instanceof JavaObject)) {
                // features contribute to hashCode:
                CollectionEnumeration featenumeration = node.featureNames();
                while (featenumeration.hasMoreElements()) {
                    Name feature = (Name) featenumeration.nextElement();
                    hashCode += feature.hashCode();
                    computeHashCode(node.delta(feature), visited);
                }
            }
        }
    }

    /** Get the Type of the Feature Structure. */
    public Type getType() {
        return node.getType();
    }

    /** Get the root node of the Feature Structure. */
    public Node getRoot() {
        return node;
    }

    /** Get the Java Object contained in this Feature Structure.
       * Throws ClassCastException if the root's type is not
       * a JavaType.
       */
    public Object getJavaObject() {
        return ((JavaType) node.getType()).getJavaObject();
    }

    /** Get the Type of the Feature Structure found at the given path.
      *  A path is a sequence of Features (meant to be starting at the root node).
      *  The syntax of a path is feature_1:feature_2:...feature_n.
      */
    public Type getType(String path) throws NoSuchFeatureException {
        return node.delta(new Path(path)).getType();
    }

    /** Test whether this Feature Structure subsumes that Feature Structure. */
    public boolean subsumes(FeatureStructure that) {
        return Subsumption.subsumes(this, that);
    }

    /** Test whether this Feature Structure is subsumed by that Feature Structure. */
    public boolean subsumedBy(FeatureStructure that) {
        return that.subsumes(this);
    }

    /** Calculate the unification of this Feature Structure and that Feature Structure. */
    public FeatureStructure unify(FeatureStructure that)
            throws UnificationFailure {
        return new FeatureStructure(EquivRelation.unify(this, that));
    }

    /** Calculate the unification of this Feature Structure and that Feature Structure
      * under the given path.
      */
    public FeatureStructure unify(FeatureStructure that, String path)
            throws UnificationFailure {
        return unify(that, new Path(path));
    }

    /** Calculate the unification of this Feature Structure and that Feature Structure
      * under the given path.
      */
    public FeatureStructure unify(FeatureStructure that, Path path)
            throws UnificationFailure {
        return new FeatureStructure(EquivRelation.unify(this, path, that));
    }

    /** Calculate the Feature Structure resulting from this Feature Structure
      * by unifying the nodes at the given paths.
      */
    public FeatureStructure equate(String path1, String path2)
            throws UnificationFailure {
        return equate(new Path(path1), new Path(path2));
    }

    /** Calculate the Feature Structure resulting from this Feature Structure
      * by unifying the nodes at the given paths.
      */
    public FeatureStructure equate(Path path1, Path path2)
            throws UnificationFailure {
        return new FeatureStructure(EquivRelation.unify(this, path1, path2));
    }

    /** Test whether this Feature Structure can be unified with that Feature Structure. */
    public boolean canUnify(FeatureStructure that) {
        return EquivRelation.canUnify(this, that);
    }

    /** Return an Enumeration of the codes of all Features of this Feature Structure. */
    public CollectionEnumeration featureNames() {
        return node.featureNames();
    }

    /** Returns whether this Feature Structure has the given Feature. */
    public boolean hasFeature(String feature) {
        return node.hasFeature(new Name(feature));
    }

    /** Returns whether this Feature Structure has the Feature given by its name. */
    public boolean hasFeature(Name featureName) {
        return node.hasFeature(featureName);
    }

    /** Returns the Feature Structure at the given path.
      *  A path is a sequence of Features (meant to be starting at the root node).
      *  The syntax of a path is feature_1:feature_2:...feature_n.
      *  The empty String returns the Feature Structure itself.
      *  The exception is thrown if at any point, the feature given
      *  by the path does not exist in the current Node.
      */
    public FeatureStructure at(String path) throws NoSuchFeatureException {
        return at(new Path(path));
    }

    public FeatureStructure at(Path path) throws NoSuchFeatureException {
        return new FeatureStructure(delta(path), true);
    }

    public Object unpackingAt(Path path) throws NoSuchFeatureException {
        Node subnode = node.delta(path);
        Type subtype = subnode.getType();
        if (subtype instanceof JavaType) {
            return ((JavaType) subtype).getJavaObject();
        }
        return new FeatureStructure(subnode, true);
    }

    public Node delta(String path) throws NoSuchFeatureException {
        return delta(new Path(path));
    }

    public Node delta(Name feature) throws NoSuchFeatureException {
        return node.delta(feature);
    }

    public Node delta(Path path) throws NoSuchFeatureException {
        return node.delta(path);
    }

    public boolean canInstantiate() {
        UpdatableSet toBeInstantiated = new HashedSet();
        int countToBeInstantiated;
        boolean canInstantiate;
        do {
            countToBeInstantiated = toBeInstantiated.size();
            canInstantiate = canInstantiate(node, new HashedSet(),
                                            toBeInstantiated);
        } while (!canInstantiate
                         && toBeInstantiated.size() > countToBeInstantiated);

        // repeat while additional "toBeInstantiated"-nodes were found.
        return canInstantiate;
    }

    private static boolean isInstantiated(Node node, Set toBeInstantiated) {
        if (toBeInstantiated.includes(node)) {
            return true;
        }
        Type typ = node.getType();
        return typ instanceof JavaType
               || typ instanceof BasicType && ((BasicType) typ).isObject();
    }

    private static boolean canInstantiate(Node node, UpdatableSet visited,
                                          UpdatableSet toBeInstantiated) {
        if (isInstantiated(node, toBeInstantiated)) {
            return true;
        }
        Type typ = node.getType();
        if (visited.includes(node)) {
            return !typ.isInstanceType();


            // cyclic reference to a Node in "unknown" state:
            // in that case, we only allow cycles for non-instance types.
        }
        visited.include(node);
        boolean include = false;
        if (typ.isInstanceType()) {
            if (typ instanceof ListType) {
                ListType listType = (ListType) typ;
                if (listType.getSubtype() == ListType.ELIST) {
                    include = true;
                } else if (listType.getSubtype() == ListType.NELIST) {
                    include = isInstantiated(node.delta(ListType.HEAD),
                                             toBeInstantiated)
                              && isInstantiated(node.delta(ListType.TAIL),
                                                toBeInstantiated);
                }
            } else if (typ instanceof ConjunctiveType) {
                // check if there is exactly one JavaConcept to instantiate:
                JavaConcept onlyConcept = ((ConjunctiveType) typ)
                                              .getOnlyInstantiableJavaConcept();
                if (onlyConcept != null) {
                    JavaConcept javaConcept = onlyConcept;
                    CollectionEnumeration feats = node.featureNames();
                    include = true;
                    while (feats.hasMoreElements()) {
                        Name feature = (Name) feats.nextElement();
                        if (!javaConcept.getJavaFeature(feature).canSet()
                                    || !isInstantiated(node.delta(feature),
                                                               toBeInstantiated)) {
                            include = false;
                            break;
                        }
                    }
                }
            }
        }
        if (include) {
            toBeInstantiated.include(node);
        }
        boolean canInstantiate = include || !typ.isInstanceType();
        CollectionEnumeration feats = node.featureNames();
        while (feats.hasMoreElements()) {
            Name feature = (Name) feats.nextElement();
            canInstantiate &= canInstantiate(node.delta(feature), visited,
                                             toBeInstantiated);
        }
        return canInstantiate;
    }

    public FeatureStructure instantiate() throws NotInstantiableException {
        // check if no nodes have to be instantiated (may happen quite often):
        UpdatableSet toBeInstantiated = new HashedSet();
        if (canInstantiate(node, new HashedSet(), toBeInstantiated)
                    && toBeInstantiated.size() == 0) {
            return this; // don't rebuild the same FS!
        }
        return new FeatureStructure(instantiate(node, new HashedMap()), false);
    }

    private static Node instantiate(Node node, UpdatableMap objMap)
            throws NotInstantiableException {
        if (objMap.includesKey(node)) {
            return (Node) objMap.at(node);
        }
        Type typ = node.getType();
        Node newNode = null;
        boolean mapFeatures = true;
        if (typ.isInstanceType()) {
            if (typ instanceof JavaType) {
                newNode = node;
                mapFeatures = false;
            } else if (typ instanceof ListType) {
                // copy list elements to Vector:
                Node current = node;
                Vector<Object> elements = new Vector<Object>();
                while (current != null
                               && ((ListType) current.getType()).getSubtype() == ListType.NELIST) {
                    Node objNode = instantiate(current.delta(ListType.HEAD),
                                               objMap);
                    elements.addElement(((JavaType) objNode.getType())
                        .getJavaObject());
                    current = current.delta(ListType.TAIL);
                }
                Object[] elemArray = new Object[elements.size()];
                elements.copyInto(elemArray);
                Object array = JavaArrayType.makeArray(elemArray);
                newNode = new JavaArrayType(array).newNode();
                mapFeatures = false;
            } else {
                // there has to be exactly one JavaConcept to instantiate:
                try {
                    JavaConcept javaConcept = (JavaConcept) ((ConjunctiveType) typ).concepts.elements()
                                                                                            .nextConcept();
                    Object object = javaConcept.getJavaClass().newInstance();
                    newNode = new JavaObject(object);
                } catch (Exception e) {
                    logger.error("Cannot instantiate: " + e);
                    // logger.error(e.getMessage(), e);
                    throw new NotInstantiableException(new FeatureStructure(node,
                                                                            false));
                    // should not fail if canInstantiate() has been called before!
                }
            }
        } else {
            // copy old Node with new feature values:
            newNode = typ.newNode();
        }
        objMap.putAt(node, newNode);
        if (mapFeatures) {
            CollectionEnumeration feats = node.featureNames();
            while (feats.hasMoreElements()) {
                Name feature = (Name) feats.nextElement();
                newNode.setFeature(feature,
                                   instantiate(node.delta(feature), objMap));
            }
        }
        return newNode;
    }

    public FeatureStructure change(String feature, FeatureStructure newValue) {
        Name featureName = new Name(feature);
        FSNode newNode = (FSNode) node.duplicate();
        newNode.setFeature(featureName, newValue.node);
        return new FeatureStructure(newNode);
    }

    public Enumeration<Node> getNodes() {
        return addNodes(new HashedSet(), node).elements();
    }

    private static UpdatableSet addNodes(UpdatableSet nodes, Node fs) {
        if (!(fs instanceof JavaObject) && !nodes.includes(fs)) {
            nodes.include(fs);
            CollectionEnumeration featenumeration = fs.featureNames();
            while (featenumeration.hasMoreElements()) {
                Name feature = (Name) featenumeration.nextElement();
                addNodes(nodes, fs.delta(feature));
            }
        }
        return nodes;
    }

    public Path onePathTo(Node target) {
        return onePathTo(new HashedSet(), node, Path.EPSILON, target);
    }

    private static Path onePathTo(UpdatableSet nodes, Node fs, Path path,
                                  Node target) {
        if (fs.equals(target)) {
            return path;
        }
        if (!nodes.includes(fs)) {
            nodes.include(fs);
            CollectionEnumeration featenumeration = fs.featureNames();
            while (featenumeration.hasMoreElements()) {
                Name feature = (Name) featenumeration.nextElement();
                Path found = onePathTo(nodes, fs.delta(feature),
                                       path.append(feature), target);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public Enumeration<Node> backwardsReachableNodes(Node target) {
        UpdatableMap map = new HashedMap();
        UpdatableMap pam = new HashedMap();
        reverse(node, new HashedSet(), 0, map, pam);
        if (!map.includesKey(target)) {
            // target is not reachable at all!
            // logger.debug("Target node "+target+" was not found!");
            return EmptyEnumeration.INSTANCE;
        }
        Node newRoot = (Node) map.at(target);
        FeatureStructure reverseFS = new FeatureStructure(newRoot, false);


        //        logger.debug("FS: "+this+"reversed at "+onePathTo(target)+": "
        //  			 +reverseFS);
        //  logger.debug("Map:");
        //  Enumeration keys=map.keys();
        //  while (keys.hasMoreElements()) {
        //    Node key=(Node)keys.nextElement();
        //    logger.debug(onePathTo(key)+" mapped to "+reverseFS.onePathTo((Node)map.at(key))+":"+map.at(key).hashCode());
        //  }
        Enumeration<Node> reachenumeration = reverseFS.getNodes();
        UpdatableSet backwardsReachableNodes = new HashedSet();

        //      logger.debug("Backwards Reachable nodes:");
        while (reachenumeration.hasMoreElements()) {
            Node bnode = (Node) pam.at(reachenumeration.nextElement());
            backwardsReachableNodes.include(bnode);
            //	  logger.debug(bnode+", ");
        }

        return backwardsReachableNodes.elements();
    }

    private static Node getReverse(UpdatableMap map, UpdatableMap pam, Node fs) {
        Node rev;
        if (map.includesKey(fs)) {
            rev = (Node) map.at(fs);
        } else {
            rev = Type.ANY.newNode();
            map.putAt(fs, rev);


            // logger.debug("Mapping "+fs.hashCode()+" to "+rev.hashCode());
            pam.putAt(rev, fs);
        }
        return rev;
    }

    private int reverse(Node fs, UpdatableSet visited, int featureCnt,
                        UpdatableMap map, UpdatableMap pam) {
        if (visited.includes(fs) || fs instanceof JavaObject) {
            return featureCnt;
        }
        visited.include(fs);
        Node fsRev = getReverse(map, pam, fs);

        // logger.debug("Visiting "+onePathTo(fs)+":"+fs.hashCode());
        CollectionEnumeration featenumeration = fs.featureNames();
        while (featenumeration.hasMoreElements()) {
            Name feature = (Name) featenumeration.nextElement();
            Node next = fs.delta(feature);
            Node nextRev = getReverse(map, pam, next);
            nextRev.setFeature(new Name("f" + featureCnt), fsRev);


            // logger.debug("Setting reverse feature "+feature+" (f"+featureCnt+") in "+nextRev.hashCode()+" to "+fsRev.hashCode());
            featureCnt = reverse(next, visited, ++featureCnt, map, pam);
        }
        return featureCnt;
    }

    /** Remove all informationless nodes from this FS.
     */
    public void reduce() {
        UpdatableSet infoNodes = new HashedSet();
        infoNodes.include(node);
        // the root is always regared as "infoful"
        // findInfo until no more infoNodes are added:


        //int cnt = 0;
        int lastNoInfoNodes;
        int noInfoNodes = 1;
        do {
            //++cnt;
            UpdatableSet allNodes = new HashedSet();
            lastNoInfoNodes = noInfoNodes;
            findInfo(node, allNodes, infoNodes);
            noInfoNodes = infoNodes.size();


            // Often, FSs are already reduced. Then, all nodes are
            // declared as "infoful".
            if (noInfoNodes == allNodes.size()) {
                // logger.debug("Discovered that fs was already reduced after "+cnt+" turns.");
                return;
            }
        } while (noInfoNodes > lastNoInfoNodes);


        // logger.debug("Reduction took "+cnt+" turns.");
        // make a deep copy of the whole object graph, leaving out
        // infoless nodes:
        node = copyInfoNodes(node, new HashedMap(), infoNodes);
    }

    private static void findInfo(Node fs, UpdatableSet visited,
                                 UpdatableSet infoNodes) {
        if (visited.includes(fs)) {
            // found co-reference:
            infoNodes.include(fs);
        } else {
            visited.include(fs);
            if (fs instanceof JavaObject) {
                infoNodes.include(fs); // java objects are always infoful
            } else {
                CollectionEnumeration featenumeration = fs.featureNames();
                Type fstype = fs.getType();
                boolean hasInfo = infoNodes.includes(fs);
                while (featenumeration.hasMoreElements()) {
                    Name feature = (Name) featenumeration.nextElement();
                    Node next = fs.delta(feature);
                    boolean nextHasInfo = infoNodes.includes(next);
                    if (!nextHasInfo
                                && !next.getType()
                                                .equals(fstype.appropType(feature))) {
                        // found more special type in feature value
                        // logger.debug("Found specialised node type "+next.getType());
                        infoNodes.include(next);
                        nextHasInfo = true;
                    }
                    if (!hasInfo && nextHasInfo) {
                        infoNodes.include(fs);
                        hasInfo = true;
                    }
                    findInfo(next, visited, infoNodes);
                }
            }
        }
    }

    private static Node copyInfoNodes(Node fs, UpdatableMap map, Set infoNodes) {
        if (fs instanceof JavaObject) {
            return fs;
        }
        if (map.includesKey(fs)) {
            return (Node) map.at(fs);
        }
        Node copy = fs.duplicate();
        map.putAt(fs, copy);
        CollectionEnumeration featenumeration = fs.featureNames();
        while (featenumeration.hasMoreElements()) {
            Name feature = (Name) featenumeration.nextElement();
            Node next = copy.delta(feature);
            if (infoNodes.includes(next)) {
                copy.setFeature(feature, copyInfoNodes(next, map, infoNodes));
            } else {
                // remove feature to infoless node:
                copy.setFeature(feature, null);
            }
        }
        return copy;
    }

    public Feature getFirstMissingAssociation() {
        Type type = node.getType();
        CollectionEnumeration feats = type.appropFeatureNames();
        JavaConcept jc = null;
        if (type instanceof ConjunctiveType) {
            jc = ((ConjunctiveType) type).getOnlyInstantiableJavaConcept();
        }
        while (feats.hasMoreElements()) {
            Name feature = (Name) feats.nextElement();
            if (!node.hasFeature(feature)) {
                Type approp = type.appropType(feature);
                if (!(approp instanceof BasicType)
                            && (jc == null
                                       || jc.getJavaFeature(feature).canSet())) {
                    return new Feature(feature, approp);
                }
            }
        }
        return null;
    }

    public boolean equals(Object that) {
        if (that instanceof FeatureStructure) {
            // works, but is *very* inefficient:
            //return this.subsumes((FeatureStructure)that) && ((FeatureStructure)that).subsumes(this);
            // FSs have to have exactly the same types, features & co-references
            return Equality.equals(this, (FeatureStructure) that);
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        return PrettyPrinter.toString(this);
    }
}