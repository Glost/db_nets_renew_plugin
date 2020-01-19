package de.renew.refactoring.wizard;

import de.renew.refactoring.match.TextFigureMatch;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


/**
 * Cell Renderer and Cell Editor that renders a {@link TextFigureMatch} and
 * provides a button to reveal the figure in a drawing.
 *
 * @author 2mfriedr
 */
public class ShowTextFigureButtonCell extends AbstractCellEditor
        implements TableCellEditor, TableCellRenderer {
    private static final long serialVersionUID = 2994225888544885462L;
    private JButton _button = new JButton("Show");
    private TextFigureMatch _match;

    @Override
    public Object getCellEditorValue() {
        return _match;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row,
                                                   int column) {
        _match = (TextFigureMatch) value;
        _button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DrawingOpener.open(_match.getDrawing(),
                                       _match.getTextFigure());
                }
            });
        return _button;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        return getTableCellRendererComponent(table, value, isSelected, true,
                                             row, column);
    }
}