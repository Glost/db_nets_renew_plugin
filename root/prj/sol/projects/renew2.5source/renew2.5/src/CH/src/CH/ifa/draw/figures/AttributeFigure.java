/*
 * @(#)AttributeFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import org.freehep.graphicsio.HyperrefGraphics;

import CH.ifa.draw.framework.DrawingContext;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.standard.AbstractFigure;

import CH.ifa.draw.util.BSpline;
import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.io.IOException;

import java.util.Enumeration;


/**
 * A figure that can keep track of an open ended set of attributes.
 * The attributes are stored in a dictionary implemented by
 * FigureAttributes.
 *
 * @see CH.ifa.draw.framework.Figure
 * @see CH.ifa.draw.framework.Handle
 * @see FigureAttributes
 */


// It is assume by PolyLineFigure that AttributeFigure
// inherits directly from AbstractFigure.
public abstract class AttributeFigure extends AbstractFigure
        implements FigureWithID {

    /**
     * The default attributes associated with a figure.
     * If a figure doesn't have an attribute set, a default
     * value from this shared attribute set is returned.
     * @see #getAttribute
     * @see #setAttribute
     */
    private static FigureAttributes fgDefaultAttributes = null;
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -10857585979273442L;

    /**
     * Name of the attribute holding the ID required by
     * interface FigureWithID.
     * The ID is stored as an attribute to avoid changing
     * the data format written to streams.
     * @see CH.ifa.draw.framework.FigureWithID
     **/
    private static final String _idAttr = "FigureWithID";
    public static final String LINE_STYLE_NORMAL = "";
    public static final String LINE_STYLE_DOTTED = "1 2";
    public static final String LINE_STYLE_DASHED = "10";
    public static final String LINE_STYLE_MEDIUM_DASHED = "15 10";
    public static final String LINE_STYLE_LONG_DASHED = "20";
    public static final String LINE_STYLE_DASH_DOTTED = "7 3 1 3";
    public static final String LINE_WIDTH_KEY = "LineWidth";
    public static final Integer LINE_WIDTH_DEFAULT = 1;

    /**
     * The attributes of a figure. Each figure can have
     * an open ended set of attributes. Attributes are
     * identified by name.
     * @see #getAttribute
     * @see #setAttribute
     * @serial
     */
    private FigureAttributes fAttributes;
    @SuppressWarnings("unused")
    private int attributeFigureSerializedDataVersion = 1;

    protected AttributeFigure() {
    }

    public void internalDraw(Graphics g) {
        Color fill = getFillColor();
        if (!ColorMap.isTransparent(fill)) {
            g.setColor(fill);
            drawBackground(g);
        }
        Color frame = getFrameColor();
        if (!ColorMap.isTransparent(frame)) {
            g.setColor(frame);
            Graphics2D g2 = (Graphics2D) g;
            String lineStyle = getLineStyle();
            BasicStroke stroke = (BasicStroke) g2.getStroke();
            BasicStroke bs = new BasicStroke(getLineWidth(),
                                             stroke.getEndCap(),
                                             stroke.getLineJoin(),
                                             stroke.getMiterLimit(),
                                             lineStyle2ArrayOfFloat(lineStyle),
                                             stroke.getDashPhase());
            g2.setStroke(bs);
            drawFrame(g2);
            g2.setStroke(stroke);
        }
    }

    /**
     * Draws the figure in the given graphics. Draw is a template
     * method calling drawBackground followed by drawFrame.
     *
     * If it is sometimes required to override this method,
     * make internalDraw() more public and override that method.
     */
    public void draw(Graphics g) {
        if (isVisible()) {
            if (g instanceof HyperrefGraphics && hasAttribute("targetLocation")) {
                ((HyperrefGraphics) g).drawLink(displayBox(),
                                                (String) getAttribute("targetLocation"));
            }
            internalDraw(g);
            if (g instanceof HyperrefGraphics && hasAttribute("targetLocation")) {
                ((HyperrefGraphics) g).drawLinkEnd();
                ;
            }
        }
    }

    /**
     * Draws the figure in an  appearance according to the DrawingContext.
     * @param g the Graphics to draw into
     * @param dc the DrawingContext to obey
     */
    public void draw(Graphics g, DrawingContext dc) {
        if (!dc.isVisible(this)) {
            return;
        }
        if (dc.isHighlighted(this)) {
            Color fill = getFillColor();
            Color frame = getFrameColor();
            Color text = null;
            if (ColorMap.isTransparent(fill) || ColorMap.isBackground(fill)
                        || this instanceof PolyLineFigure) {
                if (this instanceof TextFigure) {
                    text = (Color) getAttribute("TextColor");
                    setTextColor(ColorMap.hilight(text));
                } else if (!ColorMap.isTransparent(frame)
                                   && !ColorMap.isBackground(fill)) {
                    setFrameColor(ColorMap.hilight(frame));
                }
            } else {
                setFillColor(ColorMap.hilight(fill));
            }
            internalDraw(g);
            setFillColor(fill);
            setFrameColor(frame);
            if (text != null) {
                setTextColor(text);
            }
        } else {
            internalDraw(g);
        }
    }

    /**
     * Draws the background of the figure.
     * @param g UNUSED
     * @see #draw
     */
    protected void drawBackground(Graphics g) {
    }

    /**
     * Draws the frame of the figure.
     * @param g UNUSED
     * @see #draw
     */
    protected void drawFrame(Graphics g) {
    }

    /**
     * Gets the fill color of a figure. This is a convenience
     * method.
     * @see #getAttribute
     */
    public Color getFillColor() {
        return (Color) getAttribute("FillColor");
    }

    /**
     * Gets the frame color of a figure. This is a convenience
     * method.
     * @see #getAttribute
     */
    public Color getFrameColor() {
        return (Color) getAttribute("FrameColor");
    }

    /**
     * Sets the fill color of a figure <b>without</b> issuing
     * a <tt>changed</tt> Event.
     */
    public void setFillColor(Color color) {
        if (fAttributes == null) {
            fAttributes = new FigureAttributes();
        }
        fAttributes.set("FillColor", color);
    }

    /**
     * Sets the frame color of a figure <b>without</b> issuing
     * a <tt>changed</tt> Event.
     */
    public void setFrameColor(Color color) {
        if (fAttributes == null) {
            fAttributes = new FigureAttributes();
        }
        fAttributes.set("FrameColor", color);
    }

    /**
     * Sets the text color of a figure <b>without</b> issuing
     * a <tt>changed</tt> Event.
     */
    public void setTextColor(Color color) {
        if (fAttributes == null) {
            fAttributes = new FigureAttributes();
        }
        fAttributes.set("TextColor", color);
    }

    //---- figure attributes ----------------------------------
    private static void initializeAttributes() {
        fgDefaultAttributes = new FigureAttributes();
        fgDefaultAttributes.set("FrameColor", Color.black);
        fgDefaultAttributes.set("FillColor", new Color(0x70DB93));
        fgDefaultAttributes.set("TextColor", Color.black);
        fgDefaultAttributes.set(TextFigure.ALIGN_ATTR,
                                new Integer(TextFigure.LEFT));
        fgDefaultAttributes.set("ArrowMode", new Integer(0));
        fgDefaultAttributes.set("FontName", "Helvetica");
        fgDefaultAttributes.set("FontSize", new Integer(12));
        fgDefaultAttributes.set("FontStyle", new Integer(Font.PLAIN));
        fgDefaultAttributes.set("LineShape",
                                new Integer(PolyLineFigure.LINE_SHAPE));
        fgDefaultAttributes.set("BSplineSegments",
                                new Integer(BSpline.DEFSEGMENTS));
        fgDefaultAttributes.set("BSplineDegree", new Integer(BSpline.DEFDEGREE));


        // Initialize the ID of this Figure to NOID.
        // (See comments to getID(), setID() and _idAttr below.)
        fgDefaultAttributes.set(_idAttr, new Integer(FigureWithID.NOID));
    }

    /**
     * Gets a the default value for a named attribute
     * @see #getAttribute
     */
    public static Object getDefaultAttribute(String name) {
        if (fgDefaultAttributes == null) {
            initializeAttributes();
        }
        return fgDefaultAttributes.get(name);
    }

    /**
     * Returns the named attribute or null if a
     * a figure doesn't have an attribute.
     * All figures support the attribute names
     * <code>FillColor</code> and <code>FrameColor</code>.
     */
    public Object getAttribute(String name) {
        if (fAttributes != null) {
            if (fAttributes.hasDefined(name)) {
                return fAttributes.get(name);
            }
        }
        Object supAttr = super.getAttribute(name);
        if (supAttr == null) {
            return getDefaultAttribute(name);
        } else {
            return supAttr;
        }
    }

    /**
     * Checks if this figure has defined attribute name
     * @param name Name of the attribute
     * @return true if the figure has defined attribute name
     */
    public Boolean hasAttribute(String name) {
        if (fAttributes != null && fAttributes.hasDefined(name)) {
            return true;
        }
        if (super.getAttribute(name) != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns the list of defined attribute keys for this figure
     *
     * @return Enumeration<String> the attribute names
     */
    public Enumeration<String> getAttributeKeys() {
        Enumeration<String> attr = new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        };
        if (fAttributes != null) {
            attr = fAttributes.definedAttributes();
        }
        return attr;
    }

    /**
     * Sets the named attribute to the new value
     */
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        if (fAttributes == null) {
            fAttributes = new FigureAttributes();
        }
        fAttributes.set(name, value);
        changed();
    }

    /**
     * Stores the Figure to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        super.write(dw);


        // The write() method of PolyLineFigure assumes that
        // the first thing that this method writes is a string
        // and not a number.
        if (fAttributes == null) {
            dw.writeString("no_attributes");
        } else {
            dw.writeString("attributes");
            fAttributes.write(dw);
        }
    }

    /**
     * Reads the Figure from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        String s = dr.readString();
        if (s.toLowerCase().equals("attributes")) {
            if (fAttributes == null) {
                fAttributes = new FigureAttributes();
            }
            fAttributes.read(dr);
            Enumeration<String> attrenumeration = fAttributes.definedAttributes();
            while (attrenumeration.hasMoreElements()) {
                String attr = attrenumeration.nextElement();
                Object val = fAttributes.get(attr);
                super.setAttribute(attr, val);
            }
        }

        // The line style attribute has been changed
        // from Integer to String, but I didn't want to
        // increase the storable data version for this.
        // (Current stream version is 8.)
        Object lineStyle = getAttribute("LineStyle");
        if (lineStyle instanceof Integer) {
            int value = ((Integer) lineStyle).intValue();
            if (value <= 0) {
                setAttribute("LineStyle", null);

            } else {
                setAttribute("LineStyle", lineStyle.toString());
            }
        }
    }

    // An additional line concerning this attribute has
    // been added to initializeDefaultAttributes.


    /**
     * Get the ID as required by interface FigureWithID.
     * @see CH.ifa.draw.framework.FigureWithID
     **/
    public int getID() {
        return ((Integer) getAttribute(_idAttr)).intValue();
    }

    /**
     * Set the ID as required by interface FigureWithID.
     * @see CH.ifa.draw.framework.FigureWithID
     **/
    public void setID(int id) {
        setAttribute(_idAttr, new Integer(id));
    }

    protected String getLineStyle() {
        String lineStyle = (String) getAttribute("LineStyle");
        if (lineStyle == null) {
            return LINE_STYLE_NORMAL;
        } else {
            return lineStyle;
        }
    }

    protected Integer getLineWidth() {
        Integer lineWidth = (Integer) getAttribute(LINE_WIDTH_KEY);
        if (lineWidth == null) {
            return LINE_WIDTH_DEFAULT;
        } else {
            return lineWidth;
        }
    }

    protected void setLineStyle(String lineStyle) {
        setAttribute("LineStyle", lineStyle);
    }

    protected BasicStroke getBasicStroke(String lineStyle) {
        float[] f = lineStyle2ArrayOfFloat(lineStyle);
        return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                               0, f, 0);
    }

    /**
     * @param lineStyle
     * @return
     */
    private float[] lineStyle2ArrayOfFloat(String lineStyle) {
        float[] f;

        // the guard seems redundant
        if (lineStyle != AttributeFigure.LINE_STYLE_NORMAL
                    && lineStyle.length() != 0) {
            String[] split = lineStyle.split(" ");
            f = new float[split.length];
            for (int i = 0; i < split.length; i++) {
                f[i] = Float.parseFloat(split[i]);
            }
        } else {
            f = null;
            // Apparently, { 1 } is the same as { 1, 1 } (i.e. very fine  dots)
            // This shows in Renew (screen), PNG and SVG as solid line.
            // In contrast, PS, PDF and EPS show the dotted line.
            // Both solutions (null and { 1, 0} work.
            // The values ({ 1 } and {1 1}) can now only be forced through the 
            // Line Style >  other ... dialogue.
            // f = new float[] { 1, 0 };
        }
        return f;
    }
}