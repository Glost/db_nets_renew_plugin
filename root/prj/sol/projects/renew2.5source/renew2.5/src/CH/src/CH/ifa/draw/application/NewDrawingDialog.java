package CH.ifa.draw.application;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;


/**
 * A dialog to chosse the type of a new drawing.
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class NewDrawingDialog extends JDialog {
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private JLabel typeLabel;
    private JList typeList;
    private int result = -1;

    public NewDrawingDialog(JFrame parent, String[] types, int selectedIndex) {
        super(parent, true);
        initComponents();
        typeList.setListData(types);
        if (selectedIndex < types.length && selectedIndex >= 0) {
            typeList.setSelectedIndex(selectedIndex);
        }
        pack();
    }

    private void initComponents() {
        buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        typeLabel = new JLabel();
        typeList = new JList();

        getContentPane().setLayout(new BorderLayout());

        setTitle("Choose new drawing type");
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    closeDialog();
                }
            });

        typeLabel.setText("Available drawing types:");
        getContentPane().add(typeLabel, BorderLayout.NORTH);

        typeList.setToolTipText("Choose the type of the drawing to create");
        getContentPane().add(typeList, BorderLayout.CENTER);
        typeList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        commitDialog();
                    }
                }
            });

        okButton.setText("New");
        okButton.setToolTipText("Creates a new drawing of the chosen type.");
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    commitDialog();
                }
            });
        buttonPanel.add(okButton);

        cancelButton.setToolTipText("Cancels the dialog without creating a drawing.");
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    closeDialog();
                }
            });
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void commitDialog() {
        result = typeList.getSelectedIndex();
        setVisible(false);
    }

    private void closeDialog() {
        result = -1;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}