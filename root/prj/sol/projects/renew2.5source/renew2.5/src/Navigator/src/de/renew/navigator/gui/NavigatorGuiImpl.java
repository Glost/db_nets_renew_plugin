package de.renew.navigator.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Iconkit;

import de.renew.navigator.FilesystemController;
import de.renew.navigator.NavigatorAction;
import de.renew.navigator.NavigatorConfigurator;
import de.renew.navigator.NavigatorExtension;
import de.renew.navigator.NavigatorGui;
import de.renew.navigator.NavigatorPlugin;
import de.renew.navigator.events.DirectoryOpeningEvent;
import de.renew.navigator.gui.actions.AddFilesAction;
import de.renew.navigator.gui.actions.CollapseAllAction;
import de.renew.navigator.gui.actions.ExpandAction;
import de.renew.navigator.gui.actions.OpenHomeAction;
import de.renew.navigator.gui.actions.OpenNetPathAction;
import de.renew.navigator.gui.actions.RefreshAllAction;
import de.renew.navigator.gui.actions.RemoveAllAction;
import de.renew.navigator.gui.actions.RemoveOneAction;
import de.renew.navigator.gui.filters.JavaSearchFilter;
import de.renew.navigator.gui.filters.RNWSearchFilter;
import de.renew.navigator.models.BackgroundTask;
import de.renew.navigator.models.Directory;
import de.renew.navigator.models.Leaf;
import de.renew.navigator.models.Model;
import de.renew.navigator.models.NavigatorFileTree;
import de.renew.navigator.models.SearchFilter;
import de.renew.navigator.models.TreeElement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * The NavigatorGUI class contains all GUI related implementation for the
 * navigator. It is based upon a JFrame and setting up the interface, including
 * all viewable parts of the Navigator. The main component is the JTree,
 * containing all the opened FileTreeNodes.
 * <p/>
 * This class offers methods to access and manipulate the tree as well as
 * opening files in Renew. For this purpose, multiple node selection is
 * supported. These methods are mainly in use by the registered listeners of
 * this GUI.
 *
 * @author Hannes Ahrens (4ahrens)
 * @version March 2009
 */
