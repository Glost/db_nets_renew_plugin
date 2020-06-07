package de.renew.dbnets.datalogic;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionCall implements TransitionInscription {

    private static final String STRING_LITERAL_REGEX = "\\\"(?<value>.*)\\\"";

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(STRING_LITERAL_REGEX);

    private static final Random RANDOM = new Random();

    private final String actionName;

    // TODO: List<String> (because we need ordered collection).
    private final Collection<String> params;

    private Action action;

    private VariableMapper variableMapper;

    public ActionCall(String actionName, Collection<String> params) {
        this.actionName = actionName;
        this.params = params;
    }

    public String getActionName() {
        return actionName;
    }

    public Collection<String> getParams() {
        return params;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setVariableMapper(VariableMapper variableMapper) {
        this.variableMapper = variableMapper;
    }

    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        // TODO: Check whether occurrences are necessary here or not.
        return Collections.emptySet();
    }

    public void executeAction() {
        List<Variable> paramsValues = params.stream().map(this::mapParamToRandomValue).collect(Collectors.toList());

        // TODO: execute.
    }

    private Variable mapParamToValue(String param) {
        param = param.trim();

        Matcher stringLiteralMatcher = STRING_LITERAL_PATTERN.matcher(param);

        if (stringLiteralMatcher.matches()) {
            return new Variable(stringLiteralMatcher.group("value"), null);
        }

        Variable randomValue = mapParamToRandomValue(param);

        if (randomValue.isComplete() && randomValue.isBound()) {
            return randomValue;
        }

        return variableMapper.map(new LocalVariable(param)); // TODO: if no such variable?
    }

    private Variable mapParamToRandomValue(String param) {
        switch (param) {
            case "dbn_rand_int": return new Variable(RANDOM.nextInt(), null);
            case "dbn_rand_long": return new Variable(RANDOM.nextLong(), null);
            case "dbn_rand_float": return new Variable(RANDOM.nextFloat(), null);
            case "dbn_rand_double": return new Variable(RANDOM.nextDouble(), null);
            case "dbn_rand_boolean": return new Variable(RANDOM.nextBoolean(), null);
            case "dbn_rand_string": return new Variable(UUID.randomUUID().toString(), null);
            default: return new Variable();
        }
    }
}
