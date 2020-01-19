package de.renew.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * This class manages a dependency structure among plugins.
 * It is used by adding plugin information in form of DependencyCheckList.DependencyElements
 * to an instance.
 * A list of plugins that can be loaded with regards to their dependencies will
 * be provided. This list is sorted from elements with no dependencies to elements that
 * are dependent on the previous ones.
 * At this time, implementation does NOT allow circular dependencies among modules,
 * i.e., A depends on B depends on C depends on A will result in A, B and C being
 * regarded as containing unfulfilled dependencies.
 */
public class DependencyCheckList<O> {

    /** The list of Elements the dependencies of which are fulfilled. **/
    private List<DependencyElement<O>> _fulfilledElements = new Vector<DependencyElement<O>>();

    /** The list of fulfilled provisions (contains Strings). **/
    private Collection<String> _provided = new HashSet<String>();

    /** The list of Elements the dependencies of which are not fulfilled. **/
    private Collection<DependencyElement<O>> _unfulfilledElements = new Vector<DependencyElement<O>>();

    // The list of provisions needed by _unfulfilledElements (contains Strings)
    //    private Collection _required = new HashSet();
    // The list of provisions provided by _unfulfilledElements (contains Strings)
    //    private Collection _fulfillCandidates = new HashSet();


    /**
     * Constructor for DependencyCheckList.
     */
    public DependencyCheckList() {
        super();
    }

    /**
     * Add the given element to the dependency list.
     */
    public synchronized void addElement(DependencyElement<O> el) {
        /*
         * Iterate through all elements that are already fulfilled.
         * remove all of their provisions from current requirements.
         * if requirement list is empty, insert element into list.
         * if not all requirements are met, put element into unfulfilled list
         */
        if (el._requirements.isEmpty()) {
            _fulfilledElements.add(0, el);
            _provided.addAll(el._provisions);

            // new provisions have been added, outstanding requirements removed.
            // update unfulfilled list.
            checkUnfulfilled();
            return;
        }
        int limit = _fulfilledElements.size();
        Collection<String> requirements = new Vector<String>(el._requirements);
        for (int i = 0; i < limit; i++) {
            DependencyElement<O> dl = _fulfilledElements.get(i);
            requirements.removeAll(dl._provisions);
            if (requirements.isEmpty()) {
                _fulfilledElements.add(i + 1, el);
                _provided.addAll(el._provisions);

                // new provisions have been added, outstanding requirements removed.
                // update unfulfilled list.
                checkUnfulfilled();
                return;
            }
        }

        _unfulfilledElements.add(el);
    }

    /**
     * remove the given element from the dependency list.
     * an exception is thrown if the removal would not leave
     * the dependency relation intact.
     */
    public synchronized void removeElement(O toRemove)
            throws DependencyNotFulfilledException {
        Collection<DependencyElement<O>> after = new Vector<DependencyElement<O>>();
        for (int i = _fulfilledElements.size() - 1; i >= 0; i--) {
            DependencyElement<O> de = _fulfilledElements.get(i);
            if (toRemove.equals(de._contained)) {
                // check whether there were objects before
                if (!after.isEmpty()) {
                    Collection<DependencyElement<O>> hurt = new Vector<DependencyElement<O>>();
                    for (DependencyElement<O> hurtCand : after) {
                        if (hurtCand.requiresAny(de._provisions)) {
                            hurt.add(hurtCand);
                        }
                    }
                    if (!hurt.isEmpty()) {
                        throw new DependencyNotFulfilledException("cannot remove "
                                                                  + toRemove
                                                                  + ": other elements are dependent on it.",
                                                                  hurt);
                    }
                }
                _fulfilledElements.remove(i);
                _provided.removeAll(de._provisions);
                return;
            }
            after.add(de);
        }
    }

