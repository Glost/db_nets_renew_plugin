/**
 *
 */
package de.renew.diagram;



/**
 * @author cabac
 *
 */
public class DCServiceTextFigure extends DiagramTextFigure {
    private static final String EXCHANGE = "ex";

    /**
     *
     */
    public DCServiceTextFigure() {
        super();
        setAlternateText(EXCHANGE);
    }

    /**
     * @param canBeConnected
     */
    public DCServiceTextFigure(boolean canBeConnected) {
        super(canBeConnected);
        setAlternateText(EXCHANGE);
    }

    /**
     * @param text
     */
    public DCServiceTextFigure(String text) {
        super(text);
        setAlternateText(EXCHANGE);
    }

    /**
     * @param text
     * @param isReadOnly
     */
    public DCServiceTextFigure(String text, boolean isReadOnly) {
        super(text, isReadOnly);
        setAlternateText(EXCHANGE);
    }
}