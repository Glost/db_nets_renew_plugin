package de.renew.navigator.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import CH.ifa.draw.io.CombinationFileFilter;

import de.renew.io.RNWFileFilter;

import de.renew.navigator.models.Directory;
import de.renew.navigator.models.Leaf;
import de.renew.navigator.models.TreeElement;

import java.io.File;

import java.net.URL;

import java.nio.file.FileSystems;

import java.util.Collection;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-09
 */
public class FilesystemIOLoaderTest {
    private FileFilterBuilder builder;
    private FilesystemIOLoader loader;
    private CombinationFileFilter ff;
    private File fixtures;

    private static TreeElement findChildByName(Collection<TreeElement> elements,
                                               String name) {
        for (TreeElement element : elements) {
            if (element.getName().equals(name)) {
                return element;
            }
        }

        return null;
    }

    @Before
    public void setUp() throws Exception {
        builder = mock(FileFilterBuilderImpl.class);
        loader = new FilesystemIOLoader(builder);

        // Create file filter.
        ff = new CombinationFileFilter("Stub");
        ff.add(new RNWFileFilter());
        when(builder.buildFileFilter()).thenReturn(ff);

        // Load fixtures.
        final URL resource = getClass()
                                 .getResource("/de/renew/navigator/io/fixtures");
        fixtures = new File(resource.toURI());
    }

    @Test
    public void testLoadFile() throws Exception {
        ProgressListener listener = mock(ProgressListener.class);
        final URL resource = getClass()
                                 .getResource("/de/renew/navigator/testfile.txt");
        final File testFile = new File(resource.toURI());

        final TreeElement result = loader.loadPath(testFile, listener);

        verifyZeroInteractions(builder);
        verifyZeroInteractions(listener);

        // Assert content of results.
        assertEquals(result.getFile(), testFile);
        assertEquals(result.getName(), testFile.getName());
        assertNull(result.getParent());
        assertTrue(result instanceof Leaf);
    }

    @Test
    public void testLoadDirectory() throws Exception {
        ProgressListener listener = mock(ProgressListener.class);

        final TreeElement result = loader.loadPath(fixtures, listener);

        verify(builder).buildFileFilter();

        // Assert content of results.
        assertEquals(result.getFile(), fixtures);
        assertEquals(result.getName(), fixtures.getName());
        assertNull(result.getParent());
        assertTrue(result instanceof Directory);

        final Directory dir = (Directory) result;
        assertEquals(dir.getChildren().size(), fixtures.listFiles(ff).length);
        assertEquals(6, dir.getChildren().size());
        assertTrue(dir.isOpened());
        assertNull(dir.getType());

        for (TreeElement child : dir.getChildren()) {
            assertSame(child.getParent(), dir);
        }
    }

    @Test
    public void testRenameFile() {
        ProgressListener listener = mock(ProgressListener.class);
        String dir = fixtures.getAbsolutePath()
                     + FileSystems.getDefault().getSeparator();

        final File fig1 = new File(dir + "fig1.rnw");
        final File fig2 = new File(dir + "Fig1.rnw");
        final File myfig1 = new File(dir + "myfig1.rnw");
        assertTrue(fig1.isFile());
        try {
            final Directory result = (Directory) loader.loadPath(fixtures,
                                                                 listener);
            assertEquals(6, result.getChildren().size());
            assertNotNull(findChildByName(result.getChildren(), "fig1.rnw"));
            assertNull(findChildByName(result.getChildren(), "myfig1.rnw"));
            assertNull(findChildByName(result.getChildren(), "Fig1.rnw"));

            if (!fig1.renameTo(fig2)) {
                fail("Could not rename the file 'fig1.rnw'. Please check your filesystem!");
            }

            loader.refreshPath(result, fixtures, listener);
            assertEquals(6, result.getChildren().size());
            assertNull(findChildByName(result.getChildren(), "fig1.rnw"));
            assertNull(findChildByName(result.getChildren(), "myfig1.rnw"));
            assertNotNull(findChildByName(result.getChildren(), "Fig1.rnw"));

            if (!fig2.renameTo(myfig1)) {
                fail("Could not rename the file 'Fig1.rnw'. Please check your filesystem!");
            }

            loader.refreshPath(result, fixtures, listener);
            assertEquals(6, result.getChildren().size());
            assertNull(findChildByName(result.getChildren(), "fig1.rnw"));
            assertNull(findChildByName(result.getChildren(), "Fig1.rnw"));
            assertNotNull(findChildByName(result.getChildren(), "myfig1.rnw"));

        } finally {
            // Retrieve initial state.
            if (!myfig1.renameTo(fig1)) {
                fail("Could not rename the file 'myfig1.rnw' back to 'fig1.rnw'??");
            }
        }
    }
}