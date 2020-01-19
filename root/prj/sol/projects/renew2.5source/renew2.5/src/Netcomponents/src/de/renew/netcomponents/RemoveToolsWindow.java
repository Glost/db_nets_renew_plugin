package de.renew.netcomponents;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;


/**
 * Provides the user with a window frame for selecting palettes
 * which can be removed from the menuFrame (Renew GUI).
 *
 * @see RemoveToolsControl
 * @author Lawrence Cabac
 */
public class RemoveToolsWindow extends JFrame {

    /**
     * The controller for the removin of ComponentsTools.
     */
    private RemoveToolsControl rtc;

    /**
     * The displayed List of the ComponentsTools.
     */
    private JList jList;

    public RemoveToolsWindow(Vector<ComponentsTool> v) {
        new RemoveToolsWindow(v, null);

    }

    /**
     * Generates the JFrame that displays the selecteble ComponentsTools in a JList.
     * @param v - The selecteble ComponentsTools.
     * @param rtc - The controller for the removin of ComponentsTools.
     */
    public RemoveToolsWindow(Vector<ComponentsTool> v, RemoveToolsControl rtc) {
        this.rtc = rtc;
        jList = new JList(v);


        //JPanel panel = new JPanel();
        //panel.add(jList);
        JScrollPane scrollPane = new JScrollPane(jList);

        JButton removeButton = new JButton("Remove palette(s)");
        removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeComponentsTools(jList.getSelectedValues());
                }
            });

        JButton cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });


        //        JButton selectAllButton = new JButton("Select all");
        //        selectAllButton.addActionListener(new ActionListener() {
        //            public void actionPerformed(ActionEvent e) {
        //                selectAll();
        //            }
        //        });
        GridBagConstraints gbc = null;

        this.getContentPane().setLayout(new GridBagLayout());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel l = new JLabel("Net-Component Tools");
        gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                                     GridBagConstraints.NORTHEAST,
                                     GridBagConstraints.NONE,
                                     new Insets(10, 10, 0, 5), 1, 1);
        this.getContentPane().add(l, gbc);

        gbc = new GridBagConstraints(0, 2, 2, 1, 1, 1,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(5, 5, 0, 5), 1, 1);
        this.getContentPane().add(scrollPane, gbc);

        gbc = new GridBagConstraints(0, 3, 1, 1, 0, 0,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 5, 5), 1, 1);
        this.getContentPane().add(removeButton, gbc);


        //        gbc = new GridBagConstraints(1, 3, 1, 1, 1, 0, gbc.NORTHEAST, gbc.NONE, 
        //                                     new Insets(5, 5, 5, 5), 1, 1);
        //        this.getContentPane().add(selectAllButton, gbc);
        gbc = new GridBagConstraints(1, 3, 1, 1, 1, 0,
                                     GridBagConstraints.NORTHEAST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 5, 5), 1, 1);
        this.getContentPane().add(cancelButton, gbc);


        this.pack();
        this.setVisible(true);
    }

    /**
     * Calls the controller to remove the selected items.
     * @param os - items selected by user.
     */
    public void removeComponentsTools(Object[] os) {
        //        logger.debug("Remove Button gedrueckt ");
        //        for (int i=0 ; i< os.length; i++){
        //            logger.debug("Selected: "+i+"  "+ os[i]);
        //        }
        if (os.length > 0) {
            rtc.removeFromList(os);
        }
    }

    /**
     * Updates the displayed list with a (modified) Vector.
     * @param v - the Vector to be displayed as a JList.
     */
    public void update(Vector<ComponentsTool> v) {
        jList.setListData(v);
        pack();
    }
}