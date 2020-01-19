package de.renew.call;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.lang.reflect.Method;

import java.util.Hashtable;


class StubGenerator {
    static private String USAGE_MESSAGE = "Required arguments: [-d PATH] CLASS NET {[+]INTERFACE}\n\n"
                                          + "CLASS must be a fully qualified class name.\n"
                                          + "NET must be a valid net identifier.\n"
                                          + "If an INTERFACE in the list is preceeded by '+',\n"
                                          + "its void methods will be designated as 'break void'.\n\n"
                                          + "The resulting stub file will be placed in the directory\n"
                                          + "that corresponds to the package of CLASS, if any.\n"
                                          + "If the -d option if specified, PATH is taken as the root directory\n"
                                          + "for the target stub file, otherwise the current directory.\n"
                                          + "No directory will be created. No files will be overwritten.\n";

    static String complexForPrimitive(Class<?> clazz) {
        if (clazz == Boolean.TYPE) {
            return "Boolean";
        }
        if (clazz == Byte.TYPE) {
            return "Byte";
        }
        if (clazz == Character.TYPE) {
            return "Character";
        }
        if (clazz == Short.TYPE) {
            return "Short";
        }
        if (clazz == Integer.TYPE) {
            return "Integer";
        }
        if (clazz == Long.TYPE) {
            return "Long";
        }
        if (clazz == Float.TYPE) {
            return "Float";
        }
        if (clazz == Double.TYPE) {
            return "Double";
        }
        return "";
    }

    static String getTypeName(Class<?> clazz) {
        int level = 0;
        while (clazz.isArray()) {
            level++;
            clazz = clazz.getComponentType();
        }
        StringBuffer name = new StringBuffer(clazz.getName());
        while (level > 0) {
            name.append("[]");
            level--;
        }
        return name.toString();
    }

    static String makeSignature(Method method) {
        Class<?>[] params = method.getParameterTypes();

        StringBuffer signature = new StringBuffer();
        signature.append(getTypeName(method.getReturnType()));
        signature.append(" ");
        signature.append(method.getName());
        signature.append("(");
        for (int k = 0; k < params.length; k++) {
            if (k > 0) {
                signature.append(",");
            }
            signature.append(getTypeName(params[k]));
        }
        signature.append(")");
        return signature.toString();
    }

    static void outputMethod(Method method, boolean breakWanted,
                             PrintWriter writer) {
        Class<?>[] params = method.getParameterTypes();
        StringBuffer head = new StringBuffer("  ");
        if ((method.getReturnType() == Void.TYPE) && breakWanted) {
            head.append("break ");
        }
        head.append(getTypeName(method.getReturnType()));
        head.append(" ");
        head.append(method.getName());
        head.append("(");
        for (int k = 0; k < params.length; k++) {
            if (k > 0) {
                writer.println(head);
                head = new StringBuffer();
                head.append("    ");
            }
            String parString = getTypeName(params[k]) + " arg" + k;
            head.append(parString);
            if (k < params.length - 1) {
                head.append(",");
            }
        }
        head.append(")");
        if (params.length > 1) {
            writer.println(head);
            head = new StringBuffer();
            head.append(" ");
        }
        head.append(" {");
        writer.println(head);
        head = new StringBuffer("    this:");
        head.append(method.getName());
        head.append("(instance");
        for (int k = 0; k < params.length; k++) {
            head.append(",arg" + k);
        }
        head.append(");");
        writer.println(head);
        if (method.getReturnType() == Void.TYPE) {
            writer.println("    this:result(instance);");
        } else {
            writer.println("    this:result(instance,return);");
        }
        writer.println("  }");
    }

    public static void main(String[] args) {
        if (args.length < 2 || ("-d".equals(args[0]) && args.length < 4)) {
            System.err.println(USAGE_MESSAGE);
            System.exit(0);
        }

        int index = 0;
        String path = "";
        if ("-d".equals(args[0])) {
            index++;
            path = args[index++].substring(2).trim();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        String stubname = args[index++];
        String fileName = path + stubname.replace('.', File.separatorChar)
                          + ".stub";
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            System.err.println("Target file " + fileName
                               + " already exists. Aborting.");
        }
        String netName = args[index++];

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(outputFile));

            boolean[] breakWanted = new boolean[args.length - index];
            String[] name = new String[args.length - index];
            for (int i = 0; index < args.length; i++) {
                breakWanted[i] = (args[index].charAt(0) == '+');
                if (breakWanted[i]) {
                    name[i] = args[index].substring(1);
                } else {
                    name[i] = args[index];
                }
                index++;
            }

            int dotPos = stubname.lastIndexOf(".");
            String className;
            if (dotPos > 0) {
                writer.println("package " + stubname.substring(0, dotPos) + ";");
                className = stubname.substring(dotPos + 1);
            } else {
                className = stubname;
            }
            int interfaceCount = name.length;
            writer.println("class " + className + " for net " + netName);
            if (interfaceCount > 0) {
                writer.println("implements");
                for (int i = 0; i < interfaceCount; i++) {
                    if (i + 1 < interfaceCount) {
                        writer.println("  " + name[i] + ",");
                    } else {
                        writer.println("  " + name[i]);
                    }
                }
            }
            writer.println("{");

            Hashtable<String, Boolean> allMethods = new Hashtable<String, Boolean>();


            // Insert all methods already provided by NetInstance,
            // so that these are not overwritten, if an interface
            // implements NetInstance.
            Method[] netInstanceMethods = de.renew.net.NetInstance.class
                                              .getMethods();
            for (int j = 0; j < netInstanceMethods.length; j++) {
                Method method = netInstanceMethods[j];
                String signatureString = makeSignature(method);
                allMethods.put(signatureString, Boolean.FALSE);
            }

            for (int i = 0; i < interfaceCount; i++) {
                Class<?> clazz;
                try {
                    // Because the generator usually runs outside the
                    // plugin system, we use the standard class loader
                    // here.
                    clazz = Class.forName(name[i]); // No reload required.
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e.toString());
                }
                if (!clazz.isInterface()) {
                    throw new RuntimeException("Not an interface: " + name[i]);
                }
                Method[] methods = clazz.getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    String signatureString = makeSignature(method);

                    if (!allMethods.containsKey(signatureString)) {
                        allMethods.put(signatureString,
                                       new Boolean(breakWanted[i]));
                        outputMethod(method, breakWanted[i], writer);
                    } else {
                        Boolean wasBreakWanted = allMethods.get(signatureString);
                        if (method.getReturnType() == Void.TYPE) {
                            if (wasBreakWanted.booleanValue() ^ breakWanted[i]) {
                                throw new RuntimeException("Method requested twice, but only once as break void. "
                                                           + signatureString);
                            }
                        }
                    }
                }
            }
            writer.println("}");
            writer.close();
        } catch (Exception e) {
            System.err.println("An error occurred during the creation of the stub file.");
            System.err.println(e.getMessage());
            try {
                //NOTICEnull
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                // This is probably due to earlier errors.
            }
            try {
                if (outputFile.exists()) {
                    outputFile.delete();
                }
            } catch (Exception ex) {
                System.err.println("Could not remove broken file " + fileName
                                   + ".");
                // This is probably due to earlier errors.
            }
        }
    }
}