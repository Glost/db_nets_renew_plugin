package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.Value;

import java.lang.reflect.InvocationTargetException;


public class BasicFunction implements Function {
    private final static int iLOR = 1;
    private final static int iLAND = 2;
    private final static int iOR = 3;
    private final static int iAND = 4;
    private final static int iXOR = 5;
    private final static int iEQUAL = 6;
    private final static int iNEQUAL = 7;
    private final static int iLESS = 8;
    private final static int iGREATER = 9;
    private final static int iLESSEQUAL = 10;
    private final static int iGREATEREQUAL = 11;
    private final static int iSHL = 12;
    private final static int iSHR = 13;
    private final static int iSSHR = 14;
    private final static int iPLUS = 15;
    private final static int iMINUS = 16;
    private final static int iTIMES = 17;
    private final static int iDIVIDE = 18;
    private final static int iMOD = 19;
    public final static BasicFunction LOR = new BasicFunction(iLOR);
    public final static BasicFunction LAND = new BasicFunction(iLAND);
    public final static BasicFunction OR = new BasicFunction(iOR);
    public final static BasicFunction AND = new BasicFunction(iAND);
    public final static BasicFunction XOR = new BasicFunction(iXOR);
    public final static BasicFunction EQUAL = new BasicFunction(iEQUAL);
    public final static BasicFunction NEQUAL = new BasicFunction(iNEQUAL);
    public final static BasicFunction LESS = new BasicFunction(iLESS);
    public final static BasicFunction GREATER = new BasicFunction(iGREATER);
    public final static BasicFunction LESSEQUAL = new BasicFunction(iLESSEQUAL);
    public final static BasicFunction GREATEREQUAL = new BasicFunction(iGREATEREQUAL);
    public final static BasicFunction SHL = new BasicFunction(iSHL);
    public final static BasicFunction SHR = new BasicFunction(iSHR);
    public final static BasicFunction SSHR = new BasicFunction(iSSHR);
    public final static BasicFunction PLUS = new BasicFunction(iPLUS);
    public final static BasicFunction MINUS = new BasicFunction(iMINUS);
    public final static BasicFunction TIMES = new BasicFunction(iTIMES);
    public final static BasicFunction DIVIDE = new BasicFunction(iDIVIDE);
    public final static BasicFunction MOD = new BasicFunction(iMOD);
    private final static String[] funcNames;
    private int funcNum;

    static {
        funcNames = new String[20];
        funcNames[iLOR] = "BasicFunc(LOR)";
        funcNames[iLAND] = "BasicFunc(LAND)";
        funcNames[iOR] = "BasicFunc(OR)";
        funcNames[iAND] = "BasicFunc(AND)";
        funcNames[iXOR] = "BasicFunc(XOR)";
        funcNames[iEQUAL] = "BasicFunc(EQUAL)";
        funcNames[iNEQUAL] = "BasicFunc(NEQUAL)";
        funcNames[iLESS] = "BasicFunc(LESS)";
        funcNames[iGREATER] = "BasicFunc(GREATER)";
        funcNames[iLESSEQUAL] = "BasicFunc(LESSEQUAL)";
        funcNames[iGREATEREQUAL] = "BasicFunc(GREATEREQUAL)";
        funcNames[iSHL] = "BasicFunc(SHL)";
        funcNames[iSHR] = "BasicFunc(SHR)";
        funcNames[iSSHR] = "BasicFunc(SSHR)";
        funcNames[iPLUS] = "BasicFunc(PLUS)";
        funcNames[iMINUS] = "BasicFunc(MINUS)";
        funcNames[iTIMES] = "BasicFunc(TIMES)";
        funcNames[iDIVIDE] = "BasicFunc(DIVIDE)";
        funcNames[iMOD] = "BasicFunc(MOD)";
    }

    private BasicFunction(int funcNum) {
        this.funcNum = funcNum;
    }

