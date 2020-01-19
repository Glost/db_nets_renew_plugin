/*
 * Created on 11.08.2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.config.PropertyPrinter;
import org.apache.log4j.spi.OptionHandler;

import CH.ifa.draw.DrawPlugin;

import de.renew.engine.common.SimulatorEventLogger;

import de.renew.gui.ConfigureEngineController;
import de.renew.gui.ConfigureSimulationTabController;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;


/**
 * An option panel controller to configure concurrent features of
 * the simulation engine.
 *
 * @author Sven Offermann
 **/
public class ConfigureLoggingController
        implements ConfigureSimulationTabController, TreeSelectionListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConfigureEngineController.class);
    private ConfigureLoggingTab tab;
    private HashMap<Appender, GenericAppenderEditor> appenders;

    public ConfigureLoggingController() {
        this.tab = new ConfigureLoggingTab(this);
        this.tab.setRootNode(createLoggerTreeNodes());
        this.appenders = new HashMap<Appender, GenericAppenderEditor>();
    }

    public Component getTab() {
        return tab;
    }

    private MutableTreeNode createLoggerTreeNodes() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("defined loggers");

        Enumeration<?> ls = LogManager.getCurrentLoggers();
        while (ls.hasMoreElements()) {
            Logger logger = (Logger) ls.nextElement();
            if (logger.getName().startsWith(SimulatorEventLogger.SIM_LOG_PREFIX)) {
                DefaultMutableTreeNode loggerNode = new DefaultMutableTreeNode(new TreeNodeLoggerWrapper(logger));

                boolean hasAppender = false;
                Enumeration<?> as = logger.getAllAppenders();
                while (as.hasMoreElements()) {
                    hasAppender = true;
                    Appender a = (Appender) as.nextElement();
                    if (!(a instanceof org.apache.log4j.varia.NullAppender)) {
                        DefaultMutableTreeNode aNode = new DefaultMutableTreeNode(new TreeNodeAppenderWrapper(logger,
                                                                                                              a));
                        loggerNode.add(aNode);
                    }
                }

                if (hasAppender) {
                    root.add(loggerNode);
                }
            }
        }

        return root;
    }

    public void commitTab(Properties props) {
        Enumeration<?> ls = LogManager.getCurrentLoggers();
        while (ls.hasMoreElements()) {
            Logger logger = (Logger) ls.nextElement();
            if (logger.getName().startsWith(SimulatorEventLogger.SIM_LOG_PREFIX)) {
                Enumeration<?> as = logger.getAllAppenders();
                while (as.hasMoreElements()) {
                    Appender a = (Appender) as.nextElement();
                    GenericAppenderEditor ae = appenders.get(a);
                    if (ae != null) {
                        ae.applyChanges();
                        if (a instanceof OptionHandler) {
                            ((OptionHandler) a).activateOptions();
                            Logger.getLogger(logger.getName()).addAppender(a);
                        }
                    }
                }
            }
        }
    }

    public void updateTab(Properties props) {
        this.appenders = new HashMap<Appender, GenericAppenderEditor>();
        updateTab();
    }

    private void updateTab() {
        this.tab.setRootNode(createLoggerTreeNodes());
    }

    protected ActionListener createAddLoggerAction() {
        return new AddLoggerAction();
    }

    protected ActionListener createRemoveLoggerAction(String loggerName) {
        return new RemoveLoggerAction(loggerName);
    }

    protected ActionListener createAddAppenderAction(String loggerName,
                                                     String appenderTypeName) {
        return new AddAppenderAction(loggerName, appenderTypeName);
    }

    protected ActionListener createRemoveAppenderAction(String loggerName,
                                                        Appender appender) {
        return new RemoveAppenderAction(loggerName, appender);
    }

    protected ActionListener createExportConfigurationAction() {
        return new ExportConfigurationAction();
    }


    // Implementation of TreeSelectionListener
    public void valueChanged(TreeSelectionEvent e) {
        Object selected = ((DefaultMutableTreeNode) e.getPath()
                                                     .getLastPathComponent())
                              .getUserObject();
        if (selected instanceof TreeNodeAppenderWrapper) {
            Appender appender = ((TreeNodeAppenderWrapper) selected).getAppender();
            if (!appenders.containsKey(appender)) {
                appenders.put(appender, new GenericAppenderEditor(appender));
            }
            JScrollPane rightComponent = new JScrollPane(appenders.get(appender));
            rightComponent.setMinimumSize(new Dimension(300, 200));
            tab.setRightSide(rightComponent);

        }
    }

    // Inner action classes.
    private class AddLoggerAction implements ActionListener {
        public AddLoggerAction() {
        }

        public void actionPerformed(ActionEvent e) {
            boolean found = false;
            Logger logger = null;

            NewLoggerDialog newLoggerDialog = new NewLoggerDialog(getParentDialog());
            newLoggerDialog.setVisible(true);
            if (newLoggerDialog.isCommitted()) {
                while (!found) {
                    String loggerName = SimulatorEventLogger.SIM_LOG_PREFIX
                                        + "." + newLoggerDialog.getLogger();

                    logger = Logger.getLogger(loggerName);
                    Enumeration<?> enumeration = logger.getAllAppenders();
                    if (!enumeration.hasMoreElements()) {
                        found = true;
                    }
                }
            }

            //NOTICEnull
            if (logger != null) {
                logger.addAppender(new org.apache.log4j.varia.NullAppender());
            }

            updateTab();
        }

        private Dialog getParentDialog() {
            Component component = tab;
            while (!(component instanceof Dialog)) {
                component = component.getParent();
            }
            return (Dialog) component;
        }
    }

    private class RemoveLoggerAction implements ActionListener {
        private String loggerName;

        public RemoveLoggerAction(String loggerName) {
            this.loggerName = loggerName;
        }

        public void actionPerformed(ActionEvent e) {
            // get logger and remove all appenders;
            Logger logger = Logger.getLogger(loggerName);
            logger.removeAllAppenders();

            updateTab();
        }
    }

    private class AddAppenderAction implements ActionListener {
        private String loggerName;
        private String appenderTypeName;

        public AddAppenderAction(String loggerName, String appenderTypeName) {
            this.loggerName = loggerName;
            this.appenderTypeName = appenderTypeName;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                Appender appender = AppenderFactory.getInstance()
                                                   .createNewAppender(appenderTypeName);

                Logger.getLogger(loggerName).addAppender(appender);

                updateTab();
            } catch (Exception ex) {
                logger.error("Can't create new appender instance of appender type "
                             + appenderTypeName);
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private class RemoveAppenderAction implements ActionListener {
        private String loggerName;
        private Appender appender;

        public RemoveAppenderAction(String loggerName, Appender appender) {
            this.loggerName = loggerName;
            this.appender = appender;
        }

        public void actionPerformed(ActionEvent e) {
            Logger.getLogger(loggerName).removeAppender(appender);

            updateTab();
        }
    }

    private class ExportConfigurationAction implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            File propertiesOutputFile = promptGenerateLocation();

            if (propertiesOutputFile != null) {
                // If file does not exist, try to create a new one.
                if (!propertiesOutputFile.exists()) {
                    try {
                        propertiesOutputFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                PrintWriter pw;
                try {
                    pw = new PrintWriter(propertiesOutputFile);
                    PropertyPrinter pp = new PropertyPrinter(pw);
                    pp.print(pw);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Prompt the user for a location where to generate the KBFiles to.
         * @return
         */
        private File promptGenerateLocation() {
            File file = new File(DrawPlugin.getCurrent().getIOHelper()
                                           .getLastPath(), "/");

            JFileChooser dialog = new JFileChooser();
            dialog.setCurrentDirectory(file);
            //dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dialog.setDialogTitle("Select a destination for the generated log4j configuration file.");

            if (dialog.showSaveDialog(DrawPlugin.getGui().getFrame()) == JFileChooser.APPROVE_OPTION) {
                file = dialog.getSelectedFile();
                DrawPlugin.getCurrent().getIOHelper()
                          .setLastPath(file.getParentFile());
            } else {
                return null;
            }
            return file;
        }
    }
}