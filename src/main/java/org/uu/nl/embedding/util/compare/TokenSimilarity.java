package org.uu.nl.embedding.util.compare;

import java.util.Iterator;

public class TokenSimilarity extends JaccardSimilarity {

    private static final String[] ILLEGAL_TOKENS = new String[] {
            "the", "of", "and", "a", "an", "to",
            "in", "is", "you", "that", "it","for",
            "on", "from", "are", "as", "with",
            "at", "or", "by", "but", "if"
    };

    @Override
    protected void preProcessNormalized(String item) {

        if(item.isEmpty()) return;

        if(!tokenIndex.containsKey(item)) {

            final Iterator<String> it = new Tokenator(item);
            final int[] temp = new int[(item.length() + 1) / 3];
            int i = 0;
            while(it.hasNext()) {
                final String token = it.next();
                if(!tokenMap.containsKey(token))
                    tokenMap.put(token, tokenMap.size());

                temp[i] = tokenMap.get(token);
                i++;
            }
            final int[] indexes = new int[i];
            System.arraycopy(temp, 0, indexes, 0, indexes.length );
            tokenIndex.put(item, indexes);
        }
    }

    private static class Tokenator implements Iterator<String> {

        private static final char TOKEN_SEPARATOR = ' ';
        private final String item;
        private int tokenCharIndex = 0;
        private String currentToken;

        public Tokenator(String item) {
            this.item = item;
        }

        private boolean isIllegalToken(String token) {

            for (String illegalToken : ILLEGAL_TOKENS) {
                if (token.length() <= 1) return true;
                if (token.equals(illegalToken)) return true;
            }

            return false;
        }


        @Override
        public boolean hasNext() {

            for(int charPosition = tokenCharIndex; charPosition < item.length(); charPosition++) {
                if(item.charAt(charPosition) == TOKEN_SEPARATOR || charPosition == item.length() - 1) {

                    final String token = item
                            .substring(tokenCharIndex, charPosition + 1)
                            .trim()
                            .replace("\\.$", "");

                    if(isIllegalToken(token)) {
                        tokenCharIndex = charPosition + 1;
                    } else {
                        tokenCharIndex = charPosition + 1;
                        currentToken = token;
                        return true;
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
