package de.renew.dbnets.shadow;

import de.renew.formalism.java.SingleJavaDBNetCompiler;
import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;

/**
 * The factory for creating the db-net's compiler.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class SingleJavaDBNetCompilerFactory implements ShadowCompilerFactory {

    /**
     * Creates the db-net's compiler.
     *
     * @return the db-net's compiler.
     */
    @Override
    public ShadowCompiler createCompiler() {
        return new SingleJavaDBNetCompiler();
    }
}
