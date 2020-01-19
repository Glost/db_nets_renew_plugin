/*
 * Created on 11.08.2004
 *
 */
package de.renew.gui.logging;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * A Swing panel to configure logging features of the
 * simulation engine.
 *
 * @author Sven Offermann
 **/
public class ConfigureLoggingTab extends JPanel {
    private ConfigureLoggingController controller;
    private JTree loggerTree;
    private JScrollPane infoPane;
    private JSplitPane splitPane;

    public ConfigureLoggingTab(ConfigureLoggingController controller) {
        super(new BorderLayout());
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setName("Logging");

        this.loggerTree = new JTree();
        //this.loggerTree.setPreferredSize(new Dimension(300, 200));
        this.loggerTree.addMouseListener(new PopupMenuMouseListener());
        TreeSelectionModel selModel = this.loggerTree.getSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        loggerTree.addTreeSelectionListener(controller);

        JScrollPane leftComponent = new JScrollPane(loggerTree);
        leftComponent.setMinimumSize(new Dimension(300, 200));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent,
                                   infoPane);


        add(splitPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Select an appender to display and edit attributes."),
                  "North");
        panel.setPreferredSize(new Dimension(300, 200));
        setRightSide(panel);
    }

    protected void setRootNode(MutableTreeNode root) {
        this.loggerTree.setModel(new DefaultTreeModel(root));
    }

    protected void setRightSide(JComponent c) {
        this.splitPane.setRightComponent(c);
        this.splitPane.resetToPreferredSizes();
    }

    // popup menu implementation
    private class PopupMenuMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            Component c = e.getComponent();
            if ((c instanceof JTree) && (e.isPopupTrigger())) {
                openPopup((JTree) c, e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            Component c = e.getComponent();
            if ((c instanceof JTree) && (e.isPopupTrigger())) {
                openPopup((JTree) c, e);
            }
        }

        private void openPopup(JTree tree, MouseEvent e) {
            Point p = e.getPoint();
            TreePath path = tree.getPathForLocation(p.x, p.y);

            if (path == null) {
                return;
            }
            Object o = ((DefaultMutableTreeNode) path.getLastPathComponent())
                           .getUserObject();

            JPopupMenu popup = new JPopupMenu();

            JMenuItem item1 = new JMenuItem("add logger");
            item1.addActionListener(controller.createAddLoggerAction());
            item1.setEnabled(false);
            popup.add(item1);

            JMenuItem item2 = new JMenuItem("remove logger");
            item2.setEnabled(false);
            popup.add(item2);

            popup.addSeparator();

            JMenu menu3 = new JMenu("add appender");
            menu3.setEnabled(false);
            popup.add(menu3);

            JMenuItem item4 = new JMenuItem("remove appender");
            item4.setEnabled(false);
            popup.add(item4);

            JMenuItem item5 = new JMenuItem("export configuration");
            item5.addActionListener(controller.createExportConfigurationAction());
            popup.add(item5);


            if (o instanceof TreeNodeLoggerWrapper) {
                TreeNodeLoggerWrapper wrapper = (TreeNodeLoggerWrapper) o;
                item2.setEnabled(true);
                item2.addActionListener(controller.createRemoveLoggerAction(wrapper.getLogger()
                                                                                   .getName()));
                String[] appenderTypes = AppenderFactory.getInstance()
                                                        .getAllAppenderTypes();
                for (int x = 0; x < appenderTypes.length; x++) {
                    JMenuItem item = new JMenuItem(appenderTypes[x]);
                    item.addActionListener(controller.createAddAppenderAction(wrapper.getLogger()
                                                                                     .getName(),
                                                                              appenderTypes[x]));
                    menu3.add(item);
                }
                menu3.setEnabled(true);
            }

            if (o instanceof TreeNodeAppenderWrapper) {
                TreeNodeAppenderWrapper wrapper = (TreeNodeAppenderWrapper) o;

                menu3.setEnabled(true);
                item4.addActionListener(controller.createRemoveAppenderAction(wrapper.getLogger()
                                                                                     .getName(),
                                                                              wrapper
                                                                              .getAppender()));
                item4.setEnabled(true);
            } else if (o instanceof String) {
                item1.setEnabled(true);
            }

            popup.show(tree, e.getX(), e.getY());
        }
    }
}