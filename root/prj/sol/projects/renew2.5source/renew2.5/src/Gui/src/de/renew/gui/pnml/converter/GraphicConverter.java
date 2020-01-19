/*
 * Created on 18.07.2003
 */
package de.renew.gui.pnml.converter;

import CH.ifa.draw.figures.PolyLineFigure;

import java.awt.Color;

import java.util.StringTokenizer;


/**
 * @author 0schlein
 */
public class GraphicConverter {
    private static GraphicConverter _instance;

    protected GraphicConverter() {
        super();
    }

    public static GraphicConverter instance() {
        if (_instance == null) {
            _instance = new GraphicConverter();
        }
        return _instance;
    }

    public String parseRenewLineStyle(String style) {
        String result = "solid";
        if (style != null) {
            if (style.equals(PolyLineFigure.LINE_STYLE_DASHED)) {
                result = "dash";
            }
            if (style.equals(PolyLineFigure.LINE_STYLE_DOTTED)) {
                result = "dot";
            }
        }
        return result;
    }

    public String parsePNMLLineStyle(String style) {
        String result = PolyLineFigure.LINE_STYLE_NORMAL;
        if (style.equals("dash")) {
            result = PolyLineFigure.LINE_STYLE_DASHED;
        } else if (style.equals("dot")) {
            result = PolyLineFigure.LINE_STYLE_DOTTED;
        }
        return result;

    }

    /**
     * Parses the given String for a valid CSS2 color.
     * @throws IllegalArgumentException() if its not a valid CSS2 string
     * @param color to be parsed
     * @return the color object found in the string
     */
    public Color parseCSS2Color(String color) {
        Color result = null;
        if (color.equalsIgnoreCase("aqua")) {
            result = new Color(0, 0xFF, 0xFF);
        } else if (color.equalsIgnoreCase("black")) {
            result = new Color(0, 0, 0);
        } else if (color.equalsIgnoreCase("blue")) {
            result = new Color(0, 0, 0xFF);
        } else if (color.equalsIgnoreCase("fuchsia")) {
            result = new Color(0xFF, 0, 0xFF);
        } else if (color.equalsIgnoreCase("gray")) {
            result = new Color(0x80, 0x80, 0x80);
        } else if (color.equalsIgnoreCase("green")) {
            result = new Color(0, 0x80, 0);
        } else if (color.equalsIgnoreCase("lime")) {
            result = new Color(0, 0xFF, 0);
        } else if (color.equalsIgnoreCase("maroon")) {
            result = new Color(0x80, 0, 0);
        } else if (color.equalsIgnoreCase("navy")) {
            result = new Color(0, 0, 0x80);
        } else if (color.equalsIgnoreCase("olive")) {
            result = new Color(0x80, 0x80, 0);
        } else if (color.equalsIgnoreCase("purple")) {
            result = new Color(0x80, 0, 0x80);
        } else if (color.equalsIgnoreCase("red")) {
            result = new Color(0xFF, 0, 0);
        } else if (color.equalsIgnoreCase("silver")) {
            result = new Color(0xC0, 0xC0, 0xC0);
        } else if (color.equalsIgnoreCase("teal")) {
            result = new Color(0, 0x80, 0x80);
        } else if (color.equalsIgnoreCase("white")) {
            result = new Color(0xFF, 0xFF, 0xFF);
        } else if (color.equalsIgnoreCase("yellow")) {
            result = new Color(0xFF, 0xFF, 0);
        } else if (color.startsWith("#")) {
            color = color.substring(1);
            if (color.length() == 3) {
                char[] string = { color.charAt(0), color.charAt(0), color.charAt(1), color
                                                                                     .charAt(1), color
                                                                                                 .charAt(2), color
                                                                                                             .charAt(2) };
                color = new String(string);
            }
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            result = new Color(r, g, b);

        } else if (color.startsWith("rgb(")) {
            color = color.substring(4, color.length() - 1);
            StringTokenizer tokenizer = new StringTokenizer(color, ",");
            if (tokenizer.countTokens() == 3) {
                int r = Integer.parseInt(tokenizer.nextToken());
                int g = Integer.parseInt(tokenizer.nextToken());
                int b = Integer.parseInt(tokenizer.nextToken());
                result = new Color(r, g, b);
            }
        }
        if (result == null) {
            throw new IllegalArgumentException();
        }
        return result;
    }
}