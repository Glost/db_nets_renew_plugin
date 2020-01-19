package de.renew.refactoring.edit;

import de.renew.refactoring.match.Match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Sorter for {@link Match} objects.</p>
 *
 * <p>The only public method {@link #sorted(List)} sorts objects to an order
 * that allows renaming them.</p>
 *
 * <p>Example: if there are two matches A and B (A before B) in the same text
 * figure's text and A is renamed first to name with different length, the
 * index of B is changed. If B is renamed first, A's index stays unchanged and
 * both can be renamed without the need to repeat the search for matches or
 * keeping track of index shifts.</p>
 *
 * @author 2mfriedr
 */
public abstract class MatchSorter<T extends Match, S> {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(MatchSorter.class);
    private Comparator<T> _comparator;

    /**
     * Sorts {@link Match} objects by first grouping them by the groups
     * determined in {@link #group(Match)}, sorting the individual groups, and
     * rejoining the groups to a list.
     *
     * @param matches the matches to be sorted
     * @return a sorted list
     */
    public List<T> sorted(final List<T> matches) {
        Map<S, List<T>> groups = groupedMatches(matches);
        for (List<T> group : groups.values()) {
            sortGroup(group);
        }

        List<T> sortedMatches = new ArrayList<T>();
        for (List<T> group : groups.values()) {
            sortedMatches.addAll(group);
        }
        return sortedMatches;
    }

    /**
     * Determines the group that a match is sorted into.
     *
     * @param match the match
     * @return the group
     */
    protected abstract S group(T match);

    /**
     * Groups match objects by the groups determined in {@link #group(Match)}.
     *
     * @param matches the matches to be grouped
     * @return a map of groups
     */
    private Map<S, List<T>> groupedMatches(final List<T> matches) {
        Map<S, List<T>> grouped = new HashMap<S, List<T>>();
        for (T match : matches) {
            S group = group(match);
            if (!grouped.containsKey(group)) {
                grouped.put(group, new ArrayList<T>());
            }
            grouped.get(group).add(match);
        }
        return grouped;
    }

    /**
     * Sorts a list of match objects in place with a comparator that orders
     * them by start index in descending order.
     *
     * @param matches the matches to be sorted
     */
    private void sortGroup(final List<T> group) {
        if (_comparator == null) {
            _comparator = new Comparator<T>() {
                    @Override
                    public int compare(T first, T second) {
                        return second.getStart() - first.getStart();
                    }
                };
        }
        Collections.sort(group, _comparator);
    }
}