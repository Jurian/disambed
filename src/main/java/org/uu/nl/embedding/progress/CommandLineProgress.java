package org.uu.nl.embedding.progress;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class CommandLineProgress implements Publisher, AutoCloseable {

    private final ProgressBar pb;

    public CommandLineProgress(String name) {
        pb = new ProgressBar(name, 100, ProgressBarStyle.ASCII);
    }

    @Override
    public void setExtraMessage(String msg) {
        pb.setExtraMessage(msg);
    }

    @Override
    public void setNewMax(long max) {
        this.pb.maxHint(max);
    }

    @Override
    public void updateProgress(ProgressState progressState) {
        pb.stepTo(progressState.getN());
    }

    @Override
    public void close() {
        pb.close();
    }
}
