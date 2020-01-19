package de.renew.refactoring.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;


/**
 * InputPanel that wraps a {@link JComboBox}.
 *
 * @author 2mfriedr
 */
public class ComboBoxPanel<T> extends InputPanel<T> {
    private static final long serialVersionUID = -4567023850241640087L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ComboBoxPanel.class);
    final List<T> _items;
    JComboBox<T> _comboBox;
    T _selectedItem;

    public ComboBoxPanel(final String intro, final List<T> items) {
        _items = items;

        add(new JLabel(intro));

        _comboBox = new JComboBox<T>(new ComboBoxModel<T>() {
                @Override
                public int getSize() {
                    return _items.size();
                }

                @Override
                public T getElementAt(int index) {
                    return _items.get(index);
                }

                @Override
                public void addListDataListener(ListDataListener l) {
                }

                @Override
                public void removeListDataListener(ListDataListener l) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void setSelectedItem(Object anItem) {
                    _selectedItem = (T) anItem;
                }

                @Override
                public Object getSelectedItem() {
                    return _selectedItem;
                }
            });
        _comboBox.setSelectedIndex(0);

        _comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    inputChanged(getInput());
                }
            });
        add(_comboBox);
    }

    @Override
    public T getInput() {
        return _selectedItem;
    }

    @Override
    public void inputChanged(T input) {
    }

    @Override
    public void focus() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _comboBox.requestFocusInWindow();
                }
            });
    }
}