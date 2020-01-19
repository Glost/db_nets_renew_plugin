/*
 * Created on Apr 13, 2003
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 */
public class PDFFileFilter extends SimpleFileFilter {
    public PDFFileFilter() {
        this.setExtension("pdf");
        this.setDescription("Portable Document Format");
    }
}