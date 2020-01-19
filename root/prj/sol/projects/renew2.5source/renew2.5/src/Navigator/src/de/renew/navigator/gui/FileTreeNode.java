package de.renew.navigator.gui;

import de.renew.navigator.models.Directory;
import de.renew.navigator.models.TreeElement;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * The FileTreeNode class is a basic TreeNode implementation for Files.
 * It loads its children on request, can reload the children but doesn't offer methods to add or remove Children directly.
 * For using the DefaultTreeModel, the MutableTreeNode interface has to be implemented.
 * This is done by the MutableFileTreeNode class which is extending this class.
 * Cause this class is loading it's children as FileTreeNodes, the protected loadChildren method has to be overwritten on extension of this class.
 *
 * @author Hannes Ahrens (4ahrens)
 * @version March 2009
 */
public class FileTreeNode implements MutableTreeNode {
    final protected Vector<TreeNode> children;
    final protected TreeElement model;
    protected TreeNode parent = null;

    /**
     * This constructor creates a root node.
     *
     * @param model the file to be contained
     */
    public FileTreeNode(TreeElement model) {
        assert (model != null);
        this.children = new Vector<TreeNode>();
        this.model = model;
    }

    /**
     * @see javax.swing.tree.TreeNode#children()
     */
    public Enumeration<TreeNode> children() {
        return children.elements();
    }

    /**
     * Only directories allow children in a file system.
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     *
     * @return is contained file a directory?
     */
    public boolean getAllowsChildren() {
        return model instanceof Directory;
    }

    /**
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int childIndex) {
        return children.elementAt(childIndex);
    }

    /**
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * @see javax.swing.tree.MutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
     */
    public void insert(MutableTreeNode child, int index) {
        child.setParent(this);
        children.add(index, child);
    }

    /**
     * Adds a tree node as child.
     */
    public void add(MutableTreeNode child) {
        child.setParent(this);
        children.add(child);
    }

    /**
     * @see javax.swing.tree.MutableTreeNode#remove(int)
     */
    public void remove(int index) {
        children.remove(index);
    }

    /**
     * @see javax.swing.tree.MutableTreeNode#remove(javax.swing.tree.MutableTreeNode)
     */
    public void remove(MutableTreeNode node) {
        children.remove(node);
    }

    /**
     * @see javax.swing.tree.MutableTreeNode#removeFromParent()
     */
    public void removeFromParent() {
        if (parent != null && parent instanceof MutableTreeNode) {
            ((MutableTreeNode) parent).remove(this);
        }
    }

    /**
     * @see javax.swing.tree.MutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
     */
    public void setParent(MutableTreeNode newParent) {
        parent = newParent;
    }

    /**
     * This method is currently doing nothing.
     * @see javax.swing.tree.MutableTreeNode#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object object) {
    }

    /**
     * This method is giving the tree path from the root node.
     * @see javax.swing.tree.DefaultMutableTreeNode#getPath()
     *
     * @return all parent TreeNodes + this
     */
    public TreePath getPath() {
        LinkedList<TreeNode> path = new LinkedList<TreeNode>();
        TreeNode node = this;

        while (node != null) {
            path.addFirst(node);
            node = node.getParent();
        }

        return new TreePath(path.toArray(new TreeNode[path.size()]));
    }

    /**
     * All non directory files are leafs.
     * @see javax.swing.tree.TreeNode#isLeaf()
     *
     * @return is no directory
     */
    public boolean isLeaf() {
        return !getAllowsChildren() || children.isEmpty();
    }

    /**
     * @return the contained file of this class
     */
    public File getFile() {
        return model.getFile();
    }

    /**
     * The toString method get's used by the JTree to determine the text for representing a node.
     * Therefore the filename gets returned.
     *
     * @return the name of this node
     */
    public String toString() {
        return model.getName();
    }

    /**
     * @return a list of all opened directories.
     */
    public List<FileTreeNode> getOpenedDirectories() {
        if (!(model instanceof Directory)) {
            return Collections.emptyList();
        }

        Directory directory = (Directory) model;
        if (!directory.isOpened()) {
            return Collections.emptyList();
        }

        List<FileTreeNode> result = new ArrayList<FileTreeNode>();
        for (TreeNode child : children) {
            if (child instanceof FileTreeNode) {
                result.addAll(((FileTreeNode) child).getOpenedDirectories());
            }
        }
        result.add(this);

        return result;
    }

    public TreeElement getModel() {
        return model;
    }

    /**
     * @return The tooltip text for this node.
     */
    public String getToolTip() {
        return model.getFile().getAbsolutePath();
    }
}