/*
 * Created on Apr 18, 2003
 *
 * FileFilter for the standard ifa drawing.
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 *
 * This FileFilter accepts standard JHotDraw drawings which have the extension "draw".
 */
public class IFAFileFilter extends SimpleFileFilter {
    public IFAFileFilter() {
        this.setExtension("draw");
        setDescription("Simple Drawing (*.draw)");
    }
}