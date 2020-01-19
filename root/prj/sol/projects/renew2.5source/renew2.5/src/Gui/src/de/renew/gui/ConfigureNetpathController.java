package de.renew.gui;

import de.renew.util.PathEntry;

import java.awt.Component;

import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.ListSelectionModel;


/**
 * An option panel controller to configure remote access properties.
 * @author Michael Duvigneau
 */
class ConfigureNetpathController implements ConfigureSimulationTabController {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConfigureNetpathController.class);
    private ConfigureNetpathTab tab;
    private NetpathModel model = null;

    public ConfigureNetpathController() {
        this.tab = new ConfigureNetpathTab(this);
    }

    public Component getTab() {
        return tab;
    }

    public void commitTab(Properties props) {
        if (model != null) {
            String newPath = model.asPath();
            props.setProperty("de.renew.netPath", newPath);
            logger.debug("ConfigureNetpathController: " + "Configured netpath="
                         + newPath + ".");
        }
    }

    public void updateTab(Properties props) {
        this.model = new NetpathModel(props.getProperty("de.renew.netPath",
                                                        System.getProperty("user.dir")));
        tab.setPathList(model);
    }

    public void addEntry() {
        EditPathEntryDialog dialog = new EditPathEntryDialog(getParentDialog(),
                                                             "Add");
        dialog.setEntry(new PathEntry("", false));
        dialog.setVisible(true);
        if (dialog.isCommitted()) {
            PathEntry newEntry = dialog.getEntry();
            int insertionPoint = tab.getSelection().getMinSelectionIndex();
            if (insertionPoint == -1) {
                model.add(newEntry);
            } else {
                model.add(insertionPoint, newEntry);
            }
        }
    }

    public void editEntry() {
        ListSelectionModel selection = tab.getSelection();
        for (int index = selection.getMinSelectionIndex();
                     index <= selection.getMaxSelectionIndex(); index++) {
            if (selection.isSelectedIndex(index)) {
                EditPathEntryDialog dialog = new EditPathEntryDialog(getParentDialog(),
                                                                     "Change");
                dialog.setEntry(model.get(index));
                dialog.setVisible(true);
                if (dialog.isCommitted()) {
                    model.set(index, dialog.getEntry());
                }
            }
        }
    }

    public void upEntry() {
        ListSelectionModel selection = tab.getSelection();
        int insertionIndex = selection.getMinSelectionIndex() - 1;
        if (insertionIndex < 0) {
            insertionIndex = 0;
        }
        int[] indices = computeSelectedIndices(selection);
        PathEntry[] toMove = model.removeAll(indices);
        model.addAll(insertionIndex, toMove);
        selection.addSelectionInterval(insertionIndex,
                                       insertionIndex + toMove.length - 1);
    }

    public void downEntry() {
        ListSelectionModel selection = tab.getSelection();
        int insertionIndex = selection.getMaxSelectionIndex() + 1;
        int[] indices = computeSelectedIndices(selection);
        PathEntry[] toMove = model.removeAll(indices);
        insertionIndex = insertionIndex - toMove.length + 1;
        if (insertionIndex > model.getSize()) {
            insertionIndex = model.getSize();
        }
        model.addAll(insertionIndex, toMove);
        selection.addSelectionInterval(insertionIndex,
                                       insertionIndex + toMove.length - 1);
    }

    public void removeEntry() {
        model.removeAll(computeSelectedIndices(tab.getSelection()));
    }

    private int[] computeSelectedIndices(ListSelectionModel selection) {
        int maxCount = selection.getMaxSelectionIndex()
                       - selection.getMinSelectionIndex() + 1;
        int[] indices = new int[maxCount];
        int count = 0;
        for (int index = selection.getMinSelectionIndex();
                     index <= selection.getMaxSelectionIndex(); index++) {
            if (selection.isSelectedIndex(index)) {
                indices[count] = index;
                count++;
            }
        }
        if (count < maxCount) {
            int[] shortenedIndices = new int[count];
            for (int i = 0; i < count; i++) {
                shortenedIndices[i] = indices[i];
            }
            indices = shortenedIndices;
        }
        return indices;
    }

    private JDialog getParentDialog() {
        Component component = tab;
        while (!(component instanceof JDialog)) {
            component = component.getParent();
        }
        return (JDialog) component;
    }
}