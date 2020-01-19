package de.renew.formalism.java;

import de.renew.expression.CallExpression;
import de.renew.expression.EqualsExpression;
import de.renew.expression.Expression;
import de.renew.expression.Function;
import de.renew.expression.InvertibleExpression;
import de.renew.expression.ListExpression;
import de.renew.expression.LocalVariable;
import de.renew.expression.NoArgExpression;
import de.renew.expression.TupleExpression;
import de.renew.expression.TypeCheckingExpression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.function.ArrayFunction;
import de.renew.formalism.function.ArrayWriteFunction;
import de.renew.formalism.function.CastFunction;
import de.renew.formalism.function.ConstructorFunction;
import de.renew.formalism.function.DynamicConstructorFunction;
import de.renew.formalism.function.DynamicFieldFunction;
import de.renew.formalism.function.DynamicFieldWriteFunction;
import de.renew.formalism.function.DynamicMethodFunction;
import de.renew.formalism.function.DynamicStaticMethodFunction;
import de.renew.formalism.function.Executor;
import de.renew.formalism.function.FieldFunction;
import de.renew.formalism.function.FieldWriteFunction;
import de.renew.formalism.function.Identity;
import de.renew.formalism.function.MethodFunction;
import de.renew.formalism.function.StaticFieldFunction;
import de.renew.formalism.function.StaticFieldWriteFunction;
import de.renew.formalism.function.StaticMethodFunction;

import de.renew.util.Types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Collection;
import java.util.List;
import java.util.Vector;


