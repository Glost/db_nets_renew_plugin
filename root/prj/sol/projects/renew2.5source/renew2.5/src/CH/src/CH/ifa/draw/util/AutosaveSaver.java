package CH.ifa.draw.util;

import CH.ifa.draw.framework.Drawing;

import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.IOException;


public interface AutosaveSaver {
    public void saveAutosaveFile(Drawing drawing, File filename, Point loc,
                                 Dimension size) throws IOException;
}