    private String generalToString(Object param) {
        if (param == null) {
            return "null";
        } else if (param instanceof Value) {
            return ((Value) param).value.toString();
        } else {
            return param.toString();
        }
    }

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 2) {
            throw new Impossible();
        }
        Object obj1 = tuple.getComponent(0);
        Object obj2 = tuple.getComponent(1);


        // If one of the two objects is a string, I don't even need
        // to unwrap values.
        if (obj1 instanceof String || obj2 instanceof String) {
            if (funcNum == iPLUS) {
                return generalToString(obj1) + generalToString(obj2);
            }
        }


        // Ok, there are no Strings involved.
        // Make sure both operands are values or none at all.
        if (obj1 instanceof Value ^ obj2 instanceof Value) {
            throw new Impossible();
        }

        // Let's consider equality operations on objects.
        if (!(obj1 instanceof Value)) {
            Boolean result;
            switch (funcNum) {
            case iEQUAL:
                result = new Boolean(obj1 == obj2);
                break;
            case iNEQUAL:
                result = new Boolean(obj1 != obj2);
                break;
            default:
                throw new Impossible();
            }
            return new Value(result);
        }


        // Both arguments are values.
        obj1 = ((Value) obj1).value;
        obj2 = ((Value) obj2).value;

        // Booleans are next.
        if (obj1 instanceof Boolean) {
            if (obj2 instanceof Boolean) {
                boolean left = ((Boolean) obj1).booleanValue();
                boolean right = ((Boolean) obj2).booleanValue();

                Boolean result;
                switch (funcNum) {
                case iLOR:
                    result = new Boolean(left || right);
                    break;
                case iLAND:
                    result = new Boolean(left && right);
                    break;
                case iOR:
                    result = new Boolean(left | right);
                    break;
                case iAND:
                    result = new Boolean(left & right);
                    break;
                case iXOR:
                    result = new Boolean(left ^ right);
                    break;
                case iEQUAL:
                    result = new Boolean(left == right);
                    break;
                case iNEQUAL:
                    result = new Boolean(left != right);
                    break;
                default:
                    throw new Impossible();
                }
                return new Value(result);
            }
            throw new Impossible();
        }


        // I will replace characters by integers, so that
        // there are only numbers left: byte, short, integer,
        // long, float, double.
        if (obj1 instanceof Character) {
            obj1 = new Integer(((Character) obj1).charValue());
        }
        if (obj2 instanceof Character) {
            obj2 = new Integer(((Character) obj2).charValue());
        }

        // Now we proceed with the unwrapped numbers.
        if (obj1 instanceof Double || obj2 instanceof Double) {
            double left = ((Number) obj1).doubleValue();
            double right = ((Number) obj2).doubleValue();

            Object result;
            try {
                switch (funcNum) {
                case iEQUAL:
                    result = new Boolean(left == right);
                    break;
                case iNEQUAL:
                    result = new Boolean(left != right);
                    break;
                case iLESS:
                    result = new Boolean(left < right);
                    break;
                case iGREATER:
                    result = new Boolean(left > right);
                    break;
                case iLESSEQUAL:
                    result = new Boolean(left <= right);
                    break;
                case iGREATEREQUAL:
                    result = new Boolean(left >= right);
                    break;
                case iPLUS:
                    result = new Double(left + right);
                    break;
                case iMINUS:
                    result = new Double(left - right);
                    break;
                case iTIMES:
                    result = new Double(left * right);
                    break;
                case iDIVIDE:
                    result = new Double(left / right);
                    break;
                case iMOD:
                    result = new Double(left % right);
                    break;
                default:
                    throw new Impossible();
                }
            } catch (Exception e) {
                // Either an impossible or a runtime exception happened.
                if (e instanceof InvocationTargetException) {
                    throw new Impossible("InvocationTargetException occured during method call: "
                                         + ((InvocationTargetException) e)
                                             .getTargetException());
                } else {
                    throw new Impossible("Exception occured during method call: "
                                         + e);
                }
            }
            return new Value(result);
        }

        if (obj1 instanceof Float || obj2 instanceof Float) {
            float left = ((Number) obj1).floatValue();
            float right = ((Number) obj2).floatValue();

            Object result;
            try {
                switch (funcNum) {
                case iEQUAL:
                    result = new Boolean(left == right);
                    break;
                case iNEQUAL:
                    result = new Boolean(left != right);
                    break;
                case iLESS:
                    result = new Boolean(left < right);
                    break;
                case iGREATER:
                    result = new Boolean(left > right);
                    break;
                case iLESSEQUAL:
                    result = new Boolean(left <= right);
                    break;
                case iGREATEREQUAL:
                    result = new Boolean(left >= right);
                    break;
                case iPLUS:
                    result = new Float(left + right);
                    break;
                case iMINUS:
                    result = new Float(left - right);
                    break;
                case iTIMES:
                    result = new Float(left * right);
                    break;
                case iDIVIDE:
                    result = new Float(left / right);
                    break;
                case iMOD:
                    result = new Float(left % right);
                    break;
                default:
                    throw new Impossible();
                }
            } catch (Exception e) {
                // Either an impossible or a runtime exception happened.
                throw new Impossible();
            }
            return new Value(result);
        }


        // We must redirect calls to the shift operators, because the
        // result type of these operators depends only on the type
        // of the left argument.
        if (funcNum == iSHL || funcNum == iSHR || funcNum == iSSHR) {
            if (obj1 instanceof Long) {
                long left = ((Number) obj1).longValue();
                long right = ((Number) obj2).longValue();
                Object result;
                switch (funcNum) {
                case iSHL:
                    result = new Long(left << right);
                    break;
                case iSHR:
                    result = new Long(left >> right);
                    break;
                case iSSHR:
                    result = new Long(left >>> right);
                    break;
                default:
                    throw new Impossible();
                }
                return new Value(result);
            }

            int left = ((Number) obj1).intValue();
            long right = ((Number) obj2).longValue();
            Object result;
            switch (funcNum) {
            case iSHL:
                result = new Integer(left << right);
                break;
            case iSHR:
                result = new Integer(left >> right);
                break;
            case iSSHR:
                result = new Integer(left >>> right);
                break;
            default:
                throw new Impossible();
            }
            return new Value(result);
        }

        if (obj1 instanceof Long || obj2 instanceof Long) {
            long left = ((Number) obj1).longValue();
            long right = ((Number) obj2).longValue();

            Object result;
            try {
                switch (funcNum) {
                case iOR:
                    result = new Long(left | right);
                    break;
                case iAND:
                    result = new Long(left & right);
                    break;
                case iXOR:
                    result = new Long(left ^ right);
                    break;
                case iEQUAL:
                    result = new Boolean(left == right);
                    break;
                case iNEQUAL:
                    result = new Boolean(left != right);
                    break;
                case iLESS:
                    result = new Boolean(left < right);
                    break;
                case iGREATER:
                    result = new Boolean(left > right);
                    break;
                case iLESSEQUAL:
                    result = new Boolean(left <= right);
                    break;
                case iGREATEREQUAL:
                    result = new Boolean(left >= right);
                    break;
                case iPLUS:
                    result = new Long(left + right);
                    break;
                case iMINUS:
                    result = new Long(left - right);
                    break;
                case iTIMES:
                    result = new Long(left * right);
                    break;
                case iDIVIDE:
                    result = new Long(left / right);
                    break;
                case iMOD:
                    result = new Long(left % right);
                    break;
                default:
                    throw new Impossible();
                }
            } catch (Exception e) {
                // Either an impossible or a runtime exception happened.
                throw new Impossible();
            }
            return new Value(result);
        }

        int left = ((Number) obj1).intValue();
        int right = ((Number) obj2).intValue();

        Object result;
        try {
            switch (funcNum) {
            case iOR:
                result = new Integer(left | right);
                break;
            case iAND:
                result = new Integer(left & right);
                break;
            case iXOR:
                result = new Integer(left ^ right);
                break;
            case iEQUAL:
                result = new Boolean(left == right);
                break;
            case iNEQUAL:
                result = new Boolean(left != right);
                break;
            case iLESS:
                result = new Boolean(left < right);
                break;
            case iGREATER:
                result = new Boolean(left > right);
                break;
            case iLESSEQUAL:
                result = new Boolean(left <= right);
                break;
            case iGREATEREQUAL:
                result = new Boolean(left >= right);
                break;
            case iPLUS:
                result = new Integer(left + right);
                break;
            case iMINUS:
                result = new Integer(left - right);
                break;
            case iTIMES:
                result = new Integer(left * right);
                break;
            case iDIVIDE:
                result = new Integer(left / right);
                break;
            case iMOD:
                result = new Integer(left % right);
                break;
            default:
                throw new Impossible();
            }
        } catch (Exception e) {
            // Either an impossible or a runtime exception happened.
            if (e instanceof InvocationTargetException) {
                throw new Impossible("InvocationTargetException occured during method call: "
                                     + ((InvocationTargetException) e)
                                         .getTargetException());
            } else {
                throw new Impossible("Exception occured during method call: "
                                     + e);
            }
        }
        return new Value(result);
    }

    public final String toString() {
        String result = null;
        try {
            result = funcNames[funcNum];
        } catch (ArrayIndexOutOfBoundsException e) {
            // result = null;
        }
        if (result == null) {
            result = "BasicFunc(<<<INVALID!>>>)";
        }
        return result;
    }
}