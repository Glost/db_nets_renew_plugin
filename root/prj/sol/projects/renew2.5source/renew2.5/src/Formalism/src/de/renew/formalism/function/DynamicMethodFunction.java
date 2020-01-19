package de.renew.formalism.function;

import de.renew.util.Types;


public class DynamicMethodFunction extends AbstractMethodFunction {
    String method;

    public DynamicMethodFunction(String method) {
        this.method = method;
    }

    public Object doFunction(Object obj, Object[] paramArr)
            throws Exception {
        return Executor.executeMethod(obj.getClass(), obj, method, paramArr);
    }

    public String toString() {
        return "DynamicMethodFunc: "
               + Executor.renderMethodSignature(Types.UNTYPED, method, null);
    }
}