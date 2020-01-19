package CH.ifa.draw.application;

import CH.ifa.draw.framework.Drawing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;


/**
 * A windows menu which groups its menu items by category.
 * <p></p>
 * WindowsMenu.java
 * Created: Tue Mar 20  2001
 * @author Michael Duvigneau
 */
public class WindowsMenu extends JMenu {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(WindowsMenu.class);
    private static final String NO_CATEGORY = "Other windows";

    // private DrawApplication editor;
    // JMenu menu;


    /**
     * Table to organize the menus in categories.
     * Contains pairs of the type (<i>category</i>, <i>menu list</i>).
     * <i>category</i> is of type <code>String</code>.
     * <i>menu list</i> is of type {@link CH.ifa.draw.application.WindowsMenu.MenuList}.
     **/
    private Map<String, MenuList> categoryMap = Collections.synchronizedMap(new HashMap<String, MenuList>());

    /**
     * Table to keep the category of represented objects.
     * Contains pairs of the type (<i>item object</i>, <i>category</i>).
     * <i>item object</i> is the identifying object (of any type).
     * <i>category</i> is of type <code>String</code>.
     **/
    private Map<Object, String> objectMap = Collections.synchronizedMap(new HashMap<Object, String>());

    public WindowsMenu(String name) {
        super(name);
        // menu = new JMenu(name);
        // this.editor = editor;
        setEnabled(false);
    }

    void addDrawing(Drawing drawing, JRadioButtonMenuItem item) {
        String category = drawing.getWindowCategory();
        addCategorizedItem(category, item, drawing);
    }

    void removeDrawing(Drawing drawing) {
        removeItemFor(drawing);
    }

