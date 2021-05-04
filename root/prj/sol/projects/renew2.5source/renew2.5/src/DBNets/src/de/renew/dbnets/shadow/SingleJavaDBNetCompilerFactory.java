package de.renew.dbnets.shadow;

import de.renew.formalism.java.SingleJavaDBNetCompiler;
import de.renew.shadow.ShadowCompiler;
import de.renew.shadow.ShadowCompilerFactory;

/**
 * The factory for creating the db-net's compiler.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
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
