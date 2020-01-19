package de.renew.lola.parser;

public class Marking {
    private String name;
    private int tokens;

    public Marking(String name, int tokens) {
        this.name = name;
        this.tokens = tokens;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @return the tokens
     */
    public int getTokens() {
        return tokens;
    }

    /**
     * @param tokens the tokens to set
     */
    protected void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public boolean equals(Object o) {
        if (o instanceof Marking) {
            return getName().equals(((Marking) o).getName())
                   && getTokens() == ((Marking) o).getTokens();
        } else {
            return false;
        }
    }
}