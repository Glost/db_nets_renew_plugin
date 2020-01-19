package CH.ifa.draw.io.importFormats;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.SimpleFileFilter;

import java.net.URL;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;


/**
 * Abstract implementation of ImportFormatMulti and extension of the class ImportFormatAbstract.
 */
public abstract class ImportFormatMultiAbstract extends ImportFormatAbstract
        implements ImportFormatMulti {
    // Attributes
    // All ImportFormats that have been added (MultiFormats do count as one element).
    private List<ImportFormat> _formats;

    // constructor
    public ImportFormatMultiAbstract(String name, String filterName) {
        super(name, new CombinationFileFilter(filterName));
        setFormats(new LinkedList<ImportFormat>());
    }

    // Methods


    /**
     * List of all added ImportFormats.
     * @ensure result != null
     */
    protected List<ImportFormat> formats() {
        List<ImportFormat> result = null;
        result = _formats;
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }

    /**
     * Sets _formats to formats.
     * @require formats != null
     * @ensure formats() != null
     * @ensure formats().equals(formats)
     * @param formats the value to be set.
     */
    protected void setFormats(List<ImportFormat> formats) {
        _formats = formats;
        assert (formats() != null) : "Failure in ImportFormatAbstract: formats() == null";
        assert (formats().equals(formats)) : "Failure in ImportFormatAbstract: formats() != formats";
    }

    /**
     * Returns a CombinationFileFilter that contains all added FileFilters
     * (Does not contain other CombinationFileFilters but their elements).
     * @ensure result != null.
     * @return CombinationFileFilter the CombinationFileFilter for the ImportFormat.
     */
    protected CombinationFileFilter comFileFilter() {
        CombinationFileFilter result = null;
        result = (CombinationFileFilter) fileFilter();
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }

    /**
     * Adds a fileFilter to the CombinationFileFilter (If the fileFilter itself is a
     * CombinationFileFilter only its elements are added).
     * @require fileFilter != null.
     * @param filter the fileFilter to be added.
     */
    protected void addFileFilter(FileFilter fileFilter) {
        if (fileFilter instanceof CombinationFileFilter) {
            CombinationFileFilter comFileFilter = (CombinationFileFilter) fileFilter;
            Iterator<SimpleFileFilter> filters = comFileFilter.getFileFilters()
                                                              .iterator();
            while (filters.hasNext()) {
                FileFilter element = filters.next();
                addFileFilter(element);
            }
        } else {
            if (fileFilter instanceof SimpleFileFilter) {
                comFileFilter().add((SimpleFileFilter) fileFilter);
            }
        }
    }

    /**
     * Removes the fileFilter from the CombinationFileFilter (If fileFilter is
     * a CombinationFileFiler all its elements are removed).
     * @require fileFilter != null.
     * @param filter fileFilter to be removed.
     */
    protected void removeFileFilter(FileFilter fileFilter) {
        if (fileFilter instanceof CombinationFileFilter) {
            CombinationFileFilter comFileFilter = (CombinationFileFilter) fileFilter;
            Iterator<SimpleFileFilter> filters = comFileFilter.getFileFilters()
                                                              .iterator();
            while (filters.hasNext()) {
                FileFilter element = filters.next();
                removeFileFilter(element);
            }
        } else {
            if (fileFilter instanceof SimpleFileFilter) {
                comFileFilter().remove((SimpleFileFilter) fileFilter);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Implementation of the ImportFormatMulti Interface
    // ---------------------------------------------------------------------


    /**
     * @see de.renew.io.ImportFormatMulti#addImportFormat(de.renew.io.ImportFormat)
     */
    public void addImportFormat(ImportFormat format) {
        formats().add(format);
        FileFilter filter = format.fileFilter();
        addFileFilter(filter);
    }

    /**
     * @see de.renew.io.ImportFormatMulti#getImportFormats()
     */
    public ImportFormat[] allImportFormats() {
        ImportFormat[] result = null;
        result = new ImportFormat[formats().size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = formats().get(pos);
        }
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ImportFormatMulti#removeImportFormat(de.renew.io.FormatId)
     */
    public void removeImportFormat(ImportFormat format) {
        removeFileFilter(format.fileFilter());
        formats().remove(format);
    }

    /**
      * @see de.renew.io.ImportFormat#importFiles(java.io.File[])
     */
    public Drawing[] importFiles(URL[] paths) throws Exception {
        Drawing[] result = null;
        Vector<Drawing> v = new Vector<Drawing>();
        for (int posfiles = 0; posfiles < paths.length; posfiles++) {
            ImportFormat[] formats = allImportFormats();
            for (int posformat = 0; posformat < formats.length; posformat++) {
                if (formats[posformat].canImport(paths[posfiles])) {
                    URL[] a = new URL[1];
                    a[0] = paths[posfiles];
                    Drawing[] drawings = formats[posformat].importFiles(a);
                    for (int posdraw = 0; posdraw < drawings.length;
                                 posdraw++) {
                        v.add(drawings[posdraw]);
                    }
                }
            }
        }
        result = new Drawing[v.size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = v.get(pos);
        }
        assert (result != null) : "Failure in ImportFormatAbstract: result == null";
        return result;
    }
}