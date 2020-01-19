package de.renew.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 * A configuration dialog for the Renew simulation.
 * The dialog is managed by {@link ConfigureSimulationController}.
 *
 * @author Michael Duvigneau
 **/
class ConfigureSimulationDialog extends JDialog {
    private JPanel buttonPanel;
    private JButton applyButton;
    private JButton updateButton;
    private JButton updateSimButton;
    private JButton okButton;
    private JButton closeButton;
    private JTabbedPane optionsTabbedPane;
    private JLabel noteLabel;
    private final ConfigureSimulationController controller;

    /**
     * Creates the new dialog and its components. The given
     * <code>initalTabs</code> are added to the options tabbed
     * pane.
     * @param parent      the frame to which this dialog belongs.
     * @param controller  the controller which manages this dialog
     * @param initialTabs the option panels to add to the dialog
     *                    before it gets layouted.
     **/
    public ConfigureSimulationDialog(JFrame parent,
                                     ConfigureSimulationController controller,
                                     Component[] initialTabs) {
        super(parent, false);
        this.controller = controller;
        initComponents();
        for (int i = 0; i < initialTabs.length; i++) {
            optionsTabbedPane.add(initialTabs[i]);
        }
        pack();
    }

    /**
     * Creates and initialises the dialog's fixed components.
     **/
    private void initComponents() {
        noteLabel = new JLabel();
        optionsTabbedPane = new JTabbedPane();
        buttonPanel = new JPanel();
        applyButton = new JButton();
        updateButton = new JButton();
        updateSimButton = new JButton();
        okButton = new JButton();
        closeButton = new JButton();

        setName("Configure Simulation");
        setTitle("Configure Simulation");
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    controller.closeDialog();
                }
            });

        noteLabel.setText("Settings will not take effect until a new simulation is set up.");
        getContentPane().add(noteLabel, BorderLayout.NORTH);

        getContentPane().add(optionsTabbedPane, BorderLayout.CENTER);

        applyButton.setText("Apply");
        applyButton.setToolTipText("Confirms these settings for the next simulation setup and closes the dialog.");
        applyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.commitDialog();
                }
            });
        buttonPanel.add(applyButton);

        updateButton.setToolTipText("Copies the current settings of the simulator plugin into this dialog.");
        updateButton.setText("Update");
        updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.updateDialog();
                }
            });
        buttonPanel.add(updateButton);

        updateSimButton.setToolTipText("Copies the settings of the current simulation into this dialog.");
        updateSimButton.setText("Update from Simulation");
        updateSimButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.updateDialogFromSimulation();
                }
            });
        buttonPanel.add(updateSimButton);

        okButton.setText("OK");
        okButton.setToolTipText("Confirms these settings for the next simulation setup and closes the dialog.");
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.commitDialog();
                    controller.closeDialog();
                }
            });
        buttonPanel.add(okButton);

        closeButton.setToolTipText("Closes the dialog without changing the settings.");
        closeButton.setText("Close");
        closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.closeDialog();
                }
            });
        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds the given Swing component as a new tab to the dialog.
     * @param tab  the options panel to add to the dialog.
     **/
    public void addOptionsTab(Component tab) {
        optionsTabbedPane.add(tab);
    }

    /**
     * Removes the given Swing component from this dialog's
     * tabbed pane.
     * @param tab  the options panel to remove from the dialog.
     **/
    public void removeOptionsTab(Component tab) {
        optionsTabbedPane.remove(tab);
    }

    /**
     * Removes all tabs from this dialog's tabbed pane.
     **/
    public void removeAllOptionTabs() {
        optionsTabbedPane.removeAll();
    }
}