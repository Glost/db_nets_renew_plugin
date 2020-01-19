package de.renew.gui;



/**
 * @author Joern
 */
public interface FigureCreatorHolder {
    public void registerCreator(FigureCreator cr);

    public void unregisterCreator(FigureCreator cr);

    public void registerCreator(TextFigureCreator tcr);

    public void unregisterCreator(TextFigureCreator tcr);
}