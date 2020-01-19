/*
 * Created on May 26, 2004
 *
 */
package CH.ifa.draw;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;


/**
 * Workaround created to address JDK 1.4 bug 4911422
 * http://developer.java.sun.com/developer/bugParade/bugs/4911422.html
 * until JRE 1.5.
*/
public class WorkaroundMenuItemUI extends BasicMenuItemUI {
    private WorkaroundMenuItemUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new WorkaroundMenuItemUI();
    }

    protected MouseInputListener createMouseInputListener(JComponent c) {
        return new ModifiedMouseInputHandler();
    }

    private class ModifiedMouseInputHandler extends MouseInputHandler {
        public void mouseExited(MouseEvent mouseEvent) {
            MenuSelectionManager menuSelectionManager = MenuSelectionManager
                                                            .defaultManager();
            if ((mouseEvent.getModifiers()
                        & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK
                                  | InputEvent.BUTTON3_MASK)) != 0) {
                MenuSelectionManager.defaultManager()
                                    .processMouseEvent(mouseEvent);
            } else {
                MenuElement[] path = menuSelectionManager.getSelectedPath();
                if (path.length > 1
                            && !(path[path.length - 1] instanceof JPopupMenu)) {
                    menuSelectionManager.setSelectedPath(removeLastPathElement(path));
                }
            }
        }

        private MenuElement[] removeLastPathElement(MenuElement[] path) {
            MenuElement[] newPath = new MenuElement[path.length - 1];
            for (int i = 0; i < path.length - 1; i++) {
                newPath[i] = path[i];
            }
            return newPath;
        }
    }
}