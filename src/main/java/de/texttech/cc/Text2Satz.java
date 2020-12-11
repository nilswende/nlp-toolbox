package de.texttech.cc;


import de.texttech.cc.converter.ConvertPdf;
import de.texttech.cc.converter.Html2Text;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Input-Files bereits durch TextProcessing in txt umgewandelt, dependency auf de.texttech.cc.converter unnötig.
 * Parameter per Konstruktor/Setter übergebbar, keine main-Methode nötig als Library.
 * CharacterIteratorStream (buffered) neu implementieren, da er das Interface verletzt (U+ffff statt -1 am Ende) und so die dependency entfernt werden kann.
 * -> keine externen dependencies und de.texttech.cc komplett entfernbar
 * Ziel: Textdatei in 1 Satz je Zeile umwandeln. Sollte keine 100 Zeilen benötigen.
 * Sind die Schnörkel in #processText nötig?
 * Wird ASCII (Byte=Char) oder Unicode ('\u00ab check') genutzt?
 *
 */
public class Text2Satz {

	private HashSet abbreviations;
	private Locale language;
	private String database;
	private String htmlConverter;
	private String dbConnectionName;
	private String workingDir;
	static int success = 0;
	private boolean keep_newline;
	private boolean wrapLongLines;
	private boolean rawMode;
	//   private BonusTags discoveryConverter;
	static final int MAX_TAG_LEN = 25;
	public boolean verbose;
	protected String htmlConverterCmdArray[];
	protected int htmlConverterArgPos;

	public static void usage()
			throws IllegalArgumentException {
		System.err.println("Segments text to sentences and saves them into one file.");
		System.err.println("Usage: text2satz -L {de|en|fr} -d datenbank [-h htmlConverter] [-a abkuerzungsda" +
				"tei] [-n] [textfiles|-f filefile]"
		);
		System.err.println("          -n: keep line breaks as sentence borders");
		System.err.println("          -h: htmlConverter; either 'internal' or");
		System.err.println("              a quoted call like 'lynx -dump -nolist -nopause -width=255'");
		System.err.println("          -f: read names of infiles from given file");
		System.err.println("              If neither -f nor a list of files are given,");
		System.err.println("              text2satz reads file standard input for file names.");
		System.err.println("          -p: working dir where to create temp and out files");
		System.err.println("          -w: wrap long sentences after next space after 220th column");
		System.err.println("          -r: raw mode: don't do any separation, but delete heading numbers+tabs");
		System.err.println("          -V: print version information");
		System.err.println("          -v: generate more verbose output");
		throw new IllegalArgumentException("Text2Satz failed");
	}

	public Text2Satz() {
		abbreviations = null;
		language = Locale.GERMANY;
		database = "db";
		htmlConverter = null;
		dbConnectionName = null;
		workingDir = null;
		keep_newline = false;
		wrapLongLines = false;
		rawMode = false;
		// discoveryConverter = null;
		verbose = false;
		htmlConverterCmdArray = null;
		htmlConverterArgPos = 0;
		success = 0;
	}

	public int getNrOfTextsProcessed() {
		return success;
	}

	public void setLanguage(Locale locale) {
		language = locale;
	}

	public void setDatabase(String s) {
		database = s;
	}

	public void setHtmlConverter(String s) {
		htmlConverter = s;
		StringTokenizer stringtokenizer = new StringTokenizer(htmlConverter);
		int i = stringtokenizer.countTokens();
		htmlConverterCmdArray = new String[i + 1];
		int j;
		for (j = 0; j < i && stringtokenizer.hasMoreTokens(); j++) {
			htmlConverterCmdArray[j] = stringtokenizer.nextToken();
		}

		htmlConverterArgPos = j;
	}

	public void setWorkingDir(String s) {
		workingDir = s;
	}

	public void setKeepNewline(boolean flag) {
		keep_newline = flag;
	}

	public void setWrapLongLines(boolean flag) {
		wrapLongLines = flag;
	}

	public void setDbConnectionName(String s) {
		dbConnectionName = s;

	}

	public void setRawMode(boolean flag) {
		rawMode = flag;
	}

