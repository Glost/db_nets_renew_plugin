package de.renew.navigator.models;

import de.renew.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


final public class SearchFilter {
    private final String name;
    private final Type type;
    private final List<String> terms;
    private final boolean caseSensitive;

    /**
     * @param type type of search filter
     * @param caseSensitive if it should check the case
     * @param terms search terms
     */
    public SearchFilter(String name, Type type, boolean caseSensitive,
                        String... terms) {
        this.name = name;
        this.type = type;
        this.caseSensitive = caseSensitive;
        this.terms = new ArrayList<String>();

        if (terms != null) {
            for (String text : terms) {
                String tmp = StringUtil.trimToNull(text);
                if (tmp == null) {
                    continue;
                }
                this.terms.add(tmp);
            }
        }
    }

    /**
     * Matches a string with the search filter.
     *
     * @param check string to check
     * @return true, if the string matches
     */
    public boolean match(final String check) {
        if (this.terms.isEmpty()) {
            return true;
        }

        String toCheck = StringUtil.trimToNull(check);
        if (toCheck == null) {
            return true;
        }

        // Check all
        final String checkText = caseSensitive ? toCheck : toCheck.toLowerCase();
        for (String term : terms) {
            final String termText = caseSensitive ? term : term.toLowerCase();

            if (isStartsWith() && checkText.startsWith(termText)) {
                return true;
            }

            if (isContains() && checkText.contains(termText)) {
                return true;
            }

            if (isEndsWith() && checkText.endsWith(termText)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates the search filter.
     *
     * @return true, if valid
     */
    public boolean isValid() {
        for (String text : terms) {
            if (text != null && !text.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEndsWith() {
        return type == Type.ENDS_WITH;
    }

    public boolean isStartsWith() {
        return type == Type.STARTS_WITH;
    }

    public boolean isContains() {
        return type == Type.CONTAINS;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Type getType() {
        return type;
    }

    public List<String> getTerms() {
        return terms;
    }

    public String getName() {
        return name;
    }

    /**
     * Type of the search filter
     */
    public enum Type {STARTS_WITH,
        CONTAINS,
        ENDS_WITH;
    }
}