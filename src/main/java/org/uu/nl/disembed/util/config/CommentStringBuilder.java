package org.uu.nl.disembed.util.config;

public class CommentStringBuilder {

    public static final String COMMENT = "#\t";
    public static final String NEWLINE = "\n";

    private final StringBuilder builder;

    public CommentStringBuilder(){
        this.builder = new StringBuilder();
    }

    public CommentStringBuilder(StringBuilder builder){
        this.builder = builder;
    }

    public CommentStringBuilder append(CommentStringBuilder other) {
        this.builder.append(other);
        return this;
    }

    public CommentStringBuilder appendLineNoComment() {
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendLineNoComment(String s) {
        builder.append(s);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendNoComment(String s) {
        builder.append(s);
        return this;
    }

    public CommentStringBuilder append(String s) {
        builder.append(COMMENT);
        builder.append(s);
        return this;
    }

    public CommentStringBuilder append() {
        return this.append("");
    }

    public CommentStringBuilder appendLine() {
        return this.appendLine("");
    }

    public CommentStringBuilder appendLine(CommentStringBuilder other) {
        this.builder.append(other);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendLine(String s) {
        builder.append(COMMENT);
        builder.append(s);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendKeyValueLine(String key, String value) {
        builder.append(COMMENT);
        builder.append(key);
        builder.append(": ");
        builder.append(value);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendKeyValueLine(String key, int value) {
        builder.append(COMMENT);
        builder.append(key);
        builder.append(": ");
        builder.append(value);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendKeyValueLine(String key, float value) {
        builder.append(COMMENT);
        builder.append(key);
        builder.append(": ");
        builder.append(value);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendKeyValueLine(String key, boolean value) {
        builder.append(COMMENT);
        builder.append(key);
        builder.append(": ");
        builder.append(value);
        builder.append(NEWLINE);
        return this;
    }

    public CommentStringBuilder appendKeyValueLine(String key, double value) {
        builder.append(COMMENT);
        builder.append(key);
        builder.append(": ");
        builder.append(value);
        builder.append(NEWLINE);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
