package de.renew.refactoring.renamechannel;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;

import de.renew.refactoring.match.DownlinkMatch;
import de.renew.refactoring.match.LinkMatch;
import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.match.UplinkMatch;
import de.renew.refactoring.parse.LinkParser;
import de.renew.refactoring.search.DrawingSearcher;
import de.renew.refactoring.search.range.DrawingSearchRange;

import java.util.ArrayList;
import java.util.List;


/**
 * Finds uplinks and downlinks that match a specified name and parameter count
 * across drawings.
 *
 * @author 2mfriedr
 */
class RenameChannelReferencingLinkFinder extends DrawingSearcher<LinkMatch> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameChannelReferencingLinkFinder.class);
    private final LinkParser _parser;
    private final String _channel;
    private final int _parameterCount;

    RenameChannelReferencingLinkFinder(final LinkParser parser,
                                       final String channel,
                                       final int parameterCount,
                                       final DrawingSearchRange searchRange) {
        super(searchRange);
        _parser = parser;
        _channel = channel;
        _parameterCount = parameterCount;
    }

    @Override
    public List<LinkMatch> searchDrawing(final Drawing drawing) {
        List<LinkMatch> matches = new ArrayList<LinkMatch>();

        FigureEnumeration figures = drawing.figures();
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (figure instanceof CPNTextFigure
                        && !(figure instanceof DeclarationFigure)) {
                CPNTextFigure textFigure = (CPNTextFigure) figure;

                LinkMatch uplink = findUplink(textFigure, drawing);
                if (uplink != null) {
                    matches.add(uplink);
                }
                matches.addAll(findDownlinks(textFigure, drawing));
            }
        }

        logger.debug("Found " + matches.size()
                     + " referencing links in drawing " + drawing.getName());
        return matches;
    }

    /**
     * Finds an uplink with the previously specified channel name and parameter
     * count in a text figure.
     *
     * @param textFigure the text figure
     * @param drawing the drawing
     * @return an uplink match object if an uplink was found, otherwise {@code
     * null}
     */
    private UplinkMatch findUplink(final CPNTextFigure textFigure,
                                   final Drawing drawing) {
        StringMatch uplinkMatch = _parser.findUplink(textFigure.getText(),
                                                     _channel, _parameterCount);
        if (uplinkMatch != null) {
            StringMatch channelNameMatch = _parser.findChannelName(uplinkMatch
                                               .match());
            return new UplinkMatch(drawing, textFigure, uplinkMatch,
                                   channelNameMatch, _parameterCount);
        }
        return null;
    }

    /**
     * Finds downlinks with the previously specified channel name and parameter
     * count in a text figure.
     *
     * @param textFigure the text figure
     * @param drawing the drawing
     * @return a list of downlinks match objects, may be empty if no downlinks
     * were found
     */
    private List<DownlinkMatch> findDownlinks(final CPNTextFigure textFigure,
                                              final Drawing drawing) {
        List<DownlinkMatch> matches = new ArrayList<DownlinkMatch>();
        List<StringMatch> downlinks = _parser.findDownlinks(textFigure.getText(),
                                                            _channel,
                                                            _parameterCount);

        for (StringMatch downlinkMatch : downlinks) {
            StringMatch channelNameMatch = _parser.findChannelName(downlinkMatch
                                                                   .match());
            DownlinkMatch match = new DownlinkMatch(drawing, textFigure,
                                                    downlinkMatch,
                                                    channelNameMatch,
                                                    _parameterCount);
            matches.add(match);
        }
        return matches;
    }
}