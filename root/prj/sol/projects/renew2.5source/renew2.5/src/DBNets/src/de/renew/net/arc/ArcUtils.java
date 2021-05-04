package de.renew.net.arc;

import de.renew.engine.searcher.Executable;

/**
 * The utility methods for the arcs.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ArcUtils {

    /**
     * The constructor.
     * Should never be invoked since it is the utility class with only static methods.
     */
    private ArcUtils() {
        throw new AssertionError();
    }

    /**
     * Wraps the executable in the SimpleOutputArcExecutable wrapper if the executable is output arc's executable,
     * but not the rollback arc's executable.
     * Otherwise, returns the given executable as is.
     *
     * @param executable The executable which is the candidate for wrapping.
     * @param rollbackArcExecutable The db-net transition's rollback arc's executable.
     *                              May be null, if the transition has no rollback arcs.
     * @return The wrapped executable if the executable is output arc's executable,
     * but not the rollback arc's executable.
     * Otherwise, returns the given executable as is.
     */
    public static Executable wrapOutputArcExecutable(Executable executable,
                                                     RollbackArcExecutable rollbackArcExecutable) {
        if ((executable instanceof OutputArcExecutable) && !(executable instanceof RollbackArcExecutable)) {
            return new SimpleOutputArcExecutable((OutputArcExecutable) executable, rollbackArcExecutable);
        }

        return executable;
    }
}
