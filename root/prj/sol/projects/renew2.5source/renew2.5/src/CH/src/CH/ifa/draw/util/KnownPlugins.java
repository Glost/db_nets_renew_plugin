package CH.ifa.draw.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class KnownPlugins {
    private static final Map<String, String> knownPlugins = new HashMap<String, String>();

    static {
        knownPlugins.put("de.renew.gui.CPNDrawing", "Renew Gui Plugin");
        knownPlugins.put("de.renew.diagram.drawing.DiagramDrawing",
                         "Renew Diagram Plugin");
        knownPlugins.put("de.renew.agent.arm.diagram.ARMDrawing",
                         "Mulan ARM Plugin");
        knownPlugins.put("de.renew.agent.modeller.diagram.view.MASDiagramDrawing",
                         "Mulan KBE Plugin");
    }

    public static String guessPluginByClass(String className) {
        for (Entry<String, String> entry : knownPlugins.entrySet()) {
            if (entry.getKey().equals(className)) {
                return entry.getValue();
            }
        }
        return null;
    }
}