    /**
     * Removes the given element from the dependency list. If
     * there are other elements depending on the given element,
     * they will become unfulfilled. There is a recursive
     * behaviour as all elements dependent on now unfulfilled
     * elements also have to become unfulfilled.
     * @param toRemove the object to remove from the dependency list.
     * @return all elements that had to change their state.
     **/
    public synchronized Collection<DependencyElement<O>> removeElementWithDependencies(O toRemove) {
        // Here we store the element referred to by toRemove
        DependencyElement<O> elementToRemove = null;


        // Here we collect provisions not affected by the removal
        Set<String> guaranteedProvisions = new HashSet<String>();


        // Here we collect provisions affected by the removal
        Set<String> retractedProvisions = new HashSet<String>();


        // Here we store all elements affected by the removal
        List<DependencyElement<O>> elementsToRetract = new Vector<DependencyElement<O>>(_fulfilledElements
                                                                                        .size());


        // Loop across all fulfilled elements until we encounter
        // the element to remove. All elements in front of the
        // specified element are guaranteed to keep their
        // fulfilled state, so collect their provisions.
        int i = 0;
        while ((elementToRemove == null) && (i < _fulfilledElements.size())) {
            DependencyElement<O> de = _fulfilledElements.get(i);
            if (toRemove.equals(de._contained)) {
                elementToRemove = de;
            } else {
                guaranteedProvisions.addAll(de._provisions);
                i++;
            }
        }


        // If we found the element, loop across the rest and find
        // the affected elements.
        if (elementToRemove != null) {
            // The set of guaranteed provisions can increase
            // during the loop, therefore it can happen that we
            // have to compute this several times. We need to
            // remember the starting position.
            int again = i + 1;
            while (true) {
                // Start with an empty list of elements to retract.
                elementsToRetract.clear();
                retractedProvisions.clear();

                // Calculate the initial set of provisions to retract.
                retractedProvisions.addAll(elementToRemove._provisions);
                retractedProvisions.removeAll(guaranteedProvisions);


                // Loop only if we have something to retract.
                // If we don't loop, nothing will change during this
                // iteration of the outer loop. Therefore, it should be
                // terminated, too.
                boolean loop = !retractedProvisions.isEmpty();
                if (!loop) {
                    break;
                }

                // The inner loop starts at the remembered position and can
                // run until the end of list is reached. It should also be
                // aborted (and restarted), if a thought-to-be-retracted
                // provision is also provided by another element - this is
                // indicated by setting "loop" to false.
                i = again;
                while (loop && (i < _fulfilledElements.size())) {
                    DependencyElement<O> hurtCand = _fulfilledElements.get(i);
                    if (hurtCand.requiresAny(retractedProvisions)) {
                        // Register this element as affected.
                        elementsToRetract.add(hurtCand);
                        // Increase the set of retracted provisions,
                        // but do not mark guaranteed provisions
                        // as retracted.
                        retractedProvisions.addAll(hurtCand._provisions);
                        retractedProvisions.removeAll(guaranteedProvisions);
                    } else {
                        // Increase the set of guaranteed provisions.
                        guaranteedProvisions.addAll(hurtCand._provisions);
                        // Check whether this candidate provides
                        // anything we did already mark as retracted.
                        // In this case, we have to redo the loop.
                        loop = !retractedProvisions.removeAll(hurtCand._provisions);
                    }
                    i++;
                }


                // If "loop" is still true, the exit reason was the end
                // of the list. So there were no reprovided provisions and
                // we can quit the outer loop, too.
                if (loop) {
                    break;
                }
            }

            // Now we have a list of elements to retract. Let's do it.
            _fulfilledElements.remove(elementToRemove);
            _fulfilledElements.removeAll(elementsToRetract);
            _unfulfilledElements.addAll(elementsToRetract);
        }
        return elementsToRetract;
    }

    /**
     * Retrieves the list of all elements the dependencies of which
     * are fulfilled.
     * @return a List containing DependencyElements
     */
    public synchronized List<DependencyElement<O>> getFulfilled() {
        return _fulfilledElements;
    }

    /**
     * Retrieves the collection of all elements with at least one
     * unfulfilled dependency.
     * @return a Collection containing DependencyElements
     */
    public synchronized Collection<DependencyElement<O>> getUnfulfilled() {
        return _unfulfilledElements;
    }

    /**
     * Retrieves the list of all objects contained in the elements
     * the dependencies of which are fulfilled.
     * @return a List containing DependencyElements
     */
    public synchronized List<O> getFulfilledObjects() {
        List<O> result = new Vector<O>();
        for (DependencyElement<O> el : getFulfilled()) {
            result.add(el._contained);
        }
        return result;
    }

