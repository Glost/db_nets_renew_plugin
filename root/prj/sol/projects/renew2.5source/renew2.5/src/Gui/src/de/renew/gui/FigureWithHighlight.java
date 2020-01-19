package de.renew.gui;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureWithDependencies;


/** Interface for Figures
  * which can have an associated highlight figure.
  */
public interface FigureWithHighlight extends FigureWithDependencies {
    public void setHighlightFigure(Figure fig);

    public Figure getHighlightFigure();
}