public class JavaHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(JavaHelper.class);
    public static final String KNOWN_METHODS = "\nKnown methods:";
    public static final String KNOWN_FIELDS = "\nKnown fields:";

    public static ParseException makeParseException(String msg, Token token) {
        if (token != null) {
            Token posToken = token.next == null ? token : token.next;
            ParseException ex = new ParseException("Error in line "
                                                   + posToken.beginLine
                                                   + ", column "
                                                   + posToken.beginColumn
                                                   + ":\n" + msg);
            ex.currentToken = token;
            return ex;
        } else {
            return new ParseException(msg);
        }
    }

    public static ExtendedParseException makeExtendedParseException(String msg,
                                                                    Object o,
                                                                    Token token) {
        if (token != null) {
            Token posToken = token.next == null ? token : token.next;
            ExtendedParseException ex = new ExtendedParseException("Error in line "
                                                                   + posToken.beginLine
                                                                   + ", column "
                                                                   + posToken.beginColumn
                                                                   + ":\n"
                                                                   + msg, o);
            ex.currentToken = token;
            return ex;
        } else {
            return new ExtendedParseException(msg, o);
        }
    }

    public static TypedExpression makeTypedBinary(TypedExpression left,
                                                  TypedExpression right,
                                                  Function fun, Class<?> type) {
        return new TypedExpression(type,
                                   new CallExpression(type,
                                                      new TupleExpression(left
                   .getExpression(), right.getExpression()), fun));
    }

    public static TypedExpression makeBooleanBinary(TypedExpression left,
                                                    TypedExpression right,
                                                    Function fun) {
        return makeTypedBinary(left, right, fun, Boolean.TYPE);
    }

    public static TypedExpression makeBinary(TypedExpression left,
                                             TypedExpression right,
                                             Function fun, Token errToken)
            throws ParseException {
        Class<?> type;
        if (!left.isTyped() || !right.isTyped()) {
            type = Types.UNTYPED;
        } else if (left.getType() == Boolean.TYPE
                           || right.getType() == Boolean.TYPE) {
            if (left.getType() != Boolean.TYPE
                        || right.getType() != Boolean.TYPE) {
                throw makeParseException("Operator types do not match.",
                                         errToken);
            }
            type = Boolean.TYPE;
        } else if (left.getType() == Double.TYPE
                           || right.getType() == Double.TYPE) {
            type = Double.TYPE;
        } else if (left.getType() == Float.TYPE
                           || right.getType() == Float.TYPE) {
            type = Float.TYPE;
        } else if (left.getType() == Long.TYPE || right.getType() == Long.TYPE) {
            type = Long.TYPE;
        } else {
            type = Integer.TYPE;
        }

        return makeTypedBinary(left, right, fun, type);
    }

    public static Class<?> unaryNumericPromotion(Class<?> old, Token errToken)
            throws ParseException {
        // Accept untyped expressions.
        if (old == Types.UNTYPED) {
            return old;
        }

        // Exclude reference types and boolean.
        if (old == null || !old.isPrimitive()) {
            throw makeParseException("Primitive type expected.", errToken);
        }
        if (old == Boolean.TYPE) {
            throw makeParseException("Numeric type expected.", errToken);
        }

        // Convert small types to int.
        if (old == Byte.TYPE || old == Short.TYPE || old == Character.TYPE) {
            return Integer.TYPE;
        } else {
            return old;
        }
    }

    public static Class<?> unaryIntegralPromotion(Class<?> old, Token errToken)
            throws ParseException {
        if (old == Float.TYPE || old == Double.TYPE) {
            throw makeParseException("Integral type expected.", errToken);
        }
        return unaryNumericPromotion(old, errToken);
    }

    // ensureNonVoid ensures that the given expression is not
    // of the type void.
    public static void ensureNonVoid(TypedExpression expr, Token errToken)
            throws ParseException {
        if (expr.getType() == Void.TYPE) {
            throw makeParseException("Expression of type void not allowed here.",
                                     errToken);
        }
    }

    // ensureConvertability ensures that the given typed expression
    // provides a result the can be converted to the given type.
    //
    // If the expression is untyped, the method always succeeds.
    public static void ensureConvertability(Class<?> clazz,
                                            TypedExpression expr, Token errToken)
            throws ParseException {
        if (expr.isTyped()
                    && !Types.allowsWideningConversion(expr.getType(), clazz)) {
            throw makeParseException("Cannot convert "
                                     + makeTypeErrorString(expr.getType())
                                     + " to " + makeTypeErrorString(clazz)
                                     + ".", errToken);
        }
    }

    public static void ensureEnumerateability(TypedExpression expr,
                                              Token errToken)
            throws ParseException {
        if (expr.isTyped()) {
            if (expr.getType() == null || !expr.getType().isPrimitive()
                        || expr.getType() == Double.TYPE
                        || expr.getType() == Float.TYPE) {
                throw makeParseException("Enumerable type expected.", errToken);
            }
        }
    }

    public static void ensureBinaryMatch(TypedExpression left,
                                         TypedExpression right, Token errToken)
            throws ParseException {
        if (left.isTyped() && right.isTyped()) {
            Class<?> leftType = left.getType();
            Class<?> rightType = right.getType();
            boolean leftRef = (leftType == null || !leftType.isPrimitive());
            boolean rightRef = (rightType == null || !rightType.isPrimitive());
            if (leftRef ^ rightRef) {
                throw makeParseException("Operator types do not match.",
                                         errToken);
            }
            if (leftType == Boolean.TYPE ^ rightType == Boolean.TYPE) {
                throw makeParseException("Operator types do not match.",
                                         errToken);
            }
        }
    }

    public static TypedExpression makeExplicitCastExpression(Class<?> clazz,
                                                             TypedExpression expr,
                                                             Token errToken)
            throws ParseException {
        // We must distiguish the following cases:
        // - An identity cast. No action.
        // - The cast does not loose information.
        // (Typical: byte -> int, String -> Object, Object -> String)
        // - The cast might loose information.
        // (Typical: int -> byte, long -> double)
        // - The cast is impossible.
        // (Typical: int -> Object, String -> Class)
        if (expr.getType() == clazz) {
            return expr;
        } else if (!expr.isTyped()) {
            if (clazz.isPrimitive()) {
                // We do not know which class the untyped expression
                // will take at runtime, so we must restrict ourselves to
                // directed information transfer.
                return new TypedExpression(clazz,
                                           new CallExpression(clazz,
                                                              expr.getExpression(),
                                                              new CastFunction(clazz)));
            } else {
                // It is still save to allow bidirectional communication.
                // With references types, the value is only checked
                // and handed through.
                return new TypedExpression(clazz,
                                           new TypeCheckingExpression(clazz,
                                                                      expr
                           .getExpression()));
            }
        } else if (Types.allowsCast(expr.getType(), clazz)) {
            if (clazz.isPrimitive()) {
                if (expr.getType() == null) {
                    // Nothing to do. Casting a constant null is trivial.
                    return new TypedExpression(clazz, expr.getExpression());
                } else if (Types.allowsLosslessWidening(expr.getType(), clazz)) {
                    return new TypedExpression(clazz,
                                               new InvertibleExpression(clazz,
                                                                        expr
                               .getExpression(), new CastFunction(clazz),
                                                                        new CastFunction(expr
                                                                                         .getType())));
                } else {
                    return new TypedExpression(clazz,
                                               new CallExpression(clazz,
                                                                  expr
                               .getExpression(), new CastFunction(clazz)));
                }
            } else {
                // We must guard against two cases.
                // The cast may fail and a value might be illegally
                // fed backwards into the equation.
                return new TypedExpression(clazz,
                                           new TypeCheckingExpression(clazz,
                                                                      makeGuardedExpression(expr)));
            }
        } else {
            throw makeParseException("Cannot cast "
                                     + Types.typeToString(expr.getType())
                                     + " to " + Types.typeToString(clazz) + ".",
                                     errToken);
        }
    }

    public static Expression[] makeExpressionArray(Vector<TypedExpression> vector) {
        Expression[] result = new Expression[vector.size()];
        for (int i = 0; i < result.length; i++) {
            TypedExpression expr = vector.elementAt(i);
            result[i] = expr.getExpression();
        }
        return result;
    }

    public static TupleExpression makeTupleExpression(Vector<TypedExpression> vector) {
        return new TupleExpression(makeExpressionArray(vector));
    }

    public static ListExpression makeListExpression(Vector<TypedExpression> vector,
                                                    boolean tailed) {
        return new ListExpression(makeExpressionArray(vector), tailed);
    }

    public static Expression makeGuardedExpression(TypedExpression expr) {
        // The typed expression is wrapped in a type checking expression
        // to ensure that no illegally typed value is propagated
        // backwards into the expression.
        if (expr.isTyped() && expr.getType() != null) {
            return new TypeCheckingExpression(expr.getType(),
                                              expr.getExpression());
        } else {
            return expr.getExpression();
        }
    }

    public static TupleExpression makeGuardedTupleExpression(Vector<TypedExpression> vector) {
        // We receive a vector of typed expressions and convert it
        // into a tuple expression that is sufficiently guarded.
        Expression[] result = new Expression[vector.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = makeGuardedExpression(vector.elementAt(i));
        }
        return new TupleExpression(result);
    }

    public static Class<?>[] makeTypeArray(Vector<TypedExpression> vector) {
        Class<?>[] types = new Class<?>[vector.size()];
        for (int i = 0; i < types.length; i++) {
            TypedExpression expr = vector.elementAt(i);
            if (!expr.isTyped()) {
                // If one component is untyped, then the whole vector is
                // untyped.
                return null;
            }
            types[i] = expr.getType();
        }
        return types;
    }

    public static Class<?> increaseArrayLevel(Class<?> clazz, Token errToken)
            throws ParseException {
        try {
            return java.lang.reflect.Array.newInstance(clazz, 0).getClass();
        } catch (Exception e) {
            throw makeParseException("Could not create class.", errToken);
        }
    }

    public static String makeTypeErrorString(Class<?> type) {
        if (type == null) {
            return "a null expression";
        } else if (type == Types.UNTYPED) {
            return "an untyped expression";
        } else {
            return "an expression of type " + Types.typeToString(type);
        }
    }

    // NOTICEsignature
    // NOTICEthrows
    private static TypedExpression makeTypedEquality(TypedExpression left,
                                                     TypedExpression right,
                                                     Token errToken)
            throws ParseException {
        // It is assumed that this routine is called form
        // makeEqualityAssertion where the proper checks have been
        // undertaken. Both expressions are typed, different,
        // and the right expression allows a lossless widening conversion
        // to the type of the left expression.
        Class<?> leftType = left.getType();
        Class<?> rightType = right.getType();

        Expression castedRight;
        if (rightType == null) {
            // A constant null is assigned. No casts are required.
            castedRight = right.getExpression();
        } else if (rightType.isPrimitive()) {
            // The equality will only hold if the two sides result in the
            // same value. The conversion that is required is done in such
            // a way that it is invertible. The two casts will automatically
            // detect cases where one side is out of range for the other side.
            castedRight = new InvertibleExpression(leftType,
                                                   right.getExpression(),
                                                   new CastFunction(leftType),
                                                   new CastFunction(rightType));
        } else {
            // There is no conversion involved, but we must make sure
            // that no illegal value is fed back into the right expression
            // that has a narrower type.
            castedRight = makeGuardedExpression(right);
        }

        return new TypedExpression(left.getType(),
                                   new EqualsExpression(left.getType(),
                                                        left.getExpression(),
                                                        castedRight));
    }

    public static TypedExpression makeEqualityAssertion(TypedExpression left,
                                                        TypedExpression right,
                                                        Token errToken)
            throws ParseException {
        Class<?> leftType = left.getType();
        Class<?> rightType = right.getType();
        if (leftType == Void.TYPE || rightType == Void.TYPE) {
            throw makeParseException("Expression of type void not allowed here.",
                                     errToken);
        }
        if (!left.isTyped() || !right.isTyped()
                    || Types.allowsIdentityConversion(leftType, rightType)) {
            return new TypedExpression(leftType,
                                       new EqualsExpression(leftType,
                                                            makeGuardedExpression(left),
                                                            makeGuardedExpression(right)));
        }

        // Both expressions are typed.
        if (Types.allowsLosslessWidening(rightType, leftType)) {
            // NOTICEsignature
            return makeTypedEquality(left, right, errToken);
        }
        if (Types.allowsLosslessWidening(leftType, rightType)) {
            // NOTICEsignature
            return makeTypedEquality(right, left, errToken);
        }
        throw makeParseException("Type mismatch in assignment.", errToken);
    }

    public static TypedExpression makeSinglePartExpression(PrimaryPart firstPart,
                                                           TypedExpression right,
                                                           ParsedDeclarationNode declNode)
            throws ParseException {
        return makeSinglePartExpression(firstPart, right, declNode, false);
    }

    public static TypedExpression makeSinglePartExpression(PrimaryPart firstPart,
                                                           TypedExpression right,
                                                           ParsedDeclarationNode declNode,
                                                           boolean refactoring)
            throws ParseException {
        switch (firstPart.type) {
        case PrimaryPart.EXPR:
            if (right == null) {
                return (TypedExpression) firstPart.obj;
            } else {
                throw makeParseException("Invalid left hand side of assignment.",
                                         firstPart.token);
            }
        case PrimaryPart.NAME:
            Object meaning;
            try {
                meaning = declNode.interpreteName((String) firstPart.obj);
            } catch (LinkageError e) {
                String errorMessage = "No such variable: " + firstPart.obj
                                      + "\n(denotes class with linkage problem:\n"
                                      + e.toString() + ").";
                logger.warn(errorMessage);
                logger.debug("Encountered " + e, e);
                throw makeParseException(errorMessage, firstPart.token);
            }
            if (meaning instanceof LocalVariable) {
                Class<?> type = declNode.findType((LocalVariable) meaning);

                LocalVariable variable = (LocalVariable) meaning;
                if (refactoring) {
                    addTokenInformation(variable, firstPart.token.next);
                }

                if (right == null) {
                    return new TypedExpression(type,
                                               new VariableExpression(type,
                                                                      variable));
                } else {
                    // This is called in an action inscription
                    // where we do not want to propagate information
                    // backwards due to consistency reasons.
                    //
                    // Furthermore, an inscription like "action x=y"
                    // is accepted as compile time type safe, if
                    // x is assignable from y. But backward information
                    // transfer would ruin this condition.
                    //
                    // Note that the inscription "x=y" is interpreted
                    // and implemented differently.
                    if (type == Types.UNTYPED) {
                        return new TypedExpression(type,
                                                   new EqualsExpression(type,
                                                                        new VariableExpression(type,
                                                                                               variable),
                                                                        new CallExpression(right
                                                                                           .getType(),
                                                                                           right
                                                                                           .getExpression(),
                                                                                           Identity.FUN)));
                    } else if (right.isTyped()
                                       && Types.allowsWideningConversion(right
                                           .getType(), type)) {
                        return new TypedExpression(type,
                                                   new EqualsExpression(type,
                                                                        new VariableExpression(type,
                                                                                               variable),
                                                                        new CallExpression(right
                                                                                           .getType(),
                                                                                           right
                                                                                           .getExpression(),
                                                                                           new CastFunction(type))));
                    } else {
                        throw makeParseException("Cannot assign "
                                                 + makeTypeErrorString(right
                                  .getType()) + " to "
                                                 + makeTypeErrorString(type)
                                                 + ".", firstPart.token);
                    }
                }
            } else if (meaning instanceof Class<?>) {
                throw makeParseException("No such variable: " + firstPart.obj
                                         + " (denotes a class).",
                                         firstPart.token);
            } else {
                logger.debug("No variable " + firstPart.obj + " declared in "
                             + declNode);
                logger.debug("Meaning of " + firstPart.obj + " is " + meaning);


                Collection<VariableSuggestion> variableSuggestions = VariableSuggestion
                                                                     .suggest(firstPart,
                                                                              right,
                                                                              declNode);
                throw makeExtendedParseException("No such variable: "
                                                 + firstPart.obj,
                                                 variableSuggestions,
                                                 firstPart.token);
            }
        default:
            throw new RuntimeException("Malformed primary vector.");
        }
    }

    public static TypedExpression makeConstructorExpression(Class<?> clazz,
                                                            Vector<TypedExpression> vector,
                                                            Token errToken)
            throws ParseException {
        Class<?>[] types = makeTypeArray(vector);
        if (types != null) {
            // All arguments were typed.
            // Determine the constructor now.
            Constructor<?> constructor = null;
            try {
                constructor = Executor.findBestConstructor(clazz, types, true);
                if (constructor == null) {
                    throw makeParseException("Multiple constructors match: "
                                             + Executor.renderMethodSignature(clazz,
                                                                              "<init>",
                                                                              types),
                                             errToken);
                }
            } catch (NoSuchMethodException e) {
                Collection<ConstructorSuggestion> suggestions = ConstructorSuggestion
                                                                .suggest(clazz);
                throw makeExtendedParseException("No such constructor: "
                                                 + Executor
                          .renderMethodSignature(clazz, "<init>", types),
                                                 suggestions, errToken);
            } catch (LinkageError e) {
                logger.warn("Encountered " + e, e);
                throw makeParseException("Cannot not bind constructor "
                                         + Executor.renderMethodSignature(clazz,
                                                                          "<init>",
                                                                          types)
                                         + " due to " + e + ": ", errToken);
            }
            return new TypedExpression(clazz,
                                       new CallExpression(clazz,
                                                          makeTupleExpression(vector),
                                                          new ConstructorFunction(constructor)));
        } else {
            // At least one argument was untyped.
            // Determine the constructor at runtime.
            return new TypedExpression(clazz,
                                       new CallExpression(clazz,
                                                          makeTupleExpression(vector),
                                                          new DynamicConstructorFunction(clazz)));
        }
    }

    // superClass: If non-null, designates the class for which
    // a method should be sought. Typically used for
    // super.method(...) handling.
    // name: method name
    // types: must match the types of the argument vector
    // args: argument vector of typed expressions
    public static TypedExpression makeMethodCall(TypedExpression expr,
                                                 Class<?> superClass,
                                                 String name,
                                                 Vector<TypedExpression> args,
                                                 Token errorToken)
            throws ParseException {
        if (superClass != null) {
            // Unfortunately, there seems to be no way to invoke an
            // overridden method by means of the reflection API.
            // Therefore it is impossible to specify the invoked
            // base class explicitly.
            throw JavaHelper.makeParseException("Calls to super object are not supported.",
                                                errorToken);
        }

        Class<?> targetType = expr.getType();
        if (targetType == null) {
            throw makeParseException("Cannot invoke method on null object.",
                                     errorToken);
        }

        Class<?>[] types = makeTypeArray(args);
        if (types == null || !expr.isTyped()) {
            // This case does not need to be caught because it gets already
            // caught above
            /*
             * if (superClass != null) { throw makeParseException(
             * "Cannot invoke super method with untyped arguments.",
             * errorToken); }
             */
            return new TypedExpression(Types.UNTYPED,
                                       new CallExpression(Types.UNTYPED,
                                                          new TupleExpression(expr
                                                                              .getExpression(),
                                                                              makeTupleExpression(args)),
                                                          new DynamicMethodFunction(name)));
        } else {
            // This does not work. See explanation above.
            /*
             * if (superClass != null) { targetType = superClass; }
             */
            Method method;
            try {
                method = Executor.findBestMethod(targetType, name, types, true);
                if (method == null) {
                    throw makeParseException("Multiple methods match: "
                                             + Executor.renderMethodSignature(targetType,
                                                                              name,
                                                                              types),
                                             errorToken);
                }
            } catch (NoSuchMethodException e) {
                List<MethodSuggestion> methodSuggestions = MethodSuggestion
                                                               .suggest(targetType,
                                                                        name,
                                                                        types,
                                                                        Integer.MAX_VALUE);

                throw makeExtendedParseException("No such method: "
                                                 + Executor
                          .renderMethodSignature(targetType, name, types)
                                                 + checkForGivenPrefix(name)
                                                 + JavaHelper.KNOWN_METHODS,
                                                 methodSuggestions, errorToken);
            } catch (LinkageError e) {
                logger.warn("Encountered " + e, e);
                throw makeParseException("Cannot not bind method "
                                         + Executor.renderMethodSignature(targetType,
                                                                          name,
                                                                          types)
                                         + " due to " + e + ": ", errorToken);
            }

            // Even if the method is a static method,
            // it is invoked like an ordinary instance method.
            // This ensures the greatest compatibility to Java.
            return new TypedExpression(method.getReturnType(),
                                       new CallExpression(method.getReturnType(),
                                                          new TupleExpression(expr
                                                                              .getExpression(),
                                                                              makeTupleExpression(args)),
                                                          new MethodFunction(method)));
        }
    }

    public static TypedExpression makeExpression(Vector<PrimaryPart> vector,
                                                 TypedExpression right,
                                                 ParsedDeclarationNode declNode,
                                                 Token rightErrToken)
            throws ParseException {
        return makeExpression(vector, right, declNode, rightErrToken, false);
    }

    public static TypedExpression makeExpression(Vector<PrimaryPart> vector,
                                                 TypedExpression right,
                                                 ParsedDeclarationNode declNode,
                                                 Token rightErrToken,
                                                 boolean refactoring)
            throws ParseException {
        if (right != null) {
            ensureNonVoid(right, rightErrToken);
        }

        PrimaryPart firstPart = vector.elementAt(0);

        if (vector.size() == 1) {
            return makeSinglePartExpression(firstPart, right, declNode,
                                            refactoring);
        }

        // Ok, so it is a long vector, we must analyse it part by part.
        int m = vector.size();
        if (right != null) {
            // We must create an assignment, so we leave a single
            // item in the queue, if we haven't read it so far.
            m--;
        }

        // How many parts did we analyse?
        int i = 1;

        // During the composition the following variable is either
        // null or a class or a local variable or a typed expression.
        // Ultimately, it will hold an expression.
        Object composed = null;

        // The longest name tried is stored in the case we need to report
        // an error.
        String name;

        switch (firstPart.type) {
        case PrimaryPart.EXPR:
            composed = firstPart.obj;
            name = firstPart.toString();
            break;
        case PrimaryPart.NAME:
            name = (String) firstPart.obj;
            if (declNode == null) {
                break;
            }
            try {
                composed = declNode.interpreteName(name);
            } catch (LinkageError e) {
                logger.warn("Encountered " + e, e);
                throw makeParseException("Could not load class " + name
                                         + " due to " + e.toString(),
                                         firstPart.token);
            }

            // Where did we find the longest match?
            int lasti = i;

            while (i < m) {
                PrimaryPart part = vector.elementAt(i);
                i++;
                if (part.type != PrimaryPart.NAME) {
                    // Leave the while loop.
                    // I hate to do this, but here it is really the
                    // best solution.
                    break;
                }
                name = name + "." + (String) part.obj;
                Object newComposed;
                try {
                    newComposed = declNode.interpreteName(name);
                } catch (LinkageError e) {
                    logger.warn("Encountered " + e, e);
                    throw makeParseException("Could not load class " + name
                                             + " due to linkage problem:\n"
                                             + e.toString(), firstPart.token);
                }
                if (newComposed != null) {
                    composed = newComposed;
                    lasti = i;
                }
            }

            // Reset the list pointer to the last value
            // where we parsed a name.
            i = lasti;

            // At this time composed might be null if no appropriate name
            // was found.
            break;
        default:
            throw new RuntimeException("Malformed primary vector.");
        }

        if (composed == null) {
            logger.debug("No class or variable " + name + " declared in "
                         + declNode);
            throw makeParseException("No such class or variable: " + name,
                                     firstPart.token);
        }

        // Let's get rid of local variables.
        if (composed instanceof LocalVariable) {
            if (refactoring) {
                addTokenInformation((LocalVariable) composed,
                                    firstPart.token.next);
            }

            Class<?> type = declNode.findType((LocalVariable) composed);
            composed = new TypedExpression(type,
                                           new VariableExpression(type,
                                                                  (LocalVariable) composed));
        }

        // From now on, the composed object can only be a class or
        // a typed expression.
        while (i < m) {
            PrimaryPart part = vector.elementAt(i);
            i++;
            switch (part.type) {
            case PrimaryPart.EXPR:
                throw new RuntimeException("Malformed primary vector.");
            case PrimaryPart.NAME:
                // Does an argument list follow?
                Vector<TypedExpression> args = null;
                if (i < vector.size()) {
                    PrimaryPart additionalPart = vector.elementAt(i);
                    if (additionalPart.type == PrimaryPart.CALL) {
                        i++;
                        // The warning regarding the next cast can be suppressed
                        // because the type has
                        // been checked by
                        // "additionalPart.type == PrimaryPart.CALL"
                        @SuppressWarnings("unchecked")
                        Vector<TypedExpression> vec = (Vector<TypedExpression>) additionalPart.obj;
                        args = vec;
                    }
                }

                if (args != null) {
                    // Method call.
                    if (composed instanceof Class<?>) {
                        Class<?>[] types = makeTypeArray(args);
                        if (types == null) {
                            composed = new TypedExpression(Types.UNTYPED,
                                                           new CallExpression(Types.UNTYPED,
                                                                              makeTupleExpression(args),
                                                                              new DynamicStaticMethodFunction((String) part.obj,
                                                                                                              (Class<?>) composed)));
                        } else {
                            Method method;
                            try {
                                method = Executor.findBestMethod((Class<?>) composed,
                                                                 (String) part.obj,
                                                                 types, true);
                                if (method == null) {
                                    throw makeParseException("Multiple methods match: "
                                                             + Executor
                                              .renderMethodSignature((Class<?>) composed,
                                                                     (String) part.obj,
                                                                     types),
                                                             part.token);
                                }
                            } catch (NoSuchMethodException e) {
                                List<MethodSuggestion> methodSuggestions = MethodSuggestion
                                                                           .suggest((Class<?>) composed,
                                                                                    (String) part.obj,
                                                                                    types,
                                                                                    Modifier.STATIC);

                                throw makeExtendedParseException("No such static method: "
                                                                 + Executor
                                          .renderMethodSignature((Class<?>) composed,
                                                                 (String) part.obj,
                                                                 types)
                                                                 + checkForGivenPrefix((String) part.obj)
                                                                 + JavaHelper.KNOWN_METHODS,
                                                                 methodSuggestions,
                                                                 part.token);
                            } catch (LinkageError e) {
                                logger.warn("Encountered " + e, e);
                                throw makeParseException("Cannot not bind method "
                                                         + Executor
                                          .renderMethodSignature((Class<?>) composed,
                                                                 (String) part.obj,
                                                                 types)
                                                         + " due to " + e
                                                         + ": ", part.token);
                            }
                            if ((method.getModifiers() & Modifier.STATIC) == 0) {
                                throw makeParseException("Cannot make static call to "
                                                         + "instance method.",
                                                         part.token);
                            }
                            composed = new TypedExpression(method.getReturnType(),
                                                           new CallExpression(method
                                                                              .getReturnType(),
                                                                              makeTupleExpression(args),
                                                                              new StaticMethodFunction(method)));
                        }
                    } else {
                        // composed instanceof TypedExpression
                        composed = makeMethodCall((TypedExpression) composed,
                                                  null, (String) part.obj,
                                                  args, part.token);
                    }
                } else {
                    // Field access.
                    if (composed instanceof Class<?>) {
                        Class<?> clazz = (Class<?>) composed;
                        Field field = null;
                        try {
                            field = clazz.getField((String) part.obj);
                        } catch (Exception e) {
                            List<FieldSuggestion> fieldSuggestions = FieldSuggestion
                                                                     .suggest((Class<?>) composed,
                                                                              (String) part.obj,
                                                                              Modifier.STATIC);
                            throw makeExtendedParseException("No such static field: "
                                                             + part.obj
                                                             + JavaHelper.KNOWN_FIELDS,
                                                             fieldSuggestions,
                                                             part.token);
                        }
                        composed = new TypedExpression(field.getType(),
                                                       new NoArgExpression(field
                                                                           .getType(),
                                                                           new StaticFieldFunction(field)));
                    } else {
                        // composed instanceof TypedExpression
                        if (((TypedExpression) composed).isTyped()) {
                            Class<?> type = ((TypedExpression) composed).getType();
                            if (type == null) {
                                throw makeParseException("Cannot access field of null object.",
                                                         part.token);
                            }
                            if ((type.isArray()) && ("length".equals(part.obj))) {
                                composed = new TypedExpression(Integer.TYPE,
                                                               new CallExpression(Integer.TYPE,
                                                                                  ((TypedExpression) composed)
                                                                                  .getExpression(),
                                                                                  new DynamicFieldFunction((String) part.obj)));
                            } else {
                                Field field = null;
                                try {
                                    field = type.getField((String) part.obj);
                                } catch (Exception e) {
                                    List<FieldSuggestion> fieldSuggestions = FieldSuggestion
                                                                             .suggest(type,
                                                                                      (String) part.obj,
                                                                                      Integer.MAX_VALUE);
                                    throw makeExtendedParseException("No such field: "
                                                                     + part.obj
                                                                     + JavaHelper.KNOWN_FIELDS,
                                                                     fieldSuggestions,
                                                                     part.token);
                                }
                                composed = new TypedExpression(field.getType(),
                                                               new CallExpression(field
                                                                                  .getType(),
                                                                                  ((TypedExpression) composed)
                                                                                  .getExpression(),
                                                                                  new FieldFunction(field)));
                            }
                        } else {
                            composed = new TypedExpression(Types.UNTYPED,
                                                           new CallExpression(Types.UNTYPED,
                                                                              ((TypedExpression) composed)
                                                                              .getExpression(),
                                                                              new DynamicFieldFunction((String) part.obj)));
                        }
                    }
                }
                break;
            case PrimaryPart.ARRAY:
                if (!(composed instanceof TypedExpression)) {
                    throw makeParseException("No such class or variable.",
                                             part.token);
                }
                Class<?> type = Types.UNTYPED;
                if (((TypedExpression) composed).isTyped()) {
                    type = ((TypedExpression) composed).getType();
                    if (!type.isArray()) {
                        throw makeParseException("Not an array.", part.token);
                    }
                    type = type.getComponentType();
                }
                ensureConvertability(Integer.TYPE, (TypedExpression) part.obj,
                                     part.token);
                composed = new TypedExpression(type,
                                               new CallExpression(type,
                                                                  new TupleExpression(((TypedExpression) composed)
                                                                                      .getExpression(),
                                                                                      ((TypedExpression) part.obj)
                                                                                      .getExpression()),
                                                                  ArrayFunction.FUN));
                break;
            case PrimaryPart.CALL:
                throw makeParseException("Bad method call or no such method.",
                                         part.token);
            default:
                throw new RuntimeException("Malformed primary vector.");
            }
        }

        if (right != null) {
            if (i == vector.size()) {
                throw makeParseException("Invalid left hand side of assignment.",
                                         rightErrToken);
            }

            // Process the very last item in the queue.
            PrimaryPart part = vector.elementAt(i);
            switch (part.type) {
            case PrimaryPart.NAME:
                if (composed instanceof TypedExpression) {
                    if (((TypedExpression) composed).isTyped()) {
                        Class<?> type = ((TypedExpression) composed).getType();
                        Field field = null;
                        try {
                            field = type.getField((String) part.obj);
                        } catch (Exception e) {
                            throw makeParseException("No such field.",
                                                     part.token);
                        }
                        ensureConvertability(field.getType(), right,
                                             rightErrToken);
                        composed = new TypedExpression(field.getType(),
                                                       new CallExpression(field
                                                                          .getType(),
                                                                          new TupleExpression(((TypedExpression) composed)
                                                                                              .getExpression(),
                                                                                              right
                                                                                              .getExpression()),
                                                                          new FieldWriteFunction(field)));
                    } else {
                        composed = new TypedExpression(Types.UNTYPED,
                                                       new CallExpression(Types.UNTYPED,
                                                                          new TupleExpression(((TypedExpression) composed)
                                                                                              .getExpression(),
                                                                                              right
                                                                                              .getExpression()),
                                                                          new DynamicFieldWriteFunction((String) part.obj)));
                    }
                } else {
                    // composed instanceof Class
                    Class<?> clazz = (Class<?>) composed;
                    Field field = null;
                    try {
                        field = clazz.getField((String) part.obj);
                    } catch (Exception e) {
                        throw makeParseException("No such field.", part.token);
                    }
                    ensureConvertability(field.getType(), right, rightErrToken);
                    composed = new TypedExpression(field.getType(),
                                                   new CallExpression(field
                                   .getType(), right.getExpression(),
                                                                      new StaticFieldWriteFunction(field)));
                }
                break;
            case PrimaryPart.ARRAY:
                if (!(composed instanceof TypedExpression)) {
                    throw makeParseException("No such class or variable.",
                                             part.token);
                }
                Class<?> type = Types.UNTYPED;
                if (((TypedExpression) composed).isTyped()) {
                    type = ((TypedExpression) composed).getType();
                    if (!type.isArray()) {
                        throw makeParseException("Not an array.", part.token);
                    }
                    type = type.getComponentType();
                    ensureConvertability(type, right, rightErrToken);
                }
                ensureConvertability(Integer.TYPE, (TypedExpression) part.obj,
                                     part.token);
                composed = new TypedExpression(type,
                                               new CallExpression(type,
                                                                  new TupleExpression(((TypedExpression) composed)
                                                                                      .getExpression(),
                                                                                      ((TypedExpression) part.obj)
                                                                                      .getExpression(),
                                                                                      right
                                                                                      .getExpression()),
                                                                  ArrayWriteFunction.FUN));
                break;
            default:
                throw new RuntimeException("Malformed primary vector.");
            }
        }

        if (!(composed instanceof TypedExpression)) {
            throw makeParseException("No such class or variable.",
                                     firstPart.token);
        }

        return (TypedExpression) composed;
    }

    /**
     * Checks if the user has filtered his search request. Decides if the header
     * needs one more row to show the letters the user has filtered for.
     *
     * @param obj
     *            String of the users input after the object-identifier.
     * @return empty String or String that is showing the letters the user has
     *         filtered for to add to the header.
     */
    private static String checkForGivenPrefix(String obj) {
        String name = obj;

        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
            if (name.length() > 0) {
                name = " \n (prefixing \"" + name + "\")";
            }
        }

        return name;
    }

    private static void addTokenInformation(LocalVariable var, Token t) {
        var.variableBeginLine = t.beginLine;
        var.variableBeginColumn = t.beginColumn;
        var.variableEndLine = t.endLine;
        var.variableEndColumn = t.endColumn;

    }
}