	public void readAbbrevs(String filepath) {
		// abbreviations = Files.lines().filter(s -> !s.isEmpty()).collect(Collectors.toSet());
		// oder Scanner, wenn Leerzeichen beachtet werden sollen
		abbreviations = new HashSet();
		try {
			StringBuffer stringbuffer = new StringBuffer("");
			FileInputStream fileinputstream = new FileInputStream(filepath);
			char c;
			while ((c = (char) fileinputstream.read()) != '\uFFFF') {
				if (c == '\n' || c == ' ') {
					abbreviations.add(stringbuffer.toString());
					stringbuffer.delete(0, stringbuffer.length());
				} else {
					stringbuffer.append(c);
				}
			}
			abbreviations.remove("");
		} catch (Exception exception) {
			System.err.println("Problems while reading abbreviations:" + exception.getMessage());
		}
	}

	private int writeRestOfLine(DataOutputStream dataoutputstream, BufferedInputStream bufferedinputstream)
			throws IOException {
		int j = 0;
		int i;
		for (i = bufferedinputstream.read(); i != '\n' && i != '\r' && i != -1; i = bufferedinputstream.read()) {
			j++;
			dataoutputstream.write((char) i);
		}

		if (i != -1) {
			dataoutputstream.write((char) i);
		}
		return j;
	}

	private String taggedHeader(String s) {
		//    return new String(" <quelle><name>" + database + Integer.toString(success + 1) + "</name><name_lang>" + s + "</name_lang></quelle>\n");
		return new String("");


	}

	private DataOutputStream getOutStream(String s)
			throws IOException {
		if (success > 0) {
			return new DataOutputStream(new FileOutputStream(s, true));
		} else {
			return new DataOutputStream(new FileOutputStream(s));
		}
	}

	private int printInRawMode(String s, File file, String s1) {
		BufferedInputStream bufferedinputstream = null;
		DataOutputStream dataoutputstream = null;
		try {
			bufferedinputstream = new BufferedInputStream(new FileInputStream(file));
			dataoutputstream = getOutStream(s1);
		} catch (IOException ioexception) {
			System.err.println("Problems while opening file:" + ioexception.getMessage());
			return 0;
		}
		boolean directWrite = false;
		int j = 0;
		StringBuffer stringbuffer = new StringBuffer();
		try {
			dataoutputstream.writeBytes(/*"\n" +*/ taggedHeader(s));
			do {
				int i;
				if ((i = bufferedinputstream.read()) <= 0) {
					break;
				}
				if (directWrite) {
					dataoutputstream.write(i);
					if (i == '\n') {
						directWrite = false;
						j = 0;
						stringbuffer = new StringBuffer();
					}
				} else if (i == 9) {
					directWrite = true;
				} else if (i == '\n') {
					dataoutputstream.writeBytes(stringbuffer.toString());
					dataoutputstream.write(i);
					directWrite = false;
					j = 0;
					stringbuffer = new StringBuffer();
				} else if (++j < 0x10000) {
					stringbuffer.append((char) i);
				} else if (j == 0x10000) {
					stringbuffer = new StringBuffer();
				}
			} while (true);
		} catch (Exception exception) {
			System.err.println("Problems while io: " + exception.getMessage());
			exception.printStackTrace();
		} finally {
			try {
				bufferedinputstream.close();
				dataoutputstream.close();
			} catch (IOException ioexception1) {
				System.err.println("Problems while closing the file: " + ioexception1.getMessage());
			}
		}
		success++;
		return 1;
	}

	private boolean sentenceEndChar(char c) {
		if (c == '.' || c == '!' || c == '?') {
			return true;
		}
		return c == '"' || c == '\''
				|| c == '\u00ab' // LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
				|| c == '\u00bb'; // RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
	}

