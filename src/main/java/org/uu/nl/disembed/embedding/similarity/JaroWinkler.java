package org.uu.nl.disembed.embedding.similarity;

public class JaroWinkler extends NameSimilarity {

    public JaroWinkler() {
        super(new info.debatty.java.stringsimilarity.JaroWinkler());
    }

}
