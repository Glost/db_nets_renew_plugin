package CH.ifa.draw.standard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.util.StringTokenizer;


public class MultiLineLabel extends Component {
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    protected String label;
    protected int margin_width;
    protected int margin_height;
    protected int alignment;
    protected int num_lines;
    protected String[] lines;
    protected int[] line_widths;
    protected int max_width;
    protected int line_height;
    protected int line_ascent;
    protected boolean measured = false;

    public MultiLineLabel(String label, int margin_width, int margin_height,
                          int alignment) {
        this.label = label;
        this.margin_width = margin_width;
        this.margin_height = margin_height;
        this.alignment = alignment;

        newLabel();
    }

    public MultiLineLabel(String label, int margin_width, int margin_height) {
        this(label, margin_width, margin_height, LEFT);
    }

    public MultiLineLabel(String label, int alignment) {
        this(label, 10, 10, alignment);
    }

    public MultiLineLabel(String label) {
        this(label, 10, 10, LEFT);
    }

    public MultiLineLabel() {
        this("");
    }

    public void setLabel(String label) {
        this.label = label;
        newLabel();
        measured = false;
        repaint();
    }

    public void setFont(Font f) {
        super.setFont(f);
        measured = false;
        repaint();
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        repaint();
    }

    public void setAlignment(int a) {
        alignment = a;
        repaint();
    }

    public void setMarginWidth(int mw) {
        margin_width = mw;
        repaint();
    }

    public void setMarginHeight(int mh) {
        margin_height = mh;
        repaint();
    }

    public String getLabel() {
        return label;
    }

    public int getAlignment() {
        return alignment;
    }

    public int getMarginWidth() {
        return margin_width;
    }

    public int getMarginHeight() {
        return margin_height;
    }

    public Dimension getPreferredSize() {
        if (!measured) {
            measure();
        }
        return new Dimension(max_width + 2 * margin_width,
                             num_lines * line_height + 2 * margin_height);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void paint(Graphics g) {
        int x;
        int y;
        Dimension size = this.getSize();
        if (!measured) {
            measure();
        }
        y = line_ascent + (size.height - num_lines * line_height) / 2;
        for (int i = 0; i < num_lines; i++, y += line_height) {
            switch (alignment) {
            default:
            case LEFT:
                x = margin_width;
                break;
            case CENTER:
                x = (size.width - line_widths[i]) / 2;
                break;
            case RIGHT:
                x = size.width - margin_width - line_widths[i];
                break;
            }
            g.drawString(lines[i], x, y);
        }
    }

    protected synchronized void newLabel() {
        StringTokenizer t = new StringTokenizer(label, "\n");
        num_lines = t.countTokens();
        lines = new String[num_lines];
        line_widths = new int[num_lines];
        for (int i = 0; i < num_lines; i++) {
            lines[i] = t.nextToken();
        }
    }

    protected synchronized void measure() {
        FontMetrics fm = this.getFontMetrics(this.getFont());
        line_height = fm.getHeight();
        line_ascent = fm.getAscent();
        max_width = 0;
        for (int i = 0; i < num_lines; i++) {
            line_widths[i] = fm.stringWidth(lines[i]);
            if (line_widths[i] > max_width) {
                max_width = line_widths[i];
            }
        }
        measured = true;
    }
}