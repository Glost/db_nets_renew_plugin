package de.renew.formalism.function;

import de.renew.expression.Function;


public class Identity implements Function {
    public static Function FUN = new Identity();

    private Identity() {
    }

    public Object function(Object arg) {
        return arg;
    }
}