/*
 * Created on Aug 1, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public class LetterImpl implements Letter {
    private String _name;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LetterImpl) {
            return _name.equals(((LetterImpl) obj).getName());
        } else {
            return false;
        }
    }

    /**
    * @param token
    */
    public LetterImpl(String token) {
        _name = token;
    }

    /**
     * @return the name as string.
     */
    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return "" + _name;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        return ((Letter) o).getName().compareTo(getName());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}