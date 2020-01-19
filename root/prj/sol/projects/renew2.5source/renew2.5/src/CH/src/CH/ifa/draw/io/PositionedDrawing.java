package CH.ifa.draw.io;

import CH.ifa.draw.framework.Drawing;

import java.awt.Dimension;
import java.awt.Point;


public class PositionedDrawing {
    private Point windowLoc;
    private Dimension windowDim;
    private Drawing drawing;

    public PositionedDrawing(Point windowLocation, Dimension windowDim,
                             Drawing drawing) {
        this.windowLoc = windowLocation;
        this.windowDim = windowDim;
        this.drawing = drawing;
    }

    public Point getWindowLocation() {
        return windowLoc;
    }

    public Dimension getWindowDimension() {
        return windowDim;
    }

    public Drawing getDrawing() {
        return drawing;
    }
}