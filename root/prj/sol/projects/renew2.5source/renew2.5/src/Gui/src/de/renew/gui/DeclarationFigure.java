/*
 * @(#)DeclarationFigure.java 5.1
 *
 */
package de.renew.gui;

import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;


public class DeclarationFigure extends CPNTextFigure implements ShadowHolder {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -7877776241236946511L;
    @SuppressWarnings("unused")
    private int declFigureSerializedDataVersion = 1;

    /**
     * The shadow of this declaration figure.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    private transient ShadowDeclarationNode shadow = null;

    public DeclarationFigure() {
        super(INSCRIPTION, false);
        setAlignment(LEFT);
    }

    public void release() {
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    public ShadowNetElement buildShadow(ShadowNet net) {
        if (shadow != null) {
            shadow.discard();
        }
        shadow = new ShadowDeclarationNode(net, getText());
        shadow.context = this;
        shadow.setID(this.getID());


        // No longer calling the syntax check. This is done
        // after editing the text and in no other place should a
        // syntax check occur.
        return shadow;
    }

    public ShadowNetElement getShadow() {
        return shadow;
    }
}