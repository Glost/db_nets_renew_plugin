/*
 * Created on Aug 1, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public class WordImpl implements Word {
    private String _name;

    /**
     *
     */
    public WordImpl() {
        _name = "";
    }

    /**
     * @param token
     */
    public WordImpl(String token) {
        _name = token;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            Word word = (Word) obj;
            return this.getName().equals(word.getName());
        }
        return false;
    }


    /**
     * @return the word as string.
     */
    @Override
    public String getName() {
        return _name;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.model.Word#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        if ("".equals(getName())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "" + _name;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}