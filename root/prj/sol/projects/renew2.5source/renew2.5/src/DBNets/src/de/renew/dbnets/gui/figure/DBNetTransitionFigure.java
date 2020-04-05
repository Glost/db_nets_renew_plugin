package de.renew.dbnets.gui.figure;

import CH.ifa.draw.figures.RectangleFigure;
import de.renew.net.DBNetTransition;

public class DBNetTransitionFigure extends RectangleFigure {

    private final DBNetTransition transition;

    public DBNetTransitionFigure(DBNetTransition transition) {
        this.transition = transition;
    }

    public DBNetTransition getTransition() {
        return transition;
    }
}
