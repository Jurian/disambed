package org.uu.nl.embedding;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * Mainly used for consistency of the look and feel among the application parts
 *
 * @author Jurian Baas
 */
public class Settings {

    private static Settings uniqueInstance;

    private Settings() {}

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
