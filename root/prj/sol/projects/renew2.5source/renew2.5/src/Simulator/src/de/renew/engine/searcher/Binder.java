package de.renew.engine.searcher;

public interface Binder {

    /**
     * Returns roughly the number of possible bindings to test.
     * Sometimes the searcher parameter ought to be
     * removed. It is unneccessary.
     *
     * @param searcher the current <code>Searcher</code>
     */
    public int bindingBadness(Searcher searcher);

    /**
     * Loop through all the possible bindings: <ul>
     * <li>  register triggers, if required,   </li>
     * <li>  add other binders if required,    </li>
     * <li>  bind variables,                   </li>
     * <li>  call the searcher,                </li>
     * <li>  undo the bindings, and            </li>
     * <li>  remove the binders.               </li>
     * </ul>
     *
     * @param searcher the current <code>Searcher</code>
     */
    public void bind(Searcher searcher);
}