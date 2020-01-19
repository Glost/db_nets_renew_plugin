package de.renew.refactoring.renamechannel;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;

import de.renew.refactoring.edit.TextFigureMatchEditor;
import de.renew.refactoring.match.LinkMatch;
import de.renew.refactoring.util.StringHelper;

import java.util.List;


/**
 * RenameChannelEditor edits {@link LinkMatch} objects and returns the changed
 * drawings.
 *
 * @author 2mfriedr
 */
public class RenameChannelEditor extends TextFigureMatchEditor<LinkMatch, Drawing> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameChannelEditor.class);
    private final String _newChannelName;

    /**
     * Constructs a RenameChannelEditor.
     *
     * @param linkMatches a list of link matches
     * @param newChannelName the new channel name
     */
    RenameChannelEditor(final List<LinkMatch> linkMatches,
                        final String newChannelName) {
        super(linkMatches);
        _newChannelName = newChannelName;
    }

    @Override
    public Drawing performEdit(LinkMatch match) {
        TextFigure textFigure = match.getTextFigure();
        String text = textFigure.getText();
        String linkString = match.getMatch();

        int channelStart = match.getChannelNameStart();
        int channelEnd = match.getChannelNameEnd();

        // replace channel name
        String newLink = StringHelper.replaceRange(linkString, channelStart,
                                                   channelEnd, _newChannelName);
        logger.debug("Replaced channel name: " + newLink);

        // replace whole old link
        int linkStart = match.getStart();
        int linkEnd = match.getEnd();
        String newText = StringHelper.replaceRange(text, linkStart, linkEnd,
                                                   newLink);

        changeText(match, newText);
        return match.getDrawing();
    }
}