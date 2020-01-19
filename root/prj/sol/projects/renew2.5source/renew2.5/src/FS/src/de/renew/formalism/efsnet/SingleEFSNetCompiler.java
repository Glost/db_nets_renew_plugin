package de.renew.formalism.efsnet;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.HashedSet;
import collections.LinkedList;
import collections.Seq;
import collections.UpdatableMap;
import collections.UpdatableSeq;
import collections.UpdatableSet;

import de.uni_hamburg.fs.ConceptImpl;
import de.uni_hamburg.fs.ConjunctiveType;
import de.uni_hamburg.fs.EquivRelation;
import de.uni_hamburg.fs.FSNode;
import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.ParsedConjunctiveType;
import de.uni_hamburg.fs.ParsedType;
import de.uni_hamburg.fs.Partition;
import de.uni_hamburg.fs.Path;
import de.uni_hamburg.fs.Type;
import de.uni_hamburg.fs.TypeException;
import de.uni_hamburg.fs.TypeSystem;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.CallExpression;
import de.renew.expression.ConstantExpression;
import de.renew.expression.EqualsExpression;
import de.renew.expression.Expression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.fs.FSNetCompiler;
import de.renew.formalism.fs.FSNetParser;
import de.renew.formalism.fs.FSUnifyExpression;
import de.renew.formalism.fs.SingleFSNetCompiler;
import de.renew.formalism.fsnet.SingleXFSNetCompiler;
import de.renew.formalism.java.FlexibleOutArcFactory;
import de.renew.formalism.java.ParsedDeclarationNode;
import de.renew.formalism.java.TimedExpression;
import de.renew.formalism.java.TypedExpression;

import de.renew.net.ExpressionTokenSource;
import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;
import de.renew.net.arc.ClearArc;
import de.renew.net.inscription.ExpressionInscription;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import de.renew.util.Types;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


