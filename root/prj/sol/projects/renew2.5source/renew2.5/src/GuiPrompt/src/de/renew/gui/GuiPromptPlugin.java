package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;

import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.command.CLCommand;

import de.renew.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * @author 6schumac
 *
 */
public class GuiPromptPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(GuiPromptPlugin.class);

    // MenuExtender menuExtender;
    JMenuItem _menu;
    PromptDialog _promptDialog;

    public GuiPromptPlugin(URL location) throws PluginException {
        super(location);
    }

    public GuiPromptPlugin(PluginProperties props) {
        super(props);
    }

    public void init() {
        _menu = createMenu();
        DrawPlugin.getCurrent().getMenuManager()
                  .registerMenu(DrawPlugin.PLUGINS_MENU, _menu);
        PluginManager.getInstance()
                     .addCLCommand("guiprompt", new GuiPromptCommand());
    }

    public boolean cleanup() {
        PluginManager.getInstance().removeCLCommand("guiprompt");

        DrawPlugin current = DrawPlugin.getCurrent();
        if (current != null) {
            current.getMenuManager().unregisterMenu(_menu);
        }

        if (_promptDialog != null) {
            _promptDialog.dispose();
            _promptDialog = null;
        }
        return true;
    }

    /**
     * Creates a menu item that creates and shows the gui prompt dialog
     * when chosen.
     *
     * @return  the created menu item.
     **/
    private JMenuItem createMenu() {
        JMenuItem mi = new JMenuItem("Show Gui prompt...");
        mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showPromptDialog();
                }
            });
        mi.putClientProperty(MenuManager.ID_PROPERTY,
                             getProperties().getProperty("requires"));
        return mi;
    }

    /**
     * Creates and shows the prompt dialog, if not already visible.
     * <p>
     * This method must be called within the AWT event thread.
     * </p>
     **/
    private void showPromptDialog() {
        if (_promptDialog == null) {
            _promptDialog = new PromptDialog();
        }
        _promptDialog.setVisible(true);
    }

    // ------------------------------------------- private classes --------


    /**
     * A Swing dialog that allows to enter commands, execute them and view
     * the results.
     **/
    private class PromptDialog extends JFrame {
        JTextField _inputField = new JTextField(30);
        private JEditorPane _responseArea;
        private PrintStream _responseStream;
        private ResponseDocument _responseDocument;

        public PromptDialog() {
            super("Command Input");

            JButton execute = new JButton("execute");
            JButton clear = new JButton("clear");

            execute.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        executeCommand();
                    }
                });
            clear.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        clearFields();
                    }
                });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(_inputField);
            buttonPanel.add(execute);
            buttonPanel.add(clear);


            _responseArea = new JEditorPane();
            _responseArea.setEditable(false);
            setupResponseDocument();

            JRootPane root = getRootPane();
            root.setLayout(new BorderLayout());
            root.add(buttonPanel, BorderLayout.NORTH);
            root.add(new JScrollPane(_responseArea), BorderLayout.CENTER);

            addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        logger.debug("window closing.");
                        _responseStream.close();
                        if (_responseDocument != null) {
                            _responseDocument.terminate();
                            _responseDocument = null;
                        }
                        _promptDialog = null;
                        _responseStream = null;
                        dispose();
                    }
                });
            _inputField.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            executeCommand();
                        }
                    }
                });

            pack();
        }

        private void executeCommand() {
            try {
                String input = _inputField.getText();
                Map<String, CLCommand> commands = PluginManager.getInstance()
                                                               .getCLCommands();
                String[] cl = StringUtil.splitStringWithEscape(input);
                if (cl.length == 0) {
                    return;
                }
                final CLCommand c = commands.get(cl[0]);
                if (c == null) {
                    _responseStream.println("unknown command: " + cl[0]);
                } else {
                    final String[] nc = new String[cl.length - 1];
                    for (int i = 0; i < nc.length; i++) {
                        nc[i] = cl[i + 1];
                    }
                    logger.debug("GuiPrompt: scheduling command: " + input);
                    _responseStream.println(">" + input);
                    new Thread() {
                            public void run() {
                                _responseDocument.revive();
                                c.execute(nc, _responseStream);
                            }
                        }.start();
                }
            } catch (RuntimeException e) {
                _responseStream.println("GuiPrompt: an exeption occurred: " + e);
                logger.error(e.getMessage(), e);
            }
        }

        private void clearFields() {
            _inputField.setText("");
            setupResponseDocument();
        }

        private void setupResponseDocument() {
            // Set up the response stream pipe.
            try {
                PipedOutputStream pipe = new PipedOutputStream();
                _responseStream = new PrintStream(pipe, true);
                _responseDocument = new ResponseDocument(pipe);
                _responseArea.setDocument(_responseDocument);
                _responseStream.println("Execution feedback:\n");
            } catch (IOException e) {
                _responseDocument = null;
                _responseStream = System.out;
                _responseArea.setDocument(_responseArea.getEditorKit()
                                                       .createDefaultDocument());
                _responseArea.setText("Feedback stream could not be established: "
                                      + e.toString()
                                      + "\nFallback to console output.");
                logger.error(e.toString(), e);
            }
        }

        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                registerExitBlock();
                DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                          .addFrame(DrawPlugin.WINDOWS_CATEGORY_TOOLS, this);
            }
        }

        public void dispose() {
            DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                      .removeFrame(this);
            registerExitOk();
            super.dispose();
        }
    }

    /**
     * A command line command to create and show the prompt dialog.
     **/
    private class GuiPromptCommand implements CLCommand {
        public String getDescription() {
            return "Opens a window where commands can be executed.";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return null;
        }

        public void execute(final String[] args, final PrintStream response) {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            showPromptDialog();
                            response.println("Prompt dialog now visible.");
                        }
                    });
            } catch (InterruptedException e) {
                // unexpected, but harmless.
            } catch (InvocationTargetException e) {
                response.println("Exception while showing prompt dialog: "
                                 + e.getTargetException());
                logger.error(e.getTargetException().toString(), e);
            }
        }
    }
}