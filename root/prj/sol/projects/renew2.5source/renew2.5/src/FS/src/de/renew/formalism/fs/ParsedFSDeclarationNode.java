package de.renew.formalism.fs;

import de.uni_hamburg.fs.Concept;
import de.uni_hamburg.fs.TypeSystem;

import de.renew.formalism.java.ParsedDeclarationNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;


/**
 * Extends <code>ParsedDeclarationNode</code> to collect namespaces of
 * concepts for feature structures.
 * <p>
 * </p>
 *
 * @author Frank Wienberg
 * @author documentation by Michael Duvigneau
 * @since Renew 1.6
 **/
public class ParsedFSDeclarationNode extends ParsedDeclarationNode {

    /**
     * Stores names of concept namespaces.
     * Names are added via {@link #addAccess} from back to front.
     * Elements of the list are of type {@link String}.
     **/
    private LinkedList<String> accesses = new LinkedList<String>();

    /**
     * Creates a new <code>ParsedFSDeclarationNode</code> without a default
     * namespace.
     **/
    public ParsedFSDeclarationNode() {
    }

    /**
     * Creates a new <code>ParsedFSDeclarationNode</code> with a default
     * namespace.
     *
     * @param defaultAccess   the identifier of the namespace that is
     *                        accessable by default.
     **/
    public ParsedFSDeclarationNode(String defaultAccess) {
        super();
        accesses.addFirst(defaultAccess);
    }

    /**
     * Adds the given namespace to the list of accessable concept
     * namespaces.  In the case of ambigous concepts defined in multiple
     * namespaces, the one that has been added last wins.
     *
     * @param decl   the identifier of the namespace to access.
     * @param token  the <code>de.renew.formalism.java.Token</code> that has
     *               been parsed into <code>decl</code>. This parameter is
     *               currently not used, but might be useful to report
     *               error locations. It may be <code>null</code>.
     * @exception de.renew.formalism.java.ParseException if an error occurs
     **/
    public void addAccess(String decl, de.renew.formalism.java.Token token)
            throws de.renew.formalism.java.ParseException {
        accesses.addFirst(decl);
        // logger.debug("Accessing Namespace "+decl+".");
    }

    /**
     * Tries to interprete the given name either as a concept, a class or
     * as a local variable.
     * <p>
     * First, all namespaces that have been declared via {@link #addAccess}
     * are searched (from last to first) for a concept with the given name.
     * The method delegates to {@link ParsedDeclarationNode#interpreteName}
     * if no concept has been found.
     * </p>
     *
     * @param name {@inheritDoc}
     * @return a {@link Concept}, if the name denotes a concept.<br>
     *         Otherwise, returns
     *         {@inheritDoc}
     * @throws LinkageError {@inheritDoc}
     **/
    public Object interpreteName(final String name) throws LinkageError {
        // A concept in an imported namespace?
        Concept asConcept = interpreteAsConcept(name);
        if (asConcept != null) {
            return asConcept;
        }
        return super.interpreteName(name);
    }

    /**
     * Looks up the given name either in the tables of accessible concept
     * name spaces, declared variables and imported classes.
     * <p>
     * Although concept name spaces are accessed in the manner of starred
     * Java imports, the included concepts are treated as well known.  This
     * is done because a defined concept always overrides any other meaning
     * of the same name.
     * </p>
     *
     * @param name {@inheritDoc}
     * @return a {@link Concept}, if the name denotes a concept.<br>
     *         Otherwise, returns
     *         {@inheritDoc}
     * @throws LinkageError {@inheritDoc}
     **/
    public Object interpreteWellKnownName(final String name)
            throws LinkageError {
        // A concept in an imported name space?
        Concept asConcept = interpreteAsConcept(name);
        if (asConcept != null) {
            return asConcept;
        }
        return super.interpreteWellKnownName(name);
    }

    /**
     * Looks up a concept with the given name.
     *
     * @param name  the name to interpret as concept
     * @return  the concept associated with the given name, if there is any.
     *          Otherwise, returns <code>null</code>.
     **/
    private Concept interpreteAsConcept(final String name) {
        for (Iterator<String> namespaces = accesses.iterator();
                     namespaces.hasNext();) {
            String nmspc = namespaces.next();
            try {
                return TypeSystem.instance().conceptForName(nmspc + "::" + name);
            } catch (NoSuchElementException e) {
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @param buffer    {@inheritDoc}
     * @param separator {@inheritDoc}
     **/
    protected void augmentToString(StringBuffer buffer, String separator) {
        super.augmentToString(buffer, separator);
        buffer.append(separator);
        buffer.append("accesses=").append(accesses);
    }
}