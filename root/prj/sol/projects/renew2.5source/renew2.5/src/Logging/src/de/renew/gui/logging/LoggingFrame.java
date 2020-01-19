/*
 * Created on 18.08.2004
 */
package de.renew.gui.logging;

import de.renew.gui.ComponentRenderer;
import de.renew.gui.JComponentCellEditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
* GUI for showing the trace messages produced during the
* simulation of petri nets
*
* @author Sven Offermann
*/
public class LoggingFrame extends JFrame {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(LoggingFrame.class);
    private LoggingController controller;
    private JTable loggerTable;
    private JScrollPane scrollPane;
    private JComboBox loggerComboBox;
    private JCheckBox updateCheckBox;

    public LoggingFrame(LoggingController controller, String[] loggerNames) {
        this.controller = controller;

        setTitle("Simulation log messages");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initGUI(loggerNames);

        pack();
        setVisible(true);
    }

    public void initGUI(String[] loggerNames) {
        try {
            loggerComboBox = new JComboBox(loggerNames);
            loggerComboBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox) e.getSource();
                        String loggerName = (String) cb.getSelectedItem();
                        controller.changeLogger(loggerName);
                    }
                });

            scrollPane = new JScrollPane();

            loggerTable = new JTable(new TableModel(true)) {
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        TableColumn tableColumn = getColumnModel()
                                                      .getColumn(column);
                        TableCellRenderer renderer = tableColumn.getCellRenderer();

                        if (renderer == null) {
                            Class<?> c = getColumnClass(column);
                            if (c.equals(Object.class)) {
                                Object o = getValueAt(row, column);
                                if (o != null) {
                                    c = getValueAt(row, column).getClass();
                                }
                            }
                            renderer = getDefaultRenderer(c);
                        }

                        return renderer;
                    }

                    public TableCellEditor getCellEditor(int row, int column) {
                        TableColumn tableColumn = getColumnModel()
                                                      .getColumn(column);
                        TableCellEditor editor = tableColumn.getCellEditor();

                        if (editor == null) {
                            Class<?> c = getColumnClass(column);
                            if (c.equals(Object.class)) {
                                Object o = getValueAt(row, column);
                                if (o != null) {
                                    c = getValueAt(row, column).getClass();
                                }
                            }
                            editor = getDefaultEditor(c);
                        }

                        return editor;
                    }
                };

            TableCellRenderer defaultRenderer = loggerTable.getDefaultRenderer(JComponent.class);
            loggerTable.setDefaultRenderer(JComponent.class,
                                           new ComponentRenderer(defaultRenderer));
            TableCellEditor defaultEditor = loggerTable.getDefaultEditor(JComponent.class);
            loggerTable.setDefaultEditor(JComponent.class,
                                         new JComponentCellEditor(defaultEditor,
                                                                  null));

            setName("Simulation Trace");

            BorderLayout thisLayout = new BorderLayout();
            this.getContentPane().setLayout(thisLayout);
            // this.setSize(new java.awt.Dimension(345,262));
            this.getContentPane().add(loggerComboBox, BorderLayout.NORTH);

            this.getContentPane().add(scrollPane, BorderLayout.CENTER);

            scrollPane.add(loggerTable);
            scrollPane.setViewportView(loggerTable);

            updateCheckBox = new JCheckBox("permanent update");
            updateCheckBox.setSelected(true);
            updateCheckBox.addActionListener(new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        controller.setPermanentUpdate(updateCheckBox.isSelected());
                    }
                });
            this.getContentPane().add(updateCheckBox, BorderLayout.SOUTH);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public ComboBoxModel getComboBoxModel() {
        return this.loggerComboBox.getModel();
    }

    public void setTableModel(LoggerTableModel dm) {
        if (dm != null) {
            this.loggerTable.setModel(dm);
            dm.addTableModelListener(new TableModelListenerImpl());
            updateRowHeights();
        }
    }

    public LoggerTableModel getTableModel() {
        return (LoggerTableModel) this.loggerTable.getModel();
    }

    public String getSelectedLoggerName() {
        return (String) loggerComboBox.getSelectedItem();
    }

    public void updateRowHeights() {
        TableModel tm = (TableModel) this.loggerTable.getModel();
        for (int x = 0; x < tm.getRowCount(); x++) {
            Object o = tm.getValueAt(x, 0);
            if (o instanceof JComponent) {
                JComponent c = (JComponent) o;
                this.loggerTable.setRowHeight(x, c.getPreferredSize().height);
            }
        }
    }

    public void updateRowHeight(int row) {
        TableModel tm = (TableModel) this.loggerTable.getModel();
        Object o = tm.getValueAt(row, 0);
        if (o instanceof JComponent) {
            JComponent c = (JComponent) o;
            this.loggerTable.setRowHeight(row, c.getPreferredSize().height);
        }
    }

    // implementation of the TableModelListener
    private class TableModelListenerImpl implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if ((e.getType() == TableModelEvent.INSERT)
                        || (e.getType() == TableModelEvent.UPDATE)) {
                for (int x = e.getFirstRow(); x <= e.getLastRow(); x++) {
                    updateRowHeight(x);
                }
            }
        }
    }
}