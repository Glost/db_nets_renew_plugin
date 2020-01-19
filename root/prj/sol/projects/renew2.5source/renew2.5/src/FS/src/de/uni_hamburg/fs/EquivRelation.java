package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.HashedSet;
import collections.UpdatableMap;
import collections.UpdatableSet;

import java.util.Stack;


public class EquivRelation {
    // public static org.apache.log4j.Logger logger = 
    //				org.apache.log4j.Logger.getLogger(EquivRelation.class) ;

    /** tie maps Nodes to Unificator Nodes. */
    private UpdatableMap tie = new HashedMap();

    /** eit maps Unificator Nodes to sets of Nodes.
      */
    private UpdatableMap eit = new HashedMap();

    /** todo is a Stack of ToDoItems, pairs of nodes
     *  which still have to be unified or nodes which
     *  have to be retyped. */
    private Stack<ToDoItem> todo = new Stack<ToDoItem>();

    /** Constructs a new Equivalence Relation. */
    public EquivRelation() {
    }

    /** Return the Unification of fs1 and fs2. */
    public static Node unify(FeatureStructure fs1, FeatureStructure fs2)
            throws UnificationFailure {
        // logger.debug("Unifying "+fs1.seqNo+" and "+fs2.seqNo+".");
        EquivRelation er = new EquivRelation();
        Node root = fs1.getRoot();
        er.unify(root, fs2.getRoot());
        er.extensionalize();
        return er.rebuild(root);
    }

    /** Return the result of unifying fs2 into fs1 at the given path. */
    public static Node unify(FeatureStructure fs1, Path path,
                             FeatureStructure fs2) throws UnificationFailure {
        // logger.debug("Unifying "+fs1.seqNo+" at "+path+" and "+fs2.seqNo+".");
        Node fs1Root = addPath(fs1.getRoot(), path);
        EquivRelation er = new EquivRelation();
        er.unify(fs1Root.delta(path), fs2.getRoot());
        er.extensionalize();
        return er.rebuild(fs1Root);
    }

    /** Return the result of identifying the sub-fss of fs at paths path1 and path2. */
    public static Node unify(FeatureStructure fs, Path path1, Path path2)
            throws UnificationFailure {
        Node fsRoot = addPath(addPath(fs.getRoot(), path1), path2);
        EquivRelation er = new EquivRelation();
        er.unify(fsRoot.delta(path1), fsRoot.delta(path2));
        er.extensionalize();
        return er.rebuild(fsRoot);
    }

    private static Node addPath(Node root, Path path) throws UnificationFailure {
        // We have to unify a "most general" path into root:
        Node pathRoot = createPath(false, root, path);
        if (pathRoot == null) {
            // The FS already contains the path!
            return root;
        }

        //logger.debug("Unifying with path "+new FeatureStructure(pathRoot,false));
        EquivRelation pathER = new EquivRelation();
        pathER.unify(root, pathRoot);
        pathER.extensionalize();
        return pathER.rebuild(root);
    }

    private static Node createPath(boolean infoAdded, Node fs, Path path)
            throws UnificationFailure {
        Type type = fs.getType();
        if (type instanceof JavaObject) {
            type = new ConjunctiveType(((JavaObject) type).concept);
        }
        Node copy = type.newNode();
        if (path.isEmpty()) {
            if (!infoAdded) {
                copy = null;
            }
        } else {
            Name feature = path.first();
            if (!infoAdded && !fs.hasFeature(feature)) {
                infoAdded = true;
            }
            try {
                Node nextCopy = createPath(infoAdded, fs.delta(feature),
                                           path.butFirst());
                if (nextCopy == null) {
                    return null;
                }
                copy.setFeature(feature, nextCopy);
            } catch (NoSuchFeatureException nsf) {
                //logger.error("During createPath: "+nsf);
                throw new UnificationFailure();
            }
        }
        return copy;
    }

    /** Return whether fs1 and fs2 can be unified. */
    public static boolean canUnify(FeatureStructure fs1, FeatureStructure fs2) {
        EquivRelation er = new EquivRelation();
        try {
            er.unify(fs1.getRoot(), fs2.getRoot());
            er.extensionalize();
            return true;
        } catch (UnificationFailure uff) {
            return false;
        }
    }

