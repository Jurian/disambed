package org.uu.nl.embedding.progress;

public class DoNothingPublisher implements Publisher {

    @Override
    public void setExtraMessage(String msg) { }

    @Override
    public void setNewMax(long max) { }

    @Override
    public void updateProgress(ProgressState progressState) { }
}
