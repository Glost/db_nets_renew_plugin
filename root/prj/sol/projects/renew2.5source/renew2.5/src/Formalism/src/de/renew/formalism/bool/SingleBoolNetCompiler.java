package de.renew.formalism.bool;

import de.renew.expression.CallExpression;
import de.renew.expression.ConstantExpression;
import de.renew.expression.EqualsExpression;
import de.renew.expression.Expression;
import de.renew.expression.Function;
import de.renew.expression.LocalVariable;
import de.renew.expression.TupleExpression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.function.BasicFunction;

import de.renew.net.ConstantTokenSource;
import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;
import de.renew.net.inscription.ExpressionInscription;
import de.renew.net.inscription.GuardInscription;
import de.renew.net.inscription.RangeEnumeratorInscription;
import de.renew.net.loading.NetLoader;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowPreprocessor;
import de.renew.shadow.ShadowTransition;
import de.renew.shadow.SyntaxException;

import de.renew.util.StringUtil;
import de.renew.util.Types;
import de.renew.util.Value;

import java.util.Iterator;
import java.util.Vector;


public class SingleBoolNetCompiler implements ShadowCompiler {
    private static final Value value0 = new Value(new Integer(0));
    private static final Value value1 = new Value(new Integer(1));
    private static final Expression expr0 = new ConstantExpression(Types.UNTYPED,
                                                                   value0);
    private static final Expression expr1 = new ConstantExpression(Types.UNTYPED,
                                                                   value1);
    private static final LocalVariable locVarAct = new LocalVariable("act");
    private static final VariableExpression locVarExprAct = new VariableExpression(Types.UNTYPED,
                                                                                   locVarAct);
    private int placeNum;
    private int transNum;
    private int varNum;
    private ShadowLookup lookup;

    public void setLoopbackNetLoader(NetLoader loopbackNetLoader) {
        // Ignore. No nets are loaded from this compiler.
    }

    public void setShadowLookup(ShadowLookup lookup) {
        this.lookup = lookup;
    }

    public Net createNet(String name) {
        return new Net(name);
    }

    public ShadowPreprocessor[] getRequiredPreprocessors() {
        return new ShadowPreprocessor[0];
    }

