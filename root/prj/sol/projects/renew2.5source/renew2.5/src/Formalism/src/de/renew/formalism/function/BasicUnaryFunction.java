package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;

import de.renew.util.Value;


public class BasicUnaryFunction implements Function {
    private final static int iLNOT = 1;
    private final static int iNOT = 2;
    private final static int iNEG = 3;
    private final static int iPOS = 4;
    public final static BasicUnaryFunction LNOT = new BasicUnaryFunction(iLNOT);
    public final static BasicUnaryFunction NOT = new BasicUnaryFunction(iNOT);
    public final static BasicUnaryFunction NEG = new BasicUnaryFunction(iNEG);
    public final static BasicUnaryFunction POS = new BasicUnaryFunction(iPOS);
    private int funcNum;

    private BasicUnaryFunction(int funcNum) {
        this.funcNum = funcNum;
    }

    public Object function(Object param) throws Impossible {
        if (!(param instanceof Value)) {
            throw new Impossible();
        }

        Object obj = ((Value) param).value;

        if (obj instanceof Integer || obj instanceof Character
                    || obj instanceof Short || obj instanceof Byte) {
            int arg;
            if (obj instanceof Character) {
                arg = ((Character) obj).charValue();
            } else {
                arg = ((Number) obj).intValue();
            }

            int result;
            switch (funcNum) {
            case iNOT:
                result = ~arg;
                break;
            case iNEG:
                result = -arg;
                break;
            case iPOS:
                result = +arg;
                break;
            default:
                throw new Impossible();
            }

            return new Value(new Integer(result));
        } else if (obj instanceof Long) {
            long arg = ((Long) obj).longValue();

            long result;
            switch (funcNum) {
            case iNOT:
                result = ~arg;
                break;
            case iNEG:
                result = -arg;
                break;
            case iPOS:
                result = +arg;
                break;
            default:
                throw new Impossible();
            }

            return new Value(new Long(result));
        } else if (obj instanceof Float) {
            float arg = ((Float) obj).floatValue();

            float result;
            switch (funcNum) {
            case iNEG:
                result = -arg;
                break;
            case iPOS:
                result = +arg;
                break;
            default:
                throw new Impossible();
            }

            return new Value(new Float(result));
        } else if (obj instanceof Double) {
            double arg = ((Double) obj).doubleValue();

            double result;
            switch (funcNum) {
            case iNEG:
                result = -arg;
                break;
            case iPOS:
                result = +arg;
                break;
            default:
                throw new Impossible();
            }

            return new Value(new Double(result));
        } else if (obj instanceof Boolean) {
            boolean arg = ((Boolean) obj).booleanValue();

            switch (funcNum) {
            case iLNOT:
                return new Value(new Boolean(!arg));
            default:
                throw new Impossible();
            }
        }

        throw new Impossible();
    }
}