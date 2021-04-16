package de.fernuni_hagen.kn.nlp.utils;

import de.uni_leipzig.asv.toolbox.viterbitagger.Tagger;
import de.uni_leipzig.asv.toolbox.viterbitagger.Transitions_extern;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Utilities for the ASV library.
 *
 * @author Nils Wende
 */
public class ASV {

	private ASV() {
		throw new AssertionError("no init");
	}

	/**
	 * Set the extern field on a tagger to false, since the tagger's setter ignores the parameter.<br>
	 * This causes a huge performance boost.
	 *
	 * @param tagger Tagger
	 */
	public static void setInternal(final Tagger tagger) {
		final var clazz = tagger.getClass();
		try {
			// close the file resource
			final var transitions = clazz.getDeclaredField("transitions");
			transitions.setAccessible(true);
			final var transitions_extern = (Transitions_extern) transitions.get(tagger);
			final var dataFile = transitions_extern.getClass().getDeclaredField("dataFile");
			dataFile.setAccessible(true);
			final var randomAccessFile = (RandomAccessFile) dataFile.get(transitions_extern);
			randomAccessFile.close();
			// set extern to false
			final var extern = clazz.getDeclaredField("extern");
			extern.setAccessible(true);
			extern.set(tagger, false);
			tagger.setExtern(false);
		} catch (final NoSuchFieldException | IllegalAccessException | IOException e) {
			throw new UncheckedException(e);
		}
	}

}