public class SingleEFSNetCompiler extends SingleFSNetCompiler
        implements EFSNetConstants {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SingleEFSNetCompiler.class);
    public static boolean valueSem = true;
    private Place markingPlace = null;
    private Place procPlace = null;
    private UpdatableMap places = null;

    // from shadow places to initial marking FS
    private UpdatableMap transitions = null;

    // from shadow transitions to transition rule FS
    protected void compile(ShadowPlace shadowPlace, Net net)
            throws SyntaxException {
        // Determine the name.
        String pname = shadowPlace.getName();
        if (pname == null) {
            throw new SyntaxException("In Elementary FSNets, Places must have names.")
                  .addObject(shadowPlace);
        }

        if (!pname.equals("proc") && !pname.equals("m")) {
            String initialMarking = null;
            Iterator<ShadowNetElement> elements = shadowPlace.elements()
                                                             .iterator();
            while (elements.hasNext()) {
                Object elem = elements.next();
                if (elem instanceof ShadowInscription) {
                    if (initialMarking != null) {
                        throw new SyntaxException("EFSNets are only allowed to have one initial token per place.")
                              .addObject(elem);
                    }
                    initialMarking = ((ShadowInscription) elem).inscr;
                }
            }

            if (initialMarking == null) {
                initialMarking = "[E]";
            } else {
                initialMarking = "[Tok val:" + initialMarking + "]";
            }
            places.putAt(shadowPlace, initialMarking);
            logger.debug("Initial Marking of " + pname + ": " + initialMarking);
        }

        // Create the new place;
        Place place;
        if (pname.equals("m") && (markingPlace != null)) {
            place = markingPlace;
        } else {
            place = new Place(net, pname, new NetElementID(shadowPlace.getID()));
            place.setTrace(shadowPlace.getTrace());
            getLookup().set(shadowPlace, place);
            if (pname.equals("m")) {
                markingPlace = place;
            }
        }

        if (pname.equals("proc")) {
            procPlace = place;
        }
    }

    protected void compile(ShadowTransition shadowTransition, Net net)
            throws SyntaxException {
        // Determine the name.
        String tname = shadowTransition.getName();
        if (tname == null) {
            throw new SyntaxException("In Elementary FSNets, Transitions must have names.")
                  .addObject(shadowTransition);
        }

        // find transition rule, pre-, and post-places
        UpdatableMap dotT = new HashedMap();

        // find transition rule, pre-, and post-places
        UpdatableMap tDot = new HashedMap();
        UpdatableSet allPlaces = new HashedSet(); // set of pre- and post-places
        String transitionRule = null;

        Iterator<ShadowNetElement> iterator = shadowTransition.elements()
                                                              .iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowInscription) {
                // should only be one:
                if (transitionRule != null) {
                    throw new SyntaxException("EFSNets are only allowed to have one transition inscription (the rule).")
                          .addObject(elem);
                }
                ShadowInscription inscription = (ShadowInscription) elem;
                transitionRule = inscription.inscr;
            } else if (elem instanceof ShadowArc) {
                ShadowArc shadowArc = (ShadowArc) elem;
                if (shadowArc.shadowArcType != ShadowArc.ordinary
                            && shadowArc.shadowArcType != ShadowArc.both) {
                    throw new SyntaxException("EFSNets only allow normal arcs.")
                          .addObject(shadowArc);
                }
                Iterator<ShadowNetElement> arcInscriptions = shadowArc.elements()
                                                                      .iterator();
                String arcTag;
                if (arcInscriptions.hasNext()) {
                    ShadowInscription inscription = (ShadowInscription) arcInscriptions
                                                    .next();
                    if (arcInscriptions.hasNext()) {
                        throw new SyntaxException("EFSNets have to have exactly one arc inscription (a tag).")
                              .addObject(inscription);
                    }
                    arcTag = inscription.inscr;
                } else {
                    arcTag = "#root";
                }
                Name placeName = new Name(shadowArc.place.getName());
                allPlaces.include(placeName);
                if (shadowArc.shadowArcType == ShadowArc.both
                            || shadowArc.placeToTransition) {
                    dotT.putAt(placeName, arcTag);
                }
                if (shadowArc.shadowArcType == ShadowArc.both
                            || !shadowArc.placeToTransition) {
                    tDot.putAt(placeName, arcTag);
                }
            }
        }
        if (transitionRule == null) {
            transitionRule = "[]";
        }
        StringBuffer transStr = new StringBuffer();
        transStr.append("[Tr").append('\n').append(" rule:#root")
                .append(transitionRule).append('\n').append(" eff:[Eff\n")
                .append("      pre:[M\n");


        // build pre-marking:
        Enumeration<?> placeEnum = allPlaces.elements();
        while (placeEnum.hasMoreElements()) {
            Name placeName = (Name) placeEnum.nextElement();
            transStr.append("           ").append(placeName.toString())
                    .append(':');
            if (dotT.includesKey(placeName)) {
                transStr.append("[Tok val:").append(dotT.at(placeName))
                        .append("]");
            } else {
                transStr.append("[E]");
            }
            transStr.append('\n');
        }

        transStr.append("          ]\n      post:[M\n");


        // build post-marking:
        placeEnum = allPlaces.elements();
        while (placeEnum.hasMoreElements()) {
            Name placeName = (Name) placeEnum.nextElement();
            transStr.append("            ").append(placeName.toString())
                    .append(':');
            if (tDot.includesKey(placeName)) {
                transStr.append("[Tok val:").append(tDot.at(placeName))
                        .append("]");
            } else {
                transStr.append("[E]");
            }
            transStr.append('\n');
        }
        transStr.append("           ]]]");
        transitions.putAt(shadowTransition, transStr.toString());

        // Create the new transition.
        Transition transition = new Transition(net, tname,
                                               new NetElementID(shadowTransition
                                                                .getID()));
        boolean trace = shadowTransition.getTrace();
        transition.setTrace(trace);
        getLookup().set(shadowTransition, transition);

    }

    private static Vector<Object> makeVector(Object obj) {
        Vector<Object> v = new Vector<Object>(1);
        v.addElement(obj);
        return v;
    }

    public void compile(ShadowNet shadowNet) throws SyntaxException {
        places = new HashedMap();
        transitions = new HashedMap();
        procPlace = null;

        ShadowLookup lookup = getLookup();

        // Find the net that is compiled.
        Net net = lookup.getNet(shadowNet.getName());

        super.compile(shadowNet);
        if (markingPlace == null) {
            // Create the marking place without shadownet pendant:
            markingPlace = new Place(net, "m", new NetElementID());
            //markingPlace.setID(???);
            //markingPlace.setTrace(false);
            //lookup.set(shadowPlace,markingPlace);
        }

        TypeSystem ts = TypeSystem.instance();

        String netSpace = shadowNet.getName() + "::";
        ConceptImpl M = new ConceptImpl(netSpace + "M");
        ConceptImpl Token = new ConceptImpl(netSpace + "Token");
        ConceptImpl E = new ConceptImpl(netSpace + "E");
        ConceptImpl Tok = new ConceptImpl(netSpace + "Tok");
        ConceptImpl Tr = new ConceptImpl(netSpace + "Tr");
        ConceptImpl Eff = new ConceptImpl(netSpace + "Eff");
        ConceptImpl PEff = new ConceptImpl(netSpace + "PEff");
        ConceptImpl Proc = new ConceptImpl(netSpace + "Proc");
        Partition topPartition = ts.getTopPartition();
        try {
            topPartition.addConcept(M);
            topPartition.addConcept(Token);
            topPartition.addConcept(Tr);
            topPartition.addConcept(Eff);
            topPartition.addConcept(Proc);
            E.addIsa(Token);
            Tok.addIsa(Token);
            Partition tokenSubPartition = new Partition(E);
            tokenSubPartition.addConcept(Tok);
            PEff.addIsa(Eff);
        } catch (TypeException tee) {
            // should not happen
            logger.error("Type Exception during internal TypeSystem construction!");
            return;
        }
        ParsedType ptTop = ParsedType.PARSED_TOP; //new ParsedConjunctiveType(Top);
        Tr.addApprop("rule", ptTop);
        ParsedType ptEff = new ParsedConjunctiveType(Eff);
        Tr.addApprop("eff", ptEff);
        ParsedType ptM = new ParsedConjunctiveType(M);
        Eff.addApprop(FEATpre, ptM);
        Eff.addApprop(FEATpost, ptM);
        ParsedType ptProc = new ParsedConjunctiveType(Proc);
        PEff.addApprop(FEATpostc, ptM);
        PEff.addApprop(FEATproc, ptProc);
        Proc.addApprop("m", ptM);
        Tok.addApprop("val", ptTop);
        ParsedType ptToken = new ParsedConjunctiveType(Token);

        CollectionEnumeration sortedPlaceNames = sort(places.keys()).elements();
        while (sortedPlaceNames.hasMoreElements()) {
            String place = (String) sortedPlaceNames.nextElement();
            M.addApprop(place, ptToken);
            PEff.addApprop(place, ptProc);
            Proc.addApprop(place, ptProc);
        }
        Partition procSubPartition = new Partition();
        CollectionEnumeration transEnum = transitions.keys();
        while (transEnum.hasMoreElements()) {
            ShadowTransition transition = (ShadowTransition) transEnum
                                              .nextElement();
            ConceptImpl transConcept = new ConceptImpl(netSpace
                                                       + transition.getName());
            try {
                transConcept.addIsa(Proc);
                procSubPartition.addConcept(transConcept);
            } catch (TypeException tee) {
                // should not happen
                logger.error("Type Exception during internal TypeSystem construction of Transition types!");
                return;
            }
        }
        Type tM = null;
        Type tProc = null;
        try {
            ts.inheritFeatures();
            tM = ptM.asType();
            tProc = ptProc.asType();
        } catch (UnificationFailure uff) {
            throw new SyntaxException("Something went wrong during feature inheritance:\n"
                                      + uff, uff);
        }

        // Convert all place inscriptions into inital marking FSs:
        ParsedDeclarationNode declNode = makeDeclarationNode(shadowNet);
        CollectionEnumeration placeEnum = places.keys();
        FSNode placeRoot = new FSNode(tM);
        while (placeEnum.hasMoreElements()) {
            ShadowPlace splace = (ShadowPlace) placeEnum.nextElement();
            FeatureStructure placeFS = parseFS(declNode,
                                               (String) places.at(splace));
            Place place = lookup.get(splace);
            if (placeFS.getType().getName().equals("Tok")) {
                place.add(new ExpressionTokenSource(new ConstantExpression(FeatureStructure.class,
                                                                           placeFS
                                                                           .at("val"))));
            }
            placeRoot.setFeature(new Name(splace.getName()), placeFS.getRoot());
        }
        FeatureStructure initMarking = new FeatureStructure(placeRoot);
        logger.debug("Initial Marking:" + initMarking);
        markingPlace.add(new ExpressionTokenSource(new ConstantExpression(FeatureStructure.class,
                                                                          initMarking)));
        if (procPlace != null) {
            // build initial Process:
            Node initProc = new FSNode(tProc);
            initProc.setFeature(FEATm, initMarking.getRoot());


            // add initial Process as initial marking of procPlace:
            procPlace.add(new ExpressionTokenSource(new ConstantExpression(FeatureStructure.class,
                                                                           new FeatureStructure(initProc))));
        }


        // Convert all transition inscriptions into rule FSs:
        transEnum = transitions.keys();
        while (transEnum.hasMoreElements()) {
            ShadowTransition trans = (ShadowTransition) transEnum.nextElement();
            String transString = (String) transitions.at(trans);

            // logger.debug("Unparsed rule for "+trans.getName()+":\n"+transString);
            FeatureStructure transFS = parseFS(declNode, transString).at("eff");
            FeatureStructure dTransFS = copymark(transFS);

            Transition transition = lookup.get(trans);
            Expression pre = SingleXFSNetCompiler.getVariableExpression("pre");
            getArcFactory(Arc.in, true)
                .compileArc(markingPlace, transition, trans.getTrace(),
                            Types.UNTYPED,
                            new TimedExpression(new TypedExpression(Types.UNTYPED,
                                                                    pre), null));

            Expression post = SingleXFSNetCompiler.getVariableExpression("post");
            Vector<Path> paths = new Vector<Path>();
            paths.add(PATHpre);
            Expression postExpr = SingleXFSNetCompiler.getFSAtExpression(new FSUnifyExpression(dTransFS,
                                                                                               paths,
                                                                                               makeVector(pre)),
                                                                         PATHpost);
            if (valueSem) { // value semantics?
                postExpr = new CallExpression(Types.UNTYPED, postExpr,
                                              ValueMarkingFunction.INSTANCE);
            }
            transition.add(new ExpressionInscription(new EqualsExpression(Types.UNTYPED,
                                                                          post,
                                                                          postExpr)));
            getArcFactory(Arc.out, true)
                .compileArc(markingPlace, transition, trans.getTrace(),
                            Types.UNTYPED,
                            new TimedExpression(new TypedExpression(Types.UNTYPED,
                                                                    post), null));

            if (procPlace != null) {
                // add arcs for displayed process:
                Expression proc = SingleXFSNetCompiler.getVariableExpression("proc");


                // get and remove old displayed process:
                getArcFactory(Arc.in, true)
                    .compileArc(procPlace, transition, false, Types.UNTYPED,
                                new TimedExpression(new TypedExpression(Types.UNTYPED,
                                                                        proc),
                                                    null));

                Expression newProc = new CallExpression(Types.UNTYPED, proc,
                                                        new ProcessRuleFunction(valueSem,
                                                                                processRule(trans
                                                                                            .getName(),
                                                                                            transFS)));


                // put new process onto procPlace:
                getArcFactory(Arc.out, true)
                    .compileArc(procPlace, transition, false, Types.UNTYPED,
                                new TimedExpression(new TypedExpression(Types.UNTYPED,
                                                                        newProc),
                                                    null));
            }


            // add arcs for displayed marking:
            placeEnum = places.keys();
            while (placeEnum.hasMoreElements()) {
                ShadowPlace splace = (ShadowPlace) placeEnum.nextElement();
                Place place = lookup.get(splace);


                // clear old displayed marking:
                ClearArc arc = new ClearArc(place, transition,
                                            new VariableExpression(Types.UNTYPED,
                                                                   null),
                                            Object.class);
                arc.setTrace(false);
                transition.add(arc);

                // add new tokens:
                Expression outExpr = new CallExpression(Types.UNTYPED, post,
                                                        new PlaceMarkingFunction(place
                                                                                 .getName()));
                FlexibleOutArcFactory.INSTANCE.compileArc(place, transition,
                                                          trans.getTrace(),
                                                          Types.UNTYPED,
                                                          new TimedExpression(new TypedExpression(Types.UNTYPED,
                                                                                                  outExpr),
                                                                              null));
            }


            // logger.debug(trans.getName()+":"+transFS);
        }
    }

    public static FeatureStructure parseFS(ParsedDeclarationNode declNode,
                                           String fsStr)
            throws SyntaxException {
        FSNetParser fsParser = new FSNetParser(new java.io.StringReader(fsStr));
        fsParser.setDeclarationNode(declNode);
        UpdatableMap tags = new HashedMap();
        EquivRelation er = new EquivRelation();
        try {
            Node root = fsParser.parseFS(tags, er, Path.EPSILON,
                                         new Vector<Path>(),
                                         new Vector<Object>());
            er.extensionalize();
            root = er.rebuild(root);
            return new FeatureStructure(root);
        } catch (de.renew.formalism.java.ParseException ex) {
            throw FSNetCompiler.makeSyntaxException(ex);
        } catch (Exception uff) {
            throw new SyntaxException("FS not extensionalizable!", uff);
        }
    }

    public FeatureStructure copymark(FeatureStructure rule) {
        Node pre = rule.delta(FEATpre);
        CollectionEnumeration placeEnum = places.keys();
        while (placeEnum.hasMoreElements()) {
            Name placeName = new Name(((ShadowPlace) placeEnum.nextElement())
                                 .getName());
            if (!pre.hasFeature(placeName)) {
                try {
                    rule = rule.equate(PATHpre.append(placeName),
                                       PATHpost.append(placeName));
                } catch (UnificationFailure uff) {
                    throw new RuntimeException("Internal Error in copymark:\n"
                                               + uff + "\nRule:" + rule
                                               + "placeName: " + placeName);
                }
            }
        }
        return rule;
    }

    /* Does not work for ConjunctiveTypes with concepts from different
     * namespaces and for types from concepts without namespace!
     */
    static String getNamespace(Type t) {
        String fullname = t.getFullName();
        return fullname.substring(0, fullname.indexOf("::") + 2);
    }

    public static FeatureStructure processRule(String transition,
                                               FeatureStructure transRule) {
        String netSpace = getNamespace(transRule.getType());
        Type Proc;
        Type PEff;
        Type Ttrans;
        Type M;
        try {
            PEff = ConjunctiveType.getType(netSpace + "PEff");
            Proc = ConjunctiveType.getType(netSpace + "Proc");
            M = ConjunctiveType.getType(netSpace + "M");
            Ttrans = ConjunctiveType.getType(netSpace + transition);
        } catch (UnificationFailure uff) {
            logger.error("Type " + netSpace + "PEff, Proc, M, or " + transition
                         + " not found!");
            return null;
        }
        Node processRule = new FSNode(PEff);
        processRule.setFeature(FEATpre, transRule.delta(FEATpre));
        processRule.setFeature(FEATpost, transRule.delta(FEATpost));
        Node mcopy = new FSNode(M);
        processRule.setFeature(FEATpostc, mcopy);
        Node tNode = new FSNode(Ttrans);
        tNode.setFeature(FEATm, mcopy); //transRule.delta(FEATpost));

        // for all places concerning the transition:
        Node pre = transRule.delta(FEATpre);
        CollectionEnumeration placeNames = pre.featureNames();
        while (placeNames.hasMoreElements()) {
            Name placeName = (Name) placeNames.nextElement();
            Node pNode = new FSNode(Proc);
            pNode.setFeature(placeName, tNode);
            processRule.setFeature(placeName, pNode);
        }
        return new FeatureStructure(processRule, false);
    }

    // stupid bubble-sort for place-features:
    private Seq sort(Enumeration<ShadowPlace> elems) {
        UpdatableSeq sorted = new LinkedList();
        while (elems.hasMoreElements()) {
            String elem = elems.nextElement().getName();

            // logger.debug("Inserting "+elem);
            boolean goon = true;
            for (int i = 0; goon && i < sorted.size(); ++i) {
                String sortedStr = (String) sorted.at(i);
                if (sortedStr.compareTo(elem) > 0) {
                    sorted.insertAt(i, elem);


                    // logger.debug(" at pos. "+i);
                    goon = false;
                }
            }
            if (goon) {
                // logger.debug(" at end.");
                sorted.insertLast(elem);
            }
        }
        return sorted;
    }
}