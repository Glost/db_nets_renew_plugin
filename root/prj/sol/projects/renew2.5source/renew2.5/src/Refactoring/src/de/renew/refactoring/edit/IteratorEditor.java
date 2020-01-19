package de.renew.refactoring.edit;

import de.renew.refactoring.util.ProgressCalculator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract base class for editors that uses an iterator of edits.
 *
 * @author 2mfriedr
 */
public abstract class IteratorEditor<T, R> implements Editor<R> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(IteratorEditor.class);
    private final int _numberOfEdits;
    private final Iterator<T> _edits;
    private int _numberOfPerformedEdits;
    private T _currentEdit;

    /**
     * Constructs an iterator editor with an iterator and the number of edits.
     *
     * @param edits an iterator of edits
     * @param numberOfEdits the number of edits
     */
    public IteratorEditor(final Iterator<T> edits, final int numberOfEdits) {
        logger.debug("Edits: " + edits);
        _edits = edits;
        _numberOfEdits = numberOfEdits;
    }

    /**
     * Constructs an iterator editor with a list of edits.
     *
     * @param edits a list of edits
     */
    public IteratorEditor(List<T> edits) {
        this(edits.iterator(), edits.size());
    }

    @Override
    public int getNumberOfEdits() {
        return _numberOfEdits;
    }

    @Override
    public boolean hasNextEdit() {
        return _edits.hasNext();
    }

    @Override
    public int getProgress() {
        return ProgressCalculator.calculateProgress(_numberOfPerformedEdits,
                                                    _numberOfEdits);
    }

    @Override
    public String getCurrentEditString() {
        return _currentEdit.toString();
    }

    @Override
    public R performNextEdit() {
        _numberOfPerformedEdits += 1;
        _currentEdit = _edits.next();
        return performEdit(_currentEdit);
    }

    /**
     * Performs the edit.
     *
     * @param edit the edit
     * @return an object of type R
     */
    protected abstract R performEdit(T edit);

    @Override
    public List<R> performAllEdits() {
        List<R> result = new ArrayList<R>();
        while (hasNextEdit()) {
            result.add(performNextEdit());
        }
        return result;

    }

    /**
     * Returns the item that is currently being edited.
     *
     * @return the item
     */
    protected T getCurrentEdit() {
        return _currentEdit;
    }
}