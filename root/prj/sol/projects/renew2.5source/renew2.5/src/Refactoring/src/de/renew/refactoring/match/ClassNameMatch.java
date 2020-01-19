package de.renew.refactoring.match;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNTextFigure;


/**
 * A ClassNameMatch object describes a class name occurrence inside a text
 * figure's text.
 *
 * @author 2mfriedr
 */
public class ClassNameMatch extends TextFigureMatch {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(ClassNameMatch.class);
    private final StringMatch _classNameMatch;

    public ClassNameMatch(Drawing drawing, CPNTextFigure textFigure,
                          StringMatch match, StringMatch classNameMatch) {
        super(drawing, textFigure, match);
        _classNameMatch = classNameMatch;
    }

    public StringMatch getClassNameMatch() {
        return _classNameMatch;
    }

    public int getClassNameStart() {
        return _classNameMatch.start();
    }

    public int getClassNameEnd() {
        return _classNameMatch.end();
    }

    public String getClassNameString() {
        return _classNameMatch.match();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ _classNameMatch.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof ClassNameMatch) {
            ClassNameMatch other = (ClassNameMatch) obj;
            return getClassNameMatch().equals(other.getClassNameMatch());
        }
        return false;
    }
}