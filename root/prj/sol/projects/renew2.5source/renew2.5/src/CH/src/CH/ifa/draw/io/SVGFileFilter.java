/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 */
public class SVGFileFilter extends SimpleFileFilter {
    public SVGFileFilter() {
        this.setExtension("svg");
        this.setDescription("Scalable Vector Graphic");
    }
}