package de.renew.refactoring.match;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.TransitionFigure;


/**
 * Parent class for {@link UplinkMatch} and {@link DownlinkMatch}.
 * Should not be instantiated directly.
 *
 * @author 2mfriedr
 */
public class LinkMatch extends TextFigureMatch {
    private final TransitionFigure _transitionFigure;
    private final StringMatch _channelNameMatch;
    private final int _parameterCount;

    /**
     * Constructs a new LinkMatch object. Should only be used by subclasses.
     *
     * @param drawing the drawing
     * @param textFigure the text figure
     * @param linkMatch the uplink or downlink match
     * @param channelNameMatch the channel name match
     */
    protected LinkMatch(final Drawing drawing, final CPNTextFigure textFigure,
                        final StringMatch linkMatch,
                        final StringMatch channelNameMatch,
                        final int parameterCount) {
        super(drawing, textFigure, linkMatch);
        _transitionFigure = (TransitionFigure) getTextFigure().parent();
        _channelNameMatch = channelNameMatch;
        _parameterCount = parameterCount;
    }

    /**
     * Return the match's transition figure.
     * @return the transition figure
     */
    public TransitionFigure getTransitionFigure() {
        return _transitionFigure;
    }

    /**
     * Returns the channel name's start index
     * @return the start index
     */
    public int getChannelNameStart() {
        return _channelNameMatch.start();
    }

    /**
     * Returns the channel name's end index
     * @return the end index
     */
    public int getChannelNameEnd() {
        return _channelNameMatch.end();
    }

    /**
     * Returns the channel name's string
     * @return the string
     */
    public String getChannelNameString() {
        return _channelNameMatch.match();
    }

    /**
     * Returns the uplink's parameter count.
     * @return the parameter count
     */
    public int getParameterCount() {
        return _parameterCount;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ _transitionFigure.hashCode()
               ^ _channelNameMatch.hashCode() ^ _parameterCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof LinkMatch) {
            LinkMatch other = (LinkMatch) obj;
            return getTransitionFigure() == other.getTransitionFigure()
                   && _channelNameMatch.equals(other._channelNameMatch)
                   && getParameterCount() == other.getParameterCount();
        }
        return false;
    }
}