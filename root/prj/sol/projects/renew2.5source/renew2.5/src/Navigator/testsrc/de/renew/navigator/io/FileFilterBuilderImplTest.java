package de.renew.navigator.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import CH.ifa.draw.IOHelper;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.CombinationFileFilter;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.importFormats.ImportFormat;

import de.renew.diagram.AIPFileFilter;

import de.renew.io.importFormats.XMLImportFormat;

import java.io.File;

import java.net.URI;
import java.net.URL;

import javax.swing.filechooser.FileFilter;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-09
 */
public class FileFilterBuilderImplTest {
    private FileFilterBuilderImpl fileFilterBuilder;
    private IOHelper ioHelper;
    private ImportHolder importHolder;

    @Before
    public void setUp() throws Exception {
        ioHelper = mock(IOHelper.class);
        importHolder = mock(ImportHolder.class);
        fileFilterBuilder = new FileFilterBuilderImpl(ioHelper, importHolder);
    }

    @Test
    public void testBuildFileFilter() throws Exception {
        // Test combination file filters.
        final CombinationFileFilter filter;
        filter = new CombinationFileFilter("Test Descr");
        filter.add(new AIPFileFilter());
        when(ioHelper.getFileFilter()).thenReturn(filter);

        // Test import formats.
        final ImportFormat[] importFormats = { new XMLImportFormat() };
        when(importHolder.allImportFormats()).thenReturn(importFormats);

        final CombinationFileFilter fileFilter = fileFilterBuilder
                                                     .buildFileFilter();

        verify(ioHelper).getFileFilter();
        verify(importHolder).allImportFormats();

        assertTrue(fileFilter.accept(new File("demo.xml")));
        assertTrue(fileFilter.accept(new File("demo.aip")));
        assertEquals(fileFilter.getDescription(), "Test Descr");
    }

    @Test(expected = RuntimeException.class)
    public void testBuildFileFilterThrowsException() throws Exception {
        // Test combination file filters.
        final CombinationFileFilter filter;
        filter = new CombinationFileFilter("Test Descr");
        filter.add(new AIPFileFilter());
        when(ioHelper.getFileFilter()).thenReturn(filter);

        // Test runtime ex when using wrong filter.
        final ImportFormat[] wrongImportFormats = { new ImportFormat() {
                    @Override
                    public Drawing[] importFiles(URL[] paths)
                            throws Exception {
                        return new Drawing[0];
                    }

                    @Override
                    public FileFilter fileFilter() {
                        return null;
                    }

                    @Override
                    public String formatName() {
                        return null;
                    }

                    @Override
                    public boolean canImport(URL path) {
                        return false;
                    }

                    @Override
                    public boolean canImport(URI path) {
                        return false;
                    }
                }
                                                   };
        when(importHolder.allImportFormats()).thenReturn(wrongImportFormats);

        // Bang
        fileFilterBuilder.buildFileFilter();
    }

    @Test
    public void testIsExternallyOpenedFile() throws Exception {
        String[] externals = { "kb", "java", "jsp", "xml", "xmi", "md", "tex", "html", "cfg", "gif", "png", "jpg", "jpeg" };
        String[] internals = { "rnw", "aip", "draw" };

        for (final String external : externals) {
            final File f = new File("foo." + external);
            final boolean isExt = fileFilterBuilder.isExternallyOpenedFile(f);

            assertTrue(isExt);
        }

        for (final String internal : internals) {
            final File f = new File("foo." + internal);
            final boolean isExt = fileFilterBuilder.isExternallyOpenedFile(f);

            assertFalse(isExt);
        }
    }
}