    /*
     * check if any elements in unfulfilledElements has
     * its requirements met
     */
    protected synchronized void checkUnfulfilled() {
        DependencyElement<O> toAdd = null;
        Iterator<DependencyElement<O>> it = _unfulfilledElements.iterator();
        while (it.hasNext()) {
            DependencyElement<O> el = it.next();
            if (dependencyFulfilled(el)) {
                // we found an element that has all requirements fulfilled.
                // add it to fulfilledElements, dont look any further.
                // if there are any more, recursion will catch them.
                toAdd = el;
                it.remove();
                break;
            }
        }
        if (toAdd != null) {
            addElement(toAdd);
        }
    }

    /*
     * check if the dependencies of the given Element are fulfilled
     */
    public synchronized boolean dependencyFulfilled(DependencyElement<?> el) {
        Collection<String> req = new Vector<String>(el._requirements);
        req.removeAll(_provided);
        if (req.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * A simple helper method to create DependencyElements from the given object
     * and the given provision/requirement lists.
     */
    public static <O> DependencyElement<O> createElement(O o, String[] prov,
                                                         String[] req) {
        return new DependencyElement<O>(o, Arrays.asList(prov),
                                        Arrays.asList(req));
    }

    /**
     * The element contained in the DependencyCheckList. Immutable.
     */
    public static class DependencyElement<O> {
        private O _contained;
        private Set<String> _provisions;
        private Set<String> _requirements;

        public DependencyElement(O contained, Collection<String> prov,
                                 Collection<String> req) {
            _contained = contained;
            _provisions = new HashSet<String>(prov);
            _requirements = new HashSet<String>(req);
        }

        public static DependencyElement<PluginProperties> create(PluginProperties props) {
            return new DependencyElement<PluginProperties>(props,
                                                           props.getProvisions(),
                                                           props.getRequirements());
        }

        public static DependencyElement<IPlugin> create(IPlugin plugin) {
            return new DependencyElement<IPlugin>(plugin,
                                                  plugin.getProperties()
                                                        .getProvisions(),
                                                  plugin.getProperties()
                                                        .getRequirements());
        }

        public boolean requiresAny(Collection<?extends String> c) {
            for (String requirement : c) {
                if (_requirements.contains(requirement)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return _contained + "; providing "
                   + CollectionLister.toString(_provisions) + "; requiring "
                   + CollectionLister.toString(_requirements);
        }

        /**
         * Returns an easily readable formatted string for a plugin with unfullfilled dependencies.
         * @param loadedPluginMainClasses
         * @return String formatted
         */
        public String toStringExtended(List<String> loadedPluginMainClasses) {
            List<String> missingRequirements = getMissingRequirements(loadedPluginMainClasses);
            String nameOfPlugin = _contained.toString();
            if (_contained instanceof IPlugin) {
                nameOfPlugin = ((IPlugin) _contained).getName();
            }
            if (_contained instanceof PluginProperties) {
                nameOfPlugin = ((PluginProperties) _contained).getName();
            }
            return nameOfPlugin + "\n ---> providing \n * "
                   + CollectionLister.toString(_provisions, "\n * ")
                   + ";\n ---> requiring but missing \n * "
                   + CollectionLister.toString(missingRequirements, "\n * ");
        }

        public List<String> getMissingRequirements(List<String> loadedPluginMainClasses) {
            List<String> missingRequirements = new Vector<String>();
            if (_requirements != null) {
                for (String required : _requirements) {
                    if (!loadedPluginMainClasses.contains(required)) {
                        missingRequirements.add(required);
                    }
                }
            }
            return missingRequirements;
        }

        public String getPluginName() {
            if (_contained == null) {
                return null;
            }
            if (_contained instanceof PluginProperties) {
                return ((PluginProperties) _contained).getName();
            }
            if (_contained instanceof IPlugin) {
                return ((IPlugin) _contained).getName();
            }
            return null;
        }

        public Set<String> getProvisions() {
            return _provisions;
        }
    }
}