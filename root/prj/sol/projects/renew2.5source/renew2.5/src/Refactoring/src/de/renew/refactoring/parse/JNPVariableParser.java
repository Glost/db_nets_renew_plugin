package de.renew.refactoring.parse;

import de.renew.expression.AggregateExpression;
import de.renew.expression.CallExpression;
import de.renew.expression.EqualsExpression;
import de.renew.expression.Expression;
import de.renew.expression.GuardExpression;
import de.renew.expression.InvertibleExpression;
import de.renew.expression.LocalVariable;
import de.renew.expression.TypeCheckingExpression;
import de.renew.expression.VariableExpression;

import de.renew.formalism.java.JavaNetParser;
import de.renew.formalism.java.ParsedDeclarationNode;

import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.inscription.ActionInscription;
import de.renew.net.inscription.ConditionalInscription;
import de.renew.net.inscription.CreationInscription;
import de.renew.net.inscription.DownlinkInscription;
import de.renew.net.inscription.ExpressionInscription;
import de.renew.net.inscription.GuardInscription;
import de.renew.net.inscription.RangeEnumeratorInscription;
import de.renew.net.inscription.SimpleEnumeratorInscription;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.util.StringHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * VariableParser implementation using JavaNetParser.
 *
 * @author 2mfriedr
 */
