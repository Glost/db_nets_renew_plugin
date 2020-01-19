package de.renew.formalism.fs;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.Function;

import de.renew.formalism.function.BasicFunction;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.Value;


public abstract class FSFunction implements Function {
    public final static FSFunction LESS = new FSFunction() {
        public Object doFSFunction(FeatureStructure left, FeatureStructure right) {
            return new Value(new Boolean(!left.equals(right)
                                         && left.subsumes(right)));
        }

        public Function getOrdFunction() {
            return BasicFunction.LESS;
        }
    };
    public final static FSFunction LESSEQUAL = new FSFunction() {
        public Object doFSFunction(FeatureStructure left, FeatureStructure right) {
            return new Value(new Boolean(left.subsumes(right)));
        }

        public Function getOrdFunction() {
            return BasicFunction.LESSEQUAL;
        }
    };
    public final static FSFunction GREATER = new FSFunction() {
        public Object doFSFunction(FeatureStructure left, FeatureStructure right) {
            return new Value(new Boolean(!left.equals(right)
                                         && right.subsumes(left)));
        }

        public Function getOrdFunction() {
            return BasicFunction.GREATER;
        }
    };
    public final static FSFunction GREATEREQUAL = new FSFunction() {
        public Object doFSFunction(FeatureStructure left, FeatureStructure right) {
            return new Value(new Boolean(right.subsumes(left)));
        }

        public Function getOrdFunction() {
            return BasicFunction.GREATEREQUAL;
        }
    };
    public final static FSFunction PLUS = new FSFunction() {
        public Object doFSFunction(FeatureStructure left, FeatureStructure right)
                throws Impossible {
            try {
                return left.unify(right);
            } catch (UnificationFailure uff) {
                throw new Impossible();
            }
        }

        public Function getOrdFunction() {
            return BasicFunction.PLUS;
        }
    };

    public abstract Object doFSFunction(FeatureStructure left,
                                        FeatureStructure right)
            throws Impossible;

    public abstract Function getOrdFunction();

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 2) {
            throw new Impossible();
        }
        Object obj1 = tuple.getComponent(0);
        Object obj2 = tuple.getComponent(1);

        if (obj1 instanceof FeatureStructure
                    && obj2 instanceof FeatureStructure) {
            FeatureStructure fs1 = (FeatureStructure) obj1;
            FeatureStructure fs2 = (FeatureStructure) obj2;
            return doFSFunction(fs1, fs2);
        } else {
            return getOrdFunction().function(param);
        }
    }
    //  private FSFunction() {  }
}