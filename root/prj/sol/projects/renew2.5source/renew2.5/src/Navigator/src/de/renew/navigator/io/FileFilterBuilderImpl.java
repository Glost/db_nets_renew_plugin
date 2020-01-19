package de.renew.navigator.io;

import CH.ifa.draw.IOHelper;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.importFormats.ImportFormat;
import CH.ifa.draw.io.importFormats.ImportFormatMultiAbstract;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-07
 */
public class FileFilterBuilderImpl implements FileFilterBuilder {
    private final IOHelper ioHelper;
    private final ImportHolder importHolder;

    /**
     * Constructor.
     *
     * @param ioHelper Helper to handle IO operations.
     * @param importHolder Holder of import formats.
     */
    public FileFilterBuilderImpl(IOHelper ioHelper, ImportHolder importHolder) {
        this.ioHelper = ioHelper;
        this.importHolder = importHolder;
    }

    @Override
    public CombinationFileFilter buildFileFilter() {
        final CombinationFileFilter fileFilter = ioHelper.getFileFilter();

        ImportFormat[] formats = importHolder.allImportFormats();
        for (ImportFormat format : formats) {
            // Add a single import format.
            addImportFormat(fileFilter, format);
        }

        // Add file filters of external file types.
        fileFilter.addAll(getExternalFileTypes());

        fileFilter.allowDirectory(true);
        fileFilter.allowHidden(false);

        return fileFilter;
    }

    @Override
    public boolean isExternallyOpenedFile(File file) {
        CombinationFileFilter fileFilter = new CombinationFileFilter("temp");
        fileFilter.addAll(getExternalFileTypes());

        return fileFilter.accept(file);
    }


    /**
     * Adds an import format to the accepted file types of the navigator.
     *
     * @param format import format to add
     */
    private static void addImportFormat(CombinationFileFilter target,
                                        ImportFormat format) {
        // Load each format of a multi format.
        if (format instanceof ImportFormatMultiAbstract) {
            ImportFormatMultiAbstract multiFormat = (ImportFormatMultiAbstract) format;
            ImportFormat[] allImportFormats = multiFormat.allImportFormats();
            for (ImportFormat importFormat2 : allImportFormats) {
                addImportFormat(target, importFormat2);
            }
            return;
        }

        // Load a simple file filter.
        if (format.fileFilter() instanceof SimpleFileFilter) {
            target.add((SimpleFileFilter) format.fileFilter());
            return;
        }

        throw new RuntimeException(String.format("Unsupported import format: %s",
                                                 format.getClass()));
    }

    /**s
     * @return collection of all file filters which are opened externally
     */
    private Collection<SimpleFileFilter> getExternalFileTypes() {
        return Arrays.asList(new SimpleFileFilter("kb", "Knowledge Base"),
                             new SimpleFileFilter("java", "Java Sourcecode"),
                             new SimpleFileFilter("jsp", "Java Servlet Pages"),
                             new SimpleFileFilter("xml",
                                                  "Extensible Markup Language"),
                             new SimpleFileFilter("xmi",
                                                  "XML Metadata Interchange"),
                             new SimpleFileFilter("md", "Markdown"),
                             new SimpleFileFilter("tex", "LaTeX file"),
                             new SimpleFileFilter("html", "Hypertext"),
                             new SimpleFileFilter("cfg", "Configuration"),
                             new SimpleFileFilter("gif",
                                                  "Graphics Interchange Format"),
                             new SimpleFileFilter("png",
                                                  "Portable Network Graphics"),
                             new SimpleFileFilter("jpg",
                                                  "Joint Photographic Experts Group"),
                             new SimpleFileFilter("jpeg",
                                                  "Joint Photographic Experts Group"));
    }
}