/*
 * @(#)FeatureConnectionHandle.java 5.1
 *
 */
package de.renew.gui.fs;

import de.uni_hamburg.fs.Feature;
import de.uni_hamburg.fs.FeatureStructure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.CPNTextFigure;

import java.awt.Dimension;
import java.awt.Point;


/**
 * A handle to connect FSNodes to one another.
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class FeatureConnectionHandle extends ConnectionHandle {

    /**
     * Constructs a handle for the given owner
     */
    public FeatureConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(), FeatureConnection.NormalArc);
    }

    /**
     * Constructs a handle for the given owner
     */
    public FeatureConnectionHandle(Figure owner, Locator locator) {
        super(owner, locator, FeatureConnection.NormalArc);
    }

    /**
     * Connects the figures if the mouse is released over another
     * FSNode figure;
     * otherwise, a new figure is created!
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target == null) {
            Figure owner = owner();
            Feature feature = null;
            FeatureStructure fs = null;
            if (owner instanceof FSFigure) {
                fs = ((FSFigure) owner).getFeatureStructure();
                if (fs != null) {
                    feature = fs.getFirstMissingAssociation();
                    //logger.debug("Found missing feature "+feature.feature+" of type "+feature.type);
                }
            }
            if (owner.findFigureInside(x, y) == null) {
                Figure endFigure;
                if (owner instanceof FSFigure) {
                    FSFigure fsEndFigure = new FSFigure();
                    endFigure = fsEndFigure;
                    if (feature != null) {
                        fsEndFigure.setText(new FeatureStructure(feature.type)
                            .toString());
                        if (fs != null
                                    && fsEndFigure.getFeatureStructure() != null) {
                            fs.getRoot()
                              .setFeature(feature.feature,
                                          fsEndFigure.getFeatureStructure()
                                                     .getRoot());
                        }
                    }
                } else {
                    endFigure = new FSNodeFigure("Type");
                }
                Dimension d = endFigure.displayBox().getSize();
                int w2 = d.width / 2;
                int h2 = d.height / 2;
                endFigure.displayBox(new Point(x - w2, y - h2),
                                     new Point(x - w2 + d.width,
                                               y - h2 + d.height));
                view.drawing().add(endFigure);
                String featureStr = "feat";
                if (feature != null) {
                    featureStr = feature.feature.toString();
                }
                CPNTextFigure featureFigure = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
                featureFigure.setAttribute("FillColor", ColorMap.BACKGROUND);
                featureFigure.setText(featureStr);
                featureFigure.setParent((FeatureConnection) getConnection());
                view.drawing().add(featureFigure);
            }
        }
        super.invokeEnd(x, y, anchorX, anchorY, view);
    }
}