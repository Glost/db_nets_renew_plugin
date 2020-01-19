package de.renew.refactoring.wizard;

import de.renew.refactoring.search.range.SearchRange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * InputPanel that wraps a group of radio button that allow selection of a
 * search range. The last search range is selected by default.
 *
 * @author 2mfriedr
 */
public class SearchRangeSelectPanel extends JPanel {
    private static final long serialVersionUID = -4669316167221128799L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SearchRangeSelectPanel.class);
    private Map<JRadioButton, SearchRange> _buttons;

    /**
     * Constructs a search range select panel with the last search range selected.
     *
     * @param searchRanges the search ranges
     */
    public SearchRangeSelectPanel(final List<SearchRange> searchRanges) {
        this(searchRanges, searchRanges.size() - 1);
    }

    /**
     * Constructs a search range select panel with a specified selected index.
     *
     * @param searchRanges the search ranges
     * @param defaultIndex the selected index
     */
    public SearchRangeSelectPanel(final List<SearchRange> searchRanges,
                                  final int defaultIndex) {
        super();
        createRadioButtons(searchRanges, defaultIndex);
        setLayout(new WrapLayout());
    }

    private void createRadioButtons(final List<SearchRange> searchRanges,
                                    final int defaultIndex) {
        JLabel intro = new JLabel("Search Range:");
        add(intro);

        ButtonGroup buttonGroup = new ButtonGroup();
        _buttons = new HashMap<JRadioButton, SearchRange>();

        int i = 0;
        for (SearchRange searchRange : searchRanges) {
            JRadioButton button = new JRadioButton(searchRange.description());
            button.setSelected(i == defaultIndex);

            _buttons.put(button, searchRange);
            add(button);
            buttonGroup.add(button);

            i += 1;
        }
    }

    public SearchRange getValue() {
        for (JRadioButton button : _buttons.keySet()) {
            if (button.isSelected()) {
                return _buttons.get(button);
            }
        }
        return null;
    }
}