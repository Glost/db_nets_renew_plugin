package de.renew.navigator.vc;

import de.renew.logging.CliColor;

import java.util.Date;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public class Commit {
    private String revision;
    private String message;
    private String author;
    private Date date;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(int date) {
        this.date = new Date(date * 1000L);
    }

    /**
     * Formats a commit message according to Renew / Paose rules.
     */
    public static String formatMessage(String message) {
        message = message.replaceAll("\\n\\* ", "\n• ");
        message = message.replaceAll("\\*([^\\*\\n]+)\\*",
                                     CliColor.color("$1", CliColor.BOLD));
        message = message.replaceAll("\\+([^\\+\\n]+)\\+",
                                     CliColor.color("$1", CliColor.UNDERLINE));
        message = message.replaceAll("_([^_\\n]+)_",
                                     CliColor.color("$1", CliColor.ITALIC));
        message = message.replaceAll("-([^\\-\\n]+)-",
                                     CliColor.color("$1", CliColor.CROSSED_OUT));
        message = message.replaceAll("@([^@\\n]+)@",
                                     CliColor.color("$1", CliColor.CYAN));
        return message;
    }
}