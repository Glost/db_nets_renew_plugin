package CH.ifa.draw.util;

import CH.ifa.draw.framework.Drawing;


public interface DrawingListener {
    public void drawingReleased(Drawing drawing);

    public void drawingAdded(Drawing drawing);
}