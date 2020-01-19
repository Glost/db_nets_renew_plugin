package de.renew.formalism.java;

import de.renew.expression.Expression;


/**
 * A TypedExpression consists of two parts, the type and the {@link Expression} itself.
 *
 */
public class TypedExpression {
    private Expression expr;
    private Class<?> type;

    /**
     * Create a new TypedExpression.
     * @param type the Type
     * @param expr the {@link Expression}
     */
    public TypedExpression(Class<?> type, Expression expr) {
        this.expr = expr;
        this.type = type;
    }

    /**
     * Getter for the {@link Expression} of this TypedExpression
     * @return the Expression
     */
    public Expression getExpression() {
        return expr;
    }

    /**
     * Getter for the type of this TypedExpression
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Boolean, which indicates if this TypedExpression is untyped. Untyped means, if we got a "real type" different than <code>new Object().{}getClass</code>
     * @return boolean typed
     */
    public boolean isTyped() {
        return type != de.renew.util.Types.UNTYPED;
    }

    /**
     * Converts this TypedExpression into a String.
     */
    public String toString() {
        return "TypedExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + expr + ")";
    }
}