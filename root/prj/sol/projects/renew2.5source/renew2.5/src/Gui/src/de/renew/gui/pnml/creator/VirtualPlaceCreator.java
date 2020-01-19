package de.renew.gui.pnml.creator;

import de.renew.gui.VirtualPlaceFigure;


/**
 * @author volker
 * erstellt am Apr 30, 2004
 *
 */
public class VirtualPlaceCreator extends NodeCreator {
    public VirtualPlaceCreator() {
        super("VirtualPlace");
    }

    protected void doCreateObject() {
        int parentId = ((VirtualPlaceFigure) getFigure()).getSemanticPlaceFigure()
                        .getID();
        getElement()
            .setAttribute("semanticPlaceFigure",
                          "\"" + String.valueOf(parentId) + "\"");
    }
}