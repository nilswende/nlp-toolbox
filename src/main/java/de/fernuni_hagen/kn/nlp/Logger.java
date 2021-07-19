package de.fernuni_hagen.kn.nlp;

import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * Prints information to the console.
 *
 * @author Nils Wende
 */
public final class Logger {

	private Logger() {
		// no init
	}

	/**
	 * Prints the amount of CPU time the current thread has consumed.
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
