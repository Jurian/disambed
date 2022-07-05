package org.uu.nl.embedding.util.similarity;

public class NormalizedLevenshteinFirst2Full extends FirstName2FullNameSimilarity {

    public NormalizedLevenshteinFirst2Full(){
        super(new info.debatty.java.stringsimilarity.NormalizedLevenshtein());
    }
}
