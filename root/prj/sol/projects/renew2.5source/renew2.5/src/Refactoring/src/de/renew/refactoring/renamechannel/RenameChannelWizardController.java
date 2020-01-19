package de.renew.refactoring.renamechannel;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import de.renew.refactoring.match.DownlinkMatch;
import de.renew.refactoring.match.LinkMatch;
import de.renew.refactoring.match.UplinkMatch;
import de.renew.refactoring.parse.LinkParser;
import de.renew.refactoring.search.range.DrawingSearchRange;
import de.renew.refactoring.search.range.SearchRange;
import de.renew.refactoring.search.range.SearchRanges;
import de.renew.refactoring.wizard.ComboBoxPanel;
import de.renew.refactoring.wizard.DrawingListPanel;
import de.renew.refactoring.wizard.ProgressBarWizardPage;
import de.renew.refactoring.wizard.SearchRangeSelectPanel;
import de.renew.refactoring.wizard.SelectMatchesTable;
import de.renew.refactoring.wizard.TableSelectButton;
import de.renew.refactoring.wizard.TextFieldPanel;
import de.renew.refactoring.wizard.WizardController;
import de.renew.refactoring.wizard.WizardPage;
import de.renew.refactoring.wizard.WrapLayout;

import java.awt.BorderLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;


/**
 * Wizard controller for {@link RenameChannelRefactoring}.
 *
 * @see RenameChannelRefactoring
 * @author 2mfriedr
 */
