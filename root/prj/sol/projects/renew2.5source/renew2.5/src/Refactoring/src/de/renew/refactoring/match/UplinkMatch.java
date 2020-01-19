package de.renew.refactoring.match;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNTextFigure;


/**
 * An UplinkMatch objects describes an uplink inside a text figure. It is
 * immutable and doesn't check any inputs.
 *
 * @author 2mfriedr
 */
public class UplinkMatch extends LinkMatch {
    public UplinkMatch(Drawing drawing, CPNTextFigure textFigure,
                       StringMatch linkMatch, StringMatch channelNameMatch,
                       int parameterCount) {
        super(drawing, textFigure, linkMatch, channelNameMatch, parameterCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return (obj instanceof UplinkMatch);
    }
}