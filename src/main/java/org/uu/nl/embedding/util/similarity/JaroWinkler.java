package org.uu.nl.embedding.util.similarity;

public class JaroWinkler extends NameSimilarity {

    public JaroWinkler() {
        super(new info.debatty.java.stringsimilarity.JaroWinkler());
    }

}
