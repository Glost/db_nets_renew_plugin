package de.renew.database.entitylayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * The EntityGenerator generates entity
 * java source files out of SQL create
 * statement scripts.
 */
public class EntityGenerator {

    /**
     * Generates an entity source file out of
     * a table name and SQL create statement body.
     * The file name is the name of the table,
     * appended by 'Entity', and is saved in
     * the current directory.
     * @param tableName The table name.
     * @param tableDef The create statement body.
     * @exception IOException If any I/O error occurred.
     */
    private static void generateEntitySource(String tableName, String tableDef)
            throws IOException {
        Vector<Attribute> attributes = new Vector<Attribute>();
        Vector<Attribute> primaryKey = new Vector<Attribute>();

        StringTokenizer attributeTokenizer = new StringTokenizer(tableDef, ",");
        while (attributeTokenizer.hasMoreTokens()) {
            String attributeDef = attributeTokenizer.nextToken().trim();
            StringTokenizer defTokenizer = new StringTokenizer(attributeDef, " ");

            String attributeName = "";
            if (defTokenizer.hasMoreTokens()) {
                attributeName = defTokenizer.nextToken().trim();
            }

            String attributeType = "";
            if (defTokenizer.hasMoreTokens()) {
                attributeType = defTokenizer.nextToken().trim();
            }

            if (!attributeName.equals("")) {
                Attribute attribute;
                if (attributeType.startsWith("INT")
                            || attributeType.startsWith("NUMBER")) {
                    attribute = new Attribute(attributeName, Attribute.TYPE_INT);
                } else if (attributeType.startsWith("REAL")
                                   || attributeType.startsWith("FLOAT")
                                   || attributeType.startsWith("DOUBLE")) {
                    attribute = new Attribute(attributeName, Attribute.TYPE_REAL);
                } else {
                    attribute = new Attribute(attributeName, Attribute.TYPE_CHAR);
                }

                attributes.addElement(attribute);
                if (attributeName.equals(tableName + "_ID")
                            || attributeName.equals(tableName + "ID")
                            || attributeName.equals("ID")) {
                    primaryKey.addElement(attribute);
                }
            }
        }

        generateEntitySource(tableName, attributes, primaryKey);
    }

    /**
     * Generates an entity source file out of a
     * table name and attribute and primary key lists.
     * The file name is the name of the table,
     * appended by 'Entity', and is saved in
     * the current directory.
     * @param tableName The table name.
     * @param attributes The attributes as Vector.
     * @param primaryKey The primary key attributes
     * as Vector.
     * @exception IOException If any I/O error occurred.
     */
    private static void generateEntitySource(String tableName,
                                             Vector<Attribute> attributes,
                                             Vector<Attribute> primaryKey)
            throws IOException {
        String className = sqlStringToJavaString(tableName, true) + "Entity";
        String outFileName = className + ".java";
        System.out.println("Generating " + outFileName + "...");

        PrintWriter outStream = null;
        try {
            File outFile = new File(outFileName);
            outStream = new PrintWriter(new FileOutputStream(outFile));

            outStream.print("import de.renew.database.entitylayer.*;\n"
                            + "import java.sql.*;\n\n\n" + "/**\n"
                            + " * The entity class for the table " + tableName
                            + ".\n" + " */\n" + "public class " + className
                            + " extends Entity\n" + "{\n"
                            + "\t// Table specifications\n" + "\t\n"
                            + "\t/**\n"
                            + "\t * The attributes of the entity.\n"
                            + "\t */\n"
                            + "\tprivate static Attribute[] attributes;\n"
                            + "\t\n" + "\t/**\n"
                            + "\t * The primary key of the entity.\n"
                            + "\t */\n"
                            + "\tprivate static Attribute[] primaryKey;\n"
                            + "\t\n" + "\tstatic\n" + "\t{\n"
                            + "\t\tprimaryKey = new Attribute[]\n" + "\t\t{\n");

            boolean firstAttribute = true;
            Enumeration<Attribute> primaryKeyEnum = primaryKey.elements();
            while (primaryKeyEnum.hasMoreElements()) {
                Attribute attribute = primaryKeyEnum.nextElement();

                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    outStream.print(",\n");
                }

                outStream.print("\t\t\tnew Attribute(\"" + attribute.getName());
                switch (attribute.getType()) {
                case Attribute.TYPE_INT:
                    outStream.print("\", Attribute.TYPE_INT)");
                    break;
                case Attribute.TYPE_REAL:
                    outStream.print("\", Attribute.TYPE_REAL)");
                    break;
                case Attribute.TYPE_CHAR:
                    outStream.print("\", Attribute.TYPE_CHAR)");
                    break;
                }
            }

            outStream.print("\n\t\t};\n\t\n"
                            + "\t\tattributes = new Attribute[]\n" + "\t\t{\n");

            firstAttribute = true;
            Enumeration<Attribute> attributesEnum = attributes.elements();
            while (attributesEnum.hasMoreElements()) {
                Attribute attribute = attributesEnum.nextElement();

                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    outStream.print(",\n");
                }

                outStream.print("\t\t\tnew Attribute(\"" + attribute.getName());
                switch (attribute.getType()) {
                case Attribute.TYPE_INT:
                    outStream.print("\", Attribute.TYPE_INT)");
                    break;
                case Attribute.TYPE_REAL:
                    outStream.print("\", Attribute.TYPE_REAL)");
                    break;
                case Attribute.TYPE_CHAR:
                    outStream.print("\", Attribute.TYPE_CHAR)");
                    break;
                }
            }

