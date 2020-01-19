package de.renew.formalism.fsnet;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.ListType;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.Path;
import de.uni_hamburg.fs.Type;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.CallExpression;
import de.renew.expression.EqualsExpression;
import de.renew.expression.Expression;
import de.renew.expression.LocalVariable;
import de.renew.expression.TupleExpression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.fs.FSAtFunction;
import de.renew.formalism.fs.FSNetPreprocessor;
import de.renew.formalism.fs.FSUnifyExpression;
import de.renew.formalism.fs.SingleFSNetCompiler;
import de.renew.formalism.java.InscriptionParser;
import de.renew.formalism.java.ParseException;
import de.renew.formalism.java.TimedExpression;
import de.renew.formalism.java.Token;
import de.renew.formalism.java.TypedExpression;

import de.renew.net.Transition;
import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.inscription.ActionInscription;
import de.renew.net.inscription.DownlinkInscription;
import de.renew.net.inscription.ExpressionInscription;
import de.renew.net.inscription.GuardInscription;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import de.renew.util.StringUtil;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


public class SingleXFSNetCompiler extends SingleFSNetCompiler {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SingleXFSNetCompiler.class);

    //static final long serialVersionUID = 262012294953452116L;
    public static final String RESULT = "RESULT";
    public static final String IRESULT = "I" + RESULT;
    public static final String VAR_PREFIX = "v";
    private static int varcnt = 0;
    private String emptyArcVar = null;

    protected InscriptionParser makeParser(String inscr) {
        XFSNetParser parser = new XFSNetParser(new java.io.StringReader(inscr));
        parser.setNetLoader(loopbackNetLoader);
        return parser;
    }

    protected Collection<TimedExpression> parseArcInscription(String inscr)
            throws SyntaxException {
        if (inscr == null || inscr.equals("")) {
            inscr = "#9999";
        }
        return super.parseArcInscription(inscr);
    }

    protected void compileTransitionInscriptions(ShadowTransition shadowTransition,
                                                 Vector<TransitionInscription> parsedInscriptions,
                                                 Vector<ShadowInscription> errorShadows)
            throws SyntaxException {
        logger.debug("\nCompiling Transition " + shadowTransition.getName());
        Transition transition = lookup.get(shadowTransition);

        // Transition already exists.
        // Only insert the inscriptions.
        varcnt = 0; // reset local variable counter
        emptyArcVar = null; // reset local "empty arc" variable name
        TransitionRule transitionRule = new TransitionRule();
        transitionRule.append("[Any\n");
        Iterator<ShadowNetElement> inscriptions = shadowTransition.elements()
                                                                  .iterator();
        Vector<String> inputArcVars = new Vector<String>(); // of Var-Names (Name("v0"),Name("v1")...
        Vector<String> transVars = new Vector<String>(); // of Var-Names (Name("v0"),Name("v1")...
        while (inscriptions.hasNext()) {
            Object elem = inscriptions.next();
            if (elem instanceof ShadowInscription) {
                ShadowInscription inscription = (ShadowInscription) elem;
                if (!inscription.inscr.startsWith(" ")) {
                    String transVar = newVariable();
                    transVars.addElement(transVar);
                    transitionRule.append(transVar, inscription.inscr,
                                          inscription);
                } else {
                    // JavaNet inscription:
                    Collection<TransitionInscription> subinscriptions = null;
                    try {
                        subinscriptions = makeInscriptions(inscription.inscr
                                              .substring(1),
                                                           getLookup()
                                                               .get(shadowTransition),
                                                           true);
                    } catch (SyntaxException e) {
                        throw e.addObject(inscription);
                    }

                    for (Iterator<TransitionInscription> i = subinscriptions
                             .iterator(); i.hasNext();) {
                        parsedInscriptions.addElement(i.next());
                        errorShadows.addElement(inscription);
                    }
                }
            } else if (elem instanceof ShadowArc) {
                compileSingleArc((ShadowArc) elem, transitionRule,
                                 inputArcVars, parsedInscriptions);
            } else {
                super.compileTransitionInscriptions(shadowTransition,
                                                    parsedInscriptions,
                                                    errorShadows);
            }
        }
        transitionRule.append("]");
        XFSNetParser parser = (XFSNetParser) makeParser(transitionRule.toString());
        parser.setLookup(getLookup());
        parser.setDeclarationNode(declaration);

        try {
            FSUnifyExpression fsExpr = parser.TransitionRule();


            //logger.debug("Parsed transition rule TMP0:\n"+fsExpr.getTemplate());
            // Look for Up- and Downlinks:
            Node fs = fsExpr.getTemplate().getRoot();
            String uplinkVar = null;
            String someRule = null;
            int dlVarCnt = 0;
            Enumeration<String> tVarEnum = transVars.elements();
            while (tVarEnum.hasMoreElements()) {
                String transVar = tVarEnum.nextElement();
                Name feature = new Name(VAR_PREFIX + transVar);

                // for each Transition inscription feature:
                Node subfs = fs.delta(feature);
                Type subfsType = subfs.getType();
                if (subfsType.getName().equals(FSNetPreprocessor.LINK)) {
                    if (!subfs.hasFeature(FSNetPreprocessor.RCV)) {
                        // is Uplink:
                        //logger.debug("Uplink found at "+feature+": "+new FeatureStructure(subfs));
                        if (uplinkVar != null) {
                            throw new SyntaxException("Transition has more than one uplink.");
                        }
                        uplinkVar = transVar;
                        inputArcVars.addElement(transVar);
                    } else {
                        // is Downlink:
                        addDownlink(parsedInscriptions, //fsExpr,
                                    dlVarCnt++, new Path(feature), transition);
                    }
                } else if (subfsType instanceof ListType
                                   && ((ListType) subfsType).getBaseType()
                                               .getName()
                                               .equals(FSNetPreprocessor.LINK)) {
                    // is Downlink-List:
                    Path listPath = new Path(feature);
                    while (((ListType) fs.delta(listPath).getType()).getSubtype() == ListType.NELIST) {
                        Path dlPath = listPath.append(ListType.HEAD);
                        addDownlink(parsedInscriptions, //fsExpr,
                                    dlVarCnt++, dlPath, transition);
                        listPath = listPath.append(ListType.TAIL);
                    }
                } else {
                    // is no Up- or Downlink:
                    someRule = VAR_PREFIX + transVar;
                }
            }

            //logger.debug("emptyArcVar: "+emptyArcVar+" someRule: "+someRule);
            if (emptyArcVar != null && someRule != null) {
                // equate the node at emptyArcName and the transition rule
                // at someRule:
                try {
                    fsExpr = new FSUnifyExpression(fsExpr.getTemplate()
                                                         .equate(emptyArcVar,
                                                                 someRule),
                                                   fsExpr.getPaths(),
                                                   fsExpr.getExprs());
                } catch (UnificationFailure uff) {
                    logger.error("Internal error: Unification of empty arc var and transition rule failed!\\"
                                 + uff);
                }
            }
            if (uplinkVar != null) {
                Path uplinkPath = new Path(VAR_PREFIX + uplinkVar + ":"
                                           + FSNetPreprocessor.PARAM);
                logger.debug(":s(v" + uplinkVar + ",TMP" + dlVarCnt
                             + atExpr(uplinkPath) + ",UL);");
                parsedInscriptions.addElement(new UplinkInscription("s",
                                                                    getTriple(getVariableExpression(uplinkVar),
                                                                              getFSAtExpression(getVariableExpression("TMP"
                                                                                                                      + dlVarCnt),
                                                                                                uplinkPath),
                                                                              getVariableExpression("UL"))));
                logger.debug("TMP" + (dlVarCnt + 1) + " = TMP" + dlVarCnt
                             + ".unify(UL,\"" + uplinkPath + "\");");
                parsedInscriptions.addElement(new ExpressionInscription(new EqualsExpression(FeatureStructure.class,
                                                                                             getVariableExpression("TMP"
                                                                                                                   + (dlVarCnt
                                                                                                                     + 1)),
                                                                                             new CallExpression(FeatureStructure.class,
                                                                                                                new TupleExpression(new Expression[] { getVariableExpression("TMP"
                                                                                                                                                                             + dlVarCnt), getVariableExpression("UL") }),
                                                                                                                new FSUnifyAtFunction(uplinkPath)))));
                dlVarCnt++;
            }
            logger.debug("RESULT = TMP" + dlVarCnt + ";");
            parsedInscriptions.addElement(new ExpressionInscription(new EqualsExpression(FeatureStructure.class,
                                                                                         getVariableExpression(RESULT),
                                                                                         getVariableExpression("TMP"
                                                                                                               + dlVarCnt))));

            logger.debug("action IRESULT = RESULT.instantiate();");
            parsedInscriptions.addElement(new ActionInscription(new EqualsExpression(FeatureStructure.class,
                                                                                     getVariableExpression(IRESULT),
                                                                                     new CallExpression(FeatureStructure.class,
                                                                                                        getVariableExpression(RESULT),
                                                                                                        InstantiateFunction.INSTANCE)),
                                                                transition));

            logger.debug("guard RESULT.canInstantiate();");
            parsedInscriptions.addElement(new GuardInscription(new CallExpression(Boolean.TYPE,
                                                                                  getVariableExpression(RESULT),
                                                                                  CanInstantiateFunction.INSTANCE)));


            logger.debug("TMP0=" + transitionRule + "\n+[Any");
            Enumeration<String> varenumeration = inputArcVars.elements();
            while (varenumeration.hasMoreElements()) {
                String var = varenumeration.nextElement();
                String varpath = VAR_PREFIX + var;
                if (var.equals(uplinkVar)) {
                    varpath += ":" + FSNetPreprocessor.PARAM;
                }
                logger.debug(" " + varpath + ":(v" + var + ")");
                fsExpr.getPaths().addElement(new Path(varpath));
                fsExpr.getExprs()
                      .addElement(new VariableExpression(FeatureStructure.class,
                                                         new LocalVariable(var)));
            }
            logger.debug("];");


            // Build TMP0=<transitionRule>:
            parsedInscriptions.addElement(new ExpressionInscription(new EqualsExpression(FeatureStructure.class,
                                                                                         getVariableExpression("TMP0"),
                                                                                         fsExpr)));
        } catch (de.renew.formalism.java.ParseException e) {
            throw makeSyntaxException(e, shadowTransition,
                                      transitionRule.inscrAreas);
        }
    }

    private void addDownlink(Vector<TransitionInscription> parsedInscriptions, //FSUnifyExpression fsExpr,
                             int dlVarCnt, Path dlpath, Transition transition) {
        //logger.debug("Downlink found at "+dlpath); //+": "+fsExpr.template.at(dlpath));
        String prevTmpVar = "TMP" + String.valueOf(dlVarCnt);
        String paramVar = "DL" + String.valueOf(++dlVarCnt);
        Path rcvpath = dlpath.append(FSNetPreprocessor.RCV);
        Path parampath = dlpath.append(FSNetPreprocessor.PARAM);


        // Build Downlink:
        logger.debug(prevTmpVar + atExpr(rcvpath) + ": s(" + prevTmpVar
                     + atExpr(parampath) + "," + paramVar + ",RESULT"
                     + atExpr(parampath) + ");");
        parsedInscriptions.addElement(new DownlinkInscription("s",
                                                              getTriple(getFSAtExpression(getVariableExpression(prevTmpVar),
                                                                                          parampath),
                                                                        getVariableExpression(paramVar),
                                                                        getFSAtExpression(getVariableExpression(RESULT),
                                                                                          parampath)),
                                                              getFSAtExpression(getVariableExpression(prevTmpVar),
                                                                                rcvpath,
                                                                                true),
                                                              false, transition));

        String tmpVar = "TMP" + dlVarCnt;


        // Build EqualsExpresion:
        logger.debug(tmpVar + "=" + prevTmpVar + ".unify(" + paramVar + ",\""
                     + parampath + "\");");
        parsedInscriptions.addElement(new ExpressionInscription(new EqualsExpression(FeatureStructure.class,
                                                                                     getVariableExpression(tmpVar),
                                                                                     new CallExpression(FeatureStructure.class,
                                                                                                        new TupleExpression(new Expression[] { getVariableExpression(prevTmpVar), getVariableExpression(paramVar) }),
                                                                                                        new FSUnifyAtFunction(parampath)))));
    }

    private static String atExpr(Path path) {
        return "@" + path.toString().replace(':', '@');
    }

    private static String newVariable() {
        return String.valueOf(varcnt++);
    }

    public static VariableExpression getVariableExpression(String varName) {
        return new VariableExpression(FeatureStructure.class,
                                      new LocalVariable(varName));
    }

    public static CallExpression getFSAtExpression(Expression expr, Path path) {
        return getFSAtExpression(expr, path, false);
    }

    public static CallExpression getFSAtExpression(Expression expr, Path path,
                                                   boolean unpack) {
        return new CallExpression(FeatureStructure.class, expr,
                                  new FSAtFunction(path, unpack));
    }

    public static TupleExpression getTriple(Expression c1, Expression c2,
                                            Expression c3) {
        return new TupleExpression(new Expression[] { c1, c2, c3 });
    }

    private void compileSingleArc(ShadowArc shadowArc,
                                  TransitionRule transitionRule,
                                  Vector<String> vars,
                                  Vector<TransitionInscription> parsedInscriptions)
            throws SyntaxException {
        Iterator<ShadowNetElement> arcinscrs = shadowArc.elements().iterator();
        if (arcinscrs.hasNext()) {
            logger.debug("Type:" + shadowArc.place.getName());
            do {
                ShadowInscription inscr = (ShadowInscription) arcinscrs.next();
                compileArcInscription(inscr, inscr.inscr, shadowArc,
                                      transitionRule, vars, parsedInscriptions);
            } while (arcinscrs.hasNext());
        } else {
            emptyArcVar = VAR_PREFIX
                          + compileArcInscription(null, "#9999", shadowArc,
                                                  transitionRule, vars,
                                                  parsedInscriptions);
        }
    }

    private String compileArcInscription(ShadowInscription si, String inscr,
                                         ShadowArc shadowArc,
                                         TransitionRule transitionRule,
                                         Vector<String> vars,
                                         Vector<TransitionInscription> parsedInscriptions)
            throws SyntaxException {
        String var = newVariable();
        if (logger.isDebugEnabled()) {
            logger.debug("Found arc "
                         + (shadowArc.placeToTransition ? "from" : "to") + " "
                         + lookup.get(shadowArc.place).getName()
                         + ", assigning variable " + var);
        }

        //logger.debug("  Arcinscription "+inscr+" mapped to "+var);
        transitionRule.append(var, inscr, si);
        if (shadowArc.shadowArcType == ShadowArc.inhibitor
                    || (shadowArc.shadowArcType == ShadowArc.ordinary
                               && !shadowArc.placeToTransition)) {
            boolean isEarly = shadowArc.shadowArcType == ShadowArc.inhibitor;
            Transition transition = lookup.get(shadowArc.transition);
            String result = isEarly ? RESULT : IRESULT;
            String vvar = VAR_PREFIX + var;
            logger.debug((isEarly ? "" : "action ") + vvar + " = " + result
                         + "@" + vvar + ";");
            Expression outArcVarExpr = new EqualsExpression(FeatureStructure.class,
                                                            getVariableExpression(var),
                                                            getFSAtExpression(getVariableExpression(result),
                                                                              new Path(vvar),
                                                                              true));
            if (isEarly) {
                parsedInscriptions.addElement(new ExpressionInscription(outArcVarExpr));
            } else {
                parsedInscriptions.addElement(new ActionInscription(outArcVarExpr,
                                                                    transition));
            }
        } else {
            vars.addElement(var);
        }
        getArcFactory(shadowArc)
            .compileArc(getLookup().get(shadowArc.place),
                        getLookup().get(shadowArc.transition),
                        shadowArc.getTrace(), FeatureStructure.class,
                        new TimedExpression(new TypedExpression(FeatureStructure.class,
                                                                getVariableExpression(var)),
                                            null));
        return var;
    }

    public static SyntaxException makeSyntaxException(ParseException pe,
                                                      ShadowTransition trans,
                                                      Vector<ShadowInscriptionArea> sias) {
        ShadowInscriptionArea sia = null;
        if (pe.currentToken != null) {
            Token posToken = pe.currentToken;
            if (posToken.next != null) {
                posToken = posToken.next;
            }
            int line = posToken.beginLine;


            //logger.debug("Looking for line "+line);
            Enumeration<ShadowInscriptionArea> siaelems = sias.elements();
            while (siaelems.hasMoreElements()) {
                sia = siaelems.nextElement();
                //logger.debug("Found range "+sia.startLine+"-"+sia.lastLine);
                if (sia.matches(line)) {
                    break;
                }
                sia = null;
            }
            if (sia != null) {
                posToken.beginLine -= sia.startLine - 1;
            }
        }
        SyntaxException se = makeSyntaxException(pe);
        if (sia != null) {
            se.addObject(sia.inscription);
        } else {
            se.addObject(trans);
        }
        return se;
    }
}

class ShadowInscriptionArea {
    int startLine;
    int lastLine;
    ShadowInscription inscription;

    ShadowInscriptionArea(int startLine, int lastLine,
                          ShadowInscription inscription) {
        this.startLine = startLine;
        this.inscription = inscription;
        this.lastLine = lastLine;
    }

    boolean matches(int line) {
        return startLine <= line && line <= lastLine;
    }
}

class TransitionRule {
    StringBuffer fs = new StringBuffer();
    Vector<ShadowInscriptionArea> inscrAreas = new Vector<ShadowInscriptionArea>();
    int line = 1;

    void append(String partFS) {
        fs.append(partFS);
        line += StringUtil.countLines(partFS) - 1;
    }

    void append(String var, String inscr, ShadowInscription si) {
        fs.append(SingleXFSNetCompiler.VAR_PREFIX).append(var).append(":\n");
        int startLine = ++line;
        append(inscr + "\n");
        if (si != null) {
            ShadowInscriptionArea sia = new ShadowInscriptionArea(startLine,
                                                                  line - 1, si);
            inscrAreas.addElement(sia);
        }
    }

    public String toString() {
        return fs.toString();
    }
}