	public int processText(String sourceFilepath) {
		if (verbose) {
			System.err.println("[Text2Satz] Processing file: " + sourceFilepath);
		}
		int j = 0;
		StringBuffer stringbuffer = null;
		String sourceFiletype = "";
		String tmpFilepath = "text2satz.tmp";
		String workingFilepath = null;
		if (workingDir != null) {
			tmpFilepath = workingDir + File.separator + tmpFilepath;
		}
		sourceFiletype = sourceFilepath.substring(sourceFilepath.lastIndexOf('.') + 1);
		if (sourceFiletype.length() > 5) {
			System.err.println("Warning: Ignoring file " + sourceFilepath);
			return 0;
		}
		if (sourceFiletype.compareToIgnoreCase("txt") == 0) {
			workingFilepath = new String(sourceFilepath);
		} else if (sourceFiletype.toLowerCase().indexOf("htm") != -1) {
			if (htmlConverter != null && !htmlConverter.equals("internal")) {
				workingFilepath = convertHtmlExternally(sourceFilepath, tmpFilepath);
			} else {
				workingFilepath = convertHtmlInternally(sourceFilepath, tmpFilepath);
			}
			if (workingFilepath == null) {
				return 0;
			}
		} else if (sourceFiletype.compareToIgnoreCase("lit") == 0 || sourceFiletype.compareToIgnoreCase("cyr") == 0 || sourceFiletype.compareToIgnoreCase("cas") == 0) {
			if ((workingFilepath = convertLatin2(sourceFilepath, tmpFilepath)) == null) {
				return 0;
			}
		} else if (sourceFiletype.compareToIgnoreCase("doc") == 0) {
			if ((workingFilepath = convertDoc(sourceFilepath, tmpFilepath)) == null) {
				return 0;
			}
		} else if (sourceFiletype.compareToIgnoreCase("pdf") == 0) {
			if ((workingFilepath = convertPdf(sourceFilepath, tmpFilepath)) == null) {
				return 0;
			}
		} else if (sourceFiletype.compareToIgnoreCase("ceml") == 0) {
			if ((workingFilepath = convertCeml(sourceFilepath, tmpFilepath)) == null) {
				return 0;
			}
		} else if (sourceFiletype.toLowerCase().indexOf("xml") != -1) {
			if ((workingFilepath = convertHtmlInternally(sourceFilepath, tmpFilepath)) == null) {
				return 0;
			}
		} else {
			System.err.println("Warning: Can't handle file type of file " + sourceFilepath);
			return 0;
		}
		File workingFile = new File(workingFilepath);
		if (!workingFile.canRead()) {
			System.err.println("Can't open file: " + sourceFilepath);
			return 0;
		}
		String satzFilepath = null;
		if (workingDir != null) {
			satzFilepath = workingDir + File.separator + database + ".s";
		} else {
			satzFilepath = database + ".s";
		}
		if (rawMode) {
			return printInRawMode(sourceFilepath, workingFile, satzFilepath);
		}

		BufferedInputStream bufferedinputstream = null;
		BufferedInputStream bufferedinputstream1 = null;
		BufferedInputStream bufferedinputstream2 = null;
		DataOutputStream dataoutputstream = null;
		BreakIterator sentenceBoundary = BreakIterator.getSentenceInstance(language);
		BreakIterator wordBoundary = BreakIterator.getWordInstance(language);
		CharacterIteratorStream characteriteratorstream = new CharacterIteratorStream(workingFilepath);
		CharacterIteratorStream characteriteratorstream1 = new CharacterIteratorStream(workingFilepath);
		sentenceBoundary.setText(characteriteratorstream);
		wordBoundary.setText(characteriteratorstream1);
		int start = sentenceBoundary.first();

		try {
			bufferedinputstream = new BufferedInputStream(new FileInputStream(workingFile));
			bufferedinputstream1 = new BufferedInputStream(new FileInputStream(workingFile));
			bufferedinputstream2 = new BufferedInputStream(new FileInputStream(workingFile));
			dataoutputstream = getOutStream(satzFilepath);

			int bytesReadFromIn = 0;
			int bytesReadFromIn1 = 0;
			int bytesReadFromIn2 = 0;
			stringbuffer = new StringBuffer();
			boolean seenWhitespace = false;
			boolean seenNoCR = true;
			boolean skipLinebreaks = true;

			dataoutputstream.writeBytes(/*"\n" +*/ taggedHeader(sourceFilepath));
			skipToPosition(bufferedinputstream, start); // incorrectly assumes that bytes adequately represent characters
			bytesReadFromIn += start;
			for (int end = sentenceBoundary.next(); end != BreakIterator.DONE; start = end, end = sentenceBoundary.next()) {
				int j2 = 0;
				for (int length = end - start; length > 0; length--) {
					int charFromSentence = (char) bufferedinputstream.read();
					bytesReadFromIn++;
					if (charFromSentence == '\n' || charFromSentence == '\r' || bytesReadFromIn2 == 0) {
						if (bytesReadFromIn2 <= bytesReadFromIn) {
							bytesReadFromIn2 = skipToInPosition(bufferedinputstream2, bytesReadFromIn, bytesReadFromIn2);
							stringbuffer.delete(0, stringbuffer.length());
							j = (char) bufferedinputstream2.read();
						}
						while (j == '\n' || j == '\r') { // skip linebreaks
							j = (char) bufferedinputstream2.read();
							bytesReadFromIn2++;
						}
						if (j == ' ') {
							stringbuffer.append((char) j);
							j = (char) bufferedinputstream2.read();
							bytesReadFromIn2++;
							stringbuffer.append((char) j);
						}
						if (stringbuffer.toString().compareTo(" <") == 0) {
							stringbuffer.delete(0, stringbuffer.length());
							int j3 = 0;
							do { // read up to 24 chars from in2 until '>', '\n' or EOF
								if ((j = bufferedinputstream2.read()) == '>' || j <= 0) {
									break;
								}
								bytesReadFromIn2++;
								stringbuffer.append((char) j);
							} while (++j3 <= 25 && j != '\n');
							bytesReadFromIn2++;
							if (stringbuffer.toString().compareTo("quelle") == 0) {
								dataoutputstream.writeBytes("\n <" + stringbuffer + ">");
								stringbuffer.delete(0, stringbuffer.length());
								while ((charFromSentence = bufferedinputstream2.read()) != '\n' && charFromSentence > 0) {
									bytesReadFromIn2++;
									stringbuffer.append((char) charFromSentence);
								}
								bytesReadFromIn2++;
								stringbuffer.delete(0, stringbuffer.toString().indexOf("<name_lang>") + 11);
								stringbuffer.delete(stringbuffer.toString().indexOf("</name_lang>"), stringbuffer.length());
								if (stringbuffer.length() == 0) {
									stringbuffer.append(sourceFilepath);
								}
								success++;
								dataoutputstream.writeBytes("<name>" + database + (success + 1) + "</name><name_lang>" + stringbuffer + "</name_lang></quelle>\n");
							} else if (j == '>') {
								dataoutputstream.writeBytes("\n <" + stringbuffer + ">");
								bytesReadFromIn2 += writeRestOfLine(dataoutputstream, bufferedinputstream2) + 1;
							} else {
								dataoutputstream.writeBytes("\n <" + stringbuffer);
								bytesReadFromIn2 += writeRestOfLine(dataoutputstream, bufferedinputstream2);
							}
							skipToPosition(bufferedinputstream, bytesReadFromIn2 - bytesReadFromIn - 1);
							length -= bytesReadFromIn2 - bytesReadFromIn - 1;
							for (bytesReadFromIn = bytesReadFromIn2 - 1; bytesReadFromIn > sentenceBoundary.current(); ) {
								start = end;
								end = sentenceBoundary.next();
								length += end - start;
							}

						} else if (bytesReadFromIn == start + 1) {
							dataoutputstream.write(charFromSentence);
						}
						if (skipLinebreaks) {
							continue;
						}
						if (keep_newline || seenNoCR && !skipLinebreaks) {
							dataoutputstream.write('\n');
							skipLinebreaks = true;
							seenWhitespace = false;
						} else {
							seenWhitespace = true;
						}
						if (charFromSentence != '\r') {
							seenNoCR = true;
						}
						continue;
					}
					if (Character.isWhitespace((char) charFromSentence)) {
						if (skipLinebreaks) {
							continue;
						}
						seenWhitespace = true;
						if (wrapLongLines && end - start - length - j2 > 220) {
							j2 = end - start - length;
							dataoutputstream.write('\n');
							skipLinebreaks = true;
							seenWhitespace = false;
						}
						continue;
					}
					if (charFromSentence == '-') {
						skipToPosition(bufferedinputstream2, bytesReadFromIn - bytesReadFromIn2);
						bytesReadFromIn2 = bytesReadFromIn + 1;
						j = bufferedinputstream2.read();
						if (j == '\n' || j == '\r') {
							int k3 = bufferedinputstream2.read();
							bytesReadFromIn2++;
							if ((k3 == '\n' || k3 == '\r') && j != k3) {
								k3 = bufferedinputstream2.read();
								bytesReadFromIn2++;
							}
							j = k3;
							if (!Character.isLowerCase((char) j)) {
								if (seenWhitespace) {
									dataoutputstream.write(' ');
								}
								dataoutputstream.write(charFromSentence);
							}
							seenWhitespace = seenNoCR = false;
							skipLinebreaks = true;
							continue;
						}
						if (seenWhitespace) {
							dataoutputstream.write(' ');
						}
						dataoutputstream.write(charFromSentence);
						seenWhitespace = seenNoCR = skipLinebreaks = false;
						continue;
					}
					if (seenWhitespace) {
						dataoutputstream.write(' ');
					}
					dataoutputstream.write(charFromSentence);
					seenWhitespace = seenNoCR = skipLinebreaks = false;
				}

				if (sentenceBoundary.current() > 2) {
					int boundaryPrecedingCurrentSentenceEnd = wordBoundary.preceding(sentenceBoundary.current() - 2);
					long l5 = boundaryPrecedingCurrentSentenceEnd - bytesReadFromIn1;
					if (l5 >= 0L) {
						stringbuffer.delete(0, stringbuffer.length());
						skipToPosition(bufferedinputstream1, l5);
						for (int charsInCurrentWord = wordBoundary.next() - boundaryPrecedingCurrentSentenceEnd; charsInCurrentWord > 0; charsInCurrentWord--) {
							stringbuffer.append((char) bufferedinputstream1.read());
						}

						bytesReadFromIn1 = wordBoundary.current();
						if (!abbreviations.contains(stringbuffer.toString()) && !skipLinebreaks) {
							do {
								j = (char) bufferedinputstream1.read();
								bytesReadFromIn1++;
							} while (sentenceEndChar((char) j));
							if (j != ',') {
								dataoutputstream.write('\n');
								skipLinebreaks = true;
								seenWhitespace = false;
							}
						}
					}
				}
			}

		} catch (Exception exception) {
			System.err.println("Problems while io: " + exception.getMessage());
			exception.printStackTrace();
		}
		try {
			characteriteratorstream.close();
			characteriteratorstream1.close();
			bufferedinputstream.close();
			bufferedinputstream1.close();
			bufferedinputstream2.close();
			dataoutputstream.close();
		} catch (IOException ioexception1) {
			System.err.println("Problems while closing the file: " + ioexception1.getMessage());
		}
		success++;
		return 1;
	}

