/*
 * Created on 13.09.2005
 *
 */
package de.renew.gui.logging;

import de.renew.engine.events.ExceptionEvent;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class ErrorLevelTableRenderer extends DefaultTableCellRenderer {
    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table,
                                                   Object cellObject,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row,
                                                   int column) {
        Color c = table.getBackground();

        if (cellObject instanceof ExceptionEvent) {
            if (isSelected) {
                c = Color.ORANGE;
            } else {
                c = Color.RED;
            }
        }

        this.setBackground(c);

        Component renderer = super.getTableCellRendererComponent(table,
                                                                 cellObject,
                                                                 isSelected,
                                                                 hasFocus, row,
                                                                 column);
        if (renderer instanceof JLabel) {
            ((JLabel) renderer).setToolTipText("<HTML>"
                                               + cellObject.toString()
                                                           .replaceAll("\n",
                                                                       "<BR>")
                                               + "</HTML>");
        }

        return renderer;
    }
}