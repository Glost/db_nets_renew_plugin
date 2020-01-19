package de.renew.gui.xml;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNTextFigure;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;


public class XRNCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(XRNCreator.class);

    private static void writePoint(PrintWriter writer, String tag, Point point,
                                   String indentStr) {
        writer.println(indentStr + "<" + tag + " x=\""
                       + String.valueOf(point.x) + "\" y=\""
                       + String.valueOf(point.y) + "\"/>");
    }

    private static void writeColor(PrintWriter writer, String tag, Object obj,
                                   String indentStr) {
        if (obj instanceof Color) {
            Color color = (Color) obj;
            if (ColorMap.isTransparent(color)) {
                writer.println(indentStr + "<" + tag + "><transparent/></"
                               + tag + ">");
            } else if (ColorMap.isBackground(color)) {
                writer.println(indentStr + "<" + tag + "><background/></" + tag
                               + ">");
            } else {
                writer.println(indentStr + "<" + tag + "><RGBcolor r=\""
                               + color.getRed() + "\" g=\"" + color.getGreen()
                               + "\" b=\"" + color.getBlue() + "\"/></" + tag
                               + ">");
            }
        }
    }

    private static String quoteText(String data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length(); i++) {
            switch (data.charAt(i)) {
            case '>':
                buf.append("&gt;");
                break;
            case '<':
                buf.append("&lt;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            case '"':
                buf.append("&quot;");
                break;
            case '\'':
                buf.append("&apos;");
                break;
            default:
                buf.append(data.charAt(i));
            }
        }
        return buf.toString();
    }

    private static void writeSimpleElement(PrintWriter writer, String tag,
                                           String data, String indentStr) {
        writer.println(indentStr + "<" + tag + ">" + quoteText(data) + "</"
                       + tag + ">");
    }

    private static void writeGraphics(PrintWriter writer, Figure figure,
                                      String indentStr) {
        String indentStr2 = indentStr + "  ";
        writer.println(indentStr + "<graphics>");

        if (!(figure instanceof LineConnection)) {
            Dimension size = figure.size();
            writer.println(indentStr2 + "<size w=\"" + size.width + "\" h=\""
                           + size.height + "\"/>");
        }

        if (figure instanceof TextFigure) {
            Object fontSize = ((TextFigure) figure).getAttribute("FontSize");
            if (fontSize instanceof Integer) {
                writer.println(indentStr2 + "<textsize size=\"" + fontSize
                               + "\"/>");
            }
        }

        if (!(figure instanceof LineConnection)) {
            Figure parent = null;
            if (figure instanceof ChildFigure) {
                parent = ((ChildFigure) figure).parent();
            }
            Point offset = new Point(0, 0);
            if (parent != null) {
                offset = parent.center();
            }
            Point pos = figure.center();
            pos.translate(-offset.x, -offset.y);
            writePoint(writer, "offset", pos, indentStr2);
        }

        if (figure instanceof AttributeFigure) {
            AttributeFigure af = (AttributeFigure) figure;
            writeColor(writer, "fillcolor", af.getAttribute("FillColor"),
                       indentStr2);
            writeColor(writer, "pencolor", af.getAttribute("FrameColor"),
                       indentStr2);
            writeColor(writer, "textcolor", af.getAttribute("TextColor"),
                       indentStr2);
        }

        if (figure instanceof LineConnection) {
            LineConnection conn = (LineConnection) figure;
            int n = conn.pointCount();
            boolean inv = isInverted(conn);
            for (int i = 1; i < n - 1; i++) {
                Point p = conn.pointAt(inv ? n - i - 1 : i);
                writePoint(writer, "point", p, indentStr2);
            }
        }

        writer.println(indentStr + "</graphics>");
    }

    private static void writeAllChildren(PrintWriter writer, Figure figure,
                                         String indentStr,
                                         InscriptionTypeSource typeSource) {
        if (figure instanceof ParentFigure) {
            ParentFigure parent = (ParentFigure) figure;
            parent.center();
            FigureEnumeration children = new OrderedFigureEnumeration(parent
                                             .children());
            while (children.hasMoreElements()) {
                Figure child = children.nextFigure();
                if (child instanceof CPNTextFigure) {
                    CPNTextFigure textFigure = (CPNTextFigure) child;
                    writer.println(indentStr + "<annotation"
                                   + determineAttributes(textFigure, typeSource)
                                   + ">");
                    writeSimpleElement(writer, "text", textFigure.getText(),
                                       indentStr + "  ");
                    writeGraphics(writer, textFigure, indentStr + "  ");
                    writer.println(indentStr + "</annotation>");
                }
            }
        }
    }

    private static boolean isInverted(LineConnection conn) {
        Integer dir = (Integer) conn.getAttribute("ArrowMode");
        return new Integer(PolyLineFigure.ARROW_TIP_START).equals(dir);
    }

    private static String determineAttributes(Figure figure,
                                              InscriptionTypeSource typeSource) {
        StringBuffer result = new StringBuffer(" id=\"I");
        result.append(((FigureWithID) figure).getID());
        result.append("\"");

        if (figure instanceof LineConnection) {
            LineConnection conn = (LineConnection) figure;
            FigureWithID start = (FigureWithID) conn.start().owner();
            FigureWithID end = (FigureWithID) conn.end().owner();
            if (isInverted(conn)) {
                FigureWithID temp = start;
                start = end;
                end = temp;
            }
            result.append(" source=\"I" + start.getID() + "\" target=\"I"
                          + end.getID() + "\"");

            if (figure instanceof ArcConnection) {
                result.append(" type=\"");
                switch (((ArcConnection) conn).getArcType()) {
                case ShadowArc.both:
                    result.append("double");
                    break;
                case ShadowArc.test:
                    result.append("test");
                    break;
                case ShadowArc.inhibitor:
                    result.append("inhibitor");
                    break;
                case ShadowArc.doubleOrdinary:
                    result.append("multi-ordinary");
                    break;
                case ShadowArc.doubleHollow:
                    result.append("clear");
                    break;
                default:
                    result.append("ordinary");
                }
                result.append("\"");
            }
        } else if (figure instanceof CPNTextFigure) {
            CPNTextFigure cpnTextFig = (CPNTextFigure) figure;
            if (cpnTextFig.getType() == CPNTextFigure.LABEL) {
                result.append(" type=\"comment\"");
            } else if (cpnTextFig.getType() == CPNTextFigure.NAME) {
                result.append(" type=\"name\"");
            } else if (typeSource != null) {
                String type = typeSource.getType(cpnTextFig.getText(),
                                                 cpnTextFig.getType() == CPNTextFigure.AUX);
                if (type != null) {
                    result.append(" type=\"" + type + "\"");
                }
            }
        }
        return result.toString();
    }

    // clazz must be subclass of AttributeFigure.
    private static void writeAll(Class<?> clazz, String tag,
                                 PrintWriter writer, CPNDrawing drawing,
                                 InscriptionTypeSource typeSource,
                                 InscriptionTypeSource childTypeSource) {
        FigureEnumeration figures = new OrderedFigureEnumeration(drawing.figures());
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (clazz.isInstance(figure)) {
                if (!(figure instanceof ChildFigure
                            && ((ChildFigure) figure).parent() != null)) {
                    String attr = determineAttributes(figure, typeSource);
                    writer.println("  <" + tag + attr + ">");
                    if (figure instanceof TextFigure) {
                        writeSimpleElement(writer, "text",
                                           ((TextFigure) figure).getText(),
                                           "    ");

                    }
                    writeGraphics(writer, figure, "    ");
                    writeAllChildren(writer, figure, "    ", childTypeSource);
                    writer.println("  </" + tag + ">");
                }
            }
        }
    }

    public static void write(OutputStream stream, CPNDrawing drawing,
                             final ShadowCompilerFactory compilerFactory) {
        write(new PrintWriter(stream), drawing, compilerFactory);
    }

    public static void write(Writer writer, CPNDrawing drawing,
                             final ShadowCompilerFactory compilerFactory) {
        write(new PrintWriter(writer), drawing, compilerFactory);
    }

    public static void write(PrintWriter writer, CPNDrawing drawing,
                             final ShadowCompilerFactory compilerFactory) {
        try {
            writer.println("<?xml version=\"1.0\"?>");
            writer.println("<!DOCTYPE net SYSTEM "
                           + "\"http://www.informatik.uni-hamburg.de/TGI/renew/xrn1.dtd\">");
            writer.println("<net id=\"N\" type=\"hlnet\">");
            writeAll(de.renew.gui.PlaceFigure.class, "place", writer, drawing,
                     null,
                     new InscriptionTypeSource(compilerFactory) {
                    public String calculateType(String text, boolean special,
                                                ShadowNet net)
                            throws Exception {
                        return net.checkPlaceInscription(text, special);
                    }
                });
            writeAll(de.renew.gui.TransitionFigure.class, "transition", writer,
                     drawing, null,
                     new InscriptionTypeSource(compilerFactory) {
                    public String calculateType(String text, boolean special,
                                                ShadowNet net)
                            throws Exception {
                        return net.checkTransitionInscription(text, special);
                    }
                });
            writeAll(de.renew.gui.ArcConnection.class, "arc", writer, drawing,
                     null,
                     new InscriptionTypeSource(compilerFactory) {
                    public String calculateType(String text, boolean special,
                                                ShadowNet net)
                            throws Exception {
                        return net.checkArcInscription(text, special);
                    }
                });


            // Create the special non-graphical annotation that
            // carries the net's name. It might become graphical
            // in the future.
            writer.println("  <annotation id=\"A1\" type=\"name\">");
            writeSimpleElement(writer, "text", drawing.getName(), "    ");
            writer.println("  </annotation>");


            // Create the remaining annotations.
            writeAll(de.renew.gui.CPNTextFigure.class, "annotation", writer,
                     drawing,
                     new InscriptionTypeSource(compilerFactory) {
                    public String calculateType(String text, boolean special,
                                                ShadowNet net)
                            throws Exception {
                        return net.checkDeclarationNode(text, special);
                    }
                }, null);
            writer.println("</net>");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        writer.flush();
    }
}