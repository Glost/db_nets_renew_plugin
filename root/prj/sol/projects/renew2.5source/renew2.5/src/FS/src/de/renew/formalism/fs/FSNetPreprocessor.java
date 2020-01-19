package de.renew.formalism.fs;

import collections.CollectionEnumeration;

import de.uni_hamburg.fs.Concept;
import de.uni_hamburg.fs.ConceptImpl;
import de.uni_hamburg.fs.ConceptSet;
import de.uni_hamburg.fs.CyclicHierarchyException;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.OrderedTable;
import de.uni_hamburg.fs.ParsedConjunctiveType;
import de.uni_hamburg.fs.ParsedListType;
import de.uni_hamburg.fs.ParsedType;
import de.uni_hamburg.fs.Partition;
import de.uni_hamburg.fs.TypeException;
import de.uni_hamburg.fs.TypeSystem;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.net.NetInstance;

import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.ShadowPreprocessor;
import de.renew.shadow.SyntaxException;

import java.util.Iterator;


public class FSNetPreprocessor implements ShadowPreprocessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FSNetPreprocessor.class);
    public static final String LINK = "Link";
    public static final Name RCV = new Name("rcv");
    public static final Name PARAM = new Name("param");
    private ShadowLookup shadowLookup;

    public void setShadowLookup(ShadowLookup shadowLookup) {
        this.shadowLookup = shadowLookup;
    }

    protected ShadowLookup getShadowLookup() {
        return shadowLookup;
    }

    public void preprocess(ShadowNetSystem netSystem) throws SyntaxException {
        // compile the type hierarchy...
        // First, remove all (old) user concepts:
        TypeSystem ts = TypeSystem.newInstance();
        Partition topPartition = ts.getTopPartition();


        // add link concept
        ConceptImpl linkConcept = new ConceptImpl(LINK);
        linkConcept.addApprop(RCV,
                              new ParsedConjunctiveType(new ConceptSet(ts
            .getJavaConcept(NetInstance.class))));
        linkConcept.addApprop(PARAM, ParsedType.PARSED_TOP);


        // second, compile all concepts from all drawings:
        Iterator<ShadowNet> iterator = netSystem.elements().iterator();
        while (iterator.hasNext()) {
            compileConcepts(iterator.next(), topPartition);
        }


        // then, add all "approp"-information:
        iterator = netSystem.elements().iterator();
        while (iterator.hasNext()) {
            compileApprops(iterator.next());
        }


        // then, add all "isNotA"-information:
        iterator = netSystem.elements().iterator();
        while (iterator.hasNext()) {
            compilePartitions(iterator.next());
        }


        // last, add all "isa"-information:
        iterator = netSystem.elements().iterator();
        while (iterator.hasNext()) {
            compileIsas(iterator.next());
        }

        // and now:
        try {
            ts.inheritFeatures();
        } catch (UnificationFailure uff) {
            SyntaxException ex;
            if (uff instanceof TypeException) {
                TypeException tex = (TypeException) uff;
                String msg = "Feature \"" + tex.featureName
                             + "\" has been redefined illegally in concept \""
                             + tex.concept.getName() + "\".";
                ex = new SyntaxException(msg, uff);
                ex.addObject(FSShadowLookupExtension.lookup(shadowLookup)
                                                    .getApprop(tex.concept,
                                                               tex.featureName));
            } else {
                ex = new SyntaxException("Unification failure while constructing feature types.",
                                         uff);
            }
            throw ex;
        }
    }

    private void compileConcepts(ShadowNet net, Partition topPartition)
            throws SyntaxException {
        String netName = net.getName();
        Iterator<ShadowNetElement> iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowConcept) {
                ShadowConcept shadowConcept = (ShadowConcept) elem;
                if (shadowConcept.getNamespace().equals(netName)) {
                    // only compile "local" concepts
                    String conceptName = shadowConcept.getFullName();
                    conceptName = withoutStereotype(conceptName);
                    logger.debug("Compiling concept " + conceptName + "...");
                    // Concept with that name may already exist:
                    if (TypeSystem.instance().hasConcept(conceptName)) {
                        SyntaxException ex = new SyntaxException("The concept \""
                                                                 + conceptName
                                                                 + "\" already exists.");
                        ex.addObject(shadowConcept);
                        throw ex;
                    }
                    ConceptImpl concept = new ConceptImpl(conceptName);
                    FSShadowLookupExtension.lookup(shadowLookup)
                                           .setConcept(shadowConcept, concept);
                    if (shadowConcept.elements().isEmpty()) {
                        // is this a top level concept?
                        try {
                            topPartition.addConcept(concept);
                        } catch (TypeException tex) {
                            // should never happen!
                            throw new RuntimeException(tex.toString());
                        }
                    }

                    //logger.debug("Created Concept "+conceptName);
                }
            }
        }
    }

    private static String withoutStereotype(String str) {
        return str.replaceAll("<<[^>]*>>", "").replaceAll("\n", "");
    }

    private Concept lookup(ShadowConcept shadowConcept)
            throws SyntaxException {
        // check referenced concepts:
        String conceptName = withoutStereotype(shadowConcept.getFullName());
        if (!TypeSystem.instance().hasConcept(conceptName)) {
            SyntaxException ex = new SyntaxException("Referenced Concept "
                                                     + conceptName
                                                     + " does not exist.");
            ex.addObject(shadowConcept);
            throw ex;
        }
        return TypeSystem.instance().conceptForName(conceptName);
    }

    protected SingleFSNetCompiler createSingleFSNetCompiler() {
        return new SingleFSNetCompiler();
    }

    private void compileApprops(ShadowNet net) throws SyntaxException {
        Iterator<ShadowNetElement> iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowAssoc) {
                ShadowAssoc shadowAssoc = (ShadowAssoc) elem;
                ConceptImpl concept = (ConceptImpl) lookup(shadowAssoc
                                          .getConcept());
                ConceptSet typ = new ConceptSet(lookup(shadowAssoc.getType()));
                int mult = shadowAssoc.getMultiplicity();
                Name featureName = shadowAssoc.getFeature();
                ParsedType pt = new ParsedConjunctiveType(typ);
                if (mult != ShadowAssoc.ZERO_OR_ONE) {
                    pt = new ParsedListType(mult == ShadowAssoc.ONE_OR_MORE, pt);
                }
                concept.addApprop(featureName, pt);
                FSShadowLookupExtension.lookup(shadowLookup)
                                       .setApprop(concept, featureName,
                                                  shadowAssoc);
                //logger.debug("Found Assoc: "+shadowAssoc.getFeature()+": "+typ);
            }
        }

        iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowConcept) {
                ShadowConcept shadowConcept = (ShadowConcept) elem;
                ConceptImpl concept = (ConceptImpl) lookup(shadowConcept);


                // parse lines:
                // ### this is a hack that allows us to use the standard
                // compiler decomposition.
                SingleFSNetCompiler compiler = createSingleFSNetCompiler();
                compiler.setShadowLookup(shadowLookup);
                FSInscriptionParser parser = (FSInscriptionParser) compiler
                                                 .makeParser("\n"
                                                             + shadowConcept
                                                 .getApprop());
                parser.setLookup(shadowLookup);
                ParsedFSDeclarationNode decl = (ParsedFSDeclarationNode) compiler
                                               .makeDeclarationNode(net);
                try {
                    decl.addAccess(net.getName(), null);
                } catch (de.renew.formalism.java.ParseException e) {
                }
                parser.setDeclarationNode(decl);
                try {
                    OrderedTable approps = parser.parseAppropDef();

                    //concept.setApprops(approps);
                    CollectionEnumeration appropenumeration = approps.keys();
                    while (appropenumeration.hasMoreElements()) {
                        Name featureName = (Name) appropenumeration.nextElement();
                        concept.addApprop(featureName,
                                          (ParsedType) approps.at(featureName));
                        FSShadowLookupExtension.lookup(shadowLookup)
                                               .setApprop(concept, featureName,
                                                          shadowConcept);
                    }
                } catch (de.renew.formalism.java.ParseException e) {
                    SyntaxException ex = FSNetCompiler.makeSyntaxException(e);
                    ex.addObject(shadowConcept);
                    throw ex;
                }
            }
        }
    }

    private void compilePartitions(ShadowNet net) throws SyntaxException {
        Iterator<ShadowNetElement> iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowIsa) {
                ShadowIsa shadowIsa = (ShadowIsa) elem;
                if (shadowIsa.isDisjunctive()) {
                    ConceptImpl sub = (ConceptImpl) lookup(shadowIsa.getSource());
                    ConceptImpl supi = (ConceptImpl) lookup(shadowIsa.getTarget());
                    try {
                        FSShadowLookupExtension.lookup(shadowLookup)
                                               .addToSubPartition(supi, sub);
                    } catch (TypeException tex) {
                        SyntaxException ex = new SyntaxException("This disjunction leads to a contradiction.",
                                                                 tex);
                        ex.addObject(shadowIsa);
                        throw ex;
                    }
                }
            }
        }
    }

    private void compileIsas(ShadowNet net) throws SyntaxException {
        Iterator<ShadowNetElement> iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            ShadowNetElement elem = iterator.next();
            if (elem instanceof ShadowIsa) {
                ShadowIsa shadowIsa = (ShadowIsa) elem;
                ConceptImpl sub = (ConceptImpl) lookup(shadowIsa.getSource());
                ShadowConcept supi = shadowIsa.getTarget();
                ConceptImpl sup = (ConceptImpl) lookup(supi);
                logger.debug("Compiling is-a from " + sub.getName() + " to "
                             + sup.getName());
                try {
                    sub.addIsa(sup);
                } catch (CyclicHierarchyException e) {
                    SyntaxException ex = new SyntaxException("This is-a-relation leads to a cyclic type hierarchy.",
                                                             e);
                    ex.addObject(shadowIsa);
                    throw ex;
                } catch (TypeException te) {
                    SyntaxException ex = new SyntaxException("This is-a-relation leads to a contradiction.",
                                                             te);
                    ex.addObject(shadowIsa);
                    throw ex;
                }
            }
        }
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean equals(Object that) {
        return that != null && this.getClass().equals(that.getClass());
    }
}