            outStream.print("\n\t\t};\n" + "\t}\n\n" + "\t/**\n"
                            + "\t * Creates the entity.\n"
                            + "\t * @param connection The connection to\n"
                            + "\t * be used for entity operations.\n"
                            + "\t * @param dialect The SQL dialect to\n"
                            + "\t * be used for entity operations.\n"
                            + "\t */\n" + "\tpublic " + className
                            + "(Connection connection, SQLDialect dialect)\n"
                            + "\t{\n" + "\t\tsuper(connection, dialect);\n"
                            + "\t}\n" + "\t\n" + "\t/**\n"
                            + "\t * Returns all attributes of the entity as array.\n"
                            + "\t * @param All attributes as array.\n"
                            + "\t */\n"
                            + "\tpublic Attribute[] getAttributes()\n"
                            + "\t{\n" + "\t\treturn attributes;\n" + "\t}\n"
                            + "\t\n" + "\t/**\n"
                            + "\t * Returns the primary key attributes of the entity.\n"
                            + "\t * @return The primary key attributes.\n"
                            + "\t */\n"
                            + "\tpublic Attribute[] getPrimaryKey()\n"
                            + "\t{\n" + "\t\treturn primaryKey;\n" + "\t}\n"
                            + "\t\n" + "\t/**\n"
                            + "\t * Returns the entity's table name.\n"
                            + "\t * @return The entity's table name.\n"
                            + "\t */\n" + "\tpublic String getTableName()\n"
                            + "\t{\n" + "\t\treturn \"" + tableName + "\";\n"
                            + "\t}\n" + "\t\n"
                            + "\t// Attribute getter methods\n" + "\t");

            firstAttribute = true;
            attributesEnum = attributes.elements();
            while (attributesEnum.hasMoreElements()) {
                Attribute attribute = attributesEnum.nextElement();

                String attributeClassName = "";
                switch (attribute.getType()) {
                case Attribute.TYPE_INT:
                    attributeClassName = "Integer";
                    break;
                case Attribute.TYPE_REAL:
                    attributeClassName = "Double";
                    break;
                case Attribute.TYPE_CHAR:
                    attributeClassName = "String";
                    break;
                }

                outStream.println("\n\t/**\n"
                                  + "\t * Returns the value of the attribute "
                                  + attribute.getName() + ".\n"
                                  + "\t * @return The value of the attribute "
                                  + attribute.getName() + ".\n" + "\t */\n"
                                  + "\tpublic " + attributeClassName + " get"
                                  + sqlStringToJavaString(attribute.getName(),
                                                          true) + "()\n"
                                  + "\t{\n" + "\t\treturn ("
                                  + attributeClassName + ") getValue(\""
                                  + attribute.getName() + "\");\n" + "\t}");
            }

            outStream.print("\n\t// Attribute setter methods\n\t");

