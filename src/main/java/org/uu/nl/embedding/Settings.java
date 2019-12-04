package org.uu.nl.embedding;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.uu.nl.embedding.glove.util.ThreadLocalSeededRandom;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;

/**
 * @author Jurian Baas
 */
public class Settings {

    private static Settings uniqueInstance;

    private Settings() {}

    public int threads() {
        int threads = Runtime.getRuntime().availableProcessors() -1;
        return threads == 0 ? 1 : threads;
    }

    private ThreadLocalSeededRandom threadLocalRandom;

    public void setThreadLocalRandom() {
        this.threadLocalRandom = new ThreadLocalSeededRandom(System.currentTimeMillis());
    }

    public void setThreadLocalRandom(long seed) {
        this.threadLocalRandom = new ThreadLocalSeededRandom(seed);
    }

    public ExtendedRandom getThreadLocalRandom() {
        return this.threadLocalRandom.get();
    }

    public ProgressBar progressBar(String name, long max, String unitName) {
        return new ProgressBar (
                name,
                max,
                250,
                System.out,
                ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
                " " + unitName,
                1,
                true
        );
    }

    public static synchronized Settings getInstance() {
        if (Settings.uniqueInstance == null) {
            Settings.uniqueInstance = new Settings();
        }
        return Settings.uniqueInstance;
    }

    @Override
    protected Settings clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
