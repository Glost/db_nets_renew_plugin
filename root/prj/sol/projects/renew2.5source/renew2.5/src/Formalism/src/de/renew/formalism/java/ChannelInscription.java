package de.renew.formalism.java;

public class ChannelInscription {
    public final boolean isUplink;
    public final String name;
    public final int arity;
    private final int hash;

    public ChannelInscription(boolean isUplink, String name, int arity) {
        this.isUplink = isUplink;
        this.name = name.intern();
        this.arity = arity;
        hash = name.hashCode() + arity * 135 + (isUplink ? 137 : 0);
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ChannelInscription)) {
            return false;
        }
        ChannelInscription that = (ChannelInscription) o;
        return isUplink == that.isUplink && name.equals(that.name)
               && arity == that.arity;
    }
}