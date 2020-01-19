/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 */
public class EPSFileFilter extends SimpleFileFilter {
    public EPSFileFilter() {
        this.setExtension("eps");
        this.setDescription("Encapsulated Post Script File");
    }
}