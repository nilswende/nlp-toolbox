package de.fernuni_hagen.kn.nlp;

import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * Logs information to the console.
 *
 * @author Nils Wende
 */
public final class Logger {

	private Logger() {
		throw new AssertionError("no init");
	}

	/**
	 * Logs the start of a named action.
	 *
	 * @param name the name of the action
	 * @return the start of the action as per {@link System#nanoTime()}
	 */
	public static long logStart(final String name) {
		System.out.println("start " + name);
		return System.nanoTime();
	}

	/**
	 * Logs the amount of time a named action has taken.
	 *
	 * @param name  the name of the action
	 * @param start the start of the action as per {@link System#nanoTime()}
	 */
	public static void logDuration(final String name, final long start) {
		final var d = Duration.ofNanos(System.nanoTime() - start);
		System.out.println(String.format("%s duration: %d s %d ms", name, d.toSecondsPart(), d.toMillisPart()));
	}

	/**
	 * Logs the amount of CPU time the current thread has consumed.
	 */
	public static void logCurrentThreadCpuTime() {
		final var threadMXBean = ManagementFactory.getThreadMXBean();
		if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
			final var d = Duration.ofNanos(threadMXBean.getCurrentThreadCpuTime());
			System.out.println(String.format("%s thread CPU time: %d m %d s %d ms",
					Thread.currentThread().getName(), d.toMinutesPart(), d.toSecondsPart(), d.toMillisPart()));
		} else {
			System.out.println("ThreadMXBean.getCurrentThreadCpuTime not supported on this JVM");
		}
	}

}
