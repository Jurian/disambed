package org.uu.nl.embedding;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.PrintStream;

/**
 * Mainly used for consistency of the look and feel among the application parts
 *
 * @author Jurian Baas
 */
public class Settings {

    private static Settings uniqueInstance;

    private final PrintStream pPrinter = System.out;
    private final int pUpdate = 250, pUnitSize = 1;
    private final boolean pSpeed = true;
    private final ProgressBarStyle pStyle = ProgressBarStyle.COLORFUL_UNICODE_BLOCK;

    private Settings() {}

    public ProgressBar progressBar(String name, long max, String unitName) {
        return new ProgressBar(name, max, pUpdate, pPrinter, pStyle, " " + unitName, pUnitSize, pSpeed);
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