public class RenameChannelWizardController extends WizardController {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RenameChannelWizardController.class);
    private static enum State {STARTED,
        SELECTED_LINK,
        ENTERED_NEW_CHANNEL_NAME,
        SELECTED_MATCHES,
        FINISHED;
    }
    private State _state = State.STARTED;
    private final RenameChannelRefactoring _refactoring;
    private final Map<SearchRange, List<LinkMatch>> _matches;

    public RenameChannelWizardController(final LinkParser parser,
                                         final Drawing drawing,
                                         final List<Figure> selection)
            throws NoLinkSelectedException {
        _refactoring = new RenameChannelRefactoring(parser, drawing, selection);
        _matches = new HashMap<SearchRange, List<LinkMatch>>();
    }

    @Override
    public String getTitle() {
        return "Rename Channel";
    }

    @Override
    public WizardPage getNextPage() {
        if (_state == State.STARTED) {
            List<LinkMatch> links = _refactoring.getSelectedLinks();
            if (links.size() > 1) { // FIXME and links are not equal to each other?
                return makeSelectLinkPage(links);
            }

            _refactoring.setSelectedLink(links.get(0));
            _state = State.SELECTED_LINK;
        }
        if (_state == State.SELECTED_LINK) {
            return makeEnterNewChannelNamePage(_refactoring.getOldChannelName(),
                                               _refactoring.getParameterCount());
        }

        if (_state == State.ENTERED_NEW_CHANNEL_NAME) {
            final ProgressBarWizardPage<List<LinkMatch>> page = makeSelectMatchesPage();
            SwingWorker<List<LinkMatch>, Void> worker = makeSearchWorker(page);
            worker.execute();
            return page;
        }
        if (_state == State.SELECTED_MATCHES) {
            final ProgressBarWizardPage<List<Drawing>> page = makeFinishPage();
            SwingWorker<List<Drawing>, Void> worker = makeEditWorker(page);
            worker.execute();
            return page;
        }
        return null;
    }

    private SwingWorker<List<LinkMatch>, Void> makeSearchWorker(final ProgressBarWizardPage<List<LinkMatch>> page) {
        return new SwingWorker<List<LinkMatch>, Void>() {
                @Override
                protected List<LinkMatch> doInBackground()
                        throws Exception {
                    DrawingSearchRange searchRange = _refactoring.getSearchRange();
                    if (_matches.containsKey(searchRange)) {
                        return _matches.get(searchRange);
                    }

                    List<LinkMatch> matches = new ArrayList<LinkMatch>();
                    while (_refactoring.hasNextDrawingToSearch()) {
                        matches.addAll(_refactoring.findLinksInNextDrawing());
                        page.setProgress(_refactoring
                                           .getReferencingLinkFinderProgress());
                        page.setStatus("<html>Searching drawing: <i>"
                                       + _refactoring
                                           .getCurrentlySearchedDrawingName()
                                       + "</i></html>");
                    }
                    _matches.put(searchRange, matches);
                    return matches;
                }

                @Override
                protected void done() {
                    try {
                        page.done(get());
                    } catch (Exception ignore) {
                    }
                }
            };
    }

    private SwingWorker<List<Drawing>, Void> makeEditWorker(final ProgressBarWizardPage<List<Drawing>> page) {
        return new SwingWorker<List<Drawing>, Void>() {
                @Override
                protected List<Drawing> doInBackground()
                        throws Exception {
                    while (_refactoring.hasNextEdit()) {
                        _refactoring.performNextEdit();
                        page.setProgress(_refactoring.getEditorProgress());
                        page.setStatus("<html>Editing file: <i>"
                                       + _refactoring
                                           .getCurrentlyEditedDrawingName()
                                       + "</i></html>");
                    }
                    return _refactoring.getChangedDrawings();
                }

                @Override
                protected void done() {
                    try {
                        page.done(get());
                    } catch (Exception ignore) {
                    }
                }
            };
    }

    // The pages
    static final String SELECT_LINK_PAGE = "SELECT_LINK_PAGE";
    static final String ENTER_NEW_CHANNEL_NAME_PAGE = "ENTER_NEW_CHANNEL_NAME_PAGE";
    static final String SELECT_MATCHES_PAGE = "SELECT_MATCHES_PAGE";
    static final String FINISH_PAGE = "FINISH_PAGE";

    private WizardPage makeSelectLinkPage(final List<LinkMatch> links) {
        final String intro = "Select the link to rename:";
        return new WizardPage(SELECT_LINK_PAGE) {
                ComboBoxPanel<LinkMatch> _comboBox;

                @Override
                protected void didLoad() {
                    _comboBox = new ComboBoxPanel<LinkMatch>(intro, links);
                    getPanel().add(_comboBox);
                }

                @Override
                protected void didAppear() {
                    _comboBox.focus();
                    setNextButtonEnabled(true);
                }

                @Override
                protected void saveState() {
                    _refactoring.setSelectedLink(_comboBox.getInput());
                    _state = State.SELECTED_LINK;
                }

                @Override
                protected void resetState() {
                    _refactoring.setSelectedLink(null);
                    _state = State.STARTED;
                }
            };
    }

    private WizardPage makeEnterNewChannelNamePage(final String oldChannelName,
                                                   final int parameterCount) {
        String parameters = "parameter" + ((parameterCount != 1) ? "s" : "");
        final String intro = "<html>Enter the new name for <i>"
                             + oldChannelName + "</i> (" + parameterCount + " "
                             + parameters + "):</html>";

        return new WizardPage(ENTER_NEW_CHANNEL_NAME_PAGE) {
                TextFieldPanel _textField;
                SearchRangeSelectPanel _searchRange;

                @Override
                protected void didLoad() {
                    _textField = new TextFieldPanel(intro, oldChannelName) {
                            private static final long serialVersionUID = 5496760266040789662L;

                            @Override
                            public void inputChanged(String input) {
                                configureNextButton(input);
                            }
                        };
                    getPanel().add(_textField);

                    List<SearchRange> searchRanges = new ArrayList<SearchRange>(SearchRanges
                                                                                .netDrawingSearchRanges(_refactoring
                                                                                                        .getInitialDrawing()));
                    _searchRange = new SearchRangeSelectPanel(searchRanges);
                    getPanel().add(_searchRange);
                }

                @Override
                protected void didAppear() {
                    _textField.focus();
                    configureNextButton(_textField.getInput());
                }

                @Override
                protected void saveState() {
                    _refactoring.setSearchRange((DrawingSearchRange) _searchRange
                                                .getValue());
                    _refactoring.setNewChannelName(_textField.getInput());
                    _state = State.ENTERED_NEW_CHANNEL_NAME;
                }

                @Override
                protected void resetState() {
                    _refactoring.setSearchRange(null);
                    _refactoring.setNewChannelName(null);
                    _state = State.STARTED;
                }

                private void configureNextButton(String input) {
                    setNextButtonEnabled(_refactoring.isValidChannelName(input));
                }
            };
    }

    final static int CHECKBOX_COLUMN = 0;
    final static int NET_NAME_COLUMN = 1;
    final static int CONTEXT_COLUMN = 2;
    final static int LINKMATCH_OBJECT_COLUMN = 3;
    final static int NUMBER_OF_COLUMNS = 4;

    private ProgressBarWizardPage<List<LinkMatch>> makeSelectMatchesPage() {
        return new ProgressBarWizardPage<List<LinkMatch>>(SELECT_MATCHES_PAGE) {
                List<LinkMatch> _matches;
                SelectMatchesTable<LinkMatch> _table;

                @Override
                protected void didAppear() {
                    setNextButtonEnabled(!isInProgress());
                }

                @Override
                protected String nextButtonTitle() {
                    return "Preview";
                }

                @Override
                protected void saveState() {
                    _refactoring.setLinksToReplace(_table.getSelectedItems());
                    _state = State.SELECTED_MATCHES;
                }

                @Override
                protected void resetState() {
                    _refactoring.setLinksToReplace(null);
                    _state = State.ENTERED_NEW_CHANNEL_NAME;
                }

                @Override
                protected void progressDone(List<LinkMatch> result) {
                    setNextButtonEnabled(true);

                    JPanel panel = getPanel();
                    panel.setLayout(new BorderLayout(0, 10));

                    JLabel intro = new JLabel("Select the links to be replaced:");
                    panel.add(intro, BorderLayout.NORTH);

                    _matches = result;

                    LinkMatch selected = _refactoring.getSelectedLink();
                    Object[][] rowData = makeRowData(_matches, selected);
                    final String[] columnNames = { "refactor", "drawing", "context", "" };
                    int[] uneditableRows = { 0 }; // the selected link is at index 0

                    _table = new SelectMatchesTable<LinkMatch>(LinkMatch.class,
                                                               rowData,
                                                               columnNames,
                                                               uneditableRows);
                    panel.add(new JScrollPane(_table), BorderLayout.CENTER);

                    JPanel selectButtonsPanel = makeSelectButtonsPanel(_table);
                    panel.add(selectButtonsPanel, BorderLayout.SOUTH);

                    panel.revalidate();
                    focusNextButton();
                }

                /**
                 * Makes row data for a {@link SelectMatchesTable} from a list
                 * of found matches and a pointer to the selected link.
                 * The selected link will be uneditable in the table, i.e. it
                 * is always marked for refactoring. Other links will be marked
                 * for refactoring by default but can be deselected.
                 *
                 * @param matches the list of matches
                 * @param selectedLink the selected link
                 * @return the row data
                 */
                private Object[][] makeRowData(List<LinkMatch> matches,
                                               LinkMatch selectedLink) {
                    Object[][] rowData = new Object[matches.size()][4];
                    logger.debug("Matches size: " + matches.size());

                    boolean foundSelectedLink = false;

                    for (int i = 0; i < matches.size(); ++i) {
                        LinkMatch link = matches.get(i);

                        int ins; // the insert index
                        if (link.equals(selectedLink)) {
                            ins = 0;
                            foundSelectedLink = true;
                        } else {
                            ins = (foundSelectedLink) ? i : i + 1;
                        }

                        rowData[ins] = new Object[4];
                        rowData[ins][CHECKBOX_COLUMN] = Boolean.TRUE;
                        rowData[ins][NET_NAME_COLUMN] = link.getDrawing()
                                                            .getName();
                        rowData[ins][CONTEXT_COLUMN] = link.matchWithContext();
                        rowData[ins][LINKMATCH_OBJECT_COLUMN] = link;
                    }

                    return rowData;
                }

                private JPanel makeSelectButtonsPanel(JTable table) {
                    JPanel panel = new JPanel(new WrapLayout());
                    panel.add(new JLabel("Select:"));
                    panel.add(TableSelectButton.selectAllButton("All", table,
                                                                CHECKBOX_COLUMN));
                    panel.add(TableSelectButton.selectNoneButton("None", table,
                                                                 CHECKBOX_COLUMN));

                    JButton uplinksButton = TableSelectButton
                                       .selectInstancesButton("Uplinks",
                                                              UplinkMatch.class,
                                                              table,
                                                              CHECKBOX_COLUMN,
                                                              LINKMATCH_OBJECT_COLUMN);
                    panel.add(uplinksButton);

                    JButton downlinksButton = TableSelectButton
                                       .selectInstancesButton("Downlinks",
                                                              DownlinkMatch.class,
                                                              table,
                                                              CHECKBOX_COLUMN,
                                                              LINKMATCH_OBJECT_COLUMN);
                    panel.add(downlinksButton);

                    return panel;
                }
            };
    }

    private ProgressBarWizardPage<List<Drawing>> makeFinishPage() {
        return new ProgressBarWizardPage<List<Drawing>>(FINISH_PAGE) {
                @Override
                protected void didAppear() {
                    setNextButtonEnabled(!isInProgress());
                }

                @Override
                protected boolean isLastPage() {
                    return true;
                }

                @Override
                protected void saveState() {
                    _refactoring.saveChangedDrawings();
                }

                @Override
                protected void resetState() {
                    _refactoring.restorePreviousTexts();
                }

                @Override
                protected void progressDone(List<Drawing> result) {
                    _state = State.FINISHED;
                    setNextButtonEnabled(true);
                    JPanel panel = getPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add(new DrawingListPanel(result,
                                                   "Finished. The following drawings will be saved:"),
                              BorderLayout.CENTER);
                }
            };
    }
}