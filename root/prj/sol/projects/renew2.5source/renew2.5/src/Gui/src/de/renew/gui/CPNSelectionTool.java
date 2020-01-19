/*
 * @(#)CPNSelectionTool.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.SelectionTool;

import CH.ifa.draw.util.ColorMap;

import de.renew.application.Util;

import de.renew.net.NetInstance;

import java.awt.Point;

import java.net.URI;


class CPNSelectionTool extends SelectionTool {
    public CPNSelectionTool(CPNApplication application) {
        super(application);
    }

    protected boolean alternateInspectFigure(Figure f) {
        if (f instanceof InscribableFigure) {
            editor().prepareUndoSnapshot();
            InscribableFigure inf = (InscribableFigure) f;

            // the following prevents accidental creation of multiple arc
            // inscriptions ("x")
            if (inf instanceof LineConnection
                        && inf.children().hasMoreElements()) {
                inf.children().nextFigure().inspect(view(), true);
                return true;
            }
            TextFigureCreator tfc = GuiPlugin.getCurrent().getTextFigureCreator();
            TextFigure child = tfc.createTextFigure(inf);
            child.setParent(inf);
            Point p = view().lastClick();
            child.displayBox(p, new Point(p.x + 10, p.y + 10));
            child.setText(tfc.getDefaultInscription(inf));
            child.setAttribute("TextType",
                               new Integer(CPNTextFigure.INSCRIPTION));
            if (inf instanceof LineConnection) {
                child.setAttribute("FillColor", ColorMap.BACKGROUND);
            }
            drawing().add(child);
            view().clearSelection();
            view().addToSelection(child);
            editor().commitUndoSnapshot();
            return true;
        } else {
            return super.alternateInspectFigure(f);
        }
    }

    /**
     * Fire a manual transition in a given net.
     * (E.g. sim://net:123/TRANS will fire transition named /TRANS in net
     * instance net[123].)
     *
     * @param uri - the uri donating a net instance and a transition name.
     * @see CH.ifa.draw.standard.AbstractTool#simAccess(java.net.URI)
     */
    public void simAccess(URI uri) {
        Util util = new Util();
        NetInstance instance = util.findInstance("" + uri.getPort());
        if (instance == null) {
            return;
        }
        util.fireTransition(instance, uri.getPath());
    }
}