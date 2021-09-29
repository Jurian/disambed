package org.uu.nl.embedding.util.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Output {

    private String writer;

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getWriter() {
        return this.writer;
    }

    public Configuration.EmbeddingWriter getWriterEnum() {
        return Configuration.EmbeddingWriter.valueOf(writer.toUpperCase());
    }

    private String name;

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    private List<String> type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private final Set<Integer> nodeIndex = new HashSet<>();

    public Set<Integer> getNodeIndex() {
        return nodeIndex;
    }

    public void addNodeIndex(int index) {
        this.nodeIndex.add(index);
    }
}
