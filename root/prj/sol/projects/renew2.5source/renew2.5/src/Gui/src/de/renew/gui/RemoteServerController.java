package de.renew.gui;

import org.apache.log4j.Logger;

import CH.ifa.draw.DrawPlugin;

import de.renew.gui.RemoteServerWindow.WeakNetInstanceWrapper;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.RemoteServerRegistry;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * Displays and controls a window, where connections to remote servers can
 * be established, listed and terminated. The Swing part of this window is
 * defined in {@link RemoteServerWindow}.
 * @author Timo Carl
 * @since Renew 2.0
 * @see RemoteServerWindow
 **/
public class RemoteServerController {
    public static Logger logger = Logger.getLogger(RemoteServerController.class);

    /**
     * A reference to the application where net instance windows can be
     * opened.
     **/
    private CPNApplication _editor;

    /**
     * The Swing dialog associated with this window controller.
     **/
    private RemoteServerWindow _gui;

    /**
     * Creates a new <code>RemoteServerController</code> instance along
     * with its <code>RemoteServerWindow</code>. The window is not visible
     * until {@link #show} is called.
     *
     * @param editor the application where this window should belong to and
     *               where net instance windows can be opened.
     **/
    public RemoteServerController(CPNApplication editor) {
        _editor = editor;
        _gui = new RemoteServerWindow(this);
        _gui.addWindowListener(new WindowAdapter() {
                public final void windowClosed(final WindowEvent e) {
                    DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                              .removeFrame(_gui);
                }
            });
    }

    /**
     * Displays the window and updates its contents.
     **/
    public void show() {
        updateView();
        _gui.setVisible(true);
        _gui.setState(JFrame.NORMAL);
        _gui.toFront();
        DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                  .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, _gui);
    }

    /**
     * Called when the user chooses a server from the list of active
     * connections. Updates the window contents.
     **/
    public void doSelectServer() {
        updateView();
    }

    /**
     * Called when the user wants to connect to a remote server.
     * Establishes a connection to the server specified by the user,
     * if possible.
     **/
    public void doConnect() {
        String name = _gui._dialog._name.getText();
        String host = _gui._dialog._server.getText();
        RemoteServerRegistry.ServerDescriptor server = null;
        try {
            if (!"".equals(name) && !"".equals(host)) {
                server = RemoteServerRegistry.instance()
                                             .connectServer(host, name);
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            JOptionPane.showMessageDialog(_gui,
                                          "An error occurred while connecting to remote server.\nReason:\n"
                                          + e
                                          + "\nTry setting the property de.renew.remote.rmi-host-name to\n"
                                          + "the network-address you want to use.",
                                          "RMI-Problem",
                                          JOptionPane.ERROR_MESSAGE);
        }
        _gui._dialog.dispose();
        if (server != null) {
            // We need to update before choosing the new server entry
            // because the entry is not added to the combo box before.
            updateView();
            _gui._servers.setSelectedItem(server);
        }
        updateView();
    }

    /**
     * Called when the user wants to disconnect from the currently selected
     * server. Terminates the connection, if applicable.
     **/
    public void doDisconnect() {
        int index = _gui._servers.getSelectedIndex();
        if (index >= 1) {
            // We need to reduce the index by one because the local
            // nets are included in the combo box at first position.           
            RemoteServerRegistry.instance().removeServer(index - 1);
            updateView();
        }
    }

    /**
     * Called when the user requests to display a remote net instance.
     * Opens an instance drawing based on the selected net instance.
     **/
    public void openNetInstance() {
        int index = _gui._instancesList.getSelectedIndex();
        if (index >= 0) {
            Object netObj = _gui._instancesList.getModel().getElementAt(index);
            NetInstanceAccessor net = null;
            if (netObj instanceof NetInstanceAccessor) {
                net = (NetInstanceAccessor) netObj;
            } else if (netObj instanceof WeakNetInstanceWrapper) {
                net = ((WeakNetInstanceWrapper) netObj).getInstance();
                if (net == null) {
                    // Does this mean that the weak reference has been discarded?
                    logger.debug("Refreshing net list due to null reference...");
                    updateView();
                }
            } else {
                assert false : "Unexpected object type in net instance list: "
                + netObj.getClass();
            }
            if (net != null) {
                _editor.openInstanceDrawing(net);
            }
        }
    }

    /**
     * Refreshes all information displayed in the window.
     **/
    private void updateView() {
        try {
            _gui.updateView();
        } catch (RemoteException e) {
            logger.debug(e.getMessage(), e);
            JOptionPane.showMessageDialog(_gui,
                                          "An error occurred while calling remote objects.\nReason:\n"
                                          + e, "RMI-Problem",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}