    public static Node deepCopy(Node root) {
        return new EquivRelation().rebuild(root);
    }

    private void addUnification(Node node1, Node node2) {
        todo.push(new UnifyItem(node1, node2));
    }

    private void addRetyping(Node node, Type type) {
        todo.push(new RetypeItem(node, type));
    }

    private ToDoItem nextToDoItem() {
        return todo.pop();
    }

    /** Maps a Node to a Unificator Node.
      * To do this, the first Node has to be mapped to
      * the Unificator (using tie) and has to be included
      * in the set of members of the unificator (using eit).
      * If the first Node is itself a Unificator, all its
      * elements are also mapped to the new Unificator.
      */
    private void map(Node fs, Node uni) {
        //logger.debug("mapping node "+fs.seqNo+" to node "+uni.seqNo);
        if (fs.equals(uni)) {
            return;
        }
        if (eit.includesKey(fs)) { // fs is itself an equivClass
                                   // map all of fs's elements to uni:
            CollectionEnumeration equivClassElems = ((UpdatableSet) eit.at(fs))
                                                    .elements();
            while (equivClassElems.hasMoreElements()) {
                Node memberFS = (Node) equivClassElems.nextElement();
                map(memberFS, uni);
            }
            eit.removeAt(fs);
        }

        tie.putAt(fs, uni);
        UpdatableSet equivClass;
        if (eit.includesKey(uni)) { // uni has been established before
            equivClass = (UpdatableSet) eit.at(uni);
        } else { // uni is a new equivClass
            equivClass = new HashedSet();
            eit.putAt(uni, equivClass);
        }
        equivClass.include(fs);
    }

    public Node getUnificator(Node fs) {
        if (tie.includesKey(fs)) {
            return (Node) tie.at(fs);
        } else {
            return fs;
        }
    }

    /** Updates the EquivRelation and constructs Unificator Nodes
      * so that fs1 and fs2 are unified.
      * Other nodes may become unified by recursion.
      */
    public void unify(Node fs1, Node fs2) throws UnificationFailure {
        addUnification(fs1, fs2);
        while (!todo.empty()) {
            ToDoItem tdi = nextToDoItem();


            //try {
            tdi.doIt(this);


            //  } catch (UnificationFailure uff) {
            //    logger.error("UnificationFailure during "+tdi);
            //    throw uff;
            //  }
        }

        //logger.debug("Unification done.");
    }

    void unifyOne(Node fs1, Node fs2) throws UnificationFailure {
        //logger.debug("unifying nodes of type "+fs1.getType()+" and "+fs2.getType());
        if (fs1.equals(fs2)) {
            return;
        }
        Type unitype = fs1.getType().unify(fs2.getType());
        Node uni;
        if (eit.includesKey(fs2) && unitype.equals(fs2.getType())) {
            uni = fs2;
        } else if (eit.includesKey(fs1) && unitype.equals(fs1.getType())) {
            uni = fs1;
        } else {
            uni = unitype.newNode();
        }
        addAllFeatures(uni, fs1);
        addAllFeatures(uni, fs2);
        addRetypings(uni);


        // Update the relation:
        // both Nodes are mapped to their unificator.
        map(fs1, uni);
        map(fs2, uni);

    }

    void retypeOne(Node fs, Type type) throws UnificationFailure {
        //logger.debug("Refining node of type "+fs.getType()+" with "+type);
        Type fstype = fs.getType();

        //logger.debug("Refining node of type "+fs.getType()+" with "+type);
        Type unitype = fstype.unify(type);

        //logger.debug("Succeeded with result "+unitype);
        if (!unitype.equals(fstype)) {
            //logger.debug("Retyping "+unitype+"...");
            Node retyped = unitype.newNode();


            //logger.debug("Adding Features...");
            addAllFeatures(retyped, fs);
            map(fs, retyped);


            //logger.debug("Adding Retypings...");
            addRetypings(retyped);
        }

        //logger.debug("Done.");
    }

