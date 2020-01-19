/*
 * Created on 19. August 2003, 12:35
 */
package de.renew.gui.logging;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 *
 * @author Sven Offermann
 **/
public class NewLoggerDialog extends JDialog {
    private boolean commit = false;
    private JLabel loggerLabel;
    private JButton okButton;
    private JTextField loggerField;
    private JButton cancelButton;

    /** Creates new form EditPathEntryDialog */
    public NewLoggerDialog(Dialog parent) {
        super(parent, "new logger", true);
        initComponents();
        pack();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     **/
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        loggerLabel = new JLabel();
        loggerField = new JTextField();
        okButton = new JButton();
        cancelButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    cancelDialog();
                }
            });

        loggerLabel.setText("Logger name:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(loggerLabel, gridBagConstraints);

        loggerField.setText("netname.elementname");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(loggerField, gridBagConstraints);

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

    public String getLogger() {
        return loggerField.getText();
    }

    public void setLogger(String logger) {
        loggerField.setText(logger);
    }
}