    /**
     * Adds a (hopefully non-modal) dialog to the windows menu under the
     * given category.
     *
     * @param category  the windows category to add the dialog to.
     *                  Make it a plural, if possible. May be <code>null</code>.
     * @param dialog    the dialog to bring to front when the menu entry is activated.
     **/
    public void addDialog(final String category, final Dialog dialog) {
        final JMenuItem item = new JMenuItem(dialog.getTitle());
        item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.toFront();
                }
            });
        addCategorizedItem(category, item, dialog);
    }

    public void removeDialog(Dialog dialog) {
        removeItemFor(dialog);
    }

    /**
     * Adds a frame to the windows menu under the given category.
     *
     * @param category  the windows category to add the frame to.
     *                  Make it a plural, if possible. May be <code>null</code>.
     * @param dialog    the frame to bring to front when the menu entry is activated.
     **/
    public void addFrame(final String category, final Frame frame) {
        final JMenuItem item = new JMenuItem(frame.getTitle());
        item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setExtendedState(frame.getExtendedState()
                                           & ~Frame.ICONIFIED);
                    frame.toFront();
                }
            });
        addCategorizedItem(category, item, frame);
    }

    public void removeFrame(Frame frame) {
        removeItemFor(frame);
    }

    public void activate(Drawing drawing) {
        JRadioButtonMenuItem item = getItemFor(drawing);
        Object element = null;
        for (Iterator<String> iter = objectMap.values().iterator();
                     iter.hasNext();) {
            element = iter.next();
            //FIXME element is always of type String
            if (element != item && element instanceof JRadioButtonMenuItem) {
                ((JRadioButtonMenuItem) element).setSelected(false);
            }
            item.setSelected(true);
        }
    }

    public void deactivate(Drawing drawing) {
        JRadioButtonMenuItem item = getItemFor(drawing);
        if (item != null) {
            item.setSelected(false);
        }
    }

    public void setName(Drawing drawing, String nameOnly) {
        logger.debug("WindowsMenu: changing name of drawing " + drawing + ".");
        MenuList ml = getMenuListFor(drawing);
        int oldPos = ml.list.indexOf(drawing);
        JRadioButtonMenuItem item = (JRadioButtonMenuItem) ml.menu.getItem(oldPos);
        ml.list.remove(oldPos);
        ml.menu.remove(oldPos);
        item.setText(nameOnly);
        int newPos = findSortedPosition(ml.menu, item);
        ml.list.insertElementAt(drawing, newPos);
        ml.menu.insert(item, newPos);
    }

    private void addCategorizedItem(String category, JMenuItem item,
                                    Object object) {
        if (objectMap.containsKey(object)) {
            logger.debug("WindowsMenu: object " + object
                         + " already registered.");
            removeItemFor(object);
        }

        if (category == null) {
            category = NO_CATEGORY;
        }
        logger.debug("WindowsMenu: adding object " + object + " to category "
                     + category + ".");

        MenuList ml = categoryMap.get(category);
        if (ml == null) {
            JMenu subMenu = new JMenu(category);
            subMenu.setFont(DrawApplication.getMenuFont());
            ml = new MenuList(subMenu, new Vector<Object>());
            categoryMap.put(category, ml);
            int pos = findSortedPosition(this, subMenu);
            insert(subMenu, pos);
            setEnabled(true);
        }

        int pos = findSortedPosition(ml.menu, item);
        ml.menu.insert(item, pos);
        ml.list.insertElementAt(object, pos);
        objectMap.put(object, category);
    }

    private JRadioButtonMenuItem getItemFor(Object object) {
        JRadioButtonMenuItem result = null;
        try {
            MenuList ml = getMenuListFor(object);
            int pos = ml.list.indexOf(object);
            result = (JRadioButtonMenuItem) ml.menu.getItem(pos);
        } catch (NullPointerException e) {
            // just return null then
        } catch (ArrayIndexOutOfBoundsException e) {
            // just return null then            
        }
        return result;
    }

    private MenuList getMenuListFor(Object object) {
        MenuList result = null;
        try {
            String category = objectMap.get(object);
            result = categoryMap.get(category);
        } catch (NullPointerException e) {
            // just return null then
        }
        return result;
    }

    private void removeItemFor(Object object) {
        try {
            String category = objectMap.get(object);
            MenuList ml = categoryMap.get(category);
            logger.debug("WindowsMenu: removing object " + object
                         + " from category " + category + ".");
            int pos = ml.list.indexOf(object);
            ml.menu.remove(pos);
            ml.list.removeElementAt(pos);
            objectMap.remove(object);
            if (ml.list.isEmpty()) {
                remove(ml.menu);
                categoryMap.remove(category);
                if (categoryMap.isEmpty()) {
                    setEnabled(false);
                }
            }
        } catch (NullPointerException e) {
            logger.debug("WindowsMenu: " + e + ".");
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.debug("WindowsMenu: " + e + ".");
        }
    }

    /**
     * Returns the position of the first <code>menu</code> item
     * whose label is greater than (lexicographically, ignoring
     * case) the given <code>newItem</code>'s label. If there
     * aren't any existing items in the given menu, returns 0. If
     * the new item's label is greater than all existing menu
     * items, returns <code>menu.getItemCount()</code>.
     **/
    private static int findSortedPosition(JMenu menu, JMenuItem newItem) {
        int min = 0;
        int max = menu.getItemCount();
        int pos;
        int comp;
        JMenuItem item;
        String searchString = newItem.getText().toLowerCase();

        // logger.debug("Sorting in \""+searchString+"\"");
        while (max > min) {
            pos = (max + min) / 2;
            item = menu.getItem(pos);
            comp = searchString.compareTo(item.getText().toLowerCase());


            // logger.debug
            // 	("  try: max "+max+", min "+min+", pos "+pos+", comp "+comp
            // 	 +", item \""+item.getLabel().toLowerCase()+"\"");
            if (comp < 0) {
                // searchString is less than current item label
                max = pos;
            } else if (comp >= 0) {
                // searchString is greater than or equal to current item label
                min = pos + 1;
            }
        }
        return min;
    }

    /**
     * Data structure to group a menu with a corresponding
     * list of objects.
     **/
    private class MenuList {
        public final JMenu menu;
        public final Vector<Object> list;

        public MenuList(JMenu menu, Vector<Object> list) {
            this.menu = menu;
            this.list = list;
        }
    }
}