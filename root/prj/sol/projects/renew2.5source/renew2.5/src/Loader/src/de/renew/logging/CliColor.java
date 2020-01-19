package de.renew.logging;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-14
 */
public enum CliColor {NORMAL("0"),
    BOLD("1"),
    ITALIC("3"),
    UNDERLINE("4"),
    CROSSED_OUT("9"),
    BLACK("30"),
    RED("31"),
    GREEN("32"),
    YELLOW("33"),
    BLUE("34"),
    PURPLE("35"),
    CYAN("36"),
    WHITE("37"),
    BACKGROUND_BLACK("40"),
    BACKGROUND_RED("41"),
    BACKGROUND_GREEN("42"),
    BACKGROUND_YELLOW("43"),
    BACKGROUND_BLUE("44"),
    BACKGROUND_MAGENTA("45"),
    BACKGROUND_CYAN("46"),
    BACKGROUND_LIGHT_GRAY("47");
    private final String config;

    CliColor(String config) {
        this.config = config;
    }

    /**
     * Colors a string with given options.
     *
     * @param str
     * @param formats
     * @return
     */
    public static String color(String str, CliColor... formats) {
        StringBuilder join = new StringBuilder();
        for (CliColor format : formats) {
            if (join.length() != 0) {
                join.append(';');
            }
            join.append(format.toString());
        }

        return "\u001b[" + join + "m" + str + "\u001b[m";
    }

    public String toString() {
        return config;
    }

    public String color(String str) {
        return color(str, this);
    }
}