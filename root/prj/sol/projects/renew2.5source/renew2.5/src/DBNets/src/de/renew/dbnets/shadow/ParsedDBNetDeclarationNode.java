package de.renew.dbnets.shadow;

import de.renew.dbnets.datalogic.Action;
import de.renew.dbnets.datalogic.Query;
import de.renew.dbnets.persistence.DatabaseSchemaDeclaration;
import de.renew.formalism.java.ParsedDeclarationNode;

import java.util.HashMap;
import java.util.Map;

public class ParsedDBNetDeclarationNode extends ParsedDeclarationNode {

    private final Map<String, Query> queries = new HashMap<>();

    private final Map<String, Action> actions = new HashMap<>();

    private DatabaseSchemaDeclaration databaseSchemaDeclaration;

    public DatabaseSchemaDeclaration getDatabaseSchemaDeclaration() {
        return databaseSchemaDeclaration;
    }

    public void setDatabaseSchemaDeclaration(DatabaseSchemaDeclaration databaseSchemaDeclaration) {
        this.databaseSchemaDeclaration = databaseSchemaDeclaration;
    }

    public void addQuery(Query query) {
        queries.put(query.getName(), query);
    }

    public Query getQueryByName(String name) {
        return queries.get(name);
    }

    public void addAction(Action action) {
        actions.put(action.getName(), action);
    }

    public Action getActionByName(String name) {
        return actions.get(name);
    }
}
