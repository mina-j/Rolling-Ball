
package com.example.rollingball.timer;

import java.util.Arrays;
import javafx.animation.AnimationTimer;

public class Timer extends AnimationTimer
{
	private long previous;
	private Updatable[] updatables;

	public Timer(final Updatable... updatables) {
		this.updatables = new Updatable[updatables.length];
		for (int i = 0; i < updatables.length; ++i) {
			this.updatables[i] = updatables[i];
		}
	}

	@Override
	public void handle(final long now) {
		if (this.previous == 0L) {
			this.previous = now;
		}
		final double deltaSeconds = (now - this.previous) / 1.0E9;
		this.previous = now;
		Arrays.stream(this.updatables).forEach(updatable -> updatable.update(deltaSeconds));
	}
}