	private void skipToPosition(BufferedInputStream bufferedinputstream, long start) throws IOException {
		long l1 = start;
		long l6;
		do {
			l6 = bufferedinputstream.skip(l1);
		} while (l6 > 0L && (l1 -= l6) > 0L);
	}

	private int skipToInPosition(BufferedInputStream bufferedinputstream2, int bytesReadFromIn, int bytesReadFromIn2) throws IOException {
		long l2;
		if (bytesReadFromIn2 == 0) {
			l2 = bytesReadFromIn - 1;
			bytesReadFromIn2 = bytesReadFromIn;
		} else {
			l2 = bytesReadFromIn - bytesReadFromIn2;
			bytesReadFromIn2 = bytesReadFromIn + 1;
		}
		skipToPosition(bufferedinputstream2, l2);
		return bytesReadFromIn2;
	}

	private String convertHtmlExternally(String s, String s1) {
		InputStream inputstream = null;
		try {
			htmlConverterCmdArray[htmlConverterArgPos] = s;
			inputstream = Runtime.getRuntime().exec(htmlConverterCmdArray).getInputStream();
		} catch (Exception exception) {
			System.err.println("Could not read output of " + htmlConverter + " '" + s + "': " + exception.getMessage());
			return null;
		}
		FileOutputStream fileoutputstream = null;
		try {
			fileoutputstream = new FileOutputStream(s1);
		} catch (IOException ioexception) {
			System.err.println("Could not open output file '" + s1 + "': " + ioexception.getMessage());
			return null;
		}
		try {
			byte abyte0[] = new byte[4096];
			int i = -1;
			boolean flag = false;
			while ((i = inputstream.read(abyte0)) != -1) {
				fileoutputstream.write(abyte0, 0, i);
			}
			fileoutputstream.close();
		} catch (IOException ioexception1) {
			System.out.println("Could not write output file '" + s1 + "': " + ioexception1.getMessage());
			try {
				fileoutputstream.close();
			} catch (Exception exception1) {
			}
			fileoutputstream = null;
			return null;
		}
		return s1;
	}

