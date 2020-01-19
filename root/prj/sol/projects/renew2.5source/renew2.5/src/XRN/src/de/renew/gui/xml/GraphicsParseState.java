package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.ChildFigure;

import java.awt.Point;
import java.awt.Rectangle;


class GraphicsParseState implements ParseState {
    ParseState env;
    AttributeFigure figure;
    StringBuffer value;

    GraphicsParseState(ParseState env, AttributeFigure figure) {
        this.env = env;
        this.figure = figure;
    }

    public Point parseXY(Attributes atts, String xa, String ya) {
        int x = 0;
        int y = 0;

        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            String att = atts.getQName(i);
            int num = Integer.parseInt(atts.getValue(i));
            if (xa.equals(att)) {
                x = num;
            } else if (ya.equals(att)) {
                y = num;
            }
        }
        return new Point(x, y);
    }

    public ParseState startElement(String name, Attributes atts) {
        if ("fillcolor".equals(name)) {
            return new ColorParseState(this, figure, "FillColor");
        } else if ("pencolor".equals(name)) {
            return new ColorParseState(this, figure, "FrameColor");
        } else if ("textcolor".equals(name)) {
            return new ColorParseState(this, figure, "TextColor");
        } else if ("size".equals(name)) {
            Point p = parseXY(atts, "w", "h");
            Point pos = figure.center();
            Rectangle box = figure.displayBox();
            figure.displayBox(new Point(box.x, box.y),
                              new Point(box.x + p.x, box.y + p.y));
            Point newPos = figure.center();
            figure.moveBy(pos.x - newPos.x, pos.y - newPos.y);
            return new IgnoreElementState(this);
        } else if ("offset".equals(name)) {
            Point p = parseXY(atts, "x", "y");
            Point pos = figure.center();
            int oldX = pos.x;
            int oldY = pos.y;
            if (figure instanceof ChildFigure
                        && ((ChildFigure) figure).parent() != null) {
                Point parentPos = ((ChildFigure) figure).parent().center();
                oldX = oldX - parentPos.x;
                oldY = oldY - parentPos.y;
            }
            figure.moveBy(p.x - oldX, p.y - oldY);
            return new IgnoreElementState(this);
        } else if ("point".equals(name)) {
            Point p = parseXY(atts, "x", "y");
            if (figure instanceof LineConnection) {
                LineConnection conn = (LineConnection) figure;
                conn.insertPointAt(p, conn.pointCount() - 1);
            }
            return new IgnoreElementState(this);
        } else if ("textsize".equals(name)) {
            String val = atts.getValue("size");
            if (val != null) {
                Integer size = Integer.valueOf(val);
                figure.setAttribute("FontSize", size);
            }
            return new IgnoreElementState(this);
        } else {
            // This is an unknown subelement.
            return new IgnoreElementState(this);
        }
    }

    public ParseState endElement(String name) {
        return env;
    }

    public ParseState characters(char[] ch, int start, int length) {
        return this;
    }
}