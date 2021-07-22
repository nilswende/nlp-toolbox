package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.ResultPrinter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A use case of the NLPToolbox.
 *
 * @author Nils Wende
 */
public abstract class UseCase {

	/**
	 * The AppConfig.
	 */
	protected AppConfig appConfig;

	/**
	 * Executes the use case by handing over a DBReader and a DBWriter.
	 *
	 * @param appConfig AppConfig
	 * @param dbReader  DBReader
	 * @param dbWriter  DBWriter
	 */
	public void execute(final AppConfig appConfig, final DBReader dbReader, final DBWriter dbWriter) {
		this.appConfig = appConfig;
		final var start = LocalDateTime.now();
		execute(dbReader, dbWriter);
		execute(dbReader);
		execute(dbWriter);
		final var result = getResult();
		result.setStart(start);
		result.setEnd(LocalDateTime.now());
	}

	/**
	 * Hook method to be used by use cases that need both a DBReader and a DBWriter.
	 *
	 * @param dbReader DBReader
	 * @param dbWriter DBWriter
	 */
	protected void execute(final DBReader dbReader, final DBWriter dbWriter) {

	}

	/**
	 * Hook method to be used by use cases that only need a DBReader.
	 *
	 * @param dbReader DBReader
	 */
	protected void execute(final DBReader dbReader) {

	}

	/**
	 * Hook method to be used by use cases that only need a DBWriter.
	 *
	 * @param dbWriter DBWriter
	 */
	protected void execute(final DBWriter dbWriter) {

	}

	/**
	 * Returns this use case's result.
	 *
	 * @return this use case's result
	 */
	// must be overridden in every subclass to return the concrete result type
	public abstract Result getResult();

	/**
	 * Returns true, if this use case contains a result.
	 *
	 * @return true, if this use case contains a result
	 */
	public boolean hasResult() {
		return getResult() != null;
	}

	/**
	 * The basic use case result.<br>
	 * Must be implemented by every use case implementation to be able to return a more specific result (including the use case's name).
	 *
	 * @see ResultPrinter
	 */
	public static abstract class Result {
		private LocalDateTime start = LocalDateTime.MIN;
		private LocalDateTime end = LocalDateTime.MAX;

		/**
		 * Returns a string representation of this result's content.
		 *
		 * @return a string representation of this result's content
		 */
		@Override
		public String toString() {
			return new ResultPrinter().print(this);
		}

		/**
		 * Hook method to print the specific result's contents to the printer.
		 *
		 * @param printer ResultPrinter
		 */
		public void toString(final ResultPrinter printer) {

		}

		/**
		 * Returns the name of this result's use case.
		 *
		 * @return the name of this result's use case
		 */
		public String getUseCaseName() {
			return this.getClass().getDeclaringClass().getSimpleName();
		}

		void setStart(final LocalDateTime start) {
			this.start = start;
		}

		void setEnd(final LocalDateTime end) {
			this.end = end;
		}

		/**
		 * Returns the start time.
		 *
		 * @return the start time
		 */
		public LocalDateTime getStart() {
			return start;
		}

		/**
		 * Returns the end time.
		 *
		 * @return the end time
		 */
		public LocalDateTime getEnd() {
			return end;
		}

		/**
		 * Returns the time spent executing this result's use case.
		 *
		 * @return the time spent
		 */
		public Duration getDuration() {
			return Duration.between(getStart(), getEnd());
		}
	}

}
