package de.renew.formalism.fs;

import collections.CollectionEnumeration;

import de.uni_hamburg.fs.ConjunctiveType;
import de.uni_hamburg.fs.FSNode;
import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.Type;
import de.uni_hamburg.fs.TypeException;
import de.uni_hamburg.fs.TypeSystem;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.application.SimulatorPlugin;

import de.renew.net.NetNotFoundException;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import java.util.Hashtable;
import java.util.Vector;


public class MarkingNetInstance extends ObjectNetInstance {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MarkingNetInstance.class);

    public MarkingNetInstance(FeatureStructure fs, boolean confirm)
            throws Impossible, NetNotFoundException {
        super((String) fs.at("net").getJavaObject(), extractMarking(fs));
        if (confirm) {
            confirm();
        }
    }

    public MarkingNetInstance(FeatureStructure fs)
            throws Impossible, NetNotFoundException {
        this(fs, true);
    }

    public MarkingNetInstance confirm() {
        createConfirmation(SimulatorPlugin.getCurrent().getCurrentEnvironment()
                                          .getSimulator().currentStepIdentifier());
        return this;
    }

    private static Vector<Object> vector(Object o) {
        Vector<Object> v = new Vector<Object>();
        v.addElement(o);
        return v;
    }

    public FeatureStructure equateAndFire(FeatureStructure proc,
                                          FeatureStructure transition,
                                          String[] paths)
            throws UnificationFailure {
        String path0 = "proc" + paths[0];
        proc = proc.unify(transition, path0);
        for (int i = 1; i < paths.length; ++i) {
            proc = proc.equate(path0, "proc" + paths[i]);
        }
        return proc;
    }

    public FeatureStructure fire(FeatureStructure proc,
                                 FeatureStructure transition, Object paths)
            throws UnificationFailure {
        //((FSNode)transition.getRoot()).totallyWellType();
        if (paths instanceof Tuple) {
            return equateAndFire(proc, transition,
                                 (String[]) ((Tuple) paths).asArray(String.class));
        } else {
            return proc.unify(transition, "proc" + paths);
        }
    }

    private static Hashtable<String, Vector<Object>> extractMarking(FeatureStructure fs) {
        Hashtable<String, Vector<Object>> placeMap = new Hashtable<String, Vector<Object>>();
        FSNode proc = (FSNode) fs.getRoot().delta(new Name("proc"));


        //proc.totallyWellType();
        extractMarking(placeMap, new Vector<Node>(), "", proc);
        placeMap.put("Process", vector(fs));
        return placeMap;
    }

    private static void extractMarking(Hashtable<String, Vector<Object>> placeMap,
                                       Vector<Node> visited, String path,
                                       Node curr) {
        if (visited.contains(curr)) { // already visited?
            return;
        }
        visited.addElement(curr);
        CollectionEnumeration featenumeration = curr.featureNames();
        while (featenumeration.hasMoreElements()) {
            Name featureName = (Name) featenumeration.nextElement();
            String feature = featureName.toString();
            String newpath = path + ":" + feature;
            Node next = curr.delta(featureName);
            if (next.featureNames().hasMoreElements()) {
                extractMarking(placeMap, visited, newpath, next);
            } else {
                // leaf found
                if (placeMap.containsKey(feature)) {
                    placeMap.get(feature).addElement(newpath);
                } else {
                    placeMap.put(feature, vector(newpath));
                }
            }
        }
    }

    public static FeatureStructure processMarking(FeatureStructure fs) {
        Type leafType = null;
        Type nodeType = null;
        Type emptyType = null;
        TypeSystem ts = TypeSystem.instance();
        if (!ts.hasConcept("E") || !ts.hasConcept("T")) {
            logger.error("Type E or T not found!");
            return null;
        }
        FSNode vals = null;
        try {
            emptyType = ConjunctiveType.getType("E");
            nodeType = ConjunctiveType.getType("T");
            if (!ts.hasConcept("Token")) {
                logger.error("Type Token not found - using type T.");
                leafType = nodeType;
            } else {
                leafType = ConjunctiveType.getType("Token");
                vals = new FSNode("Marking");
            }
        } catch (UnificationFailure uff) {
            logger.error(uff.getMessage(), uff);
        }

        try {
            FSNode proc = (FSNode) fs.getRoot();
            FSNode state = new FSNode("State");
            state.setFeature(new Name("proc"), proc);
            FSNode mark = new FSNode("Consumers");


            //logger.debug("Type(mark)="+mark.getType());
            state.setFeature(new Name("mark"), mark);
            if (vals != null) {
                state.setFeature(new Name("values"), vals);
            }

            processMarking(new Vector<Node>(), proc, mark, vals, leafType,
                           nodeType);

            CollectionEnumeration featenumeration = mark.getType()
                                                        .appropFeatureNames();
            while (featenumeration.hasMoreElements()) {
                Name feat = (Name) featenumeration.nextElement();
                if (!mark.hasFeature(feat)) {
                    mark.setFeature(feat, new FSNode(emptyType));
                }
            }

            return new FeatureStructure(state);
        } catch (TypeException tee) {
            logger.error("TypeException while processing marking!");
            return null;
        } catch (UnificationFailure uff) {
            return null;
        }
    }

    private static void processMarking(Vector<Node> visited, Node curr,
                                       FSNode mark, FSNode vals, Type leafType,
                                       Type nodeType) throws TypeException {
        if (visited.contains(curr)) { // already visited?
            return;
        }
        visited.addElement(curr);
        CollectionEnumeration featenumeration = curr.featureNames();
        while (featenumeration.hasMoreElements()) {
            Name featureName = (Name) featenumeration.nextElement();
            if (curr.getType().equals(leafType)
                        && featureName.toString().equals("val")) {
                continue; // do not recurse into tokens!
            }
            Node next = curr.delta(featureName);
            Type nextType = next.getType();
            if (nextType.equals(leafType)) {
                Name valName = new Name("val");
                if (next.hasFeature(valName)) {
                    // coloured leaf:
                    Name consName = new Name("cons");
                    if (!next.hasFeature(consName)) {
                        ((FSNode) next).setFeature(consName,
                                                   new FSNode(nodeType));
                    }
                    Node free = next.delta(consName);
                    if (free.getType().equals(nodeType)) {
                        // real leaf found
                        mark.setFeature(featureName, free);


                        // make deep copy of token value:
                        //  Node valCopy=null;
                        //  			try {
                        //  			    valCopy=next.delta(valName);
                        //  			    valCopy=EquivRelation.unify
                        //  				(valCopy,new FSNode(Type.TOP));
                        //  			    vals.setFeature(featureName,valCopy);
                        //  			} catch (UnificationFailure uff) {
                        //  			    logger.error("***Unexpected unification failure: Trying to add "+featureName+": "+valCopy);
                        //  			}
                        vals.setFeature(featureName, next.delta(valName));
                    }
                } else {
                    // leaf of B/E net found:
                    mark.setFeature(featureName, next);
                }
            }
            if (leafType.subsumes(nextType) || nodeType.subsumes(nextType)) {
                processMarking(visited, next, mark, vals, leafType, nodeType);
            }
        }
    }
}