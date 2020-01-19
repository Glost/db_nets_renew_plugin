package de.renew.refactoring.wizard;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;


/**
 * Input panel that wraps a {@link JList}.
 *
 * @author 2mfriedr
 */
public abstract class ListPanel<T> extends JPanel {
    private static final long serialVersionUID = 8757223583811214544L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ListPanel.class);
    private final List<T> _data;
    private final JList<String> _list;

    public ListPanel(final List<T> list) {
        this(list, null);
    }

    public ListPanel(final List<T> list, final String title) {
        _data = list;
        _list = new JList<String>();
        createPanel((title != null) ? title : "");
    }

    /**
     * Subclasses must implement this method. It is called to determine the
     * title of a list item.
     *
     * @param item the item
     * @return the title
     */
    protected abstract String getTitleForItem(T item);

    /**
     * Subclasses must implement this method. It is called when an item is
     * double-clicked.
     *
     * @param item the item
     */
    protected abstract void openItem(T item);

    private void createPanel(final String title) {
        setLayout(new BorderLayout(0, 10));

        if (!title.isEmpty()) {
            JLabel label = new JLabel(title);
            add(label, BorderLayout.NORTH);
        }

        _list.setModel(new ListModel<String>() {
                @Override
                public int getSize() {
                    return _data.size();
                }

                @Override
                public String getElementAt(int index) {
                    return getTitleForItem(_data.get(index));
                }

                @Override
                public void addListDataListener(ListDataListener l) {
                }

                @Override
                public void removeListDataListener(ListDataListener l) {
                }
            });
        add(_list, BorderLayout.CENTER);

        // copied from http://docs.oracle.com/javase/7/docs/api/javax/swing/JList.html
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = _list.locationToIndex(e.getPoint());
                    logger.debug("Double clicked on Item " + index);
                    openItem(_data.get(index));
                }
            }
        };
        _list.addMouseListener(mouseListener);
    }

    public void focus() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _list.requestFocusInWindow();
                }
            });
    }
}