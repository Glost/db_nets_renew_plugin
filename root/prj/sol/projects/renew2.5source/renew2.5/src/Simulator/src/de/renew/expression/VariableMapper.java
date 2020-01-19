package de.renew.expression;

import de.renew.unify.Copier;
import de.renew.unify.Variable;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * A variable mapper maps local variables to their respective actual
 * values in the current context.
 *
 * @author Olaf Kummer
 **/
public class VariableMapper {

    /**
     * This hashtable does the mapping for those variables
     * that I met so far.
     **/
    private Hashtable<LocalVariable, Variable> hashtable;

    /**
     * I (a variable mapper) am created. I map no variables so far.
     **/
    public VariableMapper() {
        hashtable = new Hashtable<LocalVariable, Variable>();
    }

    /**
     * Create a new instance that already
     * maps all the local variables that are
     * known to me. Assume that I would return a certain
     * variable. If that variable is send through the copier
     * you get another variable that the new instance will return
     * after the same query.
     *
     * Put differently, you get a functional composition
     * of the copier and myself.
     *
     * @param copier
     *   the copier that maps my variables to their copies
     * @return the new variable mapper
     **/
    public VariableMapper makeCopy(Copier copier) {
        VariableMapper mapper = new VariableMapper();
        Enumeration<LocalVariable> enumeration = hashtable.keys();
        while (enumeration.hasMoreElements()) {
            LocalVariable key = enumeration.nextElement();
            mapper.hashtable.put(key, (Variable) copier.copy(hashtable.get(key)));
        }
        return mapper;
    }

    /**
     * I will map the local variable to a variable.
     * On the first call with some argument value, I return a fresh
     * variable. On successive calls with the same argument value,
     * I will always be consistent and return the original variable
     * again.
     **/
    public Variable map(LocalVariable localVariable) {
        Variable variable = null;
        if (isMapped(localVariable)) {
            variable = hashtable.get(localVariable);
        } else {
            variable = new Variable();
            hashtable.put(localVariable, variable);
        }
        return variable;
    }

    /**
     * @author Friedrich Delgado Friedrichs <friedel@nomaden.org>
     *
     * Check if a local variable is already mapped.
     *
     * This method is needed to make VariableMapper useful for non-Java
     * inscription languages which use their own method of unification.
     *
     * @param localVariable
     * @return boolean
     */
    public boolean isMapped(LocalVariable localVariable) {
        return hashtable.containsKey(localVariable);
    }

    /**
     * I can return all local variables that I have mapped so far.
     **/
    public Enumeration<LocalVariable> getLocalVariables() {
        return hashtable.keys();
    }

    public void appendBindingsTo(StringBuffer result) {
        result.append("{");
        Enumeration<LocalVariable> variables = getLocalVariables();
        boolean firstEntry = true;
        while (variables.hasMoreElements()) {
            LocalVariable localVariable = variables.nextElement();

            // Only show those variables that want to be shown.
            if (localVariable.isVisible) {
                Variable variable = map(localVariable);


                // Only add those variables to the list that are
                // completely bound.
                if (variable.isBound()) {
                    if (!firstEntry) {
                        result.append(",");
                    }
                    result.append("\n  ");
                    firstEntry = false;
                    result.append(localVariable.name);
                    result.append("=");
                    result.append(variable.getValue());
                }
            }
        }
        result.append("}");
    }
}