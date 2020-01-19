package de.renew.formalism.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


public class VariableSuggestion extends Suggestion {
    private final Class<?> type;
    private final boolean importNeeded;
    private boolean editDesired = false;

    /**
     * Suggests some types for the variable in the <code>firstPart</code>.
     *
     * @param firstPart The primary part of the expression.
     * @param right     The typed expression.
     * @param declarationNode  The parsed declaration node.
     * @return Some suggestions for the variable type.
     */
    public static Collection<VariableSuggestion> suggest(PrimaryPart firstPart,
                                                         TypedExpression right,
                                                         ParsedDeclarationNode declarationNode) {
        String variableName = (String) firstPart.obj;

        List<Class<?>> variableTypes = new ArrayList<Class<?>>();

        Class<?> rightType = suggestByRightExpression(right);
        if (rightType != null) {
            variableTypes.add(rightType);
        }
        variableTypes.addAll(suggestImported(variableName.charAt(0),
                                             declarationNode.getWellKnownClasses()));
        variableTypes.addAll(suggestPrimitives(variableName.charAt(0)));
        variableTypes.addAll(suggestStatic());

        Collection<VariableSuggestion> result = new LinkedHashSet<VariableSuggestion>();
        boolean objectEditOnce = true;
        for (Class<?> type : variableTypes) {
            // an import is needed, if the declaration node cannot interpret the class name
            boolean importNeeded = false;

            if (type.getPackage() != null
                        && declarationNode.interpreteName(type.getSimpleName()) == null) {
                importNeeded = true;
            }

            VariableSuggestion sug = new VariableSuggestion(variableName, type,
                                                            importNeeded);
            if (type == Object.class && objectEditOnce) {
                sug.editDesired = true;
                objectEditOnce = false;
            }

            result.add(sug);
        }

        return result;
    }

    /**
     * Tries to suggest a type for the variable by analysing the right part of the expression.
     *
     * @param rightExp The right part of the expression.
     * @return The type of the right part, <code>null</code> if the expression or its type is <code>null</code>.
     */
    private static Class<?> suggestByRightExpression(TypedExpression rightExp) {
        if (rightExp != null && rightExp.getType() != null) {
            return rightExp.getType();
        }

        return null;
    }

    /**
     * Suggests primitive types for the variable, that begin with the same letter.
     *
     * @param variableNameFirstChar The first character of the variable name.
     * @return Some primitive types in their boxed Form (e. g. <code>java.lang.Integer</code> rather than <code>int</code>)
     */
    private static Collection<Class<?>> suggestPrimitives(char variableNameFirstChar) {
        Collection<Class<?>> variableTypes = new ArrayList<Class<?>>();

        char upperFirstChar = Character.toUpperCase(variableNameFirstChar);

        for (Class<?> primitive : Arrays.asList(new Class<?>[] { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class })) {
            if (primitive.getSimpleName().charAt(0) == upperFirstChar) {
                variableTypes.add(primitive);
            }
        }

        return variableTypes;
    }

    /**
     * Suggests imported classes for the variable type, that begin with the same character.
     *
     * @param variableNameFirstChar The first character of the variable name.
     * @param knownClasses          All well-known classes from the {@link de.renew.formalism.java.ParsedDeclarationNode}.
     * @return Some imported classes, that begin with the same character.
     */
    private static Collection<Class<?>> suggestImported(char variableNameFirstChar,
                                                        Map<String, Class<?>> knownClasses) {
        Collection<Class<?>> variableTypes = new ArrayList<Class<?>>();

        char upperFirstChar = Character.toUpperCase(variableNameFirstChar);

        for (String simpleName : knownClasses.keySet()) {
            if (simpleName.charAt(0) == upperFirstChar) {
                variableTypes.add(knownClasses.get(simpleName));
            }
        }

        return variableTypes;
    }

    /**
     * Suggests some explicitly denoted types for the variable type. ({@link de.renew.unify.List}, {@link de.renew.unify.Tuple} and {@link java.lang.Object})
     *
     * @return The same set of explicitly denoted classes.
     */
    private static Collection<Class<?>> suggestStatic() {
        Collection<Class<?>> suggestions = new ArrayList<Class<?>>();

        suggestions.add(de.renew.unify.List.class);
        suggestions.add(de.renew.unify.Tuple.class);
        suggestions.add(Object.class);
        suggestions.add(Object.class);

        return suggestions;
    }

    public VariableSuggestion(String name, Class<?> type, boolean importNeeded) {
        super(name, unboxPrimitive(type));
        this.type = type;
        this.importNeeded = importNeeded;
    }

    public boolean isImportNeeded() {
        return importNeeded;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isEditDesired() {
        return editDesired;
    }

    @Override
    public String toString() {
        String pack = type.getPackage() == null ? "" : type.getPackage()
                                                           .getName();

        StringBuilder sb = new StringBuilder();
        sb.append("<html>Declare ");
        sb.append(getName());
        sb.append(" as ");
        sb.append(getTypeName());
        if (editDesired) {
            sb.append(" and edit");
        }
        sb.append(" <font color=gray>- ");
        sb.append(pack);
        sb.append("</font></html>");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        VariableSuggestion other = (VariableSuggestion) obj;
        return type.equals(other.type) && editDesired == other.editDesired;
    }

    @Override
    public int hashCode() {
        return type.hashCode() + (editDesired ? 0 : 1);
    }

    private static String unboxPrimitive(Class<?> clazz) {
        if (clazz == Byte.class) {
            return "byte";
        } else if (clazz == Short.class) {
            return "short";
        } else if (clazz == Integer.class) {
            return "int";
        } else if (clazz == Long.class) {
            return "long";
        } else if (clazz == Float.class) {
            return "float";
        } else if (clazz == Double.class) {
            return "double";
        } else if (clazz == Boolean.class) {
            return "boolean";
        } else if (clazz == Character.class) {
            return "char";
        } else {
            return clazz.getSimpleName();
        }
    }
}