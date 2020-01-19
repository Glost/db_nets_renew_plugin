package de.renew.formalism.java;

import java.util.Vector;


/**
 * Instances of this class constitute parts of a
 * {@link JavaNetParser#PrimaryExpression}
 * (in the context of the Java net inscription language).
 * The complete primary expression is collected in a
 * {@link Vector} of <code>PrimaryPart</code> objects.
 *
 * <p>
 * The data denoting this part is stored in publicly accessible fields.
 * A part can be of different {@link #type} (see table below).  An object
 * tree (appropriate with respect to the type) is stored in the field
 * {@link #obj}.  The {@link #token} refers to the source location where
 * this part is parsed from.
 * </p>
 *
 * <table border="1">
 * <tr class="TableHeadingColor" style="text-align: left; font-size: larger">
 * <th colspan="3"> Primary part types and appropriate contents </th>
 * </tr>
 * <tr style="text-align: left">
 * <th>Value of {@link #type}</th>
 * <th>Allowed {@link obj} class</th>
 * <th>Comments</th>
 * </tr>
 *
 * <tr>
 * <td>{@link #NAME}</td>
 * <td>{@link String}</td>
 * <td>Any identifier, in case of qualified names in dot notation each
 *     segment constitutes its own part.  However, identifiers ending in
 *     <code>.class</code> form a constant <code>EXPR</code> denoting that
 *     class.</td>
 * </tr>
 *
 * <tr>
 * <td>{@link #ARRAY}</td>
 * <td>{@link TypedExpression}</td>
 * <td>As suffix to other primary parts, this part comprises an expression
 *     evaluating to an array index for array access.</td>
 * </tr>
 *
 * <tr>
 * <td>{@link #CALL}</td>
 * <td>{@link Vector}</td>
 * <td>As suffix to other primary parts, this part denotes the sequence of
 *     arguments to some method or constructor call as a
 *     <code>Vector</code> of expressions.</td>
 * </tr>
 *
 * <tr>
 * <td>{@link #EXPR}</td>
 * <td>{@link TypedExpression}</td>
 * <td>Any expression, for example a literal or
 *     constant expression, the special variable <code>this</code>, a
 *     nested expression (in parentheses), a tuple expression, a list
 *     expression, an array allocation expression or constructor call.
 * </td>
 * </tr>
 * </table>
 *
 * @author Olaf Kummer (code)
 * @author Michael Duvigneau (documentation)
 * @since Renew 1.0 (code)
 * @since Renew 2.4 (documentation)
 **/
public class PrimaryPart {

    /**
     * Used in the field {@link type} to denote that this part is a name.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public final static int NAME = 0; // obj will contain a string

    /**
     * Used in the field {@link type} to denote that this part is an
     * expression denoting an array index.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public final static int ARRAY = 1; // obj will contain an expression

    /**
     * Used in the field {@link type} to denote that this part is an
     * argument list for method or constructor calls.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public final static int CALL = 2; // obj will contain a vector

    /**
     * Used in the field {@link type} to denote that this part is an
     * arbitrary expression.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public final static int EXPR = 3; // obj will contain an expression

    /**
     * Denotes the type of this part of a primary expression.
     * Can either be {@link #NAME}, {@link #ARRAY}, {@link #CALL}, or
     * {@link #EXPR}.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public int type;

    /**
     * Contains the parsed object tree for this part of a primary expression.
     * The stored object can belong to different classes depending on this
     * part's {@link #type}.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public Object obj;

    /**
     * Stores the token that led to parsing this part of a primary
     * expression.  Its main use is to point to the location of errors
     * related to this part.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     **/
    public Token token;

    /**
     * Create a part of a primary expression of the given <code>type</code>
     * denoting the given <code>obj</code> and parsed from the given
     * <code>token</code>.  The class of <code>obj</code> must match some
     * rough criteria according to the given <code>type</code>.
     * See the {@linkplain PrimaryPart class documentation} for more details.
     *
     * @param type  is the {@link #type} of the part.
     * @param obj   is the parsed object tree for the part ({@link #obj}).
     *              Its class has to be appropriate for the given <code>type</code>.
     * @param token is the {@link #token} the part is parsed from.
     *
     * @throws RuntimeException
     *     if <code>obj</code> is not of the correct class according to the
     *     given <code>type</code>, or
     *     if an invalid <code>type</code> is specified.
     **/
    public PrimaryPart(int type, Object obj, Token token) {
        this.type = type;
        this.obj = obj;
        this.token = token;
        if (type == NAME) {
            if (!(obj instanceof String)) {
                throw new RuntimeException("NAME tags must be strings.");
            }
        } else if (type == ARRAY) {
            if (!(obj instanceof TypedExpression)) {
                throw new RuntimeException("ARRAY tags must be expressions.");
            }
        } else if (type == CALL) {
            if (!(obj instanceof Vector)) {
                throw new RuntimeException("CALL tags must be vectors.");
            }
        } else if (type == EXPR) {
            if (!(obj instanceof TypedExpression)) {
                throw new RuntimeException("EXPR tags must be expressions.");
            }
        } else {
            throw new RuntimeException("Bad primary part type: " + type);
        }
    }
}