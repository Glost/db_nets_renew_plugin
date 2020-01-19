package CH.ifa.draw.contrib;

import CH.ifa.draw.framework.Figure;

import java.awt.Polygon;


public interface OutlineFigure extends Figure {
    public Polygon outline();
}