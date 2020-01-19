/*
 *
 */
package de.renew.minimap.component;

import CH.ifa.draw.application.DrawApplication;

import java.awt.GridLayout;
import java.awt.HeadlessException;

import javax.swing.JFrame;


/**
 * The Class MiniMapFrame.
 *
 * @author Christian Roeder
 */
public class MiniMapFrame extends JFrame {
    private final String TITLE = "MiniMap";

    /**
     * Instantiates a new mini map frame.
     *
     * @throws HeadlessException the headless exception
     */
    public MiniMapFrame() throws HeadlessException {
        super();
        setTitle(TITLE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
    }

    /**
     * Sets the draw application.
     *
     * @param drawApplication the new draw application
     */
    public void setDrawApplication(DrawApplication drawApplication) {
        MiniMapPanel miniMapPanel = new MiniMapPanel(drawApplication);
        miniMapPanel.setVisible(true);
        add(miniMapPanel);
        addComponentListener(miniMapPanel);
        pack();
        setVisible(true);
    }
}