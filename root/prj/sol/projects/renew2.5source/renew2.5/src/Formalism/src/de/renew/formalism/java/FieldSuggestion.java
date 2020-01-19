package de.renew.formalism.java;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FieldSuggestion extends Suggestion {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FieldSuggestion.class);
    private final String attemptedName;
    private Field field;

    public static List<FieldSuggestion> suggest(Class<?> clazz, String name,
                                                int modifier) {
        if (logger.isDebugEnabled()) {
            logger.debug(JavaHelper.class.getName() + ": Class = "
                         + clazz.toString());
        }

        List<Field> resultFieldList = new ArrayList<Field>();

        Field[] fields = clazz.getFields();
        if (fields.length != 0) {
            String fieldPattern = name;

            boolean filter = false;

            // do filter if field name suffixes with "_"
            if (fieldPattern.endsWith("_")) {
                fieldPattern = fieldPattern.substring(0,
                                                      fieldPattern.length() - 1);
                filter = true;
            }

            // do filtering when number of fields is large
            if (fields.length > 20) {
                filter = true;
            }

            // force no filtering by typing only "_"
            if (name.equals("_")) {
                filter = false;
            }

            for (Field field : fields) {
                int mod = field.getModifiers();
                String fieldName = field.getName();
                if (logger.isDebugEnabled()) {
                    logger.debug("Name " + fieldName + " pattern: "
                                 + fieldPattern);
                    logger.debug(JavaHelper.class.getName() + ": modifier for "
                                 + field.getName() + "= " + mod + " " + !filter
                                 + fieldName.startsWith(fieldPattern));
                }
                if (!filter || fieldName.startsWith(fieldPattern)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(JavaHelper.class.getName()
                                     + ": passed filter "
                                     + ((modifier & mod) != 0));
                    }
                    if ((modifier & mod) != 0) {
                        resultFieldList.add(field);
                    }
                }
            }
        }

        List<FieldSuggestion> result = new ArrayList<FieldSuggestion>();
        for (Field field : resultFieldList) {
            result.add(new FieldSuggestion(field, name));
        }
        Collections.sort(result);

        return result;
    }

    public FieldSuggestion(Field field, String attemptedName) {
        super(field.getName(), field.getType().getSimpleName());
        this.field = field;
        this.attemptedName = attemptedName;
    }

    public String getDeclaringClassName() {
        return field.getDeclaringClass().getSimpleName();
    }

    public Field getField() {
        return field;
    }

    public String getAttemptedName() {
        return attemptedName;
    }

    @Override
    public String toString() {
        return "<html>" + getName() + " : " + getTypeName()
               + " <font color=gray>- " + getDeclaringClassName()
               + "</font></html>";
    }
}