package de.renew.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * A Swing panel to configure remote access properties.
 * @author Michael Duvigneau
 **/
class ConfigureRemoteAccessTab extends JPanel {
    private JPanel detailOptionsPanel;
    private JLabel serverClassLabel;
    private JTextField serverClassField;
    private JCheckBox enabledBox;
    private JLabel publicNameLabel;
    private JTextField publicNameField;
    private JLabel socketFactoryLabel;
    private JTextField socketFactoryField;
    private final ConfigureRemoteAccessController controller;

    public ConfigureRemoteAccessTab(ConfigureRemoteAccessController controller) {
        super(new BorderLayout());
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        setName("Remote Access");

        enabledBox = new JCheckBox();
        detailOptionsPanel = new JPanel();
        publicNameLabel = new JLabel();
        publicNameField = new JTextField();
        serverClassLabel = new JLabel();
        serverClassField = new JTextField();
        socketFactoryLabel = new JLabel();
        socketFactoryField = new JTextField();

        enabledBox.setText("Enable remote access");
        enabledBox.setToolTipText("When checked, the local simulation will be open for remote access.\n"
                                  + "You will need a running rmiregistry on the local host.");
        enabledBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    controller.enabledStateChanged();
                }
            });

        add(enabledBox, BorderLayout.NORTH);

        detailOptionsPanel.setLayout(new GridBagLayout());
        detailOptionsPanel.setBorder(new TitledBorder("Advanced options"));

        publicNameLabel.setText("Public name");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        detailOptionsPanel.add(publicNameLabel, gridBagConstraints);

        publicNameField.setToolTipText("The simulation will be published under this name.\n"
                                       + "This way, independent simulation environments on one host can be distinguished.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        detailOptionsPanel.add(publicNameField, gridBagConstraints);

        serverClassLabel.setText("Server class");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        detailOptionsPanel.add(serverClassLabel, gridBagConstraints);

        serverClassField.setToolTipText("If you need a special remote server implementation, "
                                        + "enter its full qualified classname here.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        detailOptionsPanel.add(serverClassField, gridBagConstraints);

        socketFactoryLabel.setText("Socket factory");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        detailOptionsPanel.add(socketFactoryLabel, gridBagConstraints);

        socketFactoryField.setToolTipText("Can only be set at startup time! "
                                          + "If you need a special RMI socket factory, "
                                          + "enter its full qualified classname here. "
                                          + "Leave empty to use the default factory.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        detailOptionsPanel.add(socketFactoryField, gridBagConstraints);

        add(detailOptionsPanel, BorderLayout.CENTER);
    }

    /**
     * Enables or disables the detailed options group of this tab.
     *
     * @param state whether to make the details options available
     *              to the user.
     **/
    public void enableDetailOptions(boolean state) {
        detailOptionsPanel.setEnabled(state);
        publicNameLabel.setEnabled(state);
        publicNameField.setEnabled(state);
        serverClassLabel.setEnabled(state);
        serverClassField.setEnabled(state);
        // The socket factory can only be changed at startup.
        socketFactoryLabel.setEnabled(false);
        socketFactoryField.setEnabled(false);

    }

    public void setRemoteEnabled(boolean enable) {
        enabledBox.setSelected(enable);
    }

    public boolean getRemoteEnabled() {
        return enabledBox.isSelected();
    }

    public void setServerClass(String className) {
        serverClassField.setText(className);
    }

    public String getServerClass() {
        return serverClassField.getText();
    }

    public void setPublicName(String publicName) {
        publicNameField.setText(publicName);
    }

    public String getPublicName() {
        return publicNameField.getText();
    }

    public void setSocketFactory(String className) {
        socketFactoryField.setText(className);
    }

    public String getSocketFactory() {
        return socketFactoryField.getText();
    }
}