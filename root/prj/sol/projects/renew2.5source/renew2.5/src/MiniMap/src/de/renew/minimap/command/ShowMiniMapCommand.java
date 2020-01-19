package de.renew.minimap.command;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.util.Command;

import de.renew.minimap.component.MiniMapFrame;

import de.renew.plugin.command.CLCommand;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.PrintStream;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
//import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;


// TODO: Auto-generated Javadoc
//import java.awt.BorderLayout;


/**
 * This command adds a JFrame with a MiniMap.
 *
 * @author Christian Roeder
 *
 */
public class ShowMiniMapCommand extends Command implements CLCommand {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                          .getLogger(ShowMiniMapCommand.class);
    public static final String COMMAND_NAME = "Show MiniMap...";

    //Frame containing the MiniMapFrame.
    private JFrame frame;

    //a menu item for the windows menu.
    private JMenuItem menuItem;

    /**
     * Instantiates a new show mini map command.
     */
    public ShowMiniMapCommand() {
        super(COMMAND_NAME);
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        showMiniMap();
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    @Override
    public void execute(final String[] args, final PrintStream response) {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        response.println("Show MiniMap.");
                        execute();
                    }
                });
        } catch (InterruptedException e) {
            // unexpected, but harmless.
        } catch (InvocationTargetException e) {
            response.println("Exception while showing MiniMap: "
                             + e.getTargetException());
            logger.error(e.getTargetException().toString(), e);
        }
    }

    /**
     * Gets the arguments.
     *
     * @return the arguments
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return null;
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Opens a window where commands can be executed.";
    }

    /**
     * Adds WindowsListeners to the MiniMap frame.
     */
    private void addListeners() {
        frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeMiniMapFrame();
                }
            });
    }

    /**
     * Adds an menu item for the MiniMap frame to the windows menu.
     */
    private void addMenuItemToWindowsMenu() {
        if (menuItem != null) {
            return;
        }
        menuItem = new JMenuItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bringToFront();
                }
            });
        menuItem.setText(COMMAND_NAME);
        MenuManager.getInstance().getWindowsMenu().add(menuItem);
    }

    /**
     * Bring the MiniMap frame to the front.
     */
    private void bringToFront() {
        if (frame != null) {
            frame.setVisible(true);
            frame.toFront();
        }
    }

    /**
     * Close MiniMap frame.
     */
    private void closeMiniMapFrame() {
        removeMenuItemFromWindowsMenu();
        destroyMiniMapFrame();
    }

    /**
     * Create the MiniMap frame.
     */
    private void createMiniMapFrame() {
        DrawingEditor drawingEditor = DrawPlugin.getCurrent().getDrawingEditor();

        if (drawingEditor instanceof DrawApplication) {
            MiniMapFrame miniMapFrame = new MiniMapFrame();
            miniMapFrame.setDrawApplication((DrawApplication) drawingEditor);
            frame = miniMapFrame;

        }
    }

    /**
     * Dispose the MiniMapFrame.
     */
    private void destroyMiniMapFrame() {
        frame.dispose();
        frame = null;
    }

    /**
     * Removes the menu item from the windows menu.
     */
    private void removeMenuItemFromWindowsMenu() {
        DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                  .remove(menuItem);
        menuItem = null;
    }

    /**
     * Show MiniMap.
     */
    private void showMiniMap() {
        if (frame != null) {
            destroyMiniMapFrame();
        }
        createMiniMapFrame();
        addMenuItemToWindowsMenu();
        addListeners();
    }
}