/*
 * ConfigureNetpathTab.java
 *
 * Created on 19. August 2003
 */
package de.renew.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;


/**
 *
 * @author Michael Duvigneau
 **/
class ConfigureNetpathTab extends JPanel {
    private JButton editButton;
    private JButton downButton;
    private JButton removeButton;
    private JButton upButton;
    private JList pathList;
    private JPanel buttonPanel;
    private JButton addButton;
    private JScrollPane pathScrollPane;
    private ConfigureNetpathController controller;

    /** Creates new form ConfigureNetpathTab */
    public ConfigureNetpathTab(ConfigureNetpathController controller) {
        this.controller = controller;
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     **/
    private void initComponents() {
        buttonPanel = new JPanel();
        addButton = new JButton();
        editButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        removeButton = new JButton();
        pathScrollPane = new JScrollPane();
        pathList = new JList();

        setName("Net path");
        setLayout(new BorderLayout());

        buttonPanel.setLayout(new GridLayout(0, 1));

        addButton.setText("Add...");
        addButton.setToolTipText("Opens a dialog to add a new entry in front of  the selected entry.");
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.addEntry();
                }
            });
        buttonPanel.add(addButton);

        editButton.setText("Edit...");
        editButton.setToolTipText("Opens a dialog for modification of the selected entry.");
        editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.editEntry();
                }
            });
        buttonPanel.add(editButton);

        upButton.setText("Move up");
        upButton.setToolTipText("Moves the selected entry towards the beginning of the path list");
        upButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.upEntry();
                }
            });
        buttonPanel.add(upButton);

        downButton.setText("Move down");
        downButton.setToolTipText("Moves the selected entry towards the end of the path list.");
        downButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.downEntry();
                }
            });
        buttonPanel.add(downButton);

        removeButton.setText("Delete");
        removeButton.setToolTipText("Deletes the selected entry from the path.");
        removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.removeEntry();
                }
            });
        buttonPanel.add(removeButton);

        add(buttonPanel, BorderLayout.EAST);

        pathList.setToolTipText("Select path entries to move, modify or delete them.");
        pathScrollPane.setViewportView(pathList);

        add(pathScrollPane, BorderLayout.CENTER);
    }

    public void setPathList(ListModel model) {
        pathList.setModel(model);
    }

    public ListModel getPathList() {
        return pathList.getModel();
    }

    public ListSelectionModel getSelection() {
        return pathList.getSelectionModel();
    }
}