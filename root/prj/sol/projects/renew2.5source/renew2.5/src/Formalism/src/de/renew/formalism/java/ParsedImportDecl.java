package de.renew.formalism.java;

public class ParsedImportDecl {
    public String name;
    public boolean star;

    public ParsedImportDecl(String name, boolean star) {
        this.name = name;
        this.star = star;
    }
}