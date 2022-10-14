package org.uu.nl.embedding.util.similarity;

public class NormalizedLevenshtein extends NameSimilarity {

    public NormalizedLevenshtein(){
        super(new info.debatty.java.stringsimilarity.NormalizedLevenshtein());
    }
}
