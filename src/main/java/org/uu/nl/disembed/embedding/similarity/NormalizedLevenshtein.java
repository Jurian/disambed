package org.uu.nl.disembed.embedding.similarity;

public class NormalizedLevenshtein extends NameSimilarity {

    public NormalizedLevenshtein(){
        super(new info.debatty.java.stringsimilarity.NormalizedLevenshtein());
    }

}
