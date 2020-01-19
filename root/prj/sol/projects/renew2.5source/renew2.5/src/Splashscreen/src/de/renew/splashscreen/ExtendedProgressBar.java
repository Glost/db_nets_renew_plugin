package de.renew.splashscreen;

import de.renew.plugin.load.IExtendedProgressBar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * This class creates an extended progress bar as singleton.<br>
 * It provides a textual overview of currently loaded plug-ins and a progress
 * bar to visualize the overall loading process.
 *
 * @author Eva Mueller
 * @date Nov 27, 2010
 * @version 0.2
 * @update Jan 23, 2012 Dominic Dibbern
 */
public class ExtendedProgressBar extends JPanel implements IExtendedProgressBar {
    private static final String loadedInfoScrollPane = "loadedInfoScrollPane";
    private static final String loadedInfoTextArea = "loadedInfoTextArea";
    private JProgressBar progressBar;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private static IExtendedProgressBar iExtendedProgressBar;

    /**
     * Private constructor to create singleton instance of this class.<br>
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    private ExtendedProgressBar() {
        if (iExtendedProgressBar == null) {
            init();
        }
    }

    /**
     * Get (singleton) instance of the {@link ExtendedProgressBar}.
     *
     * @return [{@link ExtendedProgressBar}]
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public synchronized static IExtendedProgressBar getInstance() {
        if (iExtendedProgressBar == null) {
            iExtendedProgressBar = new ExtendedProgressBar();
        }
        return iExtendedProgressBar;
    }

    /**
     * Create {@link JTextArea} nested within a {@link JScrollPane} for the<br>
     * textual representation of the loaded plug-ins.<br>
     * Additionally, a {@link JProgressBar} is created to visualize the overall<br>
     * plug-in loading process.
     */
    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        textArea = new JTextArea(10, 30);
        textArea.setName(loadedInfoTextArea);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setName(loadedInfoScrollPane);
        scrollPane.setAutoscrolls(true);
        textArea.setEditable(false);
        Font font = new Font("Arial", Font.BOLD, 14);
        textArea.setFont(font);
        textArea.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        add(scrollPane);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        setOpaque(false);
        setBackground(Color.WHITE);
        progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(progressBar);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.renew.plugin.splashscreen.IExtendedProgressBar#propertyChange(java
     * .beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();

            if (progress > 100) {
                progress = 100;
            }
            progressBar.setValue(progress);
            progressBar.validate();
        }
        if ("pluginLoaded" == evt.getPropertyName()) {
            String tmp = "Loaded plugin : " + evt.getNewValue() + "\n";
            textArea.append(tmp);
            textArea.setCaretPosition(textArea.getCaretPosition()
                                      + tmp.length());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.plugin.splashscreen.IExtendedProgressBar#getValue()
     */
    public int getValue() {
        return progressBar.getValue();
    }

    @Override
    public void close() {
        RenewSplashScreen.getInstance().closeSplashScreen();
    }
}