    void compile(Net net, ShadowPlace shadowPlace) throws SyntaxException {
        // Determine the name.
        String pname = shadowPlace.getName();
        if (pname == null) {
            placeNum++;
            pname = "P" + placeNum;
        }

        // Create the new place;
        Place place = new Place(net, pname,
                                new NetElementID(shadowPlace.getID()));
        place.setTrace(shadowPlace.getTrace());
        lookup.set(shadowPlace, place);


        // Insert the inscriptions.
        Iterator<ShadowNetElement> iterator = shadowPlace.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowInscription) {
                Object token;
                String inscr = StringUtil.unspace(((ShadowInscription) elem).inscr);
                if ("0".equals(inscr) || "false".equals(inscr)) {
                    token = value0;
                } else if ("1".equals(inscr) || "true".equals(inscr)) {
                    token = value1;
                } else {
                    throw new SyntaxException("Expected one of: 0, 1, true, false.")
                          .addObject(elem);
                }
                place.add(new ConstantTokenSource(token));
            } else if (elem instanceof ShadowArc) {
                // ignore it.
            } else {
                throw new SyntaxException("Unsupported place inscription: "
                                          + elem.getClass()).addObject(shadowPlace)
                                                                                              .addObject(elem);
            }
        }
    }

    String compileTransitionInscriptions(ShadowTransition shadowTransition,
                                         Vector<Place> inPlaces,
                                         Vector<Place> outPlaces)
            throws SyntaxException {
        String mode = null;


        // Parse the inscriptions.
        Iterator<ShadowNetElement> iterator = shadowTransition.elements()
                                                              .iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowInscription) {
                String inscr = StringUtil.unspace(((ShadowInscription) elem).inscr);
                if ("and".equals(inscr) || "or".equals(inscr)
                            || "xor".equals(inscr)) {
                    if (mode != null) {
                        throw new SyntaxException("Too many inscriptions.")
                                  .addObject(shadowTransition);
                    }
                    mode = inscr;
                } else {
                    throw new SyntaxException("Expected one of: and, or, xor.")
                          .addObject(elem);
                }
            } else if (elem instanceof ShadowArc) {
                ShadowArc arc = (ShadowArc) elem;
                Place place = lookup.get(arc.place);
                if (arc.shadowArcType == ShadowArc.both) {
                    inPlaces.addElement(place);
                    outPlaces.addElement(place);
                } else if (arc.shadowArcType == ShadowArc.ordinary) {
                    if (arc.placeToTransition) {
                        inPlaces.addElement(place);
                    } else {
                        outPlaces.addElement(place);
                    }
                } else {
                    throw new SyntaxException("Unsupported arc type").addObject(arc);
                }
            } else {
                throw new SyntaxException("Unsupported transition inscription").addObject(shadowTransition)
                                                                               .addObject(elem);
            }
        }

        return mode;
    }

    LocalVariable createAnonVariable() {
        return new LocalVariable(String.valueOf(varNum++), false);
    }

    LocalVariable createPlaceVariable(LocalVariableSet variables,
                                      String prefix, Place place) {
        String name = place.getName();
        if (name == null) {
            return createAnonVariable();
        }
        return variables.create(prefix + name);
    }

    void compile(Net net, ShadowTransition shadowTransition)
            throws SyntaxException {
        // Determine the name.
        String tname = shadowTransition.getName();
        if (tname == null) {
            transNum++;
            tname = "T" + transNum;
        }

        // Create the new transition;
        Transition transition = new Transition(net, tname,
                                               new NetElementID(shadowTransition
                                                                .getID()));
        transition.setTrace(shadowTransition.getTrace());
        lookup.set(shadowTransition, transition);


        // Vectors of Place objects.
        Vector<Place> inPlaces = new Vector<Place>();
        Vector<Place> outPlaces = new Vector<Place>();
        String mode = compileTransitionInscriptions(shadowTransition, inPlaces,
                                                    outPlaces);
        if (mode == null) {
            mode = "and";
        }

        // Keep track of all local variables that are created.
        LocalVariableSet variables = new LocalVariableSet();

        // Handle all input arcs.
        if ("and".equals(mode)) {
            for (int i = 0; i < inPlaces.size(); i++) {
                Place place = inPlaces.elementAt(i);
                Arc arc = new Arc(place, transition, Arc.in, locVarExprAct,
                                  ConstantExpression.doubleZeroExpression);
                arc.setTrace(shadowTransition.getTrace());
                transition.add(arc);
            }
        }

        if ("or".equals(mode) || "xor".equals(mode)) {
            Expression sum = expr0;
            for (int i = 0; i < inPlaces.size(); i++) {
                Place place = inPlaces.elementAt(i);
                Expression expr = new VariableExpression(Types.UNTYPED,
                                                         createPlaceVariable(variables,
                                                                             "in",
                                                                             place));
                Arc arc = new Arc(place, transition, Arc.in, expr,
                                  ConstantExpression.doubleZeroExpression);
                arc.setTrace(shadowTransition.getTrace());
                transition.add(arc);
                sum = new CallExpression(Types.UNTYPED,
                                         new TupleExpression(new Expression[] { sum, expr }),
                                         BasicFunction.PLUS);
            }

            if ("or".equals(mode)) {
                Function func = new Function() {
                    public Object function(Object arg) {
                        if (value0.equals(arg)) {
                            return value0;
                        } else {
                            return value1;
                        }
                    }
                };

                Expression assignExpr = new EqualsExpression(Types.UNTYPED,
                                                             locVarExprAct,
                                                             new CallExpression(Types.UNTYPED,
                                                                                sum,
                                                                                func));
                transition.add(new ExpressionInscription(assignExpr));
            } else {
                Expression assignExpr = new EqualsExpression(Types.UNTYPED,
                                                             locVarExprAct, sum);
                transition.add(new ExpressionInscription(assignExpr));

                Expression compareExpr = new CallExpression(Types.UNTYPED,
                                                            new TupleExpression(new Expression[] { sum, expr1 }),
                                                            BasicFunction.LESSEQUAL);
                transition.add(new GuardInscription(compareExpr));
            }
        }

        // Handle all output arcs.
        if ("and".equals(mode)) {
            for (int i = 0; i < outPlaces.size(); i++) {
                Place place = outPlaces.elementAt(i);
                Arc arc = new Arc(place, transition, Arc.out, locVarExprAct,
                                  ConstantExpression.doubleZeroExpression);
                arc.setTrace(shadowTransition.getTrace());
                transition.add(arc);
            }
        }

        if ("or".equals(mode)) {
            Expression sum = expr0;
            for (int i = 0; i < outPlaces.size(); i++) {
                Place place = outPlaces.elementAt(i);
                Expression expr = new VariableExpression(Types.UNTYPED,
                                                         createPlaceVariable(variables,
                                                                             "out",
                                                                             place));
                Arc arc = new Arc(place, transition, Arc.out, expr,
                                  ConstantExpression.doubleZeroExpression);
                arc.setTrace(shadowTransition.getTrace());
                transition.add(arc);
                RangeEnumeratorInscription range = new RangeEnumeratorInscription(expr,
                                                                                  0,
                                                                                  1,
                                                                                  false,
                                                                                  transition);
                transition.add(range);
                sum = new CallExpression(Types.UNTYPED,
                                         new TupleExpression(new Expression[] { sum, expr }),
                                         BasicFunction.PLUS);
                Expression compareExpr = new CallExpression(Types.UNTYPED,
                                                            new TupleExpression(new Expression[] { expr, locVarExprAct }),
                                                            BasicFunction.LESSEQUAL);
                transition.add(new GuardInscription(compareExpr));
            }

            Expression globalCompareExpr = new CallExpression(Types.UNTYPED,
                                                              new TupleExpression(new Expression[] { sum, locVarExprAct }),
                                                              BasicFunction.GREATEREQUAL);
            transition.add(new GuardInscription(globalCompareExpr));
        }

        if ("xor".equals(mode)) {
            Expression choiceExpr = new VariableExpression(Types.UNTYPED,
                                                           createAnonVariable());
            for (int i = 0; i < outPlaces.size(); i++) {
                Place place = outPlaces.elementAt(i);

                final Value iPlusOne = new Value(new Integer(i + 1));
                Function func = new Function() {
                    public Object function(Object arg) {
                        if (iPlusOne.equals(arg)) {
                            return value1;
                        } else {
                            return value0;
                        }
                    }
                };

                Arc arc = new Arc(place, transition, Arc.out,
                                  new EqualsExpression(Types.UNTYPED,
                                                       new VariableExpression(Types.UNTYPED,
                                                                              createPlaceVariable(variables,
                                                                                                  "out",
                                                                                                  place)),
                                                       new CallExpression(Types.UNTYPED,
                                                                          choiceExpr,
                                                                          func)),
                                  ConstantExpression.doubleZeroExpression);
                arc.setTrace(shadowTransition.getTrace());
                transition.add(arc);
            }

            RangeEnumeratorInscription range = new RangeEnumeratorInscription(choiceExpr,
                                                                              0,
                                                                              outPlaces
                                                                              .size(),
                                                                              false,
                                                                              transition);
            transition.add(range);

            Expression compareExpr = new CallExpression(Types.UNTYPED,
                                                        new TupleExpression(new Expression[] { new CallExpression(Types.UNTYPED,
                                                                                                                  new TupleExpression(new Expression[] { locVarExprAct, expr0 }),
                                                                                                                  BasicFunction.EQUAL), new CallExpression(Types.UNTYPED,
                                                                                                                                                           new TupleExpression(new Expression[] { choiceExpr, expr0 }),
                                                                                                                                                           BasicFunction.EQUAL) }),
                                                        BasicFunction.EQUAL);
            transition.add(new GuardInscription(compareExpr));
        }
    }

    public void compile(ShadowNet shadowNet) throws SyntaxException {
        Net net = new Net(shadowNet.getName());

        Iterator<ShadowNetElement> iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowPlace) {
                compile(net, (ShadowPlace) elem);
            }
        }

        iterator = shadowNet.elements().iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            if (elem instanceof ShadowTransition) {
                compile(net, (ShadowTransition) elem);
            }
        }

        lookup.setNet(shadowNet.getName(), net);
    }

    public String checkDeclarationNode(String inscr, boolean special,
                                       ShadowNet shadowNet) {
        return "unknown";
    }

    public String checkArcInscription(String inscr, boolean special,
                                      ShadowNet shadowNet) {
        return "unknown";
    }

    public String checkTransitionInscription(String inscr, boolean special,
                                             ShadowNet shadowNet) {
        return "unknown";
    }

    public String checkPlaceInscription(String inscr, boolean special,
                                        ShadowNet shadowNet) {
        return "unknown";
    }
}