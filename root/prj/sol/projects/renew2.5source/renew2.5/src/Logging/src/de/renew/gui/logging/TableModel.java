/*
 * Created on 12.08.2004
 */
package de.renew.gui.logging;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;


/**
 * A modifiable table model. Rows can contain any number of column objects. The
 * column names can be configured via {@link #setColumnIdentifiers(String[])}.
 * The type of objects in the first row implicitly defines the column classes
 * for all rows.
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 */
class TableModel extends AbstractTableModel {

    /**
     * Stores the rows of this table model. Each row is stored as {@link Vector}
     * of objects.
     **/
    private Vector<Vector<Object>> _rows = new Vector<Vector<Object>>();

    /**
     * Stores the column names of this table model.
     **/
    private Vector<String> _columns = new Vector<String>();

    /**
     * Stores whether all cells in this table model should be editable or not.
     **/
    private boolean editable = false;

    /**
     * Create a modifiable table model. By default the table has exactly one
     * column with empty column name.
     *
     * @param editable configures whether <em>all</em> cells in this table
     *            should be editable or not.
     **/
    public TableModel(boolean editable) {
        this.editable = editable;
        this._columns.add("");
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        return _columns.elementAt(column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return _rows.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return _columns.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column) {
        if (row >= 0) {
            try {
                Vector<Object> rowVector = _rows.elementAt(row);
                return rowVector.elementAt(column);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns the editable flag defined during construction of this table model.
     * </p>
     **/
    @Override
    public boolean isCellEditable(int row, int column) {
        return editable;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    /**
     * Append a row to the table model with the given column objects.
     *
     * @param objects the objects that form the column entries of the row to add
     **/
    public void addRow(Object[] objects) {
        Vector<Object> row = new Vector<Object>();

        for (int x = 0; x < objects.length; x++) {
            row.add(objects[x]);
        }

        _rows.add(row);
    }

    /**
     * Clear the table model completely.
     **/
    public void removeAllRows() {
        _rows.clear();
    }

    /**
     * Remove a row from the table model. The row is identified by matching with
     * the given column objects.
     *
     * @param objects the column objects forming the row to delete.
     * @return the index of the removed row
     * @throws ArrayIndexOutOfBoundsException if no matching row was found to
     *             remove
     */
    public int removeRow(Object[] objects) {
        Vector<Object> row = new Vector<Object>();
        for (int x = 0; x < objects.length; x++) {
            row.add(objects[x]);
        }

        int deletedRow = _rows.indexOf(row);
        removeRow(deletedRow);

        return deletedRow;
    }

    /**
     * Delete the given row from the table model.
     *
     * @param row the index of the row to remove.
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public void removeRow(int row) {
        _rows.remove(row);
    }

    /**
     * Replace the current set of column names with the given identifiers.
     *
     * @param idents the column names to set for the table model.
     */
    public void setColumnIdentifiers(String[] idents) {
        _columns = new Vector<String>();

        for (int x = 0; x < idents.length; x++) {
            _columns.add(idents[x]);
        }
    }
}