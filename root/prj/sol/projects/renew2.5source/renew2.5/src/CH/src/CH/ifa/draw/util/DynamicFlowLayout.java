package CH.ifa.draw.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;


/**
 * A custom layout manager for the palette.
 * @see CH.ifa.draw.standard.ToolButton
 */
public class DynamicFlowLayout extends FlowLayout {
    public DynamicFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = super.preferredLayoutSize(target);
            int targetw = target.getSize().width;
            if (targetw < dim.width) {
                // return a dynamically calculated preferred size:
                Component[] comp = target.getComponents();
                int w = 0;
                int y = 0;
                int h = 0;
                int maxw = 0;
                for (int i = 0; i < comp.length; ++i) {
                    Dimension oneDim = comp[i].getPreferredSize();
                    if (w > 0 && w + oneDim.width > targetw) {
                        // "line" break:
                        //logger.debug("break!");
                        y += h + getVgap();
                        h = 0;
                        w = 0;
                    }
                    w += oneDim.width + getHgap();
                    maxw = Math.max(maxw, w);
                    h = Math.max(h, oneDim.height);
                    //logger.debug(i+". maxw:"+maxw+", height:"+(y+h));
                }
                dim = new Dimension(maxw + getHgap(), getVgap() + y + h);
            }
            return dim;
        }
    }

    public Dimension minimumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }
}