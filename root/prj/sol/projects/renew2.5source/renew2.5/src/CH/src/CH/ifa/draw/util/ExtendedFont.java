package CH.ifa.draw.util;

import java.awt.Font;


public class ExtendedFont extends Font {
    public static final int UNDERLINED = Math.max(Math.max(Font.PLAIN, Font.BOLD),
                                                  Font.ITALIC) * 2;
    private int underlined; // either 0 or UNDERLINED

    public ExtendedFont(String name, int style, int size) {
        super(name, style & ~UNDERLINED, size);
        underlined = style & UNDERLINED;
    }

    public int getStyle() {
        return super.getStyle() | underlined;
    }

    public boolean isPlain() {
        return super.isPlain() && underlined == 0;
    }

    public boolean isUnderlined() {
        return underlined != 0;
    }

    /**
     * Compares this <code>ExtendedFont</code> object to the specified
     * <code>Object</code>.
     *
     * Two <code>ExtendedFont</code> instances are equal only if they are
     * equal like defined in {@link Font#equals Font.equals} <i>and</i>
     * share the same value for the <code>underlined</code> property.
     *
     * When compared to a <code>Font</code> object that is not an
     * <code>ExtendedFont</code>, the value of the <code>underlined</code>
     * property is ignored in the comparision.
     *
     * @param that the <code>Object</code> to compare.
     * @return <code>true</code> if the argument is a <code>Font</code>
     * object describing the same font as this object and (in case of
     * <code>ExtendedFont</code> objects) has the same
     * <code>underlined</code> property.
     **/
    public boolean equals(Object that) {
        if (super.equals(that)) {
            if (that instanceof ExtendedFont) {
                return ((ExtendedFont) that).underlined == underlined;
            }


            // At this point the original author wanted to check that
            // <code>underlined == 0</code>, but this condition would
            // violate the symmetry of the equivalence relation (other
            // <code>Font</code> implementations would not know about this
            // difference). So we have to return <code>true</code>
            // regardless of the <code>underlined</code> attribute.
            return true;
        }
        return false;
    }

    /**
     * Returns a hashcode for this <code>ExtendedFont</code>.
     * @return a hashcode value for this <code>ExtendedFont</code>.
     **/
    public int hashCode() {
        // We must use the hash code from the superclass, otherwise we
        // would violate the general equals/hashCode contract (see comment
        // within the equals method for arguments).
        return super.hashCode();
    }
}