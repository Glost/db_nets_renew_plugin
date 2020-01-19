/*
 * @(#)ToolButton.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.util.GUIProperties;
import CH.ifa.draw.util.PaletteListener;

import de.renew.plugin.PluginProperties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;


/**
 * A PaletteButton that is associated with a tool.
 * @see Tool
 */
public class ToolButton implements ActionListener, MouseListener, KeyListener {
    private static final String PROP_PRE = "de.renew.keymap.";
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ToolButton.class);
    private String fName;
    private Tool fTool;
    private JToggleButton fButton;
    private PaletteListener fListener;
    private char keyChar;

    public ToolButton(PaletteListener listener, String iconName, String name,
                      Tool tool) {
        ImageIcon[] icons = createIconsByGuess(iconName);
        createButton(listener, icons[0], icons[1], name, tool);
        associateKey(name.replaceAll(" ", "_"));
    }

    public ToolButton(PaletteListener listener, Icon icon, Icon selectedIcon,
                      String name, Tool tool) {
        createButton(listener, icon, selectedIcon, name, tool);
        associateKey(name.replaceAll(" ", "_"));
    }

    private void associateKey(String modifiedName) {
        DrawPlugin app = DrawPlugin.getCurrent();
        if (app != null) {
            PluginProperties props = app.getProperties();
            if (props.getBoolProperty(PROP_PRE + "use-mapping")) {
                String key = props.getProperty(PROP_PRE + modifiedName);
                if (key != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(ToolButton.class.getName()
                                     + ": found key mapping entry: " + key);
                    }
                    if (key.trim().length() == 1) {
                        //assumption: if a tool button is created the gui exists
                        DrawPlugin.getGui().getFrame().addKeyListener(this);
                        keyChar = key.trim().charAt(0);
                    }
                }
            }
        }
    }

    private void createButton(PaletteListener listener, Icon icon,
                              Icon selectedIcon, String name, Tool tool) {
        fButton = new JToggleButton();
        fButton.setIcon(icon);
        fButton.setToolTipText(name);
        if (selectedIcon != null) {
            fButton.setSelectedIcon(selectedIcon);
        }
        fButton.addMouseListener(this);
        fButton.addActionListener(this);
        fButton.setToolTipText(fName);
        if (GUIProperties.customToolBorders()) {
            fButton.setBorderPainted(true);
            fButton.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        fTool = tool;
        fName = name;
        fListener = listener;
    }

    public Tool tool() {
        return fTool;
    }

    public void setTool(Tool tool) {
        fTool = tool;
    }

    public String name() {
        return fName;
    }

    public String getHotkey() {
        if (Character.isLetterOrDigit(keyChar) || keyChar == '.') {
            return " (" + keyChar + ")";
        }
        return "";
    }

    public Object attributeValue() {
        return tool();
    }

    public AbstractButton button() {
        return fButton;
    }

    public void mouseClicked(MouseEvent e) {
        boolean doubleClick = (e.getClickCount() > 1);
        fListener.paletteUserSelected(this, doubleClick);
    }

    public void mouseEntered(MouseEvent e) {
        fListener.paletteUserOver(this, true);
    }

    public void mouseExited(MouseEvent e) {
        fListener.paletteUserOver(this, false);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public final void actionPerformed(ActionEvent e) {
        fListener.paletteUserSelected(this, false);
    }

    private static ImageIcon[] createIconsByGuess(final String name) {
        ImageIcon[] result = new ImageIcon[2];
        ImageIcon firstGuess;
        try {
            firstGuess = createIcon(name + ".gif");
        } catch (Exception e) {
            logger.error("Guess for " + name + ".gif failed with " + e);
            firstGuess = null;
        }
        if (firstGuess != null) {
            result[0] = firstGuess;
            result[1] = null;
        } else {
            result[0] = createIcon(name + "1.gif");
            result[1] = createIcon(name + "2.gif");
        }
        return result;
    }

    private static ImageIcon createIcon(String name) {
        //        logger.debug("icon " + name + " "
        //                           + System.getProperty("user.dir"));
        URL url = ToolButton.class.getResource(name);
        if (url == null) {
            File icon = new File(name);
            if (!icon.isFile()) {
                return null;
            }
            try {
                url = icon.toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return new ImageIcon(url);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        if (!e.isControlDown() && !e.isAltDown() && !e.isMetaDown()) {
            if (e.getKeyChar() == keyChar) {
                fListener.paletteUserSelected(this, false);
            } else if (e.getKeyChar() == Character.toUpperCase(keyChar)) {
                fListener.paletteUserSelected(this, true);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}