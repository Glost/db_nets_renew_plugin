package de.renew.formalism.java;

import de.renew.util.StringUtil;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Collection;


public class ConstructorSuggestion extends Suggestion {
    private final Constructor<?> constructor;
    private final String[] parameters;
    private final String callWithParameters;

    protected ConstructorSuggestion(String typeName, Constructor<?> constructor) {
        super(typeName, typeName);
        this.constructor = constructor;

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        this.parameters = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            this.parameters[i] = parameterTypes[i].getSimpleName();
        }

        this.callWithParameters = this.name + "("
                                  + StringUtil.join(parameters, ", ") + ")";
    }

    public static Collection<ConstructorSuggestion> suggest(Class<?> clazz) {
        Collection<ConstructorSuggestion> suggestions = new ArrayList<ConstructorSuggestion>();
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            suggestions.add(new ConstructorSuggestion(clazz.getSimpleName(),
                                                      constructor));
        }

        return suggestions;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public String getCallWithParameters() {
        return callWithParameters;
    }

    public String[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "<html>" + getCallWithParameters() + " <font color=gray>- "
               + constructor.getDeclaringClass().getSimpleName()
               + "</font></html>";
    }
}