            firstAttribute = true;
            attributesEnum = attributes.elements();
            while (attributesEnum.hasMoreElements()) {
                Attribute attribute = attributesEnum.nextElement();

                String attributeClassName = "";
                switch (attribute.getType()) {
                case Attribute.TYPE_INT:
                    attributeClassName = "Integer";
                    break;
                case Attribute.TYPE_REAL:
                    attributeClassName = "Double";
                    break;
                case Attribute.TYPE_CHAR:
                    attributeClassName = "String";
                    break;
                }

                outStream.println("\n\t/**\n"
                                  + "\t * Sets the value of the attribute "
                                  + attribute.getName() + ".\n"
                                  + "\t * @param "
                                  + sqlStringToJavaString(attribute.getName(),
                                                          false)
                                  + " The new value for the attribute.\n"
                                  + "\t */\n" + "\tpublic void set"
                                  + sqlStringToJavaString(attribute.getName(),
                                                          true) + "("
                                  + attributeClassName + " "
                                  + sqlStringToJavaString(attribute.getName(),
                                                          false) + ")\n"
                                  + "\t{\n" + "\t\tsetValue(\""
                                  + attribute.getName() + "\", "
                                  + sqlStringToJavaString(attribute.getName(),
                                                          false) + ");\n"
                                  + "\t}");
            }

            outStream.print("}\n");
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    /**
     * Generates all entities as source file out of a
     * sql create statements script.
     * The file names are the name of the resprective
     * table, appended by 'Entity', and are saved in
     * the current directory.
     * @param sql The create statements script.
     * @exception IOException If any I/O error occurred.
     */
    public static void generateEntities(String sql) throws IOException {
        sql = sql.toUpperCase().trim();
        sql = sql.replace('\n', ' ');
        sql = sql.replace('\r', ' ');
        sql = sql.replace('\t', ' ');
        int tableNamePos = sql.indexOf("CREATE TABLE ");
        while (tableNamePos >= 0) {
            tableNamePos += "CREATE TABLE ".length();
            sql = sql.substring(tableNamePos).trim();
            int tableNameEndPos = sql.indexOf(" ");
            if (tableNameEndPos >= 0) {
                String tableName = sql.substring(0, tableNameEndPos).trim();
                sql = sql.substring(tableNameEndPos).trim();
                int tableDefPos = sql.indexOf("(");
                if (tableDefPos >= 0) {
                    sql = sql.substring(tableDefPos).trim();
                    int bracketDepth = 1;
                    int tableDefEndPos;
                    for (tableDefEndPos = 1;
                                 tableDefEndPos < sql.length()
                                 && bracketDepth > 0; tableDefEndPos++) {
                        switch (sql.charAt(tableDefEndPos)) {
                        case '(':
                            bracketDepth++;
                            break;
                        case ')':
                            bracketDepth--;
                            break;
                        }
                    }

                    if (tableDefEndPos < sql.length()) {
                        String tableDef = sql.substring(1, tableDefEndPos - 1)
                                             .trim();
                        generateEntitySource(tableName, tableDef);
                        sql = sql.substring(tableDefEndPos).trim();
                    } else {
                        sql = "";
                    }

                    tableDefPos = sql.indexOf("(");
                }

                tableNameEndPos = sql.indexOf(" ");
            }

            tableNamePos = sql.indexOf("CREATE TABLE ");
        }
    }

    /**
     * The main method.
     * @param args The command line parameters.
     * @exception IOException If any I/O error occurred.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1 || args[0].charAt(0) == '-') {
            System.out.println("EntityGenerator V1.0.\n"
                               + "Builds entity classes out of create table SQL statements.\n"
                               + "Parameters: <SQL script file name>\n");
            System.exit(0);
        }

        File inFile = new File(args[0]);
        FileInputStream inStream = new FileInputStream(inFile);
        byte[] inBytes = new byte[(int) inFile.length()];
        inStream.read(inBytes);
        generateEntities(new String(inBytes));
        inStream.close();

        System.out.print("Remember to add a package information to the"
                         + " entity classes.\n");
    }

    /**
     * Converts an SQL string into Java conventions.
     * For example FIRST_NAME is translated into FirstName.
     * @param sqlString The SQL string to be converted.
     * @param upperString If the first character of
     * the Java string shall be upper case.
     * @return The Java string.
     */
    private static String sqlStringToJavaString(String sqlString,
                                                boolean upperString) {
        StringBuffer javaString = new StringBuffer();
        for (int pos = 0; pos < sqlString.length(); pos++) {
            char ch = sqlString.charAt(pos);

            if (ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'Z'
                        || ch >= 'a' && ch <= 'z') {
                javaString.append(upperString ? Character.toUpperCase(ch)
                                              : Character.toLowerCase(ch));
            }

            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z') {
                upperString = false;
            } else {
                upperString = true;
            }
        }

        return javaString.toString();
    }
}