	private String convertHtmlInternally(String s, String s1) {
		BufferedReader bufferedreader = null;
		FileWriter filewriter = null;
		Html2Text html2text = null;
		try {
			html2text = new Html2Text();
		} catch (IOException ioexception) {
			System.err.println(getClass().getName() + ".convertHtml: error when creating converter: " + ioexception);
			ioexception.printStackTrace(System.err);
			return null;
		}
		try {
			bufferedreader = new BufferedReader(new FileReader(s));
		} catch (IOException ioexception1) {
			System.err.println(getClass().getName() + ".convertHtml: error when opening file " + s + ": " + ioexception1);
			ioexception1.printStackTrace(System.err);
			return null;
		}
		try {
			filewriter = new FileWriter(s1);
		} catch (IOException ioexception2) {
			System.err.println(getClass().getName() + ".convertHtml: error when opening file " + s1 + ": " + ioexception2);
			ioexception2.printStackTrace(System.err);
			return null;
		}
		try {
			html2text.filter(bufferedreader, filewriter);
			bufferedreader.close();
		} catch (Exception exception) {
			System.err.println(getClass().getName() + ".convertHtml: error during HTML to text conversion:" + exception);
			exception.printStackTrace(System.err);
		}
		return s1;
	}

	private String convertLatin2(String s, String s1) {
		InputStream inputstream = null;
		try {
			inputstream = Runtime.getRuntime().exec("7bit2lat2.pl " + s).getInputStream();
		} catch (Exception exception) {
			System.err.println("Could not read 7bit2lat2 output: " + exception.getMessage());
		}
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(s1);
		} catch (Exception exception1) {
			System.err.println("Could not read 7bit2lat2 output: " + exception1.getMessage());
		}
		try {
			for (int i = 0; (i = inputstream.read()) != -1; ) {
				filewriter.write(i);
			}

			filewriter.close();
		} catch (Exception exception2) {
			System.err.println("Could not write temporary file: " + exception2.getMessage());
			return null;
		}
		return s1;
	}

	private String convertDoc(String s, String s1) {
		InputStream inputstream = null;
		try {
			inputstream = Runtime.getRuntime().exec("wvHtml " + s + " " + s + ".html && lynx -dump -nolist -width=255 " + s + ".html").getInputStream();
		} catch (Exception exception) {
			System.err.println("Could not read converter output: " + exception.getMessage());
			return null;
		}
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(s1);
		} catch (Exception exception1) {
			System.err.println("Could not read converter output: " + exception1.getMessage());
			return null;
		}
		try {
			for (int i = 0; (i = inputstream.read()) != -1; ) {
				filewriter.write(i);
			}

			filewriter.close();
		} catch (Exception exception2) {
			System.err.println("Could not write temporary file: " + exception2.getMessage());
			return null;
		}
		return s1;
	}

	private String convertPdf(String s, String s1) {
		if (ConvertPdf.convertFile(s, s1)) {
			return s1;
		} else {
			return null;
		}
	}

	private String convertCeml(String s, String s1) {

		return s1;
	}

	public static void main(String args[])
			throws IllegalArgumentException {
		String s = "abbrev";
		boolean flag = false;
		Text2Satz text2satz = new Text2Satz();
		String s1 = null;
		LongOpt alongopt[] = new LongOpt[13];
		alongopt[0] = new LongOpt("help", 0, null, 63);
		alongopt[1] = new LongOpt("language", 1, null, 76);
		alongopt[2] = new LongOpt("database", 1, null, 100);
		alongopt[3] = new LongOpt("abbrev", 2, null, 97);
		alongopt[4] = new LongOpt("keep_newline", 0, null, 110);
		alongopt[5] = new LongOpt("version", 0, null, 86);
		alongopt[6] = new LongOpt("files", 1, null, 102);
		alongopt[7] = new LongOpt("html", 1, null, 104);
		alongopt[8] = new LongOpt("dbconnection", 1, null, 109);
		alongopt[9] = new LongOpt("path", 1, null, 112);
		alongopt[10] = new LongOpt("raw", 0, null, 114);
		alongopt[11] = new LongOpt("wrap", 0, null, 119);
		alongopt[12] = new LongOpt("verbose", 0, null, 118);
		Getopt getopt = new Getopt("text2Satz", args, "?L:d:a:nVf:h:m:p:rwv", alongopt);
		getopt.setOpterr(false);
		do {
			int i;
			if ((i = getopt.getopt()) == -1) {
				break;
			}
			switch (i) {
				case 100: // 'd'
					text2satz.setDatabase(getopt.getOptarg());
					break;

				case 104: // 'h'
					text2satz.setHtmlConverter(getopt.getOptarg());
					break;

				case 97: // 'a'
					s = getopt.getOptarg();
					break;

				case 102: // 'f'
					s1 = getopt.getOptarg().trim();
					break;

				case 76: // 'L'
					String s2 = getopt.getOptarg();
					if (s2.startsWith("de")) {
						text2satz.setLanguage(Locale.GERMANY);
					} else if (s2.startsWith("en")) {
						text2satz.setLanguage(Locale.US);
					} else if (s2.startsWith("fr")) {
						text2satz.setLanguage(Locale.FRANCE);
					} else if (s2.startsWith("it")) {
						text2satz.setLanguage(Locale.ITALY);
					}
					break;

				case 109: // 'm'
					text2satz.setDbConnectionName(getopt.getOptarg());
					break;

				case 110: // 'n'
					text2satz.setKeepNewline(true);
					break;

				case 112: // 'p'
					text2satz.setWorkingDir(getopt.getOptarg());
					break;

				case 114: // 'r'
					text2satz.setRawMode(true);
					break;

				case 118: // 'v'
					text2satz.verbose = true;
					break;

				case 86: // 'V'
					System.out.println("text2satz 1.1");
					flag = true;
					break;

				case 119: // 'w'
					text2satz.setWrapLongLines(true);
					break;

				case 63: // '?'
					flag = true;
					break;
			}
		} while (true);
		if (flag) {
			usage();
		}
		text2satz.readAbbrevs(s);
		int j = getopt.getOptind();
		if (j < args.length) {
			for (j = j; j < args.length; j++) {
				text2satz.processText(args[j]);
			}

		} else {
			String s3 = null;
			BufferedReader bufferedreader = null;
			if (s1 == null) {
				bufferedreader = new BufferedReader(new InputStreamReader(System.in));
			} else {
				try {
					bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s1)));
				} catch (FileNotFoundException filenotfoundexception) {
					System.out.println("Could not read filenames from file " + s1);
					System.out.println(filenotfoundexception.getMessage());
				}
			}
			try {
				while ((s3 = bufferedreader.readLine()) != null) {
					text2satz.processText(s3);
				}
			} catch (Exception exception) {
				System.err.println("Could not read filenames from stdin: " + exception.getMessage());
			}
		}
		j = text2satz.getNrOfTextsProcessed();
		if (j == 0) {
			usage();
		} else {
			System.err.println("Segmented " + j + " texts into sentences.");
		}
	}

}
