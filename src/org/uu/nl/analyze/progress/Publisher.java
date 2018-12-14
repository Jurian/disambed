package org.uu.nl.analyze.progress;

import java.util.concurrent.SubmissionPublisher;

public class Publisher extends SubmissionPublisher<Progress> {

	public void updateProgress(Progress progress) {
		this.submit(progress);
	}

}
