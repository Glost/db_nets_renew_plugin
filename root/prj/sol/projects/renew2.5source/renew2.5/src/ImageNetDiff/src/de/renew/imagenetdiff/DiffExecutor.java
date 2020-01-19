package de.renew.imagenetdiff;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.StatusDisplayer;

import java.io.File;


public interface DiffExecutor {

    /**
     * Executes a diff of two drawings. Display result in separate frame.
     * @param sd - a status displayer to show the status to the user.
     * @param drawing1 - first drawing of the diff.
     * @param drawing2 - second drawing of the diff.
     * @param quite - create only the temporary files if true.
     */
    public abstract File doDiff(StatusDisplayer sd, Drawing drawing1,
                                Drawing drawing2, boolean quite);
}