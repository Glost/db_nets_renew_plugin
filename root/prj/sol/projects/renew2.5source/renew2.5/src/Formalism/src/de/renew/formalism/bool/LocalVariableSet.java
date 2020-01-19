package de.renew.formalism.bool;

import de.renew.expression.LocalVariable;

import java.util.HashSet;
import java.util.Set;


public class LocalVariableSet {
    private Set<LocalVariable> known = new HashSet<LocalVariable>();

    public LocalVariable create(String name) {
        do {
            LocalVariable var = new LocalVariable(name);
            if (known.contains(var)) {
                name = name + "'";
            } else {
                known.add(var);
                return var;
            }
        } while (true);
    }
}