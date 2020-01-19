/**
 *
 */
package CH.ifa.draw.standard;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import CH.ifa.draw.figures.EllipseFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Figure;

import java.util.Vector;


/**
 * @author Fabian Sobanski
 *
 */
public class StandardDrawingViewTest {
    @Test
    public void test() {
        Vector<Figure> v = new Vector<Figure>();
        EllipseFigure efigure = new EllipseFigure();
        ChildFigure cfigure = new TextFigure(true);
        efigure.addChild(cfigure);
        v.add(efigure);
        EllipseFigure e2figure = new EllipseFigure();
        v.add(e2figure);
        Vector<Figure> result = StandardDrawingView.expandFigureVector(v);
        assertTrue(result.contains(efigure));
        assertTrue(result.contains(e2figure));
        assertTrue(result.contains(cfigure));
    }
}