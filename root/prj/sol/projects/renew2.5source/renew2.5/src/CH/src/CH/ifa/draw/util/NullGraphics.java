package CH.ifa.draw.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;


public class NullGraphics extends Graphics {
    private Font fFont = Fontkit.getFont("Helvativa", Font.PLAIN, 12);
    private Rectangle clip = new Rectangle(0, 0, 0, 0);

    public NullGraphics() {
    }

    public Graphics create() {
        return this;
    }

    public Graphics create(int x, int y, int width, int height) {
        return this;
    }

    public void dispose() {
    }

    public Color getColor() {
        return Color.white;
    }

    public void setColor(Color c) {
    }

    public void setPaintMode() {
    }

    public void setXORMode(Color otherColor) {
    }

    public Font getFont() {
        return fFont;
    }

    public void setFont(Font font) {
        fFont = font;
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(fFont);
    }

    /**
     * This class is an instance of the Null Object design pattern.
     * It would be consequent to return another Null Object instance for the FontMetrics
     * result to avoid the deprecated method. However, the solution also requires more Null
     * Object implementations, namely of Font and its related classes GlyphVector, and
     * LineMetrics. These in turn require more Null Object implementations. So the solution
     * seems not appropriate in comparison of effort and result.
     */
    @SuppressWarnings("deprecation")
    public FontMetrics getFontMetrics(Font font) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    public java.awt.Rectangle getClipBounds() {
        return clip;
    }

    public void clipRect(int x, int y, int w, int h) {
        setClip(x, y, w, h);
    }

    public Shape getClip() {
        return clip;
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int x, int y, int w, int h,
                             ImageObserver observer) {
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
                             ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
                             Color bgcolor, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2, Color bgcolor,
                             ImageObserver observer) {
        return false;
    }

    public void drawRect(int x, int y, int w, int h) {
    }

    public void fillRect(int x, int y, int w, int h) {
    }

    public void clearRect(int x, int y, int w, int h) {
    }

    public void drawOval(int x, int y, int w, int h) {
    }

    public void fillOval(int x, int y, int w, int h) {
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
    }

    /**
     *
     * @param xPoints
     * @param yPoints
     * @param nPoints
     */
    public void writePolygonPath(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void drawPolygon(Polygon poly) {
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void fillPolygon(Polygon poly) {
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
    }

    public void setClip(int x, int y, int w, int h) {
    }

    public void setClip(Shape clip) {
    }

    public void translate(int x, int y) {
    }

    /**
     *
     * @param xscale
     * @param yscale
     */
    public void scale(double xscale, double yscale) {
    }

    public void drawString(String text, int x, int y) {
    }

    public void drawBytes(byte[] data, int start, int size, int x, int y) {
    }

    public void drawString(java.text.AttributedCharacterIterator iterator,
                           int i, int j) {
    }
}