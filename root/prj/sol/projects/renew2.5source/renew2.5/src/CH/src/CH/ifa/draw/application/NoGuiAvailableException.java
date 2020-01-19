/*
 * Created on 20.06.2003
 *
 */
package CH.ifa.draw.application;



/**
 * This Exception is thrown by the GuiPlugin Plugin
 * if the Gui Window is not open but
 * methods that depend it are called.
 *
 * @author 6schumac
 */
public class NoGuiAvailableException extends IllegalStateException {
    public NoGuiAvailableException() {
        super("No Gui Window open.");
    }

    public NoGuiAvailableException(String s) {
        super(s);
    }
}