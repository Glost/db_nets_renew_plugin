package de.renew.refactoring.renamechannel;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.TransitionFigure;

import de.renew.refactoring.match.DownlinkMatch;
import de.renew.refactoring.match.LinkMatch;
import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.match.UplinkMatch;
import de.renew.refactoring.parse.LinkParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Finds the selected uplink and/or downlinks.
 *
 * @author 2mfriedr
 */
class RenameChannelSelectedLinkFinder {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameChannelSelectedLinkFinder.class);
    private final LinkParser _parser;
    private final Drawing _drawing;
    private final Figure _selectedFigure;

    /**
     * Constructs a new selected link finder.
     *
     * @param parser the link parser that should be used
     * @param drawing the current drawing, only used for creating a {@link
     * UplinkMatch} and/or {@link DownlinkMatch} objects
     * @param selection the selection
     */
    RenameChannelSelectedLinkFinder(final LinkParser parser,
                                    final Drawing drawing,
                                    final List<Figure> selection) {
        _parser = parser;
        _drawing = drawing;
        _selectedFigure = (selection.size() == 1) ? selection.get(0) : null;
    }

    /**
     * Checks if a text figure or transition figure is selected.
     *
     * @return {@code true} if a text figure or transition figure is selected,
     * otherwise {@code false}
     */
    boolean isTextFigureOrTransitionFigureSelected() {
        return (_selectedFigure instanceof CPNTextFigure
               || _selectedFigure instanceof TransitionFigure);
    }

    /**
     * Checks if an uplink and/or downlink is selected.
     *
     * @return {@code true} if a transition figure with an uplink or downlink
     * is selected or a transition figure's child text figure with an uplink
     * or downlink is selected, otherwise {@code false}
     */
    boolean isLinkSelected() {
        return findSelectedLinks().size() > 0;
    }

    /**
     * Finds uplinks and downlinks in the selection.
     *
     * @return a list of link matches
     */
    List<LinkMatch> findSelectedLinks() {
        if (_selectedFigure instanceof TransitionFigure) {
            return findLinks((TransitionFigure) _selectedFigure);
        }
        if (_selectedFigure instanceof CPNTextFigure) {
            CPNTextFigure textFigure = (CPNTextFigure) _selectedFigure;
            if (hasParentTransitionFigure(textFigure)) {
                return findLinks(textFigure);
            }
        }
        return Collections.emptyList();
    }

    private List<LinkMatch> findLinks(CPNTextFigure textFigure) {
        List<LinkMatch> links = new ArrayList<LinkMatch>();
        String text = textFigure.getText();

        StringMatch uplinkMatch = _parser.findUplink(text);
        if (uplinkMatch != null) {
            StringMatch channelNameMatch = _parser.findChannelName(uplinkMatch
                                               .match());
            int parameterCount = _parser.findParameterCount(uplinkMatch.match());
            links.add(new UplinkMatch(_drawing, textFigure, uplinkMatch,
                                      channelNameMatch, parameterCount));
        }

        for (StringMatch downlinkMatch : _parser.findDownlinks(text)) {
            StringMatch channelNameMatch = _parser.findChannelName(downlinkMatch
                                                                   .match());
            int parameterCount = _parser.findParameterCount(downlinkMatch.match());
            links.add(new DownlinkMatch(_drawing, textFigure, downlinkMatch,
                                        channelNameMatch, parameterCount));
        }

        return links;
    }

    private List<LinkMatch> findLinks(TransitionFigure transitionFigure) {
        List<LinkMatch> links = new ArrayList<LinkMatch>();
        FigureEnumeration children = transitionFigure.children();
        while (children.hasMoreElements()) {
            Figure child = children.nextElement();
            if (child instanceof CPNTextFigure) {
                links.addAll(findLinks((CPNTextFigure) child));
            }
        }
        return links;
    }

    private static boolean hasParentTransitionFigure(CPNTextFigure textFigure) {
        return textFigure.parent() instanceof TransitionFigure;
    }
}