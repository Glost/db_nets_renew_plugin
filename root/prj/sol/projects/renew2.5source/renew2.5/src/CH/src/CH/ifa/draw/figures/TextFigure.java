/*
 * @(#)TextFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.OffsetLocator;
import CH.ifa.draw.standard.RelativeLocator;
import CH.ifa.draw.standard.TextHolder;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.ExtendedFont;
import CH.ifa.draw.util.Fontkit;
import CH.ifa.draw.util.GUIProperties;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.StringTokenizer;
import java.util.Vector;


/**
 * A text figure.
 *
 * @see TextTool
 */
public class TextFigure extends AttributeFigure implements ChildFigure,
                                                           TextHolder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TextFigure.class);
    private static String fgCurrentFontName = "SansSerif";
    private static int fgCurrentFontSize = 12;
    private static int fgCurrentFontStyle = Font.PLAIN;
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 4599820785949456124L;

    /**
     * The name of the attribute which determines the text
     * alignment. To query or set the attribute, you can use
     * the <code>set</code>- or <code>getAlignment()</code>
     * methods. But it is also possible to use <code>set</code>-
     * or <code>getAttribute()</code> with this name.
     * <p>
     * Valid values for the attribute are {@link #LEFT},
     * {@link #CENTER} and {@link #RIGHT}.
     * </p>
     **/
    public static final String ALIGN_ATTR = "TextAlignment";

    /** Specifies left-justified text, if set as {@link #ALIGN_ATTR}. */
    public static final int LEFT = 0;

    /** Specifies centered text, if set as {@link #ALIGN_ATTR}. */
    public static final int CENTER = 1;

    /** Specifies right-justified text, if set as {@link #ALIGN_ATTR}. */
    public static final int RIGHT = 2;

    static {
        int defaultFontSize = GUIProperties.defaultFontSize();
        if (defaultFontSize != -1) {
            fgCurrentFontSize = defaultFontSize;
            logger.debug("Setting default font size to " + fgCurrentFontSize
                         + " pt.");
        }
    }

    /**
     * The x coordinate of the upper left corner of this
     * figure.
     * @serial
     **/
    private int fOriginX;

    /**
     * The y coordinate of the upper left corner of this
     * figure.
     * @serial
     **/
    private int fOriginY;

    // cache of the TextFigure's size
    transient private boolean fSizeIsDirty = true;
    transient private int fWidth;
    transient private int fHeight;
    transient private Rectangle[] boxes; // cache for display boxes of lines

    /**
     * The text to be displayed as a whole.
     * @serial
     **/
    private String fText;

    /**
     * The text to be displayed, broken into lines.
     * <p>
     * This field is derived from <code>fText</code> each
     * time the text is modified by <code>setText()</code>.
     * It won't be written to a <code>StorableOutput</code>,
     * instead it will be recalculated in the read method.
     * However, the field gets serialized.
     * </p>
     * @serial
     **/
    private String[] fLines;

    /**
     * The font used to draw the text.
     * If <code>null</code>, the font will be retrieved
     * by using the values of <code>fCurrentFontName</code>,
     * -<code>Size</code> and -<code>Style</code>.
     * Therefore this field is transient.
     **/
    private transient Font fFont = null;

    /**
     * The name of the font to be used to display the text.
     * This field can be queried or modified as an attribute
     * named <code>"FontName"</code>.
     * @serial
     **/
    private String fCurrentFontName = fgCurrentFontName;

    /**
     * The size of the font to be used to display the text.
     * This field can be queried or modified as an attribute
     * named <code>"FontSize"</code>.
     * @serial
     **/
    private int fCurrentFontSize;

    /**
     * The style of the font to be used to display the text.
     * This field can be queried or modified as an attribute
     * named <code>"FontStyle"</code>.
     * @serial
     **/
    private int fCurrentFontStyle = fgCurrentFontStyle;

    /**
     * Determines whether the text can be edited.
     * @serial
     **/
    private boolean fIsReadOnly;

    /**
     * Determines whether this text figure can be connected
     * to other figures as a {@link ChildFigure}.
     * <p>
     * When written to a <code>StorableOutput</code>, this
     * field is omitted. Instead, it will default on restoration
     * to <code>true</code>, if it is currently connected,
     * to a figure, and <code>false</code>, if it's not.
     * However, the field is serialized.
     * </p>
     * @serial
     **/
    private boolean fCanBeConnected = true;

    /**
     * The figure to which this text figure is attached.
     * if <code>null</code>, it is not attached.
     * <p>
     * On restoration from <code>StorableInput</code> (but
     * not on deserialization), the parent figure has to
     * be informed about this child again.
     * </p>
     * @serial
     * @see CH.ifa.draw.standard.AbstractFigure#children
     * @see CH.ifa.draw.standard.AbstractFigure#addChild
     **/
    private ParentFigure fParent = null;

    /**
     * This locator is used only if the text figure is
     * connected to a parent figure. Then it determines
     * the location of this text figure by pointing to
     * its center (or where the center should be).
     * @serial
     **/
    private OffsetLocator fLocator = null;
    @SuppressWarnings("unused")
    private int textFigureSerializedDataVersion = 1;

    public TextFigure() {
        this("");
    }

    public TextFigure(boolean canBeConnected) {
        this("");
        fCanBeConnected = canBeConnected;
    }

    public TextFigure(String text) {
        fCurrentFontSize = fgCurrentFontSize;
        fOriginX = 0;
        fOriginY = 0;
        setAttribute("FillColor", ColorMap.color("None"));
        setAttribute("FrameColor", ColorMap.color("None"));
        internalSetText(text);
        fSizeIsDirty = true;
    }

    protected static FontMetrics getDefaultFontMetrics(Font font) {
        BufferedImage bi = new BufferedImage(1, 1,
                                             BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = bi.getGraphics();
        FontMetrics fm = g.getFontMetrics(font);
        g.dispose();
        bi = null;
        return fm;
    }

    public TextFigure(String text, boolean isReadOnly) {
        this(text);
        setReadOnly(isReadOnly);
    }

    public void moveBy(int x, int y) {
        willChange();
        basicMoveBy(x, y);
        if (fLocator != null) {
            fLocator.moveBy(x, y);
        }
        changed();
    }

    protected void basicMoveBy(int x, int y) {
        fOriginX += x;
        fOriginY += y;
    }

    public void basicDisplayBox(Point newOrigin, Point newCorner) {
        fOriginX = newOrigin.x;
        fOriginY = newOrigin.y;
    }

    public Point getOrigin() {
        return new Point(fOriginX, fOriginY);
    }

    public Rectangle displayBox() {
        textExtent();
        return new Rectangle(fOriginX, fOriginY, fWidth, fHeight);
    }

    public Rectangle textDisplayBox() {
        return displayBox();
    }

    protected String getLine(int lineIndex) {
        return fLines[lineIndex];
    }

    //NOTICEsignature
    protected int getLineAlignment(int lineIndex) {
        return getAlignment();
    }

    //NOTICEsignature
    protected Font getLineFont(int lineIndex) {
        return getFont();
    }

    /**
     *
     * @param
     *  font
     * @param g
     *  The current Graphics context. null if no Graphics object is available.
     * @return
     *  Font metrics specified by the Font and Graphics.
     *  If Graphics is null default FontMetrics will be returned.
     */
    public static FontMetrics getMetrics(Font font, Graphics g) {
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(font);
            return fm;
        }
        logger.trace("Using default font metrics because no Graphics object is at hand. ");
        return getDefaultFontMetrics(font);
    }

    public Rectangle getLineBox(Graphics g, int i) {
        if (fSizeIsDirty) {
            getLineBoxes(g);
        }
        return boxes[i];
    }

    /**
     *
     * @param i
     *  The index of the current line.
     * @param g
     *  Could be null.
     * @return
     */
    protected Dimension getLineDimension(int i, Graphics g) {
        FontMetrics metrics = getMetrics(getLineFont(i), g); //NOTICEsignature
        return new Dimension(metrics.stringWidth(getLine(i)),
                             metrics.getHeight());
    }

    /**
     *
     * @param g
     *  Could be null.
     * @return
     *  For every line of this TextFigure the Rectangle that encloses that line.
     */
    protected Rectangle[] getLineBoxes(Graphics g) {
        if (fSizeIsDirty) {
            boxes = new Rectangle[fLines.length];
            int oldWidth = fWidth;
            fWidth = 0;
            fHeight = 0;
            for (int i = 0; i < fLines.length; i++) {
                Dimension dim = getLineDimension(i, g);
                boxes[i] = new Rectangle(0, fHeight, dim.width, dim.height);
                fWidth = Math.max(fWidth, dim.width);
                fHeight += dim.height;
            }
            for (int i = 0; i < fLines.length; i++) {
                int alignment = getLineAlignment(i); //NOTICEsignature
                if (alignment != LEFT) {
                    int dx = fWidth - boxes[i].width;
                    if (alignment == CENTER) {
                        dx /= 2;
                    }
                    boxes[i].translate(dx, 0);
                }
            }


            // As a side effect, reposition the whole text figure
            // according to its alignment. This should have an
            // effect only if the width of the figure has changed.
            if (oldWidth != 0) {
                switch (getAlignment()) {
                case RIGHT:
                    fOriginX = fOriginX + oldWidth - fWidth;
                    if (fLocator != null) {
                        fLocator.moveBy((oldWidth - fWidth) / 2, 0);
                    }
                    break;
                case CENTER:
                    fOriginX = fOriginX + (oldWidth - fWidth) / 2;
                    // offsetLocator remains unchanged, when centered
                    break;
                default:
                    // fOriginX remains unchanged, when left-justified.
                    if (fLocator != null) {
                        fLocator.moveBy(-(oldWidth - fWidth) / 2, 0);
                    }
                    break;
                }
            }
            fSizeIsDirty = false;
        }
        return boxes;
    }

    /**
     * Checks if a point is inside the figure.
     */
    public boolean containsPoint(int x, int y) {
        if (super.containsPoint(x, y)) {
            if (!ColorMap.isTransparent(getFrameColor())) {
                return true; // regard as box!
            }


            // check if the point lies within some line's box:
            x -= fOriginX;
            y -= fOriginY;
            Rectangle[] boxes = getLineBoxes(null);
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether this figure is read only.
     */
    public boolean readOnly() {
        return fIsReadOnly;
    }

    /**
     * Sets the read only status of the text figure.
     */
    public void setReadOnly(boolean isReadOnly) {
        fIsReadOnly = isReadOnly;
    }

    /**
     * Gets the font.
     */
    public Font getFont() {
        if (fFont == null) {
            fFont = Fontkit.getFont(fCurrentFontName, fCurrentFontStyle,
                                    fCurrentFontSize);
        }
        return fFont;
    }

    /**
     * Sets the font.
     */
    public void setFont(Font newFont) {
        willChange();
        fFont = newFont;
        fCurrentFontName = fFont.getName();
        fCurrentFontStyle = fFont.getStyle();
        fCurrentFontSize = fFont.getSize();
        markDirty();
        changed();
    }

    /**
     * Updates the location whenever the figure changes itself.
     */
    public void changed() {
        super.changed();
        updateLocation();
    }

    public int getAlignment() {
        return ((Integer) getAttribute(ALIGN_ATTR)).intValue();
    }

    public void setAlignment(int alignment) {
        setAttribute(ALIGN_ATTR, new Integer(alignment));
    }

    /**
     * A text figure understands the "FontSize", "FontStyle", and "FontName"
     * attributes.
     */
    public Object getAttribute(String name) {
        if (name.equals("FontSize")) {
            return new Integer(fCurrentFontSize);
        }
        if (name.equals("FontStyle")) {
            return new Integer(fCurrentFontStyle);
        }
        if (name.equals("FontName")) {
            return fCurrentFontName;
        }
        return super.getAttribute(name);
    }

    /**
     * A text figure understands the "FontSize", "FontStyle", and "FontName"
     * attributes (which are applied directly to the figure instead of
     * setting them as attribute values).
     * The {@link #ALIGN_ATTR} attribute is set like any ordinary attribute,
     * but the appearance must be adapted afterwards.
     */
    public void setAttribute(String name, Object value) {
        if (name.equals("FontSize") || name.equals("FontStyle")
                    || name.equals("FontName")) {
            willChange();
            if (name.equals("FontSize")) {
                fCurrentFontSize = ((Integer) value).intValue();
            } else if (name.equals("FontStyle")) {
                int s = ((Integer) value).intValue();
                if (s == Font.PLAIN) {
                    fCurrentFontStyle = Font.PLAIN;
                } else {
                    fCurrentFontStyle = fCurrentFontStyle ^ s;
                }
            } else if (name.equals("FontName")) {
                fCurrentFontName = (String) value;
            }
            fFont = null;
            markDirty();
            changed();
            markDirty();
            changed();
        } else {
            super.setAttribute(name, value);
            if (name.equals(ALIGN_ATTR)) {
                willChange();
                markDirty();
                changed();
            }
        }
    }

    /**
     * Gets the text shown by the text figure.
     */
    public String getText() {
        return fText;
    }

    /**
     * Get the text shown by the text figure, but splitted into
     * an array of individual lines.
     */
    public String[] getLines() {
        return fLines;
    }

    /**
     * Sets the text shown by the text figure.
     */
    public void setText(String newText) {
        if (!newText.equals(fText)) {
            willChange();
            basicSetText(newText);
            changed();
        }
    }

    protected void basicSetText(String newText) {
        internalSetText(newText);
        markDirty();
    }

    protected void internalSetText(String newText) {
        fText = newText;
        fLines = splitTextLines(newText);
    }

    protected void internalSetTextHiddenParts(String fullText,
                                              String displayText) {
        fText = fullText;
        fLines = splitTextLines(displayText);
    }

    private static String[] splitTextLines(String text) {
        Vector<String> vector = new Vector<String>();
        StringTokenizer lines = new StringTokenizer(text, "\n", true);
        boolean lastLineTerminated = true;
        while (lines.hasMoreElements()) {
            String line = lines.nextToken();
            if ("\n".equals(line)) {
                line = "";
                lastLineTerminated = true;
            } else if (lines.hasMoreElements()) {
                // it was not only a separator, skip its separator:
                lines.nextToken();
                lastLineTerminated = true;
            } else {
                lastLineTerminated = false;
            }
            vector.addElement(line);
        }
        if (lastLineTerminated) {
            vector.addElement("");
        }
        String[] arr = new String[vector.size()];
        vector.copyInto(arr);
        return arr;
    }

    /**
     * Tests whether the figure accepts typing.
     */
    public boolean acceptsTyping() {
        return !fIsReadOnly;
    }

    public void internalDraw(Graphics g) {
        super.internalDraw(g);
        Color fillColor = getFillColor();
        getLineBoxes(g); // recalculate if dirty
        g.translate(fOriginX, fOriginY);
        if (ColorMap.isTransparent(getFrameColor())
                    && !ColorMap.isTransparent(fillColor)) {
            // fill each line individually:
            g.setColor(fillColor);
            for (int i = 0; i < fLines.length; i++) {
                g.fillRect(boxes[i].x, boxes[i].y, boxes[i].width,
                           boxes[i].height);
            }
        }
        Color textColor = (Color) getAttribute("TextColor");
        if (!ColorMap.isTransparent(textColor)) {
            g.setColor(textColor);
            for (int i = 0; i < fLines.length; i++) {
                drawLine(g, i);
            }
        }
        g.translate(-fOriginX, -fOriginY);
    }

    /**
     * Draws a line on the screen.
     *
     * @param g
     *  The Graphics context.
     * @param i
     *  The index of the line to be drawn.
     */
    protected void drawLine(Graphics g, int i) {
        Font font = getLineFont(i); //NOTICEsignature
        g.setFont(font);

        int x = boxes[i].x;
        int y = boxes[i].y;
        String s = getLine(i);
        // There is a bug in jdk1.1.7 drawString:
        // String is not properly converted to different encodings.  
        //
        // However there is another bug in the Sun JDK:
        // drawBytes does not respect the font before drawString
        // is called once.
        //g.drawString(" ",-10000,-10000);
        // Use drawBytes instead of drawString?
        g.drawString(s, x, y + getMetrics(font, g).getAscent());
        if (font instanceof ExtendedFont
                    && ((ExtendedFont) font).isUnderlined()) {
            FontMetrics fm = g.getFontMetrics();
            LineMetrics lm = fm.getLineMetrics(s, g);
            Graphics2D g2 = (Graphics2D) g;
            Shape shape = new Rectangle2D.Float(x,
                                                y
                                                + (int) lm.getUnderlineOffset()
                                                + getMetrics(font, g).getAscent(),
                                                fm.stringWidth(s),
                                                Math.max(1,
                                                         (int) lm
                              .getUnderlineThickness()));
            g2.fill(shape);
        }
    }

    public void drawBackground(Graphics g) {
        if (!ColorMap.isTransparent(getFrameColor())) {
            Rectangle r = displayBox();
            Graphics2D g2 = (Graphics2D) g;
            Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
            g2.fill(s);
        }
    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
        g2.draw(s);
    }

    private Dimension textExtent() {
        getLineBoxes(null);
        return new Dimension(fWidth, fHeight);
    }

    protected void markDirty() {
        fSizeIsDirty = true;
    }

    /**
     * Gets the number of rows and columns to be overlaid when the figure
     * is edited.
     */
    public Dimension overlayRowsAndColumns() {
        int columns = 20;
        String[] lines = getLines();
        for (int i = 0; i < lines.length; i++) {
            columns = Math.max(columns, lines[i].length() + 3);
        }
        int rows = Math.max(1, lines.length);

        return new Dimension(columns, rows);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        handles.addElement(new FontSizeHandle(this, RelativeLocator.southWest()));
        return handles;
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fOriginX);
        dw.writeInt(fOriginY);
        dw.writeString(fText);
        dw.writeString(fCurrentFontName);
        dw.writeInt(fCurrentFontStyle);
        dw.writeInt(fCurrentFontSize);
        dw.writeBoolean(fIsReadOnly);
        dw.writeStorable(fParent);
        dw.writeStorable(fLocator);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fOriginX = dr.readInt();
        fOriginY = dr.readInt();
        String text = dr.readString();
        setText(text);
        fCurrentFontName = dr.readString();
        fCurrentFontStyle = dr.readInt();
        fCurrentFontSize = dr.readInt();


        // The default font changed, because "Helvetica"
        // is no longer supported by Java 1.3. Patching
        // of older files is neccessary.
        if (dr.getVersion() < 8) {
            if ("Helvetica".equals(fCurrentFontName)) {
                fCurrentFontName = "SansSerif";
            }
        }
        fFont = null;
        fIsReadOnly = dr.readBoolean();

        fParent = (ParentFigure) dr.readStorable();
        fLocator = (OffsetLocator) dr.readStorable();
        if (fParent != null) {
            fParent.addChild(this);
        } else {
            // patch: doesn't have a parent=>can't have one!
            fCanBeConnected = false;
        }
        if (GUIProperties.noGraphics()) {
            fSizeIsDirty = false;
        } else {
            updateLocation();
        }
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        if (fParent != null) {
            fParent.addChild(this);
        }
        markDirty();
    }

    public boolean canBeParent(ParentFigure figure) {
        if (!fCanBeConnected) {
            return figure == null;
        }
        while (figure != null) {
            if (figure == this) {
                return false; // cyclic!!!
            }
            if (figure instanceof ChildFigure) {
                figure = ((ChildFigure) figure).parent();
            } else {
                break;
            }
        }
        return true;
    }

    public boolean setParent(ParentFigure figure) {
        if (!canBeParent(figure)) {
            return false;
        }
        if (fParent != null) {
            fParent.removeChild(this);
        }
        fParent = figure;
        if (fParent == null) {
            fLocator = null;
        } else {
            fParent.addChild(this);
            if (fLocator != null) {
                fLocator.setBase(fParent.connectedTextLocator(this));
            } else {
                fLocator = new OffsetLocator(fParent.connectedTextLocator(this));
            }
            updateLocation();
        }
        return true;

    }

    public ParentFigure parent() {
        return fParent;
    }

    /**
     * Updates the location relative to the connected figure.
     * The TextFigure is centered around the located point.
     */
    public void updateLocation() {
        if (fLocator != null) {
            Point p = fLocator.locate(fParent);
            p.x -= size().width / 2 + fOriginX;
            p.y -= size().height / 2 + fOriginY;

            if (p.x != 0 || p.y != 0) {
                willChange();
                basicMoveBy(p.x, p.y);
                changed();
            }
        }
    }

    public void release() {
        super.release();
        if (fParent != null) {
            fParent.removeChild(this);
        }
    }

    /**
     * Creates the current font to be used for new text figures.
     */
    static public Font createCurrentFont() {
        return Fontkit.getFont(fgCurrentFontName, fgCurrentFontStyle,
                               fgCurrentFontSize);
    }

    /**
     * Sets the current font name
     */
    static public void setCurrentFontName(String name) {
        fgCurrentFontName = name;
    }

    /**
     * Sets the current font size.
     */
    static public void setCurrentFontSize(int size) {
        fgCurrentFontSize = size;
    }

    /**
     * Sets the current font style.
     */
    static public void setCurrentFontStyle(int style) {
        fgCurrentFontStyle = style;
    }

    public void figureChanged(FigureChangeEvent e) {
        updateLocation();
    }

    public void figureHandlesChanged(FigureChangeEvent e) {
    }

    public void figureRemoved(FigureChangeEvent e) {
        if (listener() != null) {
            listener().figureRequestRemove(new FigureChangeEvent(this));
        }
    }

    public void figureRequestRemove(FigureChangeEvent e) {
    }

    public void figureInvalidated(FigureChangeEvent e) {
    }

    public void figureRequestUpdate(FigureChangeEvent e) {
    }

    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(parent());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (!alternate) {
            return super.inspect(view, false);
        } else {
            if (acceptsTyping()) {
                ((DrawApplication) view.editor()).doTextEdit(this);
                return true;
            }
            return false;
        }
    }
}