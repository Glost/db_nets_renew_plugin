The CH package provides a massively modified version of the JHotDraw
drawing editor.
The main class, CH.ifa.draw.DrawPlugin, provides several interface methods
that can be used by other plugins to extend the functionality.
Right now, those objects include:

1. Menu handling
================

a) Menu commands
----------------
To add a menu to the JHotDraw Gui, you use the MenuManager class, an
instance of which can be obtained by the getMenuManager() method of the
current DrawPlugin.  It is VERY IMPORTANT that the menu/menu item you are
registering identifies itself, or it will not be added.

The identification is done by using the putClientProperty(); the key String
to use as its first parameter must be "ch.ifa.draw.menu.id" (which is
stored as the public static final String member MenuManager.ID_PROPERTY).
The object to be put into this property (i.e., the second parameter) must
be a unique String (it is encouraged to use your plugin's provision as a
prefix).  You must keep your menu object referenced so you can unregister
it again when your plugin is unloaded.  If removing a previously registered
menu causes its parent menu to become empty, that parent is also removed.

To create a separator in a menu, you must create a pseudo JMenuItem to
register it.  This Separator can be obtained by the
  MenuManager.SeparatorFactory
inner class.  It is instantiated by a String containing the prefix that you
use for your ID_PROPERTY.  Separators need (and can) not be unregistered;
rather, they are removed automatically.

registerMenu(String toplevel, JMenuItem toAdd)  
            - add the given Menu Item to the parent menu with the given label.
              If such a parent is not present, one is created.

unregisterMenu(JMenuItem item)
            - remove given item from the menu it is in.
              The item will be identified by the item's ID_PROPERTY.
              If removing the item causes a separator to become top item
              of the parent menu, that separator will be removed.

b) Windows menu entries
-----------------------
The DrawApplication automatically takes care that all open drawings are
added to the windows menu (and removed, respectively).  The drawings are
ordered in categories, the category is retrieved via the method getCatetory
in the Drawing interface.

Other windows that should appear in the Windows menu must be registered
explicitly at the CH.ifa.draw.application.WindowsMenu instance that can be
obtained via
  DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
Many tool dialogs might to register themselves under the Tools category,
which is defined as a constant:
  DrawPlugin.WINDOWS_CATEGORY_TOOLS

Public methods of the WindowsMenu object:
addDialog(category, dialog)
addFrame(category, frame)
			    - Register a window with the menu. Should be called every
                  time a window is opened. The window title is used as menu
                  entry.
removeDialog(dialog)
removeFrame(frame)
			    - Deregister a window from the menu. Should be called every
                  time a window is closed.

2. Input/output
===============
getIOHelper()   - the CH.ifa.draw.IOHelper Singleton collects all methods
                  concerning the loading and saving of files;
                  getLoadPath(), loadAndOpenDrawing(), and so on.
                  Check its Javadoc for more information.

3. Drawing access
=================
getDrawingEditor()  - returns the drawing editor
                      (the main window if active, or a pseudo editor object)

4. Import/export
================
getImportHolder()/getExportHolder()
                    - returns the management classes for drawing import/export.
                      Refer to the API Doku or source code for usage information,
                      and put some in here once you know how it works.
