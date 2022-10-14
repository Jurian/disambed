package org.uu.nl.disembed.embedding.similarity;

import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

public class MyersLevenshtein  implements NormalizedStringDistance, NormalizedStringSimilarity  {
    
    /**
     * Myers' bit-parallel algorithm
     *
     * G. Myers. "A fast bit-vector algorithm for approximate string
     * matching based on dynamic programming." Journal of the ACM, 1999.
     */
    private int myers(String s1, String s2) {

        final int len1 = s1.length();
        final int len2 = s2.length();

        final long[] charIndex = new long[256];

        long eq, xv, xh, ph, mh, pv, mv, last;
        int i;
        int score = len2;

        for (i = 0; i < len2; i++) {
            charIndex[s2.charAt(i)] |= 1L << i;
        }

        mv = 0;
        pv = 0xFFFFffffFFFFffffL;
        last = 1L << (len2 - 1);

        for (i = 0; i < len1; i++) {
            eq = charIndex[s1.charAt(i)];

            xv = eq | mv;
            xh = (((eq & pv) + pv) ^ pv) | eq;

            ph = mv | ~(xh | pv);
            mh = pv & xh;

            if ((ph & last) != 0) score++;
            if ((mh & last) != 0) score--;

            ph = (ph << 1) | 1;
            mh = (mh << 1);

            pv = mh | ~(xv | ph);
            mv = ph & xv;
        }

        return score;
    }

    @Override
    public double distance(String a, String b) {

        double m_len = Math.max(a.length(), b.length());
        return myers(a, b) / m_len;

    }

    @Override
    public double similarity(String a, String b) {
        return 1.0 - distance(a, b);
    }
}