    private void addAllFeatures(Node uni, Node fs) {
        if (!(fs instanceof JavaObject) && !uni.equals(fs)) {
            CollectionEnumeration featenumeration = fs.featureNames();
            while (featenumeration.hasMoreElements()) {
                Name featureName = (Name) featenumeration.nextElement();
                Node fspost = fs.delta(featureName);

                //logger.debug("Adding feature "+featureName+" value "+fspost.getType());
                if (uni.hasFeature(featureName)) {
                    Node unipost = uni.delta(featureName);
                    addUnification(fspost, unipost);
                } else {
                    if (uni instanceof JavaObject) {
                        throw new RuntimeException("Trying to set feature "
                                                   + featureName + " in " + uni
                                                   + " to " + fspost);
                    }
                    uni.setFeature(featureName, fspost);
                }

                //logger.debug("Node feature "+featureName+" value "+uni.delta(featureName).getType());
            }
        }
    }

    private void addRetypings(Node uni) {
        Type unitype = uni.getType();
        if (!(unitype instanceof JavaObject)) {
            CollectionEnumeration feats = uni.featureNames();
            while (feats.hasMoreElements()) {
                Name feat = (Name) feats.nextElement();
                addRetyping(uni.delta(feat), unitype.appropType(feat));


                //  if (uni.delta(feat)==null)
                //        logger.debug("UNI: "+unitype+".appropType("+feat+"): "
                //        		   +unitype.appropType(feat));
            }
        }
    }

    /** Unifies all nodes in this EquivRelation according to the extensionability rule.
       */
    public void extensionalize() {
    }

    /** Rebuilds a new graph which is the Unificator.
      * fs is one of the Nodes that have been unified,
      * so the algorithm knows where to start.
      */
    public Node rebuild(Node fs) {
        //logger.debug("converting node "+fs.hashCode()+" of type "+fs.getType());
        Node uni;
        boolean expand;
        if (tie.includesKey(fs)) {
            //logger.debug("node found in relation.");
            uni = (Node) tie.at(fs);
            expand = eit.includesKey(uni);
            if (expand) {
                eit.removeAt(uni);
            }
        } else {
            //logger.debug("node not found in relation. Creating Copy ");
            //        if (eit.includesKey(fs)) {
            //logger.debug("Using unificator as new node.");
            //           uni = fs;
            //           eit.removeAt(uni);
            //        } else {
            //logger.debug("Creating copy.");
            uni = fs.duplicate();
            tie.putAt(fs, uni);


            //        } /* endif */
            expand = true;
        }

        //logger.debug("node is now mapped to node "+uni.hashCode()+" of type "+uni.getType());
        if (expand && !(uni instanceof JavaObject)) {
            //logger.debug("Expanding node...");
            CollectionEnumeration featenumeration = uni.featureNames();
            while (featenumeration.hasMoreElements()) {
                Name featureName = (Name) featenumeration.nextElement();


                //logger.debug("Expanding feature "+featureName+" of node "+uni.hashCode()+"...");
                uni.setFeature(featureName, rebuild(uni.delta(featureName)));
            }
        }


        //else {
        // logger.debug("Already expanded.");
        //}
        return uni;
    }
}

abstract class ToDoItem {
    Node node;

    ToDoItem(Node node) {
        this.node = node;
    }

    abstract void doIt(EquivRelation er) throws UnificationFailure;
}

class UnifyItem extends ToDoItem {
    Node otherNode;

    UnifyItem(Node node, Node otherNode) {
        super(node);
        this.otherNode = otherNode;
    }

    void doIt(EquivRelation er) throws UnificationFailure {
        node = er.getUnificator(node);
        otherNode = er.getUnificator(otherNode);
        er.unifyOne(node, otherNode);
    }

    public String toString() {
        return "Unifying node of type " + node.getType() + "("
               + node.getType().getClass().getName() + ") with node of type "
               + otherNode.getType() + "("
               + otherNode.getType().getClass().getName() + ").";
    }
}

class RetypeItem extends ToDoItem {
    Type type;

    RetypeItem(Node node, Type type) {
        super(node);
        this.type = type;
    }

    void doIt(EquivRelation er) throws UnificationFailure {
        node = er.getUnificator(node);
        er.retypeOne(node, type);
    }

    public String toString() {
        return "Refining node of type " + node.getType() + "("
               + node.getType().getClass().getName() + ") with type " + type
               + "(" + type.getClass().getName() + ").";
    }
}