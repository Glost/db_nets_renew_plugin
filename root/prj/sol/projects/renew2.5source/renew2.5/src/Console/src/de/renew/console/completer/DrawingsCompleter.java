/**
 *
 */
package de.renew.console.completer;

import jline.console.completer.Completer;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;

import de.renew.console.CHDependencyListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * @author cabac
 *
 */
public class DrawingsCompleter implements Completer {
    private NullCompleter nullCompleter;

    public DrawingsCompleter() {
        this.nullCompleter = new NullCompleter();
    }

    /* (non-Javadoc)
     * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        if (CHDependencyListener.drawPluginAvailable) {
            return new StringsCompleter(getDrawingNames()).complete(buffer,
                                                                    cursor,
                                                                    candidates);
        } else {
            return this.nullCompleter.complete(buffer, cursor, candidates);
        }
    }

    public ArrayList<String> getDrawingNames() {
        DrawApplication gui = DrawPlugin.getGui();
        ArrayList<String> drawingNames = new ArrayList<String>();
        if (gui != null) {
            Enumeration<Drawing> drawings = gui.drawings();
            while (drawings.hasMoreElements()) {
                Drawing drawing = (Drawing) drawings.nextElement();
                drawingNames.add(drawing.getName());
            }
        }
        return drawingNames;
    }
}