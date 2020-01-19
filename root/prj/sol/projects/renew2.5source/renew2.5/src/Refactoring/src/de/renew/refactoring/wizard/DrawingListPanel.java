package de.renew.refactoring.wizard;

import CH.ifa.draw.framework.Drawing;

import java.util.List;


/**
 * ListPanel for a list of drawings that are opened on double-click.
 *
 * @author 2mfriedr
 */
public class DrawingListPanel extends ListPanel<Drawing> {
    private static final long serialVersionUID = 5384164138354660894L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(DrawingListPanel.class);

    public DrawingListPanel(final List<Drawing> drawings, final String title) {
        super(drawings, title);
    }

    @Override
    protected String getTitleForItem(Drawing item) {
        return item.getName();
    }

    @Override
    protected void openItem(Drawing item) {
        DrawingOpener.open(item);
    }
}