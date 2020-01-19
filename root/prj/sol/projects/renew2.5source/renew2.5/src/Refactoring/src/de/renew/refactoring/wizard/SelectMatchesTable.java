package de.renew.refactoring.wizard;

import de.renew.refactoring.match.TextFigureMatch;

import java.awt.Component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;


/**
 * JTable subclass for {@link TextFigureMatch} objects.
 * The first column shows checkboxes that allow the user to mark items.
 * The last column shows {@link  ShowTextFigureButtonCell} buttons.
 *
 * @see TableSelectButton
 * @author 2mfriedr
 */
public class SelectMatchesTable<T extends TextFigureMatch> extends JTable {
    private static final long serialVersionUID = 5728970344351229491L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SelectMatchesTable.class);
    private final Class<T> _type;
    private final Object[][] _rowData;
    private final String[] _columnNames;
    private final int[] _uneditableRows;
    private final int _checkboxColumn = 0;
    private final int _textFigureMatchColumn;

    /**
     * Creates a new SelectMatchesTable with the specified class, row data,
     * and column names.
     *
     * @param type the generic type T of the object
     * @param rowData the table contents
     * @param columnNames an array of at least 2 column names
     * @param uneditableRows an array of row numbers that are not editable
     */
    public SelectMatchesTable(Class<T> type, Object[][] rowData,
                              String[] columnNames, int[] uneditableRows) {
        _type = type;
        _rowData = rowData;
        _columnNames = columnNames;
        _uneditableRows = (uneditableRows != null) ? uneditableRows : new int[0];
        _textFigureMatchColumn = _columnNames.length - 1;
        configureTable();
    }

    private boolean isRowEditable(int row) {
        for (int i = 0; i < _uneditableRows.length; ++i) {
            if (_uneditableRows[i] == row) {
                return false;
            }
        }
        return true;
    }

    private void configureTable() {
        setModel(new AbstractTableModel() {
                private static final long serialVersionUID = 5656622377240990426L;

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return _rowData[rowIndex][columnIndex];
                }

                @Override
                public int getRowCount() {
                    return _rowData.length;
                }

                @Override
                public int getColumnCount() {
                    return _columnNames.length;
                }

                @Override
                public String getColumnName(int column) {
                    return _columnNames[column];
                }

                @Override
                public Class<?extends Object> getColumnClass(int c) {
                    return _rowData.length > 0 ? getValueAt(0, c).getClass()
                                               : Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int col) {
                    if (col == _textFigureMatchColumn) {
                        return true;
                    }
                    return isRowEditable(row) && col == _checkboxColumn;
                }

                @Override
                public void setValueAt(Object value, int row, int col) {
                    if (isCellEditable(row, col)) {
                        _rowData[row][col] = value;
                        fireTableCellUpdated(row, col);
                    }
                }
            });

        setDefaultEditor(_type, new ShowTextFigureButtonCell());
        setDefaultRenderer(_type, new ShowTextFigureButtonCell());

        getColumnModel().getColumn(_checkboxColumn).setMaxWidth(60);
        getColumnModel().getColumn(_textFigureMatchColumn).setMaxWidth(80);
        setRowHeight(25);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row,
                                     int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setEnabled(isRowEditable(row) || column != _checkboxColumn);
        return c;
    }

    /**
     * Returns the items with checked checkboxes.
     *
     * @return the selected items
     */
    @SuppressWarnings("unchecked")
    public List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<T>();
        for (Object[] row : _rowData) {
            if ((Boolean) row[_checkboxColumn]) {
                selectedItems.add((T) row[_textFigureMatchColumn]);
            }
        }
        return selectedItems;
    }
}