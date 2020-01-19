package de.renew.formalism.java;

import de.renew.util.StringUtil;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MethodSuggestion extends Suggestion {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MethodSuggestion.class);
    private final Class[] types;
    private final Method method;
    private final String[] parameters;
    private final String callWithParameters;
    private final String attemptedMethod;

    public static List<MethodSuggestion> suggest(Class<?> clazz, String name,
                                                 Class<?>[] types, int modifier) {
        if (logger.isDebugEnabled()) {
            logger.debug(JavaHelper.class.getName() + ": Class = "
                         + clazz.toString());
        }

        List<Method> resultMethodList = new ArrayList<Method>();

        Method[] methods = clazz.getMethods();
        if (methods.length != 0) {
            String methodPattern = name;

            boolean filter = false;

            // do filter if method name suffixes with "_"
            if (methodPattern.endsWith("_")) {
                methodPattern = methodPattern.substring(0,
                                                        methodPattern.length()
                                                        - 1);
                filter = true;
            }

            // do filtering when number of methods is large
            if (methods.length > 20) {
                filter = true;
            }

            // force no filtering by typing only "_"
            if (name.equals("_")) {
                filter = false;
            }

            for (Method method : methods) {
                int mod = method.getModifiers();
                String methodName = method.getName();
                if (logger.isDebugEnabled()) {
                    logger.debug("Name " + methodName + " pattern: "
                                 + methodPattern);
                    logger.debug(JavaHelper.class.getName() + ": modifier for "
                                 + method.getName() + "= " + mod + " "
                                 + !filter
                                 + methodName.startsWith(methodPattern));
                }
                if (!filter || methodName.startsWith(methodPattern)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(JavaHelper.class.getName()
                                     + ": passed filter "
                                     + ((modifier & mod) != 0));
                    }
                    if ((modifier & mod) != 0) {
                        resultMethodList.add(method);
                    }
                }
            }
        }

        List<MethodSuggestion> result = new ArrayList<MethodSuggestion>();
        for (Method method : resultMethodList) {
            result.add(new MethodSuggestion(method, types, name));
        }
        Collections.sort(result);

        return result;
    }

    public MethodSuggestion(Method method, Class[] types, String attemptedMethod) {
        super(method.getName(), method.getReturnType().getSimpleName());
        this.method = method;
        this.types = types;
        this.attemptedMethod = attemptedMethod;

        Class<?>[] parameterTypes = method.getParameterTypes();
        this.parameters = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            this.parameters[i] = parameterTypes[i].getSimpleName();
        }

        this.callWithParameters = this.name + "("
                                  + StringUtil.join(parameters, ", ") + ")";
    }

    @Override
    public String toString() {
        return "<html>" + getCallWithParameters() + " : " + getTypeName()
               + " <font color=gray>- "
               + method.getDeclaringClass().getSimpleName() + "</font></html>";
    }

    public String getCallWithParameters() {
        return callWithParameters;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getAttemptedTypes() {
        return types;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String getAttemptedMethod() {
        return attemptedMethod;
    }
}