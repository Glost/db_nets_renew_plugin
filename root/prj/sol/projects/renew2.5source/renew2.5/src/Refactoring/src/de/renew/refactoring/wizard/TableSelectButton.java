package de.renew.refactoring.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTable;


/**
 * JButton that allows modifying a boolean row in a table, e.g. a {@link
 * SelectMatchesTable}.
 * Some useful buttons are provided as static methods.
 *
 * @see SelectMatchesTable
 * @author 2mfriedr
 */
public abstract class TableSelectButton extends JButton {
    private static final long serialVersionUID = -6549952914848675014L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(TableSelectButton.class);
    private final JTable _table;
    private final int _col;

    /**
     * Creates a new button.
     *
     * @param title the button's title
     * @param table the table
     * @param column the checkbox column
     */
    public TableSelectButton(final String title, final JTable table,
                             final int column) {
        super(title);
        _table = table;
        _col = column;
        addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int row = 0; row < _table.getRowCount(); row++) {
                        _table.setValueAt(new Boolean(selectRow(_table, row)),
                                          row, _col);
                    }
                }
            });
    }

    /**
     * Subclasses must override this method to determine if a row should be
     * selected.
     *
     * @param table the table
     * @param row the row
     * @return {@code true} if the row should be selected, otherwise {@code
     * false}
     */
    public abstract boolean selectRow(JTable table, int row);

    /**
     * Returns a button that selects all rows.
     *
     * @param table the table
     * @param column the checkbox column
     * @return the button
     */
    public static TableSelectButton selectAllButton(final String title,
                                                    final JTable table,
                                                    final int column) {
        return new TableSelectButton(title, table, column) {
                private static final long serialVersionUID = -1041670163206301939L;

                @Override
                public boolean selectRow(JTable table, int row) {
                    return true;
                }
            };
    }

    /**
     * Returns a button that selects no rows.
     *
     * @param table the table
     * @param column the checkbox column
     * @return the button
     */
    public static TableSelectButton selectNoneButton(final String title,
                                                     final JTable table,
                                                     final int column) {
        return new TableSelectButton(title, table, column) {
                private static final long serialVersionUID = -8167772826900151285L;

                @Override
                public boolean selectRow(JTable table, int row) {
                    return false;
                }
            };
    }

    /**
     * Returns a button that selects rows where the type of an item matches the
     * type of the specified class.
     *
     * @param title the button's title
     * @param clazz the class that the item is checked against
     * @param table the table
     * @param checkboxColumn the checkbox column
     * @param itemColumn the item column
     * @return
     */
    public static TableSelectButton selectInstancesButton(final String title,
                                                          final Class<?> clazz,
                                                          final JTable table,
                                                          final int checkboxColumn,
                                                          final int itemColumn) {
        return new TableSelectButton(title, table, checkboxColumn) {
                private static final long serialVersionUID = 4879678711675528458L;

                @Override
                public boolean selectRow(JTable table, int row) {
                    Object value = table.getValueAt(row, itemColumn);
                    return clazz.isInstance(value);
                }
            };
    }
}