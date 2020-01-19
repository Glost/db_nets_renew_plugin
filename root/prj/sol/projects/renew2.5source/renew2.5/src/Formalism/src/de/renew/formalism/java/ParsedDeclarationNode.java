package de.renew.formalism.java;

import de.renew.expression.LocalVariable;

import de.renew.util.ClassSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Collects known names, including class or package imports and variable
 * declarations.
 * <p>
 * The behaviour of a declaration node depends slightly on whether any
 * variables have been declared (see {@link #interpreteName}).
 * </p>
 *
 * @author Olaf Kummer
 * @author documentation by Michael Duvigneau
 * @since Renew 1.0
 **/
public class ParsedDeclarationNode {

    /**
     * Stores package imports (i.e. imports ending with a star).
     * Declarations are stored as {@link ParsedImportDecl} objects.
     **/
    private List<String> imports;

    /**
     * Stores fully qualified class imports.
     * Declarations are stored as a pair ({@link String}, {@link String})
     * where the key denotes the unqualified class name, and the value
     * denotes the full class name.
     **/
    private Map<String, String> wellKnownClassesNames;
    private Map<String, Class<?>> wellKnownClasses;

    /**
     * Stores variable declarations.
     * Declarations are stored as a pair ({@link String}, {@link Class})
     * where the key is the variable name and the value denotes the type of
     * the variable.
     **/
    private Map<String, Class<?>> variables;

    /**
     * Flags whether any variable declaration has been added to this
     * declaration node.
     **/
    private boolean anyVariablesDeclared;
    private Map<String, List<Integer>> variablePositions;

    /**
     * Creates an empty <code>ParsedDeclarationNode</code> without any
     * knowledge.
     **/
    public ParsedDeclarationNode() {
        imports = new ArrayList<String>();
        wellKnownClasses = new HashMap<String, Class<?>>();
        wellKnownClassesNames = new HashMap<String, String>();
        variables = new HashMap<String, Class<?>>();
        variablePositions = new HashMap<String, List<Integer>>();
        anyVariablesDeclared = false;
    }

    /**
     * Adds the given import declaration to the list of known imports.
     *
     * @param decl     the parsed import declaration.
     * @param token    the lexical token corresponding to the declaration.
     * @throws ParseException  if the import is fully qualified (does
     *                 not end with a star '*'), but a class with the same
     *                 unqualified name has been imported before.
     **/
    public void addImport(ParsedImportDecl decl, Token token)
            throws ParseException {
        if (decl.star) {
            // Remember this qualification for lookup.
            // Which imports should have priority: early or late imports?
            imports.add(decl.name);
        } else {
            // Calculate the short name of the class.
            int pos = decl.name.lastIndexOf(".");
            String name = decl.name.substring(pos + 1);

            // Warn about a name clash, if necessary.
            try {
                Object interpretation = interpreteWellKnownName(name);
                if (interpretation != null) {
                    throw JavaHelper.makeParseException("Class " + name
                                                        + " imported twice.",
                                                        token);
                }
            } catch (LinkageError e) {
                // This linkage error roots from the PREVIOUS declaration
                // (we asked if the name is known already, thereby loading
                // the known class)!
                throw JavaHelper.makeParseException("Class " + name
                                                    + " imported twice.\n"
                                                    + "Additionally, first import cannot be loaded due to "
                                                    + e.toString(), token);
            }


            // Warn about a not imported class
            Class<?> clazz;
            try {
                clazz = classForName(decl.name);
            } catch (RuntimeException e) {
                throw JavaHelper.makeParseException("Could not import class "
                                                    + decl.name + ".", token);
            }

            // Map the short name to the fully qualified name.
            wellKnownClasses.put(name, clazz);
            wellKnownClassesNames.put(name, decl.name);
        }
    }

    /**
     * Adds the given variable with the given type to the list of declared
     * variables.
     *
     * @param clazz    the type the variable has been declared with.
     * @param name     the name of the variable.
     * @param token    the lexical token corresponding to the declaration.
     * @throws ParseException  if the given name has already been declared
     *                 before, either as an imported class or a variable.
     **/
    public void addVariable(Class<?> clazz, String name, Token token)
            throws ParseException {
        // Warn about a name clash, if necessary.
        try {
            Object interpretation = interpreteWellKnownName(name);
            if (interpretation != null) {
                if (interpretation instanceof LocalVariable) {
                    throw JavaHelper.makeParseException("Variable " + name
                                                        + " declared twice.",
                                                        token);
                } else if (interpretation instanceof Class) {
                    throw JavaHelper.makeParseException("Variable " + name
                                                        + " is named identically to an imported class.",
                                                        token);
                } else {
                    throw JavaHelper.makeParseException("Variable " + name
                                                        + " is named identically to a "
                                                        + interpretation.getClass()
                                                                        .getName()
                                                        + ".", token);
                }
            }
        } catch (LinkageError e) {
            // This linkage error roots from the PREVIOUS declaration
            // (we asked if the name is known already, thereby loading
            // the known class)!
            throw JavaHelper.makeParseException("Variable " + name
                                                + " is named identically to an imported class.\n"
                                                + "Additionally, the class cannot be loaded due to"
                                                + e.toString(), token);
        }

        // Remember the variable.
        variables.put(name, clazz);

        List<Integer> position = Arrays.asList(token.beginLine,
                                               token.beginColumn,
                                               token.endLine, token.endColumn);
        variablePositions.put(name, position);

        if (!"this".equals(name)) {
            anyVariablesDeclared = true;
        }
    }

    /**
     * Returns wether the class specified by the given name can be loaded.
     * This method has the side effect of loading the class if it exists.
     * Uses the class loader configured within {@link ClassSource}.
     *
     * @param name   a <i>qualified</i> class name.
     * @return       <code>true</code> if the class exists and has
     *               successfully been loaded.
     * @throws LinkageError  if the class exists, but cannot be loaded
     *               due to linkage problems.
     **/
    private static boolean classExists(String name) throws LinkageError {
        try {
            ClassSource.classForName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Loads the class specified by the given name and returns it.
     * Uses the class loader configured within {@link ClassSource}.
     *
     * @param name   a <i>qualified</i> class name.
     * @return       the loaded <code>Class</code> object.
     * @throws RuntimeException  if the class has not been found.
     *               This should not happen if all calls to this method are
     *               preceded by a call to {@link #classExists}.
     * @throws LinkageError  if the class exists, but cannot be loaded
     *               due to linkage problems.
     **/
    private static Class<?> classForName(String name) throws LinkageError {
        try {
            return ClassSource.classForName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class: " + name, e);
        }
    }

    /**
     * Looks up the given name in the tables of declared variables
     * and imported classes.
     *
     * @param name  the name to interprete.
     * @return <ol>
     *   <li>a {@link LocalVariable} object, if the name denotes a
     *       declared variable.</li>
     *   <li>a {@link Class} object, if the name denotes an imported
     *       class.</li>
     *   <li><code>null</code>, otherwise.</li>
     *   </ol>
     * @throws LinkageError
     *   if the name denotes an imported class, but the class cannot be
     *   loaded due to linkage problems.
     **/
    public Object interpreteWellKnownName(final String name)
            throws LinkageError {
        // Lookup the declaration. A variable?
        if (variables.containsKey(name)) {
            return new LocalVariable(name);
        }

        // An imported class?
        if (wellKnownClassesNames.containsKey(name)) {
            String iName = wellKnownClassesNames.get(name);
            if (classExists(iName)) {
                return classForName(iName);
            }
        }
        return null;
    }

    /**
     * Tries to interprete the given name either as a class or as a local
     * variable.
     *
     * The method uses different hints for the interpretation:
     * <ol>
     * <li>If the name is qualified (contains dots), it must denote a
     *     class or package.</li>
     * <li>Declared names are either known as variables or imported
     *     classes.</li>
     * <li>Perhaps the name denotes a class within an imported package,
     *     the unnamed package or the <code>java.lang</code> package
     *     (however, duplicate class definitions are not detected).</li>
     * <li>If there are no variables declared, the fallback is to interpret
     *     any unknown name as a variable.</li>
     * </ol>
     *
     * @param name a <code>String</code> value
     * @return <ol>
     *   <li>a {@link Class} object, if the name denotes a class.</li>
     *   <li>a {@link LocalVariable} object, if the name denotes a
     *       local variable.</li>
     *   <li><code>null</code>, if no connection can be made.</li>
     *   </ol>
     * @throws LinkageError
     *   if the name denotes a class, but the class cannot be loaded
     *   due to linkage problems.
     **/
    public Object interpreteName(String name) throws LinkageError {
        // Is it a qualified name?
        if (name.indexOf(".") >= 0) {
            // This could be a qualified class.
            if (classExists(name)) {
                return classForName(name);
            }

            // Don't know. Maybe it is a package specification.
            return null;
        }

        // Is the object well-known?
        Object interpretation = interpreteWellKnownName(name);
        if (interpretation != null) {
            // Yes, the object was declared explicitly.
            return interpretation;
        }


        // A class in an imported package?
        Iterator<String> packages = imports.iterator();
        while (packages.hasNext()) {
            String pckg = packages.next();
            if (classExists(pckg + "." + name)) {
                return classForName(pckg + "." + name);
            }
        }

        // A class without package?
        if (classExists(name)) {
            return classForName(name);
        }

        // No. A language class?
        if (classExists("java.lang." + name)) {
            return classForName("java.lang." + name);
        }

        // Do we have any declarations?
        if (!anyVariablesDeclared) {
            // It is a variable, even though it is not declared.
            return new LocalVariable(name);
        } else {
            // Don't know. Maybe it is a package specification.
            return null;
        }
    }

    /**
     * Determines the type of the given variable based on the known
     * declarations.
     *
     * @param var  the variable whose type is of interest.
     * @return     the <code>Class</code> used in the variable declaration,
     *             if the variable has been declared.
     *             Returns {@link de.renew.util.Types#UNTYPED}, otherwise.
     **/
    public Class<?> findType(LocalVariable var) {
        if (variables.containsKey(var.name)) {
            return variables.get(var.name);
        } else {
            return de.renew.util.Types.UNTYPED;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final int sbSize = 1000;
        final String variableSeparator = ", ";
        final StringBuffer sb = new StringBuffer(sbSize);
        sb.append(this.getClass().getName());
        sb.append("(");
        sb.append("imports=").append(imports);
        sb.append(variableSeparator);
        sb.append("wellKnownClasses=").append(wellKnownClassesNames);
        sb.append(variableSeparator);
        sb.append("variables=").append(variables);
        sb.append(variableSeparator);
        sb.append("anyVariablesDeclared=").append(anyVariablesDeclared);
        augmentToString(sb, variableSeparator);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Called by {@link #toString} to allow subclasses to append their
     * fields to the object description.  Subclasses should call
     * <code>super.augmentToString(buffer, separator)</code> first.
     *
     * @param buffer     the <code>StringBuffer</code> to append information
     *                   to.  Subclasses should start every entry with a
     *                   copy of <code>separator</code>.
     * @param separator  <code>String</code> that separates fields in the
     *                   description.
     **/
    protected void augmentToString(StringBuffer buffer, String separator) {
    }

    public Map<String, Class<?>> getWellKnownClasses() {
        return wellKnownClasses;
    }

    public Map<String, Class<?>> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public Map<String, List<Integer>> getVariablePositions() {
        return Collections.unmodifiableMap(variablePositions);
    }
}