package org.uu.nl.disembed.util.progress;

import me.tongfei.progressbar.*;
import org.apache.log4j.Logger;

public class Progress {

    public static ProgressBar progressBar(String name, long max, String unitName) {
        ProgressBarBuilder builder = new ProgressBarBuilder();

        builder.setTaskName(name);
        builder.setInitialMax(max);
        builder.setUpdateIntervalMillis(250);
        builder.setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK);
        builder.setConsumer(new ConsoleProgressBarConsumer(System.out));
        builder.setUnit(" "+unitName, 1);
        builder.setMaxRenderedLength(120);

        return builder.build();
    }

    public static ProgressBar progressBar(Logger logger, String name, long max, String unitName) {

        ProgressBarBuilder builder = new ProgressBarBuilder();

        builder.setTaskName(name);
        builder.setInitialMax(max);
        builder.setUpdateIntervalMillis(250);
        builder.setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK);
        builder.setConsumer(new DelegatingProgressBarConsumer(logger::info));
        builder.setUnit(" "+unitName, 1);
        builder.setMaxRenderedLength(1000);

        return builder.build();
    }
}
