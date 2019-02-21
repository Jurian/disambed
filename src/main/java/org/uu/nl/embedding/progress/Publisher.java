package org.uu.nl.embedding.progress;

public interface Publisher  {

	void setExtraMessage(String msg);
	void setNewMax(long max);
	void updateProgress(Progress progress);

}
