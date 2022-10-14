package org.uu.nl.embedding.util.similarity;

import net.jcip.annotations.Immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Immutable
public abstract class TokenBased {

    private static final String[] ILLEGAL_TOKENS = new String[] {
            "the", "of", "and", "a", "an", "to",
            "in", "is", "you", "that", "it","for",
            "on", "from", "are", "as", "with",
            "at", "or", "by", "but", "if"
    };

    private static final char[] ILLEGAL_CHARS = new char[] {
            '|',';','\'','"','<','>','?',':', '.', ',', '(', ')', '[', ']', '{', '}','-','_'
    };

    public final Map<String, Integer> getProfile(String string) {
        HashMap<String, Integer> shingles = new HashMap<>();
        final Iterator<String> it = new Tokenator(string);

        while(it.hasNext()) {
            String token = it.next();
            shingles.merge(token, 1, Integer::sum);
        }

        return Collections.unmodifiableMap(shingles);
    }

    private static class Tokenator implements Iterator<String> {

        private static final char TOKEN_SEPARATOR = ' ';
        private final String item;
        private int tokenCharIndex = 0;
        private String currentToken;

        public Tokenator(String item) {
            this.item = item;
        }

        private boolean isLegalToken(String token) {
            if (token.length() <= 1) return false;
            for (String illegalToken : ILLEGAL_TOKENS)
                if (token.equals(illegalToken)) return false;
            return true;
        }


        @Override
        public boolean hasNext() {

            for(int charPosition = tokenCharIndex; charPosition < item.length(); charPosition++) {
                if(item.charAt(charPosition) == TOKEN_SEPARATOR || charPosition == item.length() - 1) {

                    String token = item.substring(tokenCharIndex, charPosition + 1);

                    for(char illegal_char : ILLEGAL_CHARS)
                        token = token.replace(illegal_char, ' ');

                    token = token.trim();

                    if(isLegalToken(token)) {
                        tokenCharIndex = charPosition + 1;
                        currentToken = token;
                        return true;
                    } else {
                        tokenCharIndex = charPosition + 1;
                    }
                }
            }

            return false;
        }

        @Override
        public String next() {
            return currentToken;
        }
    }
}