class NavigatorGuiImpl extends JFrame implements NavigatorGui, Observer {
    public static final long serialVersionUID = 97936353687387679L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NavigatorGuiImpl.class);
    private final FilesystemController filesystem;
    private final NavigatorFileTree model;
    private final List<NavigatorAction> actions;
    private final JPanel menuPanel = new JPanel();
    private final JTextField searchField = new JTextField();
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    private final JTree tree = new JTree(treeModel);
    private final JPanel filterPanel = new JPanel();
    private final JPanel tasksPanel = new JPanel();
    private final HashMap<NavigatorAction, Component> actionMapper = new HashMap<NavigatorAction, Component>();
    private final DefaultFileTreeCellRenderer treeCellRenderer = new DefaultFileTreeCellRenderer();
    private final List<NavigatorExtension> extensions;
    public boolean _initialized = false;
    private boolean updating = false;

    /**
     * The constructor only sets the name of the main frame of this navigator.
     */
    public NavigatorGuiImpl(final FilesystemController filesystem,
                            final NavigatorPlugin plugin) {
        super("Navigator");

        this.model = plugin.getModel();
        model.addObserver(this);

        this.actions = new ArrayList<NavigatorAction>();
        this.filesystem = filesystem;
        extensions = new LinkedList<NavigatorExtension>();

        final Container pane = getContentPane();

        this.setPreferredSize(new Dimension(320, 640));
        this.setLocation(0, 150);

        // Set Navigator icon image.
        this.setIconImage(Iconkit.instance()
                                 .getImage(NavigatorIcons.ICON_NAVIGATOR));

        // Set Window layout.
        final Dimension barDimension = new Dimension(-1, 20);
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.ipadx = 0;
        c.weightx = 1;
        c.weighty = 0;

        // Create the menu panel.
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.LINE_AXIS));
        menuPanel.setPreferredSize(barDimension);
        menuPanel.setMaximumSize(barDimension);

        // Align menu panel.
        c.gridy = 0;
        pane.add(menuPanel, c);
        initMenuBar();

        // Create the filter panel.
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
        filterPanel.setPreferredSize(barDimension);
        filterPanel.setMaximumSize(barDimension);

        // Align filter panel.
        c.gridy = 1;
        pane.add(filterPanel, c);
        initFilterBar();

        // Create listeners.
        TreeMouseListener _treeMouseListener = new TreeMouseListener(this);
        TreeKeyListener _treeKeyListener = new TreeKeyListener(this);

        // Initialize the navigator tree
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setRootVisible(false);
        tree.setFocusable(true);
        tree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addMouseListener(_treeMouseListener);
        tree.addKeyListener(_treeKeyListener);
        tree.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
        tree.setCellRenderer(treeCellRenderer);
        tree.setLargeModel(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setBackground(Color.WHITE);
        panel.add(tree, BorderLayout.LINE_START);
        JScrollPane _scrollPane = new JScrollPane(panel);
        _scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        _scrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        c.gridy = 2;
        c.weighty = 1;
        pane.add(_scrollPane, c);

        c.weighty = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.PAGE_END;

        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.PAGE_AXIS));
        pane.add(tasksPanel, c);

        tree.addTreeExpansionListener(new TreeExpansionListener(model));
        new DropTarget(this, new DragDropListener(filesystem));
        _initialized = true;

        updateGui(true);
        pack();
    }

    @Override
    public void openWindow() {
        if (isVisible()) {
            toFront();
            return;
        }

        setVisible(true);
    }

    @Override
    public void closeWindow() {
        setVisible(false);
    }

    /**
     * @param doUpdateSearchFilter if <code>true</code>, the search filter will
     *                             be updated.
     */
    public void updateGui(boolean doUpdateSearchFilter) {
        if (updating) {
            return;
        }

        updating = true;
        closeAll();
        for (TreeElement treeRoot : model.getTreeRoots()) {
            final FileTreeNode node = renderNode(treeRoot);
            if (node != null) {
                rootNode.add(node);
            }
        }

        updateTree();
        expandFromModel();

        // Set search term in gui.
        if (doUpdateSearchFilter) {
            final SearchFilter searchFilter = model.getTextSearch();
            if (searchFilter != null && searchFilter.getTerms().size() > 0) {
                String term = searchFilter.getTerms().get(0);
                searchField.setText(term);
            }
        }

        updateBackgroundTasks();
        repaint();
        updating = false;
    }

    /**
     * Adds an extension to the GUI.
     *
     * @param extension The extension registered by the plugin.
     */
    @Override
    public void addExtension(NavigatorExtension extension) {
        extensions.add(extension);
        extension.configure(new NavigatorConfigurator() {
                @Override
                public void addMenuAction(NavigatorAction action) {
                    if (actionMapper.containsKey(action)) {
                        return;
                    }

                    actionMapper.put(action, createButton(menuPanel, action));
                }

                @Override
                public void addFilterAction(NavigatorAction action) {
                    if (actionMapper.containsKey(action)) {
                        return;
                    }

                    actionMapper.put(action, createButton(filterPanel, action));
                }

                @Override
                public void addFileTreeCellRenderer(FileTreeCellRenderer renderer) {
                    treeCellRenderer.addAdditionalRenderer(renderer);
                }
            });
    }

    /**
     * Removes an extension from the GUI.
     *
     * @param extension The extension registered by the plugin.
     */
    @Override
    public boolean removeExtension(NavigatorExtension extension) {
        if (!extensions.remove(extension)) {
            return false;
        }

        extension.configure(new NavigatorConfigurator() {
                @Override
                public void addMenuAction(NavigatorAction action) {
                    menuPanel.remove(actionMapper.remove(action));
                }

                @Override
                public void addFilterAction(NavigatorAction action) {
                    filterPanel.remove(actionMapper.remove(action));
                }

                @Override
                public void addFileTreeCellRenderer(FileTreeCellRenderer renderer) {
                    treeCellRenderer.removeAdditionalRenderer(renderer);
                }
            });
        repaint();
        return true;
    }

    @Override
    public DefaultFileTreeCellRenderer getTreeCellRenderer() {
        return treeCellRenderer;
    }

    public void initFilterBar() {
        searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent keyEvent) {
                    final String param = searchField.getText();
                    if (param.isEmpty()) {
                        model.setTextSearch(null);
                    } else {
                        model.setTextSearch(new SearchFilter("search",
                                                             SearchFilter.Type.CONTAINS,
                                                             false, param));
                    }
                    model.notifyObservers(searchField);
                }
            });

        Dimension searchFieldDimension = new Dimension(198, 20);
        searchField.setMaximumSize(searchFieldDimension);
        searchField.setPreferredSize(searchFieldDimension);
        searchField.setBorder(new EmptyBorder(0, 0, 0, 0));
        filterPanel.add(searchField);

        // Create a filter button.
        createButton(filterPanel,
                     new NavigatorAction("Clear Filter", NavigatorIcons.CLEAR,
                                         null) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    model.setTextSearch(null);
                    searchField.setText("");
                    model.notifyObservers(searchField);
                }
            });

        createButton(filterPanel, new RNWSearchFilter(model));
        createButton(filterPanel, new JavaSearchFilter(model));
    }

    /**
     * Creates a new menu button.
     *
     * @param action Action of the button
     * @return the created menu button instance
     * @deprecated Use {@link #createButton(JPanel, NavigatorAction)}
     */
    @Deprecated
    public JButton createButton(String context, NavigatorAction action) {
        return createButton(getPanelForContext(context), action);
    }

    /**
     * @return all selected file tree nodes.
     */
    public List<FileTreeNode> getSelectedNodes() {
        final List<FileTreeNode> result = new ArrayList<FileTreeNode>();
        final TreePath[] selectionPaths = getSelectedTreePaths();
        for (TreePath path : selectionPaths) {
            final Object component = path.getLastPathComponent();

            if (!(component instanceof FileTreeNode)) {
                continue;
            }

            result.add((FileTreeNode) component);
        }

        return result;
    }

    /**
     * @return all selected tree paths.
     */
    public TreePath[] getSelectedTreePaths() {
        final TreePath[] selectionPaths = tree.getSelectionPaths();
        return selectionPaths == null ? new TreePath[0] : selectionPaths;
    }

    /**
     * Opens all selected files in Renew.
     * <p/>
     * For opening the files, it is using the
     * CH.ifa.draw.IOHelper.
     */
    public void openSelected() {
        final TreePath[] paths = getSelectedTreePaths();
        for (TreePath path : paths) {
            Object o = path.getLastPathComponent();
            if (o == null || !(o instanceof FileTreeNode)) {
                continue;
            }

            FileTreeNode ftn = ((FileTreeNode) o);
            final File file = ftn.getFile();

            if (file.isDirectory()) {
                continue;
            }

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (filesystem.isExternallyOpenedFile(file)
                                && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(file);
                            return null;
                        } catch (IOException ignored) {
                        }
                    }

                    DrawPlugin.getGui().openOrLoadDrawing(file.getPath()); //
                                                                           // the actual
                                                                           // opening
                                                                           // process.


                    return null;
                }
            };
            worker.execute();
        }
    }

    /**
     * This method pops up a simple context menu. It currently only contains a
     * selection field to remove the MutableTreeNode of the specified location.
     * The pop-up will only show if the node is a direct child to the internal
     * root node.
     *
     * @param x tree relative x position to show the pop-up
     * @param y tree relative y position to show the pop-up
     */
    public void showContextMenu(int x, int y) {
        final TreePath path = tree.getPathForLocation(x, y);
        if (null == path) {
            return;
        }

        final Object o = path.getLastPathComponent();
        if (o == null || !(o instanceof FileTreeNode)) {
            return;
        }

        final FileTreeNode mtn = ((FileTreeNode) o);
        JPopupMenu popupMenu = new JPopupMenu();

        // Show Remove tree action.
        if (mtn.getParent() == rootNode) {
            final RemoveOneAction action = new RemoveOneAction(this, model);
            JMenuItem menuItem = new JMenuItem(action);
            menuItem.setText(action.getActionName());
            popupMenu.add(menuItem);
        }

        // Reveal in finder.
        final String os = System.getProperty("os.name");
        String explorer = os.equals("Mac OS X") ? "Finder" : "Explorer";
        JMenuItem reveal = new JMenuItem("Reveal in " + explorer);
        reveal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        File file = mtn.getFile();
                        if (file.isFile()) {
                            file = file.getParentFile();
                        }
                        desktop.open(file);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Could not reveal.");
                    }
                }
            });
        popupMenu.add(reveal);

        for (NavigatorExtension extension : extensions) {
            JMenuItem menuItem2 = extension.getMenuItem(tree, x, y, o, mtn);
            if (menuItem2 != null) {
                popupMenu.add(menuItem2);
                popupMenu.repaint();
            }
        }
        popupMenu.show(tree, x, y);
    }

    /**
     * closeAll() removes all opened nodes from tree and triggeres autosave.
     */
    public void closeAll() {
        rootNode.removeAllChildren();
        treeModel.nodeStructureChanged(rootNode);
    }

    /**
     * collapseAll() collapses all visible nodes and invalidates the rest.
     */
    public void collapseAll() {
        logger.debug("------------> COLLAPSE ALL");
        for (TreeElement rootDirectory : model.getTreeRoots()) {
            if (rootDirectory instanceof Directory) {
                ((Directory) rootDirectory).setOpened(false);
            }
        }
        model.notifyObservers();
    }

    /**
     * Expands recursively all child nodes to the selected node
     */
    public void expand() {
        logger.debug("------------> EXPAND RECURSIVE");
        try {
            TreePath path = tree.getSelectionPath();
            TreeExpansionListener.setActive(false);

            if (path != null) {
                // Expand selected nodes.
                FileTreeNode tn = (FileTreeNode) path.getLastPathComponent();
                final TreeElement treeElement = tn.getModel();

                if (treeElement instanceof Directory) {
                    treeElement.expandAll();
                } else {
                    final Model parent = treeElement.getParent();
                    if (parent instanceof Directory) {
                        ((Directory) parent).expandAll();
                    }
                }
            } else {
                // Expand all.
                model.expandAll();
            }
            model.notifyObservers();

            TreeExpansionListener.setActive(true);
            tree.fireTreeExpanded(path);
        } catch (Exception e) {
            // Nothing to do
        }
    }

    @Override
    public void removeSelectedNodes() {
        final List<TreeElement> selectedElements = getSelectedElements();
        for (TreeElement treeElement : selectedElements) {
            if (treeElement.getParent() instanceof NavigatorFileTree) {
                model.remove(treeElement);
                continue;
            }

            treeElement.setExcluded(true);
        }
        model.notifyObservers();
    }

    @Override
    public List<TreeElement> getSelectedElements() {
        final List<TreeElement> elements = new LinkedList<TreeElement>();
        final List<FileTreeNode> selectedNodes = getSelectedNodes();

        for (FileTreeNode node : selectedNodes) {
            elements.add(node.getModel());
        }

        return elements;
    }

    @Override
    public JTree getTree() {
        return this.tree;
    }

    @Override
    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Removes a button from the menu.
     *
     * @param button the button to remove
     */
    public void removeMenuButton(JButton button) {
        menuPanel.remove(button);
        menuPanel.validate();
        menuPanel.repaint();
        if (logger.isDebugEnabled()) {
            logger.debug(NavigatorGuiImpl.class.getSimpleName() + ": "
                         + "removing button: " + button);
        }
    }

    public List<NavigatorAction> getActions() {
        return actions;
    }

    public void updateBackgroundTasks() {
        tasksPanel.removeAll();
        for (BackgroundTask task : model.getBackgroundTasks()) {
            renderTask(task);
        }
        tasksPanel.revalidate();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            if (arg instanceof DirectoryOpeningEvent) {
                final DirectoryOpeningEvent event = (DirectoryOpeningEvent) arg;
                toggleNode(event.getNode(), event.isOpening());
                return;
            }

            updateGui(arg != searchField);
        }
    }

    @Override
    public List<NavigatorExtension> getExtensions() {
        return extensions;
    }

    /**
     * Opens or clothes a file tree node.
     */
    private void toggleNode(FileTreeNode node, boolean opening) {
        if (opening) {
            tree.expandPath(node.getPath());
            return;
        }

        tree.collapsePath(node.getPath());
    }

    /**
     * Creates a new menu button.
     *
     * @param panel  The panel to add the action to.
     * @param action Action of the button
     * @return the created menu button instance
     */
    private JButton createButton(JPanel panel, NavigatorAction action) {
        Dimension buttonDimension = new Dimension(33, 20);
        final JButton menuButton = new JButton(action);
        menuButton.setMinimumSize(buttonDimension);
        menuButton.setPreferredSize(buttonDimension);
        menuButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        action.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    // Do nothing for other properties.
                    if (!propertyChangeEvent.getPropertyName().equals("active")) {
                        return;
                    }

                    final boolean newValue = (Boolean) propertyChangeEvent
                        .getNewValue();
                    menuButton.setSelected(newValue);
                }
            });

        panel.add(menuButton);
        panel.validate();
        panel.repaint();

        return menuButton;
    }

    /**
     * Repaints the tree
     */
    private void updateTree() {
        treeModel.reload();
        getContentPane().validate();
        getContentPane().repaint();
    }

    /**
     * Expands all nodes which are expanded in the model.
     */
    private void expandFromModel() {
        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            FileTreeNode treeNode = (FileTreeNode) rootNode.getChildAt(i);

//            tree.scrollPathToVisible(treeNode.getPath());
            for (FileTreeNode openedDir : treeNode.getOpenedDirectories()) {
                final TreePath path = openedDir.getPath();
                tree.expandPath(path);
            }
        }
    }

    /**
     * Render a background task.
     *
     * @param task the task to render.
     */
    private void renderTask(final BackgroundTask task) {
        final JPanel panel = new JPanel();
        final JPanel leftPanel = new JPanel();
        final JLabel label = new JLabel(task.getName());
        final JButton cancel = new JButton();
        final JProgressBar progressBar = new JProgressBar();

        final Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (o instanceof BackgroundTask) {
                    final BackgroundTask bg = (BackgroundTask) o;
                    label.setText(bg.getName() + " "
                                  + Math.round(bg.getCurrent() * 100) + " %");
                    cancel.setEnabled(bg.isCancelable());
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(Integer.MAX_VALUE);
                    progressBar.setValue(Math.round(bg.getCurrent() * Integer.MAX_VALUE));
                    progressBar.setIndeterminate(bg.isIndeterminate());
                }
            }
        };
        task.addObserver(observer);
        observer.update(task, null);

        panel.setMinimumSize(new Dimension(-1, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

        // Configure task label.
        label.setLabelFor(progressBar);
        label.setBackground(Color.RED);
        leftPanel.add(label);

        // Configure task progress bar.
        progressBar.setBackground(Color.BLUE);
        leftPanel.add(progressBar);

        panel.add(leftPanel);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));

        cancel.setAction(new AbstractAction("Cancel") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    task.cancel();
                }
            });
        panel.add(cancel);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tasksPanel.add(panel);
    }

    /**
     * Adds the default menu buttons to the menu.
     */
    private void initMenuBar() {
        createButton(menuPanel, new OpenHomeAction(filesystem));
        createButton(menuPanel, new OpenNetPathAction(filesystem));
        createButton(menuPanel, new AddFilesAction(filesystem));
        createButton(menuPanel, new ExpandAction(this));
        createButton(menuPanel, new CollapseAllAction(this));
        createButton(menuPanel, new RefreshAllAction(filesystem));
        createButton(menuPanel, new RemoveOneAction(this, model));
        createButton(menuPanel, new RemoveAllAction(model));
    }

    /**
     * Expands recursively all child nodes to a node
     */
    private void expandRecursively(TreePath tp, TreeNode tn) {
        Enumeration<?> children = tn.children();
        while (children.hasMoreElements()) {
            Object o = children.nextElement();

            if (o instanceof TreeNode) {
                TreeNode child = (TreeNode) o;

                if (!child.isLeaf()) {
                    expandRecursively(tp.pathByAddingChild(child), child);
                }
            }
        }
        tree.expandPath(tp);
    }

    /**
     * @param context the context provides a specific menu.
     * @return panel belonging to the given context
     * @deprecated Use {@link #createButton(JPanel, NavigatorAction)}
     */
    @Deprecated
    private JPanel getPanelForContext(String context) {
        if (context.equals("menu")) {
            return menuPanel;
        }

        return filterPanel;
    }

    /**
     * Renders a model as a file tree node.
     *
     * @param model The model to render.
     * @return The node being rendered.
     */
    private FileTreeNode renderNode(TreeElement model) {
        // Do not render excluded models.
        if (model.isExcluded()) {
            return null;
        }

        if (model instanceof Directory) {
            return renderDirectoryModel((Directory) model);
        }

        if (model instanceof Leaf) {
            return renderFileNode((Leaf) model);
        }

        throw new RuntimeException("model must be either File or Directory.");
    }

    /**
     * @param model The directory model to render.
     * @return The node being rendered.
     */
    private FileTreeNode renderDirectoryModel(Directory model) {
        final FileTreeNode treeNode = new FileTreeNode(model);

        // Load children of directory.
        int numChildren = 0;
        for (TreeElement child : model.getChildren()) {
            final FileTreeNode node = renderNode(child);

            // Just add non-null tree nodes.
            if (node != null) {
                treeNode.add(node);
                numChildren++;
            }
        }

        // If a directory has no children, do not render it.
        if (numChildren == 0) {
            return null;
        }

        return treeNode;
    }

    /**
     * @param model The file model to render.
     * @return The node being rendered.
     */
    private FileTreeNode renderFileNode(Leaf model) {
        return shouldRenderFileNode(model) ? new FileTreeNode(model) : null;
    }

    /**
     * Returns true, if the given model is not filtered.
     *
     * @param model The file model to check.
     * @return true, if not filtered.
     */
    private boolean shouldRenderFileNode(Leaf model) {
        int numFilters = 0;
        final String toMatch = model.getName();

        // Match the text search filter.
        if (this.model.getTextSearch() != null
                    && !this.model.getTextSearch().match(toMatch)) {
            return false;
        }

        // Check file filters with a logical OR.
        for (SearchFilter filter : this.model.getActiveFileFilters()) {
            if (!filter.isValid()) {
                continue;
            }

            numFilters++;
            if (filter.match(toMatch)) {
                return true;
            }
        }

        return numFilters == 0;
    }
}