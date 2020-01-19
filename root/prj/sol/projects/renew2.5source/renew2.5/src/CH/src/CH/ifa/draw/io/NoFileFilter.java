/*
 * Created on Apr 13, 2003
 *
 * FileFilter for all files.
 */
package CH.ifa.draw.io;

import java.io.File;


/**
 * @author Lawrence Cabac
 *
 * This FileFilter accepts all file types.
 */
public class NoFileFilter extends SimpleFileFilter {
    public NoFileFilter() {
        this.setExtension("");
        this.setDescription("No Specific File Type");
    }

    public boolean accept(File f) {
        return true;
    }
}