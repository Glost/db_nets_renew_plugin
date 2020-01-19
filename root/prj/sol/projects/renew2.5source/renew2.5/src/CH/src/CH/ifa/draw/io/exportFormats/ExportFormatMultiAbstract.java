package CH.ifa.draw.io.exportFormats;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.SimpleFileFilter;

import java.io.File;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;


/**
 * Abstract implementation of ExportFormatMulti and extension of the class ExportFormatAbstract.
 */
public abstract class ExportFormatMultiAbstract extends ExportFormatAbstract
        implements ExportFormatMulti {
    // Attributes
    // All ExportFormats that have been added (MultiFormats do count as one element).
    private List<ExportFormat> _formats;

    // constructor
    public ExportFormatMultiAbstract(String name, String filterName) {
        super(name, new CombinationFileFilter(filterName));
        setFormats(new LinkedList<ExportFormat>());
    }

    // Methods


    /**
     * Map of all added ExportFormats.
     * @ensure result != null
     */
    public List<ExportFormat> formats() {
        List<ExportFormat> result = null;
        result = _formats;
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }

    /**
     * Sets _formats to formats.
     * @require formats != null
     * @ensure formats() != null
     * @ensure formats().equals(formats)
     * @param formats the value to be set.
     */
    protected void setFormats(List<ExportFormat> formats) {
        _formats = formats;
        assert (formats() != null) : "Failure in ExportFormatAbstract: formats() == null";
        assert (formats().equals(formats)) : "Failure in ExportFormatAbstract: formats() != formats";
    }

    /**
     * Returns a CombinationFileFilter that contains all added FileFilters
     * (Does not contain other CombinationFileFilters, but their elements).
     * @ensure result != null.
     * @return CombinationFileFilter the CombinationFileFilter for the ExportFormat.
     */
    protected CombinationFileFilter comFileFilter() {
        CombinationFileFilter result = null;
        result = (CombinationFileFilter) fileFilter();
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }

    /**
     * Adds a fileFilter to the CombinationFileFilter (If the fileFilter itself is a
     * CombinationFileFilter, only its elements are added).
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
     * a CombinationFileFiler, all its elements are removed).
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
    // Implementation of the ExportFormatMulti Interface
    // ---------------------------------------------------------------------


    /**
     * @see de.renew.io.ExportFormatMulti#addExportFormat(de.renew.io.ExportFormat)
     */
    public void addExportFormat(ExportFormat format) {
        formats().add(format);
        FileFilter filter = format.fileFilter();
        addFileFilter(filter);
    }

    /**
     * @see de.renew.io.ExportFormatMulti#getExportFormats()
     */
    public ExportFormat[] allExportFormats() {
        ExportFormat[] result = null;
        result = new ExportFormat[formats().size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = formats().get(pos);
        }
        assert (result != null) : "Failure in ExportFormatAbstract: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormatMulti#removeExportFormat(de.renew.io.FormatId)
     */
    public void removeExportFormat(ExportFormat format) {
        removeFileFilter(format.fileFilter());
        formats().remove(format);
    }


    /* (non-Javadoc)
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    public ExportFormat[] canExport(File path) {
        ExportFormat[] result = super.canExport(path);
        if (path != null) {
            List<ExportFormat[]> list = new LinkedList<ExportFormat[]>();
            int count = 0;
            for (int pos = 0; pos < allExportFormats().length; pos++) {
                ExportFormat[] formats = allExportFormats()[pos].canExport(path);
                if (formats.length > 0) {
                    list.add(formats);
                    count = count + formats.length;
                }
            }
            result = new ExportFormat[count];
            Iterator<ExportFormat[]> iter = list.iterator();
            while (iter.hasNext()) {
                ExportFormat[] element = iter.next();
                for (int pos = 0; pos < element.length; pos++) {
                    result[--count] = element[pos];
                }
            }
        }
        assert (result != null) : "Failure in ExportFormatMultiAbstract: result == null";
        return result;
    }
}