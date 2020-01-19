/*
 * Created on 16.05.2003
 *
 */
package de.renew.formalism;



/**
 * @author 6schumac
 *
 */
public interface FormalismChangeListener {
    public static final int ADD = 0;
    public static final int REMOVE = 1;
    public static final int CHOOSE = 2;

    // triggered when newCompiler is chosen in the source Object
    public void formalismChanged(String compilerName, Object source, int action);
}