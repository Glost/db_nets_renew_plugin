package de.renew.refactoring.parse;

import de.renew.formalism.java.JavaNetParser;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


/**
 * Interface for variable parsers.
 * A variable parser finds variables in inscriptions and the declaration node
 * of a single drawing. The declaration node is typically passed as a
 * constructor parameter and stored as a member.
 *
 * @author 2mfriedr
 */
public interface VariableParser {

    /**
     * Checks if a name is a valid variable name.
     *
     * @param name the name to be checked
     * @return {@code true} if the name is a valid variable name, otherwise
     * {@code false}
     */
    public boolean isValidVariableName(String name);

    /**
     * Checks if a string contains a variable.
     *
     * @param string the string to be checked
     * @return {@code true} if the string contains a variable name, otherwise
     * {@code false}
     */
    public boolean containsVariable(String string);

    /**
     * Finds variables in a string.
     *
     * @param string the string to be searched
     * @return a list of string match objects
     */
    public List<StringMatch> findVariables(String string);

    /**
     * Finds variables in the declaration node.
     *
     * This is a seperate method to {@link #findVariables(String)} because the
     * declaration node can't be parsed with {@link
     * JNPParser#transitionInscriptions(JavaNetParser)} which in turn uses {@link
     * JavaNetParser#TransitionInscription(boolean, de.renew.net.Transition)}.
     * Among other things, {@code import} statements are not allowed in
     * transition inscriptions and would throw syntax exceptions.
     *
     * @return a list of string match objects
     */
    public List<StringMatch> findVariablesInDeclarationNode();

    /**
     * Returns the type of a variable, as determined by the member declaration
     * node.
     *
     * @param variable the variable name
     * @return the type
     */
    public Class<?> findVariableType(String variable);
}