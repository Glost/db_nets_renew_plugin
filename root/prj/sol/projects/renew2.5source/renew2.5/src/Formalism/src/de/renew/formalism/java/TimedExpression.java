package de.renew.formalism.java;

public class TimedExpression {
    private TypedExpression expr;
    private TypedExpression timeExpr;

    public TimedExpression(TypedExpression expr, TypedExpression timeExpr) {
        this.expr = expr;
        this.timeExpr = timeExpr;
    }

    public TypedExpression getExpression() {
        return expr;
    }

    public TypedExpression getTime() {
        return timeExpr;
    }

    public boolean isTimed() {
        return timeExpr != null;
    }

    public String toString() {
        final int sbSize = 1000;
        final String variableSeparator = ", ";
        final StringBuffer sb = new StringBuffer(sbSize);
        sb.append("TimedExpr(");
        sb.append("time: ").append(timeExpr);
        sb.append(variableSeparator);
        sb.append(expr);
        sb.append(")");
        return sb.toString();
    }
}