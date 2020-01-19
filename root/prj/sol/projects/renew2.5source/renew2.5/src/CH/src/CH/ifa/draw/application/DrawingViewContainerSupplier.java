package CH.ifa.draw.application;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.standard.StandardDrawingView;

import java.awt.Dimension;
import java.awt.Point;


public interface DrawingViewContainerSupplier {
    public DrawingViewContainer getContainer(DrawApplication appl,
                                             StandardDrawingView drawingView,
                                             Drawing drawing, Point loc,
                                             Dimension size);
}