package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;


/**
 * @author joern
 *
 */
public interface TextFigureCreator {
    public boolean canCreateFigure(InscribableFigure figure);

    public TextFigure createTextFigure(InscribableFigure figure);

    public boolean canCreateDefaultInscription(InscribableFigure figure);

    public String getDefaultInscription(InscribableFigure figure);
}