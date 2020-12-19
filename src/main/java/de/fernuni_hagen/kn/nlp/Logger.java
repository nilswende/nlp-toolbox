package de.fernuni_hagen.kn.nlp;

import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * @author Nils Wende
 */
public final class Logger {

	private Logger() {
		throw new AssertionError(); // no init
	}

	public static long logStart(final String name) {
		System.out.println("start " + name);
		return System.nanoTime();
	}

	public static void logDuration(final String name, final long start) {
		final var d = Duration.ofNanos(System.nanoTime() - start);
		System.out.println(String.format("%s duration: %d s %d ms", name, d.toSecondsPart(), d.toMillisPart()));
	}

	public static void logCurrentThreadCpuTime() {
		final var d = Duration.ofNanos(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
		System.out.println(String.format("main thread CPU time: %d m %d s %d ms", d.toMinutesPart(), d.toSecondsPart(), d.toMillisPart()));
	}

}
