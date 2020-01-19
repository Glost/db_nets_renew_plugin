/*
 * Created on Apr 16, 2003
 */
package de.renew.fa.figures;

import CH.ifa.draw.framework.ParentFigure;

import de.renew.fa.FAPlugin;

import de.renew.gui.CPNTextFigure;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;


/**
 * A FATextFigure is a TextFigure that can be used in a FADrawing.
 *
 * @author Lawrence Cabac
 */
public class FATextFigure extends CPNTextFigure {
    static final long serialVersionUID = -7270464173137023567L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FATextFigure.class);
    public static final FATextFigure Name = new FATextFigure(CPNTextFigure.NAME);
    public static final FATextFigure Inscription = new FATextFigure(CPNTextFigure.INSCRIPTION);

    public FATextFigure() {
        this(INSCRIPTION, true);
    }

    public FATextFigure(int type) {
        this(type, true);
        logger.debug("FATextFigure of type " + this.fType + " created");
    }

    public FATextFigure(int type, boolean canBeConnected) {
        super(type, canBeConnected);
    }

    public FATextFigure(int type, String text) {
        this(type, true);
        setText(text);
    }

    @Override
    protected void drawLine(Graphics g, int i) {
        Font font = getLineFont(i); //NOTICEsignature
        g.setFont(font);

        boolean useIndices = FAPlugin.getCurrent().getUseIndices();
        int x = getLineBox(g, i).x;
        int y = getLineBox(g, i).y;
        String text = getLine(i);
        if (!useIndices || text == null || text.equals("")) {
            g.drawString(text, x, y + getMetrics(font, g).getAscent());
        } else {
            AttributedString aStr = getFAText(text);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawString(aStr.getIterator(), x,
                           y + (getMetrics(font, g).getAscent()));
        }
    }

    private AttributedString getFAText(String text) {
        // Create an AttributedString of the text that has superscripted index, if any
        String strippedtext = text.replaceFirst("_\\{(.*)\\}", "$1");
        strippedtext = strippedtext.replaceFirst("_", "");
        AttributedString aStr = new AttributedString(strippedtext);


        if (!"".equals(text)) {
            aStr.addAttribute(TextAttribute.SIZE,
                              ((Integer) getAttribute("FontSize")).intValue()
                              + 2);
            if (getType() == CPNTextFigure.NAME) {
                aStr.addAttribute(TextAttribute.WEIGHT,
                                  TextAttribute.WEIGHT_BOLD, 0,
                                  strippedtext.length());
            }
        }

        if (!text.contains("_")) {
            return aStr;
        }
        if (!text.contains("{")) {
            int index = text.indexOf('_');
            aStr.addAttribute(TextAttribute.SUPERSCRIPT,
                              TextAttribute.SUPERSCRIPT_SUB, index,
                              strippedtext.length());

            return aStr;
        }

        // Determine the position of the part to be superscripted
        boolean indexswitch = false;
        boolean bracketswitch = false;

        int pos = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

//                System.out.println("c "+c+" i "+ i +" pos "+pos);
            if (c == '_') {
                indexswitch = true;
                continue;
            }
            if (indexswitch) {
                if (c == '}') {
                    bracketswitch = false;
                    indexswitch = false;
                    continue;
                } else if (c == '{') {
                    bracketswitch = true;
                    continue;
                }
            }
            if (indexswitch && bracketswitch) {
                aStr.addAttribute(TextAttribute.SUPERSCRIPT,
                                  TextAttribute.SUPERSCRIPT_SUB, pos + 0,
                                  pos + 1);
            }
            pos++;
        }

//        AttributedString aStr = new AttributedString(text);
//        AttributedCharacterIterator it = aStr.getIterator();
//
//        //        System.out.println("Iterator "+it.getBeginIndex() +" "+ it.getEndIndex());
//        for (int j = it.getBeginIndex(); j < it.getEndIndex(); j++) {
//            char c = it.next();
//            if(c == '_'){
//                aStr.addAttribute(TextAttribute., value);
//            }
//            
//            //            System.out.println("char "+c);
//            if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
//                        || c == '5' || c == '6' || c == '7' || c == '8'
//                        || c == '9') {
//                aStr.addAttribute(TextAttribute.SUPERSCRIPT,
//                                  TextAttribute.SUPERSCRIPT_SUB, j + 1, j + 2);
//                //                System.out.println("char " + c + " index "+ (j+1));
//            }
//            
//        }
//
        //        System.out.println(aStr+ " "+ aStr.getIterator());
        return aStr;
    }

    /**
    * Calculates the Dimension of the given line, considering occuring indices
    *
    * @param i
    *  The index of the current line.
    * @param g
    *  Could be null.
    * @return
    */
    @Override
    public Dimension getLineDimension(int i, Graphics g) {
        FAPlugin plugin = FAPlugin.getCurrent();
        boolean useIndices = false;
        if (plugin != null) {
            useIndices = plugin.getUseIndices();
        }
        if (!useIndices) {
            return super.getLineDimension(i, g);
        }
        FontMetrics metrics = getMetrics(getLineFont(i), g);
        AttributedString faText = getFAText(getLine(i));
        AttributedCharacterIterator it = faText.getIterator();

        //LineMetrics lmetrics = getMetrics(getLineFont(0), g).getLineMetrics(it, it.getBeginIndex(), it.getEndIndex(), g);
        //FontMetrics metrics2 = getMetrics(getLineFont(0), g);
        Rectangle2D stringBounds = metrics.getStringBounds(it,
                                                           it.getBeginIndex(),
                                                           it.getEndIndex(), g);
        double width = 0;
        double heightaddition = 0;
        it.first();
        for (int idx = 0; idx < it.getEndIndex(); idx++) {
            char c = it.current();
            if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
                        || c == '5' || c == '6' || c == '7' || c == '8'
                        || c == '9') {
                width += metrics.charWidth(c) / 1.5;
                heightaddition = metrics.getHeight() / 3;
            } else {
                width += metrics.charWidth(c);
            }
//           System.out.println("Character "+c +" "+width);
            it.next();
        }

        return new Dimension((int) width,
                             (int) stringBounds.getBounds().getHeight()
                             + (int) heightaddition);
    }

    @Override
    protected boolean canBeParent(int type, ParentFigure parent) {
        switch (type) {
        case LABEL:
            return true;
        case INSCRIPTION:
            return parent instanceof FAArcConnection;
        case AUX:
        case NAME:
            return parent instanceof FAArcConnection
                   || parent instanceof FAStateFigure;
        }
        return false;
    }

    public String getName() {
        String cln = getClass().getName();
        int ind = cln.lastIndexOf('.') + 1;
        if (ind > 0) {
            cln = cln.substring(ind);
        }

        return cln + "(" + this.getText() + ")";
    }

    @Override
    public String toString() {
        return this.getText();
    }
}