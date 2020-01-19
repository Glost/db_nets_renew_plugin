package CH.ifa.draw.util;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;


/**
 * @version         1.0
 * @author                "Timo Carl" <6carl@informatik.uni-hamburg.de>
 */
public class HotDrawFocusManager extends DefaultKeyboardFocusManager {
    private Component component;

    public HotDrawFocusManager(Component component) {
        super();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(this);
        this.component = component;
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        boolean result = super.dispatchKeyEvent(e);
        if ((e.getModifiers()
                    & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0
                    && !e.isConsumed()) {
            redispatchEvent(component, e);
        }
        return result;
    }
}