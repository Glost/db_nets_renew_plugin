package de.renew.lola.parser;

public class Node {
    private Integer x = null;
    private Integer y = null;
    private String name;

    protected Node(String name, int x, int y) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    protected Node(String name) {
        this.name = name;
    }

    public Integer getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    protected void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    protected void setY(Integer y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public boolean hasCoordinates() {
        return (getX() != null) && (getY() != null);
    }

    public boolean equals(Object o) {
        if (o instanceof Node) {
            return getName().equals(((Node) o).getName());
        } else {
            return false;
        }
    }
}