public class JNPVariableParser implements VariableParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(JNPVariableParser.class);
    private final String _declaration;
    private final ParsedDeclarationNode _declarationNode;

    /**
     * Constructs a parser with the specified declarations.
     *
     * @param declaration the declaration node's text
     */
    public JNPVariableParser(final String declaration) {
        _declaration = declaration;
        ParsedDeclarationNode decl = JNPParser.declarationNode(declaration);
        _declarationNode = (decl != null) ? decl : new ParsedDeclarationNode();
    }

    /**
     * Constructs a parser with an empty declaration node.
     */
    public JNPVariableParser() {
        this("");
    }

    /**
     * Returns a net parser with the member declaration node.
     *
     * @param input the input string
     * @return a net parser
     */
    private JavaNetParser netParser(final String input) {
        return JNPParser.netParser(input, _declarationNode);
    }

    @Override
    public boolean isValidVariableName(final String name) {
        return JNPParser.isIdentifier(netParser(name))
               && (_declarationNode.interpreteWellKnownName(name) == null);
    }

    @Override
    public boolean containsVariable(final String string) {
        return findVariables(string, true).size() > 0;
    }

    @Override
    public List<StringMatch> findVariables(final String string) {
        return findVariables(string, false);
    }

    /**
     * Finds variables in a string.
     *
     * @param string the string to be searched
     * @param stopEarly if this flag is set, the search for variables will be
     * stopped after the first variable is found (speed optimization for
     * {@link #containsVariable(String)})
     * @return a list of string match objects
     */
    private List<StringMatch> findVariables(final String string,
                                            final boolean stopEarly) {
        List<StringMatch> matches = new ArrayList<StringMatch>();

        for (TransitionInscription i : JNPParser.transitionInscriptions(netParser(string))) {
            Collection<VariableExpression> variables = null;
            if (i instanceof ExpressionInscription) {
                ExpressionInscription ei = (ExpressionInscription) i;
                variables = findVariableExpressions(ei.getExpression());
            } else if (i instanceof UplinkInscription) {
                UplinkInscription ui = (UplinkInscription) i;
                variables = findVariableExpressions(ui.params);
            } else if (i instanceof DownlinkInscription) {
                DownlinkInscription di = (DownlinkInscription) i;
                Collection<VariableExpression> callee = findVariableExpressions(di.callee);
                Collection<VariableExpression> params = findVariableExpressions(di.params);
                variables = new ArrayList<VariableExpression>(callee);
                variables.addAll(params);
            } else if (i instanceof ActionInscription) {
                ActionInscription ai = (ActionInscription) i;
                variables = findVariableExpressions(ai.getExpression());
            } else if (i instanceof CreationInscription) {
                CreationInscription ci = (CreationInscription) i;
                VariableExpression v = new VariableExpression(null,
                                                              ci.getVariable());
                variables = Collections.singleton(v);
            } else if (i instanceof ConditionalInscription) {
                ConditionalInscription ci = (ConditionalInscription) i;
                variables = findVariableExpressions(ci.getExpression());
            } else if (i instanceof GuardInscription) {
                GuardInscription gi = (GuardInscription) i;
                variables = findVariableExpressions(gi.getExpression());
            } else if (i instanceof RangeEnumeratorInscription) {
                RangeEnumeratorInscription rei = (RangeEnumeratorInscription) i;
                logger.debug("range enumerator inscription not handled yet: "
                             + rei); // TODO: ?
            } else if (i instanceof SimpleEnumeratorInscription) {
                SimpleEnumeratorInscription sei = (SimpleEnumeratorInscription) i;
                logger.debug("simple enumerator inscription not handled yet: "
                             + sei); // TODO: ?
            }

            if (variables == null) {
                continue;
            }

            for (VariableExpression variable : variables) {
                LocalVariable local = variable.getVariable();
                if (local.name.equals("this")) {
                    // "this" is a local variable but should not be found by this parser
                    continue;
                }
                matches.add(StringHelper.makeStringMatch(string,
                                                         local.variableBeginLine,
                                                         local.variableBeginColumn,
                                                         local.variableEndLine,
                                                         local.variableEndColumn));
                if (stopEarly) {
                    return matches;
                }
            }
        }

        return matches;
    }

    @Override
    public List<StringMatch> findVariablesInDeclarationNode() {
        List<StringMatch> result = new ArrayList<StringMatch>();
        for (String name : _declarationNode.getVariablePositions().keySet()) {
            List<Integer> position = _declarationNode.getVariablePositions()
                                                     .get(name);
            result.add(StringHelper.makeStringMatch(_declaration,
                                                    position.get(0),
                                                    position.get(1),
                                                    position.get(2),
                                                    position.get(3)));
        }
        return result;
    }

    @Override
    public Class<?> findVariableType(final String variable) {
        return _declarationNode.findType(new LocalVariable(variable.trim()));
    }

    /**
     * <p>Recursively finds variable expressions.</p>
     *
     * <p>{@link Expression} subclasses have to be handled separately:</p>
     *
     * <ul>
     *   <li>{@link AggregateExpression} (and its subclasses {@link
     *   ListExpression} and {@link TupleExpression}): search all child
     *   expressions</li>
     *   <li>{@link ExpressionWithTypeField} subclasses:
     *     <ul>
     *       <li>{@link CallExpression}: search the argument</li>
     *       <li>{@link ConstantExpression}: has no variables</li>
     *       <li>{@link EqualsExpression}: search the left and right side</li>
     *       <li>{@link InvertibleExpression}: search the argument</li>
     *       <li>{@link NoArgExpression}: has no variables</li>
     *       <li>{@link TypeCheckingExpression}: search the argument</li>
     *       <li>{@link VariableExpression}: add to result list</li>
     *   </ul>
     *   <li>{@link GuardExpression}: search the argument</li>
     * </ul>
     *
     * @param expression the expression to be searched
     * @return a collection of variable expressions
     */
    private Collection<VariableExpression> findVariableExpressions(final Expression expression) {
        logger.debug("Searching for variable expressions: "
                     + expression.toString());
        List<VariableExpression> expressions = new ArrayList<VariableExpression>();

        if (expression instanceof VariableExpression) {
            VariableExpression variableExpression = (VariableExpression) expression;
            expressions.add(variableExpression);
        } else if (expression instanceof AggregateExpression) {
            AggregateExpression aggregateExpression = (AggregateExpression) expression;
            for (Expression child : aggregateExpression.getExpressions()) {
                expressions.addAll(findVariableExpressions(child));
            }
        } else if (expression instanceof CallExpression) {
            CallExpression callExpression = (CallExpression) expression;
            expressions.addAll(findVariableExpressions(callExpression
                .getArgument()));
        } else if (expression instanceof EqualsExpression) {
            EqualsExpression equalsExpression = (EqualsExpression) expression;
            expressions.addAll(findVariableExpressions(equalsExpression.getLeft()));
            expressions.addAll(findVariableExpressions(equalsExpression.getRight()));
        } else if (expression instanceof InvertibleExpression) {
            InvertibleExpression invertibleExpression = (InvertibleExpression) expression;
            expressions.addAll(findVariableExpressions(invertibleExpression
                .getArgument()));
        } else if (expression instanceof TypeCheckingExpression) {
            TypeCheckingExpression typeCheckingExpression = (TypeCheckingExpression) expression;
            expressions.addAll(findVariableExpressions(typeCheckingExpression
                .getArgument()));
        } else if (expression instanceof GuardExpression) {
            GuardExpression guardExpression = (GuardExpression) expression;
            expressions.addAll(findVariableExpressions(guardExpression));
        }

        return expressions;
    }
}