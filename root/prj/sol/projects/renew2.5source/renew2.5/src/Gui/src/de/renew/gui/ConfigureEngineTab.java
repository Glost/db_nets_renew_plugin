/*
 * Created on 16.05.2003
 *
 */
package de.renew.gui;

import de.renew.plugin.BottomClassLoader;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;


/**
 * A Swing panel to configure concurrent features of the
 * simulation engine.
 * @author Michael Duvigneau
 **/
public class ConfigureEngineTab extends JPanel {
    private JCheckBox sequentialBox;
    private JLabel multiplicityLabel;
    private JTextField multiplicityField;
    private JCheckBox classReinitBox;
    private JCheckBox eagerSimulationBox;
    private JLabel priorityLabel;
    private JTextField priorityField;

    //NOTICEsignature
    public ConfigureEngineTab(ConfigureEngineController controller) {
        super(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        setName("Engine");

        sequentialBox = new JCheckBox();
        multiplicityLabel = new JLabel();
        multiplicityField = new JTextField();
        priorityField = new JTextField();
        priorityLabel = new JLabel();
        classReinitBox = new JCheckBox();
        eagerSimulationBox = new JCheckBox();

        sequentialBox.setText("Sequential mode");
        sequentialBox.setToolTipText("If activated, transitions will be restricted to sequential firing.\n"
                                     + "This is needed for some arc types and formalisms.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(sequentialBox, gridBagConstraints);

        multiplicityLabel.setText("Multiplicity: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(multiplicityLabel, gridBagConstraints);

        multiplicityField.setToolTipText("Enter the number of concurrent binding search threads.\n"
                                         + "(Values different from 1 are experimental!)");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        add(multiplicityField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        add(new JSeparator(), gridBagConstraints);

        classReinitBox.setText("Class reinit mode");
        classReinitBox.setToolTipText("If activated, custom classes (loaded from "
                                      + BottomClassLoader.CLASSPATH_PROP_NAME
                                      + ") will be reloaded on each simulation run.\n"
                                      + "(This is an experimental feature!).");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(classReinitBox, gridBagConstraints);

        eagerSimulationBox.setText("Eager simulation mode");
        eagerSimulationBox.setToolTipText("If activated, the simulator will not wait "
                                          + "for the graphical animation.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(eagerSimulationBox, gridBagConstraints);

        priorityLabel.setText("Simulation Priority (1-10): ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(priorityLabel, gridBagConstraints);

        priorityField.setToolTipText("Enter the simulations thread priority.\n"
                                     + "(Values range from 1 to 10. 10 being the highest)");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        add(priorityField, gridBagConstraints);
    }

    public boolean getSequential() {
        return sequentialBox.getModel().isSelected();
    }

    public void setSequential(boolean state) {
        sequentialBox.setSelected(state);
    }

    public String getMultiplicity() {
        return multiplicityField.getText();
    }

    public void setMultiplicity(String multiplicity) {
        multiplicityField.setText(multiplicity);
    }

    public String getPriority() {
        return priorityField.getText();
    }

    public void setPriority(String priority) {
        priorityField.setText(priority);
    }

    public boolean getClassReinit() {
        return classReinitBox.getModel().isSelected();
    }

    public void setClassReinit(boolean state) {
        classReinitBox.setSelected(state);
    }

    public boolean getEagerSimulation() {
        return eagerSimulationBox.getModel().isSelected();
    }

    public void setEagerSimulation(boolean state) {
        eagerSimulationBox.setSelected(state);
    }
}