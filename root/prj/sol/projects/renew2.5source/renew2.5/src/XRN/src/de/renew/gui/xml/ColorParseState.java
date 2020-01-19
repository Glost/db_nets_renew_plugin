package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.AttributeFigure;

import CH.ifa.draw.util.ColorMap;

import java.awt.Color;


class ColorParseState implements ParseState {
    ParseState env;
    AttributeFigure figure;
    String attr;
    StringBuffer value;
    int red;
    int green;
    int blue;

    ColorParseState(ParseState env, AttributeFigure figure, String attr) {
        this.env = env;
        this.figure = figure;
        this.attr = attr;
    }

    public ParseState startElement(String name, Attributes atts) {
        if ("RGBcolor".equals(name)) {
            int red = 0;
            int green = 0;
            int blue = 0;
            int n = atts.getLength();
            for (int i = 0; i < n; i++) {
                String att = atts.getQName(i);
                int num = Integer.parseInt(atts.getValue(i));
                if ("r".equals(att)) {
                    red = num;
                } else if ("g".equals(att)) {
                    green = num;
                } else if ("b".equals(att)) {
                    blue = num;
                }
            }
            figure.setAttribute(attr, new Color(red, green, blue));
        } else if ("transparent".equals(name)) {
            figure.setAttribute(attr, ColorMap.NONE);
        } else if ("background".equals(name)) {
            figure.setAttribute(attr, ColorMap.BACKGROUND);
        }

        return new IgnoreElementState(this);
    }

    public ParseState endElement(String name) {
        return env;
    }

    public ParseState characters(char[] ch, int start, int length) {
        return this;
    }
}