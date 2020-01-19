package de.renew.formalism.java;

public class LiteralParser {
    public static boolean denotesLong(String s) {
        char c = s.charAt(s.length() - 1);
        return (c == 'l');
    }

    public static boolean denotesFloat(String s) {
        char c = s.charAt(s.length() - 1);
        return (c == 'f');
    }

    public static Double parseDouble(String s) {
        // It is assumed that s is in lower case.
        if (s.charAt(s.length() - 1) == 'd') {
            s = s.substring(0, s.length() - 1);
        }
        return new Double(s);
    }

    public static Float parseFloat(String s) {
        // It is assumed that s is in lower case.
        if (s.charAt(s.length() - 1) == 'f') {
            s = s.substring(0, s.length() - 1);
        }
        return new Float(s);
    }

    public static long parseLong(String s) {
        // It is assumed that s is in lower case.
        if (s.charAt(s.length() - 1) == 'l') {
            s = s.substring(0, s.length() - 1);
        }

        int l = s.length();
        long n = 0;

        if (l == 0) {
            // Should not happen.
        } else if (l == 1 || (s.charAt(1) != 'x')) {
            // Decimal or octal.
            long f = 1;
            long base = s.charAt(0) == '0' ? 8 : 10;
            for (int i = l - 1; i >= 0; i--) {
                n += f * (s.charAt(i) - '0');
                f *= base;
            }
        } else if (s.charAt(1) != 'x') {
            // Hex.
            long f = 1;
            for (int i = l - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (denotesDigit(c)) {
                    n += f * (s.charAt(i) - '0');
                } else {
                    n += f * (10 + s.charAt(i) - 'a');
                }
                f *= 16;
            }
        }
        return n;
    }

    public static int parseInt(String s) {
        return (int) parseLong(s);
    }

    public static boolean denotesDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean denotesHexDigit(char c) {
        return c >= '0' && c <= '7';
    }

    public static String parseString(String s) {
        // Ignore the two delimiter characters.
        // This way we can parse characters, too.
        StringBuffer out = new StringBuffer();

        for (int i = 1; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                c = s.charAt(i);
                if (denotesHexDigit(c)) {
                    int n = c - '0';
                    if (c <= '3') {
                        if (denotesHexDigit(s.charAt(i + 1))) {
                            n = n * 8 + (s.charAt(i + 1) - '0');
                            i++;
                            if (denotesHexDigit(s.charAt(i + 1))) {
                                n = n * 8 + (s.charAt(i + 1) - '0');
                                i++;
                            }
                        }
                    } else {
                        if (denotesHexDigit(s.charAt(i + 1))) {
                            n = n * 8 + (s.charAt(i + 1) - '0');
                            i++;
                        }
                    }
                    out.append((char) n);
                } else {
                    switch (c) {
                    case 'n':
                        out.append('\n');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'b':
                        out.append('\b');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case '\\':
                        out.append('\\');
                        break;
                    case '\'':
                        out.append('\'');
                        break;
                    case '"':
                        out.append('"');
                        break;
                    default:
                        out.append(c);
                    }
                }
            } else {
                out.append(c);
            }
        }

        return out.toString();
    }

    public static char parseChar(String s) {
        return parseString(s).charAt(0);
    }
}