package de.renew.refactoring.renamechannel;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.io.DrawingFileHelper;

import de.renew.gui.GuiPlugin;

import de.renew.refactoring.match.LinkMatch;
import de.renew.refactoring.match.UplinkMatch;
import de.renew.refactoring.parse.LinkParser;
import de.renew.refactoring.search.range.DrawingSearchRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A RenameChannelRefactoring object holds the state of the refactoring and
 * delegates searching and editing operations to other objects.
 *
 * @author 2mfriedr
 *
 */
public class RenameChannelRefactoring {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameChannelRefactoring.class);
    private final LinkParser _parser;
    private final List<LinkMatch> _selectedLinks;
    private final Drawing _initialDrawing;
    private LinkMatch _selectedLink;
    private String _newChannelName;
    private DrawingSearchRange _searchRange;
    private RenameChannelReferencingLinkFinder _linkFinder;
    private RenameChannelEditor _editor;
    private List<LinkMatch> _linksToReplace;
    private Set<Drawing> _changedDrawings = new HashSet<Drawing>();

    /**
     * Constructs a new RenameChannelRefactoring object. It will try to find
     * the selected uplink and/or downlinks.
     *
     * @param initialDrawing the current drawing
     * @param selection the selection
     * @throws NoLinkSelectedException if no uplink or downlink could be found
     */
    public RenameChannelRefactoring(final LinkParser parser,
                                    final Drawing initialDrawing,
                                    final List<Figure> selection)
            throws NoLinkSelectedException {
        _parser = parser;
        _initialDrawing = initialDrawing;
        _selectedLinks = new RenameChannelSelectedLinkFinder(_parser,
                                                             initialDrawing,
                                                             selection)
                             .findSelectedLinks();
        if (_selectedLinks.size() == 0) {
            throw new NoLinkSelectedException();
        }
    }

    // Referencing Link Finder Facade
    private RenameChannelReferencingLinkFinder getLinkFinder() {
        if (_linkFinder == null) {
            _linkFinder = new RenameChannelReferencingLinkFinder(_parser,
                                                                 getOldChannelName(),
                                                                 getParameterCount(),
                                                                 _searchRange);
        }
        return _linkFinder;
    }

    private void invalidateLinkFinder() {
        _linkFinder = null;
    }

    /**
     *@see RenameChannelReferencingLinkFinder#getNumberOfItemsToSearch()
     */
    public int getNumberOfDrawingsToSearch() {
        return getLinkFinder().getNumberOfItemsToSearch();
    }

    /**
     * @see RenameChannelReferencingLinkFinder#hasNextItemToSearch()
     */
    public boolean hasNextDrawingToSearch() {
        return getLinkFinder().hasNextItemToSearch();
    }

    /**
     * @see RenameChannelReferencingLinkFinder#searchNextItem()
     */
    public List<LinkMatch> findLinksInNextDrawing() {
        return getLinkFinder().searchNextItem();
    }

    /**
     * @see RenameChannelReferencingLinkFinder#getCurrentItemString()
     */
    public String getCurrentlySearchedDrawingName() {
        return getLinkFinder().getCurrentItemString();
    }

    /**
     * @see RenameChannelReferencingLinkFinder#getProgress()
     */
    public int getReferencingLinkFinderProgress() {
        return getLinkFinder().getProgress();
    }


    // Editor Facade
    private RenameChannelEditor getEditor() {
        if (_editor == null) {
            _editor = new RenameChannelEditor(_linksToReplace, _newChannelName);
        }
        return _editor;
    }

    private void invalidateEditorAndChangedDrawings() {
        _editor = null;
        _changedDrawings = new HashSet<Drawing>();
    }

    /**
     * @see RenameChannelEditor#getNumberOfEdits()
     */
    public int getNumberOfEdits() {
        return getEditor().getNumberOfEdits();
    }

    /**
     * @see RenameChannelEditor#hasNextLink()
     */
    public boolean hasNextEdit() {
        return getEditor().hasNextEdit();
    }

    /**
     * @see RenameChannelEditor#performNextEdit()
     */
    public void performNextEdit() {
        _changedDrawings.add(getEditor().performNextEdit());
    }

    /**
     * @see RenameChannelEditor#getProgress()
     */
    public int getEditorProgress() {
        return getEditor().getProgress();
    }

    /**
     * @see RenameChannelEditor#getCurrentEditString()
     */
    public String getCurrentlyEditedDrawingName() {
        return getEditor().getCurrentEditString();
    }

    /**
     * Saves all changed drawings.
     */
    public void saveChangedDrawings() {
        for (Drawing drawing : _changedDrawings) {
            boolean success = DrawingFileHelper.saveDrawing(drawing,
                                                            drawing.getFilename(),
                                                            GuiPlugin.getCurrent()
                                                                     .getGui());
            logger.debug("Saving drawing: " + drawing.getFilename()
                         + ", success: " + success);

        }
    }

    public Drawing getInitialDrawing() {
        return _initialDrawing;
    }

    public List<LinkMatch> getSelectedLinks() {
        return Collections.unmodifiableList(_selectedLinks);
    }

    public String getOldChannelName() {
        return _parser.findChannelName(_selectedLink.getText()).match();
    }

    public int getParameterCount() {
        return _parser.findParameterCount(_selectedLink.getText());
    }

    public LinkMatch getSelectedLink() {
        return _selectedLink;
    }

    public void setSelectedLink(LinkMatch linkMatch) {
        _selectedLink = linkMatch;
    }

    public boolean isSelectedLinkUplink() {
        return (_selectedLink instanceof UplinkMatch);
    }

    public boolean isValidChannelName(final String name) {
        if (getOldChannelName().equals(name)) {
            return false;
        }
        return _parser.isValidChannelName(name);
    }

    public String getNewChannelName() {
        return _newChannelName;
    }

    public void setNewChannelName(final String newChannelName) {
        _newChannelName = newChannelName;
        invalidateLinkFinder();
    }

    public DrawingSearchRange getSearchRange() {
        return _searchRange;
    }

    public void setSearchRange(DrawingSearchRange searchRange) {
        _searchRange = searchRange;
    }

    public List<LinkMatch> getLinksToReplace() {
        return _linksToReplace;
    }

    public void setLinksToReplace(final List<LinkMatch> linksToReplace) {
        _linksToReplace = linksToReplace;
        invalidateEditorAndChangedDrawings();
    }

    public void restorePreviousTexts() {
        _changedDrawings = null;
        _editor.restorePreviousTexts();
    }

    /**
     * Returns a newly built list of changed drawings.
     *
     * @return a list of changed drawings
     */
    public List<Drawing> getChangedDrawings() {
//        Set<String> changedDrawingNames = new HashSet<String>();
//        for (Drawing drawing : _changedDrawings) {
//            changedDrawingNames.add(drawing.getName());
//        }
        return new ArrayList<Drawing>(_changedDrawings);
    }
}