package de.renew.gui;

import de.renew.shadow.DefaultShadowNetLoader;

import de.renew.util.PathEntry;
import de.renew.util.StringUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;


/**
 * Holds and manipulates a list of paths for use by the net loader.
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class NetpathModel extends AbstractListModel {
    private List<PathEntry> paths;

    public NetpathModel(String netpath) {
        PathEntry[] entries = StringUtil.canonizePaths(StringUtil.splitPaths(netpath));
        paths = new ArrayList<PathEntry>(entries.length + 5);
        for (int i = 0; i < entries.length; i++) {
            paths.add(entries[i]);
        }
    }

    public String asPath() {
        return DefaultShadowNetLoader.asPathString(paths.toArray(new PathEntry[paths
                                                                               .size()]));
    }

    // Implementation of javax.swing.ListModel
    public int getSize() {
        return paths.size();
    }

    public Object getElementAt(int n) {
        PathEntry entry = paths.get(n);
        StringBuffer buffer = new StringBuffer();
        if (entry.isClasspathRelative) {
            buffer.append("CLASSPATH");
            if (!"".equals(entry.path)) {
                buffer.append(File.separator);
            }
        }
        buffer.append(entry.path);
        return buffer.toString();
    }

    // Delegation to paths List.


    /**
     * Inserts the given path entry at the given index into the model.
     *
     * @param index  the index where the new entry is added. All entries
     *               with indices greater than or equal to this index will
     *               be pushed backwards.
     * @param entry  the net path entry to add.
     **/
    public void add(int index, PathEntry entry) {
        paths.add(index, entry);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Appends the given path entry to the end of the model.
     *
     * @param entry  the net path entry to add.
     **/
    public void add(PathEntry entry) {
        int lastindex = paths.size();
        add(lastindex, entry);
    }

    /**
     * Replaces the entry at the given index by the given path entry.
     *
     * @param index  the index where the replacement takes place.
     * @param entry  the net path entry to insert.
     **/
    public void set(int index, PathEntry entry) {
        paths.remove(index);
        paths.add(index, entry);
        fireContentsChanged(this, index, index);
    }

    /**
     * Returns the path entry at the given index.
     *
     * @param index  the index of the entry to return
     * @return the path entry at the given index.
     **/
    public PathEntry get(int index) {
        return paths.get(index);
    }

    /**
     * Removes the entry at the given index.
     *
     * @param index  the index to remove.
     *               All entries with indices equal to or greater
     *               than the given index are pulled forwards.
     **/
    public PathEntry remove(int index) {
        PathEntry removed = paths.remove(index);
        fireIntervalRemoved(this, index, index);
        return removed;
    }

    /**
     * Removes all entries specified by the given indices.
     *
     * @param indices  array of indices to remove.
     * @return array of removed entries in forward order.
     **/
    public PathEntry[] removeAll(int[] indices) {
        if (indices == null || indices.length == 0) {
            return new PathEntry[0];
        }
        List<PathEntry> removed = new ArrayList<PathEntry>(paths.size());
        for (int i = 0; i < indices.length; i++) {
            removed.add(paths.get(indices[i]));
        }


        // Remove elements (in backwards order).
        // To send as few update messages as possible,
        // regions of consecutive indexes are detected.
        // - firstConsecutiveIndex is the region end
        // - lastRemovedIndex is the region start
        // The variables are initialized so that in the
        // first round a consecutive region must be detected.
        int firstConsecutiveIndex = indices[indices.length - 1];
        int lastRemovedIndex = indices[indices.length - 1] + 1;
        for (int i = indices.length - 1; i >= 0; i--) {
            paths.remove(indices[i]);
            if (indices[i] != lastRemovedIndex - 1) {
                fireIntervalRemoved(this, lastRemovedIndex,
                                    firstConsecutiveIndex);
                firstConsecutiveIndex = indices[i];
            }
            lastRemovedIndex = indices[i];
        }
        fireIntervalRemoved(this, lastRemovedIndex, firstConsecutiveIndex);
        return removed.toArray(new PathEntry[removed.size()]);
    }

    /**
     * Inserts all given entries at the given index into the model.
     *
     * @param index  the insertion index. All existing entries
     *               with indices greater than or equal to this
     *               index will be pushed backwards.
     * @param entries  the entries to add to the model.
     **/
    public void addAll(int index, PathEntry[] entries) {
        paths.addAll(index, Arrays.asList(entries));
        fireIntervalAdded(this, index, index + entries.length - 1);
    }
}