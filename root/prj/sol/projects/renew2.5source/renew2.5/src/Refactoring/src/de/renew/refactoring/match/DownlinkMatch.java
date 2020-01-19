package de.renew.refactoring.match;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNTextFigure;


/**
 * A DownlinkMatch objects describes a downlink inside a text figure. It is
 * immutable and doesn't check any inputs.
 *
 * @author 2mfriedr
 */
public class DownlinkMatch extends LinkMatch {
    public DownlinkMatch(Drawing drawing, CPNTextFigure textFigure,
                         StringMatch linkMatch, StringMatch channelNameMatch,
                         int parameterCount) {
        super(drawing, textFigure, linkMatch, channelNameMatch, parameterCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return (obj instanceof DownlinkMatch);
    }
}