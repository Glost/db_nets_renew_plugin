/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 */
public class PSFileFilter extends SimpleFileFilter {
    public PSFileFilter() {
        this.setExtension("ps");
        this.setDescription("Post Script File");
    }
}