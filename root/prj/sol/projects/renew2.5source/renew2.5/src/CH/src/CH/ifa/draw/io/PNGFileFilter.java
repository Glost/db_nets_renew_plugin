/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 */
public class PNGFileFilter extends SimpleFileFilter {
    public PNGFileFilter() {
        this.setExtension("png");
        this.setDescription("Portable Network Graphics File");
    }
}