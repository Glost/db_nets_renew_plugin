package de.renew.formalism.fs;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Path;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Tuple;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.Vector;


public class FSUnifyExpression implements Expression {
    FeatureStructure template;
    Vector<Path> paths;
    Vector<Object> exprs;

    //  private boolean debug=false;
    //  public FSUnifyExpression(FeatureStructure template,
    //			   Vector paths, Vector exprs, boolean debug) {
    //      this(template,paths,exprs);
    //      this.debug=debug;
    //  }
    public FSUnifyExpression(FeatureStructure template, Vector<Path> paths,
                             Vector<Object> exprs) {
        this.template = template;
        this.paths = paths;
        this.exprs = exprs;
    }

    public FeatureStructure getTemplate() {
        return template;
    }

    public Vector<Path> getPaths() {
        return paths;
    }

    public Vector<Object> getExprs() {
        return exprs;
    }

    public boolean isInvertible() {
        return true;
    }

    public Class<?> getType() {
        return FeatureStructure.class;
    }

    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        if (exprs.size() > 0) {
            FSUnifier unifier = new FSUnifier(template, paths, recorder); //,debug);

            for (int i = 0; i < exprs.size(); i++) {
                Expression expr = (Expression) exprs.elementAt(i);
                Unify.unify(unifier.getVariable(i),
                            expr.startEvaluation(mapper, recorder, checker),
                            recorder);
            }

            return unifier.result.getValue();
        } else {
            return template;
        }
    }

    // In former times, features structures were not allowed to be used
    // in actions. This was changed to become more flexible, but
    // the liability for exception handling is on the user now.
    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        Tuple tuple = new Tuple(exprs.size());
        for (int i = 0; i < exprs.size(); i++) {
            Expression expr = (Expression) exprs.elementAt(i);
            Unify.unify(tuple.getComponent(i),
                        expr.registerCalculation(mapper, recorder, checker),
                        recorder);
        }

        Variable source = new Variable(tuple, recorder);
        Variable target = new Variable();

        checker.addLateVariable(source, recorder);
        checker.addCalculated(FeatureStructure.class, target,
                              source.getValue(), recorder);

        return target.getValue();
    }

    public String toString() {
        final int sbSize = 1000;
        final String variableSeparator = ", ";
        final StringBuffer sb = new StringBuffer(sbSize);
        sb.append("FSUnifyExpr(");
        sb.append("tmpl: ").append(template);
        sb.append(variableSeparator);
        sb.append("paths: ").append(paths);
        sb.append(variableSeparator);
        sb.append("exprs: ").append(exprs);
        sb.append(")");
        return sb.toString();
    }
}