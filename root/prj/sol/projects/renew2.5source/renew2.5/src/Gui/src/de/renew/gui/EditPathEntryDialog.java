/*
 * EditPathEntryDialog.java
 *
 * Created on 19. August 2003, 12:35
 */
package de.renew.gui;

import de.renew.util.PathEntry;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 *
 * @author Michael Duvigneau
 **/
public class EditPathEntryDialog extends JDialog {
    private boolean commit = false;
    private JLabel pathLabel;
    private JCheckBox useClasspathBox;
    private JButton okButton;
    private JTextField pathField;
    private JButton cancelButton;

    /** Creates new form EditPathEntryDialog */
    public EditPathEntryDialog(JDialog parent, String okText) {
        super(parent, okText + " path entry", true);
        initComponents();
        if (okText != null) {
            okButton.setText(okText);
        }
        pack();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     **/
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        pathLabel = new JLabel();
        pathField = new JTextField();
        useClasspathBox = new JCheckBox();
        okButton = new JButton();
        cancelButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    cancelDialog();
                }
            });

        pathLabel.setText("Path:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(pathLabel, gridBagConstraints);

        pathField.setText("/this/is/a/rather/long/path/name/for/unix/systems/with/slashes");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(pathField, gridBagConstraints);

        useClasspathBox.setText("relative to classpath");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(useClasspathBox, gridBagConstraints);

        okButton.setText("OK");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    commitDialog();
                }
            });
        getContentPane().add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelDialog();
                }
            });
        getContentPane().add(cancelButton, gridBagConstraints);
    }

    private void commitDialog() {
        commit = true;
        setVisible(false);
        dispose();
    }

    private void cancelDialog() {
        commit = false;
        setVisible(false);
        dispose();
    }

    public boolean isCommitted() {
        return commit;
    }

    public PathEntry getEntry() {
        return new PathEntry(pathField.getText(), useClasspathBox.isSelected());
    }

    public void setEntry(PathEntry entry) {
        useClasspathBox.setSelected(entry.isClasspathRelative);
        pathField.setText(entry.path);
    }
}