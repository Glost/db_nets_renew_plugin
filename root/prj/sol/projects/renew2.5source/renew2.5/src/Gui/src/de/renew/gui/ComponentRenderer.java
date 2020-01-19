/*
 * Created on 12.08.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.renew.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


public class ComponentRenderer implements TableCellRenderer {
    private TableCellRenderer __defaultRenderer;

    public ComponentRenderer(TableCellRenderer renderer) {
        __defaultRenderer = renderer;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row,
                                                   int column) {
        if (value instanceof Component) {
            return (Component) value;
        }
        return __defaultRenderer.getTableCellRendererComponent(table, value,
                                                               isSelected,
                                                               hasFocus, row,
                                                               column);
    }
}