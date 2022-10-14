package org.uu.nl.disembed.util.config;

public interface Configurable {
    void check() throws InvalidConfigException;
    CommentStringBuilder getBuilder();
}
