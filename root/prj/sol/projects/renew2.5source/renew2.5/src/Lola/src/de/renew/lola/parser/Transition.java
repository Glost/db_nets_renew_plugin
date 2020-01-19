package de.renew.lola.parser;

import java.util.HashMap;
import java.util.Map;


public class Transition extends Node {
    private Map<Place, Integer> pre = new HashMap<Place, Integer>();
    private Map<Place, Integer> post = new HashMap<Place, Integer>();

    protected Transition(String name) {
        super(name);
    }

    public Transition(String name, Map<Place, Integer> pre,
                      Map<Place, Integer> post) {
        super(name);
        this.pre = pre;
        this.post = post;
    }

    protected Transition(String name, int x, int y) {
        super(name, x, y);
    }

    protected Transition(String name, int x, int y, Map<Place, Integer> pre,
                         Map<Place, Integer> post) {
        super(name, x, y);
        this.pre = pre;
        this.post = post;
    }

    /**
     * @return the pre
     */
    protected Map<Place, Integer> getPre() {
        return pre;
    }

    /**
     * @param pre the pre to set
     */
    protected void setPre(Map<Place, Integer> pre) {
        this.pre = pre;
    }

    /**
     * @return the post
     */
    protected Map<Place, Integer> getPost() {
        return post;
    }

    /**
     * @param post the post to set
     */
    protected void setPost(Map<Place, Integer> post) {
        this.post = post;
    }

    public boolean equals(Object o) {
        if (o instanceof Transition) {
            return getName().equals(((Transition) o).getName());
        } else {
            return false;
        }
    }
}