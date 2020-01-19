package de.renew.formalism.java;

import de.renew.shadow.SyntaxException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ChannelCheckNode {
    public final String name;
    public final int arity;
    private final int hash;

    // 0: unchecked, 1: check active, 2: check completed, 3: start of cycle
    private int checkState;
    private boolean satisfiable;
    private Set<ChannelCheckNode> invokableChannels;

    public ChannelCheckNode(String name, int arity) {
        this.name = name.intern();
        this.arity = arity;
        hash = name.hashCode() + arity * 135;

        checkState = 0;
        satisfiable = false;
        invokableChannels = new HashSet<ChannelCheckNode>();
    }

    public void setSatisfiable() {
        satisfiable = true;
    }

    public void check() throws SyntaxException {
        String result = checkRecursively();
        if (result != null) {
            throw new SyntaxException(result);
        }
    }

    public String makeChannelName() {
        StringBuffer result = new StringBuffer(name);
        result.append('(');
        for (int i = 1; i < arity; i++) {
            result.append("*,");
        }
        if (arity > 0) {
            result.append('*');
        }
        result.append(')');
        return result.toString();
    }

    public String checkRecursively() throws SyntaxException {
        if (checkState == 2) {
            return null;
        } else if (checkState == 1) {
            checkState = 3;
            return "This completes a cycle.";
        }

        checkState = 1;

        Iterator<ChannelCheckNode> iterator = invokableChannels.iterator();
        if (iterator.hasNext()) {
            do {
                ChannelCheckNode node = iterator.next();

                if (equals(node)) {
                    throw new SyntaxException("Channel " + makeChannelName()
                                              + " can invoke itself.");
                }

                String result = node.checkRecursively();
                if (result != null) {
                    if (name.equals("")) {
                        result = "Channel " + node.makeChannelName()
                                 + " can be invoked spontaneously.\n" + result;
                    } else {
                        result = "Channel " + makeChannelName()
                                 + " can invoke channel "
                                 + node.makeChannelName() + ".\n" + result;
                    }
                    if (checkState == 3) {
                        throw new SyntaxException(result);
                    } else {
                        return result;
                    }
                }
            } while (iterator.hasNext());
        } else if (!satisfiable) {
            throw new SyntaxException("Channel " + makeChannelName()
                                      + " cannot be satisfied.");
        }

        checkState = 2;
        return null;
    }

    public void addInvokableChannel(ChannelCheckNode node) {
        invokableChannels.add(node);
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ChannelCheckNode)) {
            return false;
        }
        ChannelCheckNode that = (ChannelCheckNode) o;
        return name.equals(that.name) && arity == that.arity;
    }
}