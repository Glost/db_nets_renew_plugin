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
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionCall implements TransitionInscription {

    private static final String STRING_LITERAL_REGEX = "\\\"(?<value>.*)\\\"";

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(STRING_LITERAL_REGEX);

    private static final Random RANDOM = new Random();

    private final Action action;

    private final List<String> params;

    private VariableMapper variableMapper;

    public ActionCall(Action action, List<String> params) {
        this.action = action;
        this.params = params;
    }

    public void setVariableMapper(VariableMapper variableMapper) {
        this.variableMapper = variableMapper;
    }

    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        // TODO: Check whether occurrences are necessary here or not.
        return Collections.emptySet();
    }

    public void performAction() {
        List<Variable> paramsValues = params.stream().map(this::mapParamToValue).collect(Collectors.toList());

        Map<String, Variable> paramsValuesMap = IntStream.range(0, action.getParams().size()).boxed()
                .collect(Collectors.toMap(action.getParams()::get, paramsValues::get));

//        action.getAddedFacts().
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
