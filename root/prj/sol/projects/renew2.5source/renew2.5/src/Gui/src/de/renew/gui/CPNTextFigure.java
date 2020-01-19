package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNode;

import java.awt.Color;
import java.awt.Font;

import java.io.IOException;


public class CPNTextFigure extends TextFigure implements ShadowHolder {
    public static final int LABEL = 0;
    public static final int INSCRIPTION = 1;
    public static final int NAME = 2;
    public static final int AUX = 3;
    public static final int COMM = 4;
    public static final int[] STYLE = new int[] { Font.ITALIC, Font.PLAIN, Font.BOLD, Font.ITALIC
                                      + Font.BOLD, Font.PLAIN };
    public static final CPNTextFigure Label = new CPNTextFigure(LABEL);
    public static final CPNTextFigure Inscription = new CPNTextFigure(INSCRIPTION);
    public static final CPNTextFigure Name = new CPNTextFigure(NAME);
    public static final CPNTextFigure Aux = new CPNTextFigure(AUX);
    public static final CPNTextFigure Comm = new CPNTextFigure(COMM);

    /**
     * Determines the semantic type of the text.
     * @serial
     **/
    protected int fType = LABEL;

    /**
     * The shadow of this inscription figure.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    protected transient ShadowInscription shadow = null;

    public CPNTextFigure() {
        this(LABEL);
    }

    public CPNTextFigure(int type) {
        this(type, true);
    }

    public CPNTextFigure(int type, boolean canBeConnected) {
        super(canBeConnected);
        fType = type;
        setAttribute("FontStyle", new Integer(STYLE[type]));
        if (fType == COMM) {
            setTextColor(Color.BLUE);
        }
        setAlignment(CENTER);
    }

    public int getType() {
        return fType;
    }

    public void setAttribute(String name, Object value) {
        if (name.equals("TextType")) {
            int newType = ((Integer) value).intValue();
            if (canBeParent(newType, parent())) {
                willChange();
                fType = newType;
                super.setAttribute("FontStyle", new Integer(STYLE[fType]));
            }
        } else {
            super.setAttribute(name, value);
        }
    }

    public Object getAttribute(String name) {
        if (name.equals("TextType")) {
            return new Integer(fType);
        }
        return super.getAttribute(name);
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fType);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fType = dr.readInt();
    }

    protected void readWithoutType(StorableInput dr) throws IOException {
        super.read(dr);
        fType = INSCRIPTION;
    }

    protected boolean canBeParent(int type, ParentFigure parent) {
        switch (type) {
        case LABEL:
            return true;
        case INSCRIPTION:
            /* return parent instanceof TransitionFigure ||
                     parent instanceof PlaceFigure ||
                     parent instanceof ArcConnection;
            */


            // Much better:
            return parent instanceof InscribableFigure;
        case AUX:
        case NAME:
            return parent instanceof TransitionFigure
                   || parent instanceof PlaceFigure;
        case COMM:
            return parent instanceof TransitionFigure
                   || parent instanceof PlaceFigure;
        }
        return false;
    }

    public boolean canBeParent(ParentFigure parent) {
        if (super.canBeParent(parent)) {
            return canBeParent(fType, parent);
        }
        return false;
    }

    public void release() {
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    // Build a shadow in the given shadow net.
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = null;
        if (fType != LABEL && parent() instanceof ShadowHolder) {
            ShadowInscribable parentShadow = (ShadowInscribable) ((ShadowHolder) parent())
                                             .getShadow();
            if (parentShadow != null) {
                if ((fType == INSCRIPTION) || (fType == AUX)) {
                    shadow = new ShadowInscription(parentShadow, getText());
                    shadow.context = this;
                    shadow.setID(this.getID());
                    shadow.setSpecial(fType == AUX);
                } else if (fType == NAME && parentShadow instanceof ShadowNode) {
                    ((ShadowNode) parentShadow).setName(getText());
                } else if (fType == COMM && parentShadow instanceof ShadowNode) {
                    ((ShadowNode) parentShadow).setComment(getText());
                }
            }
        }
        return shadow;
    }

    // Get the associated shadow, if any.
    public ShadowNetElement getShadow() {
        return shadow;
    }
}