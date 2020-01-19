package CH.ifa.draw.standard;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;

import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Generic info dialog with an OK-button (modal)
 *
 * 05.10.2000 HR
 *
 */
public class InfoDialog extends JDialog {
    private Logger logger = Logger.getLogger(InfoDialog.class);
    protected JButton button;
    protected MultiLineLabel label;

    public InfoDialog(JFrame parent, String title, String message) {
        super(parent, title, true);

        getContentPane().setLayout(new BorderLayout(15, 15));

        label = new MultiLineLabel(message, 20, 20);

        getContentPane().add("Center", label);

        button = new JButton("OK");
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InfoDialog.this.dispose();
            }
        };
        button.addActionListener(listener);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        p.add(button);
        getContentPane().add("South", p);

        pack();
    }

    /**
     * Get InfoDialog with JLabel
     *
     * @param title
     * @param parent
     * @param message
     */
    public InfoDialog(JFrame parent, String title, String message,
                      boolean addRenewLink) {
        super(parent, title, true);

        try {
            getContentPane().setLayout(new BorderLayout(15, 15));
            JLabel jLabel = new JLabel("<html>" + message + "</html>");
            jLabel.setOpaque(true);

            JPanel aboutPanel = new JPanel();
            aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));
            aboutPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            aboutPanel.add(jLabel);
            aboutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            aboutPanel = createLinkLabel(aboutPanel, "http://www.renew.de",
                                         false);
            if (addRenewLink) {
                aboutPanel = createLinkLabel(aboutPanel,
                                             "http://www.paose.net", false);
            }
            aboutPanel = createLinkLabel(aboutPanel, "mailto:support@renew.de",
                                         true);

            getContentPane().add("Center", aboutPanel);

            button = new JButton("OK");
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InfoDialog.this.dispose();
                }
            };
            button.addActionListener(listener);
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
            p.add(button);
            getContentPane().add("South", p);

            pack();
        } catch (Exception e) {
            logger.error("InfoDialog " + e);
        }
    }

    private JPanel createLinkLabel(JPanel helpPanel, final String url,
                                   final boolean mail) {
        final String linktitle;
        final String linkaddress;

        if (mail) {
            if (url.startsWith("mailto:")) {
                linktitle = url.replaceFirst("mailto:", "");
                linkaddress = url;
            } else {
                linktitle = url;
                linkaddress = "mailto:" + url;
            }
        } else {
            linktitle = url;
            linkaddress = url;
        }

        JLabel pageurl = new JLabel("<html><a href=\"" + linkaddress + "\">"
                                    + linktitle + "</a></html>");
        pageurl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pageurl.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() > 0) {
                        openURL(linkaddress, mail);
                    }
                }
            });
        helpPanel.add(pageurl);
        return helpPanel;
    }

    private void openURL(String url, boolean mail) {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = URI.create(url);

            if (mail) {
                desktop.mail(uri);
            } else {
                desktop.browse(uri);
            }
        } catch (IOException e) {
            logger.error(InfoDialog.class.getSimpleName()
                         + ": could not open link " + url + ".");
            if (logger.isDebugEnabled()) {
                logger.debug(InfoDialog.class.getSimpleName() + ": " + e);
            }
        }
    }
}