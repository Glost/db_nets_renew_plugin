package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.SyntaxException;


public interface SemanticUpdateFigure extends Figure {

    /** This method is called by the CPNTextTool after endEdit() and
     * setText() and gives SemanticUpdateFigures the opportunity to update
     * their semantics based on information derivable from other figures
     * of the shadow net.
     */
    public void semanticUpdate(ShadowNet shadowNet) throws SyntaxException;
}