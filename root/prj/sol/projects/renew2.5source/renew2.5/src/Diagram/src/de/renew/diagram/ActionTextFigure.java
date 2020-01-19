/**
 *
 */
package de.renew.diagram;



/**
 * @author cabac
 *
 */
public class ActionTextFigure extends DiagramTextFigure {
    private static final String ACTION = "action";

    /**
     *
     */
    public ActionTextFigure() {
        super();
        setAlternateText(ACTION);
    }

    /**
     * @param canBeConnected
     */
    public ActionTextFigure(boolean canBeConnected) {
        super(canBeConnected);
        setAlternateText(ACTION);
    }

    /**
     * @param text
     */
    public ActionTextFigure(String text) {
        super(text);
        setAlternateText(ACTION);
    }

    /**
     * @param text
     * @param isReadOnly
     */
    public ActionTextFigure(String text, boolean isReadOnly) {
        super(text, isReadOnly);
        setAlternateText(ACTION);
    }
}