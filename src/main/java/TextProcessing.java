import JLanI.kernel.DataSourceException;
import JLanI.kernel.LanIKernel;
import JLanI.kernel.Request;
import JLanI.kernel.RequestException;
import JLanI.kernel.Response;
import de.texttech.cc.Text2Satz;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import te.indexer.Word;
import te.utils.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;


//-Xms128m -Xmx1500m -Xverify:none


public class TextProcessing {


	String baseDocDir = "";
	String inputDirPath = "";
	String satzDirPath = "";
	static String outputDirPath = "";
	String indexesDirPath = "";


	int language = 0; //1 for English    0 for German

	List ranked;

	//Resources for baseform reduction

	String redbase_en = "./resources/trees/en-nouns.tree";
	String redbase_de = "./resources/trees/de-nouns.tree";


	//Resources for compound splitting

	String red = "./resources/trees/grfExt.tree";

	String forw = "./resources/trees/kompVVic.tree";

	String back = "./resources/trees/kompVHic.tree";


	//Tagger Modelle
	String tmFile = "./resources/taggermodels/deTaggerModel.model";
	String tmFile2 = "./resources/taggermodels/english.model";


	public TextProcessing(String basedocdir) {


		baseDocDir = basedocdir;

		inputDirPath = baseDocDir + "/input/";
		satzDirPath = baseDocDir + "/satzfiles/";
		//satzFilePath = baseDocDir + "/satzfiles/satz.s";
		outputDirPath = baseDocDir + "/output/";
		indexesDirPath = baseDocDir + "/indexes/";

		File outputDir = new File(outputDirPath);
		cleanDir(outputDir);

		LanIKernel.propertyFile = "config/lanikernel";
	}


	public void preprocessing(int mode) {


		convertFiles(); //Convert input file formats into txt

		if (mode == 0) {
			extractSentenceFiles();
		} else if (mode == 1) {

			extractSentenceFileCorpus();

		}


	}


	public void convertFiles() {

		File inDir = new File(inputDirPath);

		if (inDir.isDirectory()) {


			File[] files = inDir.listFiles();

			Arrays.sort(files);


			// create subdirectory to contain the converted txt files:
			File txtDir = new File(inDir.getAbsolutePath(), "txt");
			if (!txtDir.exists()) {
				try {
					txtDir.mkdir();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				cleanDir(txtDir);


			// now loop through them and convert them
			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());


				if (curFile.isFile()) {


					try {
						System.out.println("Convert " + curFile.getAbsolutePath());


						String curtext = "";
						InputStream inputStream = null;

						try {

							Parser parser = new AutoDetectParser();
							ContentHandler contentHandler = new BodyContentHandler(10 * 1024 * 1024);
							Metadata metadata = new Metadata();


							inputStream = new FileInputStream(curFile);

							parser.parse(inputStream, contentHandler, metadata, new ParseContext());
						
						
						/*
						for (String name : metadata.names()) {  
						String value = metadata.get(name);  
						System.out.println("Metadata Name: " + name);  
						System.out.println("Metadata Value: " + value);  
						}  
						
						System.out.println("Title: " + metadata.get("title"));  
						System.out.println("Author: " + metadata.get("Author"));  */
							System.out.println("content: " + contentHandler.toString());
							curtext = contentHandler.toString();

						} catch (IOException e) {
							e.printStackTrace();
						} catch (TikaException e) {
							e.printStackTrace();
						} catch (SAXException e) {
							e.printStackTrace();
						} finally {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}


						File destFile = new File(txtDir, curFile.getName() + ".txt");


						FileWriter f = new FileWriter(destFile);
						if (curtext != null) {
							f.write(curtext);
						}

						f.close();

					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					//System.out.println("It is no file!");
				}
			} //for all files


		}


	}


	public void extractSentenceFiles() {


		File satzDir = new File(satzDirPath);

		cleanDir(satzDir);


		File inDir = new File(inputDirPath + "txt");


		if (inDir.isDirectory()) {


			File[] files = inDir.listFiles();

			Arrays.sort(files);


			// now loop through them
			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());


				if (curFile.isFile()) {


					int language = getLanguage(curFile);


					String[] cmdArray = new String[5];

					if (language == 0)
						cmdArray[0] = "-L de";

					if (language == 1)
						cmdArray[0] = "-L en";


					cmdArray[1] = "-d" + curFile.getName(); //cmdArray[1] = "-dsatz"+(i+1);
					cmdArray[2] = "-p./" + satzDirPath;                    // /data/satzfiles";

					cmdArray[3] = "-a./resources/abbreviation/abbrev.txt";
					cmdArray[4] = curFile.getAbsolutePath();


					Text2Satz.main(cmdArray);


				} else {
					//System.out.println("It is no file!");
				}
			} //for all files


		}


	}


	public void extractSentenceFileCorpus() {


		File satzDir = new File(satzDirPath);

		cleanDir(satzDir);


		File inDir = new File(inputDirPath + "txt");

		if (inDir.isDirectory()) {

			File[] files = inDir.listFiles();

			String[] cmdArray = new String[4 + files.length];
			//		cmdArray[0] = "-L de";
			cmdArray[1] = "-dcorpus";   //cmdArray[1] = "-dsatz";
			cmdArray[2] = "-p./" + satzDirPath;                     //data/satzfiles";

			cmdArray[3] = "-a./resources/abbreviation/abbrev.txt";


			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());

				if (curFile.isFile()) {


					if (i == 0) {

						int language = getLanguage(curFile);

						if (language == 0)
							cmdArray[0] = "-L de";

						if (language == 1)
							cmdArray[0] = "-L en";

					}

					cmdArray[i + 4] = files[i].getAbsolutePath();


				} else {
					//System.out.println("It is no file!");
				}


			}

			if (files.length > 0)
				Text2Satz.main(cmdArray);


		}


	}


	int findEntry(List termlist, String query) {

		//int index = Collections.binarySearch(termlist, query);

		int index = -1;

		for (int i = 0; i < termlist.size(); i++)
			if (termlist.get(i).equals(query))
				index = i;


		return index;

	}


	public void createIndexes() {


		File satzDir = new File(satzDirPath);


		if (satzDir.isDirectory()) {


			File[] files = satzDir.listFiles();

			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());


				if (curFile.isFile()) {


					System.out.println("Analysing: " + curFile.getAbsolutePath());
					System.out.println("");


					Cooccs mycooccs = new Cooccs(curFile, false);


					float[][] cooccmatrix;
					List termlist = new Vector();


					System.out.println("Filling termlist...");
					Map cooccsmap = mycooccs.getCooccMap();

					//Liste aller Terme füllen (dient als Lookup von Pos zu String)
					Set keys = cooccsmap.keySet();
					for (Iterator j = keys.iterator(); j.hasNext(); ) {
						String curStr = (String) j.next();

						termlist.add(curStr);

					}

					System.out.println("Number of all terms (types): " + termlist.size());

					cooccmatrix = new float[termlist.size()][termlist.size()];

					// Kookkurrenzmatrix füllen

					try {
						System.out.println("Filling co-occurrence matrix...");


						Set coocckeys = cooccsmap.keySet();
						for (Iterator j = keys.iterator(); j.hasNext(); ) {
							String curStr = (String) j.next();

							int keyindex = findEntry(termlist, curStr);


							Map termCooccs = (Map) cooccsmap.get(curStr);

							Set cooccvalues = termCooccs.keySet();


							for (Iterator k = cooccvalues.iterator(); k.hasNext(); ) {

								String curStr2 = (String) k.next();
								float curSig = ((Float) termCooccs.get(curStr2)).floatValue();

								int keyindex2 = findEntry(termlist, curStr2);


								if ((keyindex != -1) && (keyindex2 != -1) /*&& (keyindex!=keyindex2)*/) {

									if (keyindex == keyindex2) {
										curSig = 1;

										cooccmatrix[keyindex][keyindex2] = curSig;


									} else {


										if (curSig > 0) {

											cooccmatrix[keyindex][keyindex2] = curSig;
											//cooccmatrix[keyindex2][keyindex] = curSig;


										} else {

											cooccmatrix[keyindex][keyindex2] = (float) 0.01;
											//Man könnte hier auch statt 0 einen kleinen Wert wie 1 verwenden.


										}

									}

								}
							}


						} //cooccmatrix füllen


						List pageranks = calculatePageRanks(termlist, cooccmatrix);

						//k-means mit nur den maxentries wichtigsten Termen
						int maxentries = 1000000; //200;
						if (pageranks.size() < maxentries) maxentries = pageranks.size();

						float[][] simplifiedcooccmatrix = new float[maxentries][maxentries];
						String[] simplifiedtermlistvec = new String[maxentries];


						File topicindex = new File(indexesDirPath + "topicindex.ser");
						if (!topicindex.exists() && !topicindex.isDirectory()) {
							topicindex.createNewFile();

							Vector<String> helpvector = new Vector<String>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "topicindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpvector);
								oos.close();
								fos.close();
								System.out.printf("Serialized HashMap data is saved in topicindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						Vector topindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "topicindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							topindex = (Vector) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Topic index " + topindex.size() + " " + topindex.toString());


						File topicmapindex = new File(indexesDirPath + "topicmapindex.ser");
						if (!topicmapindex.exists() && !topicmapindex.isDirectory()) {
							topicmapindex.createNewFile();

							HashMap<String, Vector> helpmap = new HashMap<String, Vector>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "topicmapindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpmap);
								oos.close();
								fos.close();
								System.out.printf("Serialized HashMap data is saved in topicmapindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						HashMap topmapindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "topicmapindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							topmapindex = (HashMap) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Topic Map index " + topmapindex.size() + " " /*+ topmapindex.toString()*/);
  			     /* 
  			      Vector testvec = (Vector) topmapindex.get("auto");
  			      
  			    System.out.println("Topic Map index auto " + testvec.toString());
  			      
  			  testvec = (Vector) topmapindex.get("geld");
			      
			    System.out.println("Topic Map index geld " + testvec.toString());
  			      
			    testvec = (Vector) topmapindex.get("sport");
			      
  			    System.out.println("Topic Map index sport " + testvec.toString()); 
  			    
  			  testvec = (Vector) topmapindex.get("politik");
			      
			    System.out.println("Topic Map index politik " + testvec.toString());
			    */


						File invertedtopicmapindex = new File(indexesDirPath + "invertedtopicmapindex.ser");
						if (!invertedtopicmapindex.exists() && !invertedtopicmapindex.isDirectory()) {
							invertedtopicmapindex.createNewFile();

							HashMap<String, HashSet> helpmap = new HashMap<String, HashSet>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "invertedtopicmapindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpmap);
								oos.close();
								fos.close();
								System.out.printf("Serialized HashMap data is saved in invertedtopicmapindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						HashMap invtopmapindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "invertedtopicmapindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							invtopmapindex = (HashMap) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Inverted Topic Map index " + invtopmapindex.size() + " " + invtopmapindex.toString());


						//Determine file's topic

						String topic = curFile.getName().substring(0, curFile.getName().indexOf("_"));

						System.out.println("File Topic: " + topic);

						boolean topicfound = false;

						for (int j = 0; j < topindex.size(); j++) {

							String curstr = topindex.get(j).toString();

							if (curstr.equals(topic)) topicfound = true;

						}

						if (topicfound == false) topindex.add(topic);


						Vector previoustermvector = new Vector();

						if (topmapindex.containsKey(topic)) {

							previoustermvector = (Vector) topmapindex.get(topic);
						}


						for (int j = 0; j < maxentries; j++) {

							Word curWord = (Word) pageranks.get(j);

							//System.out.println("PageRank of " +curWord.getWordStr() + ": " + curWord.getSig());

							if (invtopmapindex.containsKey(curWord.getWordStr())) {
								HashSet helpset = (HashSet) invtopmapindex.get(curWord.getWordStr());

								if (!helpset.contains(topic)) helpset.add(topic);
								invtopmapindex.put(curWord.getWordStr(), helpset);


							} else {

								HashSet helpset = new HashSet();
								helpset.add(topic);
								invtopmapindex.put(curWord.getWordStr(), helpset);

							}


							boolean termfound = false;

							for (int k = 0; k < previoustermvector.size(); k++) {

								String curstr = previoustermvector.get(k).toString();

								if (curstr.equals(curWord.getWordStr())) termfound = true;

							}

							if (termfound == false) previoustermvector.add(curWord.getWordStr());


						}

						Collections.sort(previoustermvector);

						System.out.println("PreviousTermvector " + previoustermvector.toString());


						topmapindex.put(topic, previoustermvector);


						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "topicindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(topindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in topicindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "topicmapindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(topmapindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in topicmapindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "invertedtopicmapindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(invtopmapindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in invertedtopicmapindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


						System.out.println("Terme mit allen Topics: ");

						for (Iterator j = invtopmapindex.keySet().iterator(); j.hasNext(); ) {

							String key = j.next().toString();

							HashSet helpset = (HashSet) invtopmapindex.get(key);

							if (helpset.size() == topindex.size()) {

								System.out.println("\t " + key);

							}

						}


						/*Create normal indexes and centroid indexes*/

						Vector centroidquery = new Vector();


						File invertedindex = new File(indexesDirPath + "invertedindex.ser");
						if (!invertedindex.exists() && !invertedindex.isDirectory()) {
							invertedindex.createNewFile();

							HashMap<String, HashSet> helpmap = new HashMap<String, HashSet>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "invertedindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpmap);
								oos.close();
								fos.close();
								System.out.printf("Serialized HashMap data is saved in invertedindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						HashMap<String, HashSet> invindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "invertedindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							invindex = (HashMap) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Inverted index " + invindex.size() + " " + invindex.toString());

						/********************/


						File invertedcountindex = new File(indexesDirPath + "invertedcountindex.ser");
						if (!invertedcountindex.exists() && !invertedcountindex.isDirectory()) {
							invertedcountindex.createNewFile();

							HashMap<String, Integer> helpmap = new HashMap<String, Integer>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "invertedcountindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpmap);
								oos.close();
								fos.close();
								System.out.printf("Serialized HashMap data is saved in invertedcountindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						HashMap<String, Integer> invcountindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "invertedcountindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							invcountindex = (HashMap) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Inverted count index " + invcountindex.size() + " " + invcountindex.toString());

						HashMap incounindexsorted = sortByValuesDec(invcountindex);


						//Iterator helpiterator = help.keySet()
						System.out.println("Sorted incounindex: ");

						for (Object temp : incounindexsorted.keySet()) {

							//  System.out.println("Key: " + temp + "   Value: " + incounindexsorted.get(temp));


						}


						for (int j = 0; j < maxentries; j++) {

							Word curWord = (Word) pageranks.get(j);


							if (invindex.containsKey(curWord.getWordStr())) {
								HashSet helpset = invindex.get(curWord.getWordStr());
								helpset.add(curFile.getName());
								invindex.put(curWord.getWordStr(), helpset);

							} else {
								HashSet helpset = new HashSet();
								helpset.add(curFile.getName());

								invindex.put(curWord.getWordStr(), helpset);

							}


							if (invcountindex.containsKey(curWord.getWordStr())) {
								int helpvalue = (Integer) invcountindex.get(curWord.getWordStr()).intValue();
								helpvalue += mycooccs.getTermFrequency(curWord.getWordStr());


								invcountindex.put(curWord.getWordStr(), new Integer(helpvalue));

							} else {

								int helpvalue = mycooccs.getTermFrequency(curWord.getWordStr());

								invcountindex.put(curWord.getWordStr(), new Integer(helpvalue));

							}


							System.out.println("PageRank of " + curWord.getWordStr() + ": " + curWord.getSig());

							writeLinesToFile(outputDirPath + "" + curFile.getName() + "_termvector.txt", new String[]{"" + curWord.getWordStr()}, true);

							simplifiedtermlistvec[j] = curWord.getWordStr();

							if (j < 5)
								centroidquery.add(curWord.getWordStr());

						}

						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "invertedindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(invindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in invertedindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "invertedcountindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(invcountindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in invertedcountindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


						/**************************  Centroid Calculation ********************/


						File invertedcentroidindex = new File(indexesDirPath + "invertedcentroidindex.ser");
						if (!invertedcentroidindex.exists() && !invertedcentroidindex.isDirectory()) {
							invertedcentroidindex.createNewFile();

							HashMap<String, HashMap<String, Double>> helpmap = new HashMap<String, HashMap<String, Double>>();

							try {
								FileOutputStream fos = new FileOutputStream(indexesDirPath + "invertedcentroidindex.ser");
								ObjectOutputStream oos = new ObjectOutputStream(fos);
								oos.writeObject(helpmap);
								oos.close();
								fos.close();
								System.out.println("Serialized HashMap data is saved in invertedcentroidindex.ser");
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

						}


						HashMap<String, HashMap<String, Double>> invcentroidindex = null;

						try {
							FileInputStream fis = new FileInputStream(indexesDirPath + "invertedcentroidindex.ser");
							ObjectInputStream ois = new ObjectInputStream(fis);
							invcentroidindex = (HashMap) ois.readObject();
							ois.close();
							fis.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}


						System.out.println("Inverted centroid index " + invcentroidindex.size() + " " + invcentroidindex.toString());


						HashMap result = mycooccs.getCentroidbySpreadingActivation(centroidquery);


						String centroid = result.get("centroid").toString();

						System.out.println("Centroid of file	 " + curFile.getName() + " is: " + result.get("centroid"));

						writeLinesToFile(outputDirPath + "centroids_5.txt", new String[]{"" + result.get("centroid") + ";" + curFile.getName() + ";" + result.get("shortestaveragepathlength")}, true);


						if (invcentroidindex.containsKey(centroid)) {
							HashMap helpmap = invcentroidindex.get(centroid);
							helpmap.put(curFile.getName(), result.get("shortestaveragepathlength"));
							invcentroidindex.put(centroid, helpmap);

						} else {
							HashMap helpmap = new HashMap();
							helpmap.put(curFile.getName(), result.get("shortestaveragepathlength"));

							invcentroidindex.put(centroid, helpmap);

						}


						try {
							FileOutputStream fos =
									new FileOutputStream(indexesDirPath + "invertedcentroidindex.ser");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.writeObject(invcentroidindex);
							oos.close();
							fos.close();
							System.out.println("Serialized HashMap data is saved in invertedcentroidindex.ser");
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}


					} catch (Exception e) {
						System.out.println("ERROR !: " + e);
					}


				} else {
					System.out.println("It is no file!");
				}
			} //for all files

		}

	}


	public String selectTermFromTopic(String topic) {

		String result = "";


		Vector topindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "topicindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			topindex = (Vector) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}


		HashMap topmapindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "topicmapindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			topmapindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}


		HashMap invtopmapindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "invertedtopicmapindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			invtopmapindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();

		}


		HashMap invcountindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "invertedcountindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			invcountindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();

		}


		System.out.println("Inverted count index " + invcountindex.size() + " " + invcountindex.toString());

		HashMap invcountindexsorted = sortByValuesDec(invcountindex);


		//Iterator helpiterator = help.keySet()
		System.out.println("Sorted invcountindex: ");

		for (Object temp : invcountindexsorted.keySet()) {
			//System.out.println("Key: " + temp + "   Value: " + invcountindexsorted.get(temp));
		}

		HashMap invcountindexreduced = new HashMap();


		if (topic.equals("all")) {

			//selecting random topic

			int topicsize = topindex.size();

			Random ran = new Random();
			int x = ran.nextInt(topicsize);

			String rantopic = topindex.get(x).toString();

			System.out.println("Randomly selected topic " + rantopic);


			// selecting random term from topic

			Vector termvector = (Vector) topmapindex.get(rantopic);


			do {
				Random ran2 = new Random();
				int rantermindex = ran2.nextInt(termvector.size());

				result = termvector.get(rantermindex).toString();

				System.out.println("Randomly selected term " + result + " appears in " + ((HashSet) invtopmapindex.get(result)).size() + " topics.");

			} while (((HashSet) invtopmapindex.get(result)).size() != 1);


			System.out.println("Randomly selected term for usage " + result);

		} else {

			if (topmapindex.containsKey(topic)) {

				System.out.println("Topic found: " + topic);


				Vector termvector = (Vector) topmapindex.get(topic);

				for (int i = 0; i < termvector.size(); i++) {

					String helpstr = termvector.get(i).toString();

					if (((HashSet) invtopmapindex.get(helpstr)).size() == 1) {


						if (invcountindex.containsKey(helpstr)) {

							invcountindexreduced.put(helpstr, invcountindex.get(helpstr));

						}
					}

				}

				HashMap invcountindexreducedsorted = sortByValuesDec(invcountindexreduced);

				Vector termvectorreduced = new Vector();
				//Iterator helpiterator = help.keySet()
				System.out.println("Sorted invcountindexreduced: ");

				for (Object temp : invcountindexreducedsorted.keySet()) {
					//  System.out.println("Key: " + temp + "   Value: " + invcountindexreducedsorted.get(temp));

					if (termvectorreduced.size() < 25)
						termvectorreduced.add(temp);
				}


				do {
					Random ran2 = new Random();
					int rantermindex = ran2.nextInt(termvectorreduced.size());

					result = termvectorreduced.get(rantermindex).toString();

					System.out.println("Randomly selected term " + result + " appears in " + ((HashSet) invtopmapindex.get(result)).size() + " topic(s) and appears " + invcountindexreduced.get(result) + " times in them/it.");

				} while (((HashSet) invtopmapindex.get(result)).size() != 1);


				System.out.println("Randomly selected term for usage " + result);

			}


		}


		return result;
	}


	void generateAllQueries(String topic, int maxtermcount) {

		for (int div = 0; div < 30; div += 5)
			for (int nr = 2; nr < maxtermcount; nr++)
				for (int i = 0; i < 20; i++) {

					Vector query = generateQuery(nr, topic, div, div + 5);
					System.out.println("Generated query: " + query.toString());

					String curquery = "";
					for (int j = 0; j < query.size(); j++)
						curquery = curquery + query.get(j).toString() + "###";

					int lastindex = curquery.lastIndexOf("###");
					curquery = curquery.substring(0, lastindex);

					writeLinesToFile(outputDirPath + "" + "queries_" + nr + "_" + topic + "_" + div + ".txt", new String[]{"" + curquery}, true);

				}


	}


	Vector generateQuery(int termcount, String topic, double minpathlength, double maxpathlength) {

		Cooccs mycooccs = new Cooccs();
		Vector resultquery = new Vector();

		//noch prüfen, ob Topic überhaupt vorhanden ist
		Vector topindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "topicindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			topindex = (Vector) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}


		if ((termcount > 0) && (topic.equals("all") || topindex.contains(topic)))
			while (resultquery.size() < termcount) {
    	  
    		
    		  /*
    		   * 
    		   
    		    HashSet helpquery = new HashSet();
    		  for (int i=0; i<termcount; i++) {
    			  
    			  String term = "";
    			  boolean again=false;
    			  do {
    				  again=false;
    				  term = selectTermFromTopic(topic);
    				  
    				  if (!helpquery.contains(term)) {
    					  helpquery.add(term);
    				  } else {
    					  again=true;
    				  }
    				  
    				  
    			  } while (again==true);
    			  
    			  
    		  }*/


				String term = selectTermFromTopic(topic);   //Top Terms of Topic speichern
				Vector initialquery = new Vector();
				initialquery.add(term);
				int steps = 0;


				do {
					steps++;

					Vector activatedterms = mycooccs.singleSpreadingActivation(initialquery, steps);

					System.out.println("Generating query from activated terms " + activatedterms.toString());


					if (activatedterms.size() > termcount) {

						int trycount = 0;

						do {  //while ((resultquery.size()<termcount) && (trycount<11) );

							trycount++;

							System.out.println("Fast query generation");
							;

							Vector helpqueryvec = new Vector();
							helpqueryvec.add(term);

							int topiccounter = 1;

							Random ran = new Random();
							for (int lauf = 0; lauf < (termcount - 1); lauf++) {

								//int x=0;
								String ranterm = "";

								do {

									int x = ran.nextInt(activatedterms.size());

									ranterm = activatedterms.get(x).toString();

								}
								while (helpqueryvec.contains(ranterm));
								helpqueryvec.add(ranterm);

							} //for

							System.out.println("Generated query: " + helpqueryvec.toString());


							if (!topic.equals("all"))
								for (int lauf = 0; lauf < helpqueryvec.size(); lauf++) {
									HashSet curtopics = topicsOfTerm((String) helpqueryvec.get(lauf));

									System.out.println(curtopics.toString());

									if (!curtopics.contains(topic)) {
										topiccounter++;
									}

								}


							if (topic.equals("all")) {
								if (mycooccs.testQuery(helpqueryvec, minpathlength, maxpathlength)) {
									System.out.println("Query " + helpqueryvec + " can be used.");

									resultquery = helpqueryvec;

								} else {
									System.out.println("Query " + helpqueryvec + " cannot be used. Generate new query.");

								}
							} else {

								if (topiccounter == 1) {

									if (mycooccs.testQuery(helpqueryvec, minpathlength, maxpathlength)) {
										System.out.println("Query " + helpqueryvec + " can be used.");

										resultquery = helpqueryvec;

									} else {
										System.out.println("Query " + helpqueryvec + " cannot be used. Generate new query.");

									}

								} else {

									System.out.println("Query " + helpqueryvec + " cannot be used (Topiccounter). Generate new query.");

								}


							}


						} while ((resultquery.size() < termcount) && (trycount < 11));


					}


				} while ((resultquery.size() < termcount) && (steps < 11));


			}


		return resultquery;
	}


	HashSet topicsOfTerm(String term) {

		HashSet topics = new HashSet();

		HashMap invtopmapindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "invertedtopicmapindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			invtopmapindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();

		}

		if (invtopmapindex.containsKey(term)) {

			topics = (HashSet) invtopmapindex.get(term);

		}


		return topics;
	}


	//Measures the mean number of activated nodes for 10 generated centroid terms for diversity values from 1 to 30
	void activatedNodes(int termcount, String topic) {

		Cooccs mycooccs = new Cooccs();
		Vector resultquery = new Vector();

		//noch prüfen, ob Topic überhaupt vorhanden ist
		Vector topindex = null;

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "topicindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			topindex = (Vector) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}


		if ((termcount > 0) && (topic.equals("all") || topindex.contains(topic))) {
			for (double div = 1; div < 31; div++) {

				double meanactivated = 0.0;

				for (int i = 0; i < 10; i++) {  //queries


					String term = selectTermFromTopic(topic);
					Vector initialquery = new Vector();
					initialquery.add(term);

					Vector activatedterms = mycooccs.singleSpreadingActivationSlow(initialquery, 0.0, div);

					meanactivated += activatedterms.size();

				}

				meanactivated /= 10.0;

				writeLinesToFile(outputDirPath + "_results_activatedterms_" + div + ".txt", new String[]{"" + meanactivated}, true);
				writeLinesToFile(outputDirPath + "_results_activatedterms_.txt", new String[]{"" + meanactivated}, true);


			}
		}


	}


	//Measure time to process pre-generated queries from files, generate centroids and optionally query index with them
	public void queryDatabase() {

		Cooccs mycooccs = new Cooccs();


		// mycooccs.getAllNodes();
		// mycooccs.getShortestPathWeightInDatabase("Auto","VW");


		Vector query = new Vector();


		try {


			File inDir = new File(baseDocDir + "/queries/");

			if (inDir.isDirectory()) {

				File[] files = inDir.listFiles();

				// now loop through them and convert them
				for (int filecounter = 0; filecounter < files.length; filecounter++) {


					double meantime = 0.0;

					Vector allresults = new Vector();


					File queryfile = new File(files[filecounter].getAbsolutePath()/*baseDocDir + "/queries/queries_2_all_0.txt"*/);

					FileInputStream fin2 = new FileInputStream(queryfile);

					String lineorig = "";

					BufferedReader myInput2 = new BufferedReader(new InputStreamReader(fin2));

					double linecounter = 0.0;

					double useablequery = 0.0;

					while ((lineorig = myInput2.readLine()) != null) {

						linecounter++;

						if (linecounter < 21) {

							query = new Vector();

							System.out.println(lineorig);

							String[] splittedquery = lineorig.split("###");

							for (int i = 0; i < splittedquery.length; i++) {
								query.add(splittedquery[i]);
							}

							System.out.println(query.toString());

							HashMap result = mycooccs.getCentroidbySpreadingActivation(query);  //Stickoxid
							System.out.println("Centroid: " + result.get("centroid") + " " + result.get("shortestaveragepathlength") + " " + result.get("activatednodes") + " " + result.get("timeelapsed"));

							if (((double) result.get("timeelapsed")) > 0.0) {

								useablequery++;

								meantime = meantime + (double) result.get("timeelapsed");

								allresults.add(result);

							}

							//Additionally search index
							//searchIndex(query);


						}

					}

					meantime = meantime / useablequery;

					System.out.println("Average processing time for " + linecounter + " (" + useablequery + ") queries " + meantime);

					String mean = String.valueOf(meantime);

					mean = mean.substring(0, 5);

					mean = mean.replace(".", ",");


					writeLinesToFile(outputDirPath + "_results_" + queryfile.getName() + "_.txt", new String[]{"" + mean}, true);


					HashMap pathlengths = new HashMap();


					for (int i = 0; i < allresults.size(); i++) {
						HashMap result = (HashMap) allresults.get(i);

						pathlengths.put(i, result.get("shortestaveragepathlength"));

					}


					HashMap pathlengthssorted = sortByValuesInc(pathlengths);


					//Iterator helpiterator = help.keySet()
					System.out.println("Sorted pathlengths");

					for (Object temp : pathlengthssorted.keySet()) {

						HashMap result = (HashMap) allresults.get((int) temp);

						System.out.println("\t " + temp + "  Pathlength: " + pathlengthssorted.get(temp) + "  Timeelapsed: " + result.get("timeelapsed"));
						//  writeLinesToFile(outputDirPath+"_results_"+queryfile.getName()+"_.txt", new String[] {""+pathlengthssorted.get(temp)+";"+result.get("timeelapsed") }, true);

						//  writeLinesToFile(outputDirPath+"_results_"+queryfile.getName()+"_.txt", new String[] {""+meantime }, true);


					}


				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	//Perform Document Search (by Centroid and by Boolean Retrieval)
	void searchIndex(Vector query) {

		System.out.println("Document Search for: " + query.toString());

		Cooccs mycooccs = new Cooccs();

		HashMap<String, HashSet> invindex = null;
		HashMap<String, Double> matchingdocuments = new HashMap<String, Double>();

		HashMap<String, HashMap<String, Double>> invcentroidindex = null;
		HashMap<String, Double> centroiddistances = new HashMap<String, Double>();

		String centroid = "";


		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "invertedindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			invindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}


		// System.out.println("Inverted index " + invindex.size() + " " + invindex.toString());

		try {
			FileInputStream fis = new FileInputStream(indexesDirPath + "invertedcentroidindex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			invcentroidindex = (HashMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}


		System.out.println("Documents in Index: ");

		for (Iterator i = invcentroidindex.keySet().iterator(); i.hasNext(); ) {

			String key = i.next().toString();

			System.out.println("Centroid " + key + "\t " + invcentroidindex.get(key));

		}


		//System.out.println("Inverted centroid index " + invcentroidindex.size() + " " + invcentroidindex.toString());


		if (!query.isEmpty()) {

			if (query.size() > 1) {


				HashMap result = mycooccs.getCentroidbySpreadingActivation(query);  //Stickoxid
				System.out.println("Centroid found for query: " + result.get("centroid") + " " + result.get("shortestaveragepathlength") + " " + result.get("activatednodes") + " " + result.get("timeelapsed"));
				centroid = result.get("centroid").toString();


				for (String key : invcentroidindex.keySet()) {

					double pathweight = mycooccs.getShortestPathWeightInDatabase(centroid, key);

					if (pathweight == -1) pathweight = Double.MAX_VALUE;

					centroiddistances.put(key, pathweight);

				}


				HashMap<String, Double> sortedMap = sortByValuesInc(centroiddistances);
				System.out.println(sortedMap);

				int rank = 0;

				for (String key : sortedMap.keySet()) {

					rank++;
					System.out.println("Document(s) at rank " + rank + " (with centroid " + key + "): ");

					HashMap<String, Double> help = invcentroidindex.get(key);
					//Iterator helpiterator = help.keySet()

					for (String temp : help.keySet()) {
						System.out.println("\t " + temp + "  Distance to Query Centroid " + centroid + ": " + sortedMap.get(key));
					}

				}


				// Boolsches Retrieval ****

				System.out.println("\nBoolean Retrieval: ");

				for (Iterator i = query.iterator(); i.hasNext(); ) {

					String curQueryTerm = i.next().toString();

					if (invindex.containsKey(curQueryTerm)) {

						HashSet<String> help = invindex.get(curQueryTerm);

						for (String temp : help) {

							if (matchingdocuments.containsKey(temp)) {

								double helpvalue = matchingdocuments.get(temp);
								helpvalue++;
								matchingdocuments.put(temp, helpvalue);

							} else {

								matchingdocuments.put(temp, 1.0);

							}

						}

					}

				}


				HashMap<String, Double> sortedMap2 = sortByValuesDec(matchingdocuments);


				System.out.println(sortedMap2);

				rank = 0;

				for (String key : sortedMap2.keySet()) {

					rank++;
					System.out.println("Document at rank " + rank + ": ");

					// if (sortedMap2.containsKey(key))
					System.out.println("\t \"" + key + "\"	number of matching query terms: " + matchingdocuments.get((String) key) + "  " + sortedMap2.get((String) key));

				}


				//Query not empty and size > 1
			}
		}

	}


	private HashMap sortByValuesDec(HashMap map) {
		List list = new LinkedList(map.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}


	private HashMap sortByValuesInc(HashMap map) {
		List list = new LinkedList(map.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}


	public void createDB() {


		File satzDir = new File(satzDirPath);


		if (satzDir.isDirectory()) {


			File[] files = satzDir.listFiles();

			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());


				if (curFile.isFile()) {


					System.out.println("Filling DB with: " + curFile.getAbsolutePath());
					System.out.println("");


					Cooccs mycooccs = new Cooccs(curFile, true);


				}
			}
		}
	}


	public void analysis() {


		File satzDir = new File(satzDirPath);


		if (satzDir.isDirectory()) {


			File[] files = satzDir.listFiles();

			Arrays.sort(files);


			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());


				if (curFile.isFile()) {


					System.out.println("Analysing: " + curFile.getAbsolutePath());
					System.out.println("");


					Cooccs mycooccs = new Cooccs(curFile, false);


					float[][] cooccmatrix;
					List termlist = new Vector();


					System.out.println("Filling termlist...");
					Map cooccsmap = mycooccs.getCooccMap();

					//Liste aller Terme füllen (dient als Lookup von Pos zu String)
					Set keys = cooccsmap.keySet();
					for (Iterator j = keys.iterator(); j.hasNext(); ) {
						String curStr = (String) j.next();

						termlist.add(curStr);


					}

					System.out.println("Number of all terms (types): " + termlist.size());


					cooccmatrix = new float[termlist.size()][termlist.size()];

					//fill co-occurrence matrix

					try {
						System.out.println("Filling co-occurrence matrix...");


						Set coocckeys = cooccsmap.keySet();
						for (Iterator j = keys.iterator(); j.hasNext(); ) {
							String curStr = (String) j.next();

							int keyindex = findEntry(termlist, curStr);


							Map termCooccs = (Map) cooccsmap.get(curStr);

							Set cooccvalues = termCooccs.keySet();


							for (Iterator k = cooccvalues.iterator(); k.hasNext(); ) {

								String curStr2 = (String) k.next();
								float curSig = ((Float) termCooccs.get(curStr2)).floatValue();

								int keyindex2 = findEntry(termlist, curStr2);


								if ((keyindex != -1) && (keyindex2 != -1) /*&& (keyindex!=keyindex2)*/) {

									if (keyindex == keyindex2) {
										curSig = 1;

										cooccmatrix[keyindex][keyindex2] = curSig;

										//Scaling *100 when using DICE coefficient; if LL: not necessary

									} else {


										if (curSig > 0) {

											cooccmatrix[keyindex][keyindex2] = curSig;
											//cooccmatrix[keyindex2][keyindex] = curSig;

											//Scaling *100 when using DICE coefficient; if LL: not necessary


										} else {

											cooccmatrix[keyindex][keyindex2] = (float) 0.01;


										}

									}

								}
							}


						} //cooccmatrix füllen


						List pageranks = calculatePageRanks(termlist, cooccmatrix);

						int maxentries = 1000000; //200;
						if (pageranks.size() < maxentries) maxentries = pageranks.size();

						float[][] simplifiedcooccmatrix = new float[maxentries][maxentries];
						String[] simplifiedtermlistvec = new String[maxentries];


						Vector centroidquery = new Vector();
						HashSet mostfrequentterms = new HashSet();

						for (int j = 0; j < maxentries; j++) {

							Word curWord = (Word) pageranks.get(j);

							System.out.println("PageRank of " + curWord.getWordStr() + ": " + curWord.getSig());

							writeLinesToFile(outputDirPath + "" + curFile.getName() + "_termvector.txt", new String[]{"" + curWord.getWordStr()}, true);

							simplifiedtermlistvec[j] = curWord.getWordStr();

							if (mostfrequentterms.size() < 25)
								mostfrequentterms.add(curWord.getWordStr());

							if (j < 5)
								centroidquery.add(curWord.getWordStr());

						}


						HashMap result = mycooccs.getCentroidbySpreadingActivation(centroidquery);


						String centroid = result.get("centroid").toString();

						System.out.println("Centroid of file " + curFile.getName() + " is: " + result.get("centroid"));
						writeLinesToFile(outputDirPath + "centroidsdata_" + curFile.getName() + ".txt", new String[]{"" + curFile.getName() + ";" + result.get("centroid") + ";" + result.get("shortestaveragepathlength")}, true);

  				  
			  /*
  				System.out.println("Centroid candidate data: " + result.get("centroidcandidatesdata"));
				  
  				HashMap centroidcandidatesdata = (HashMap)result.get("centroidcandidatesdata");
  				
  				HashMap centroidcandidatesdatasorted = sortByValuesInc(centroidcandidatesdata);
  				
  				System.out.println("Centroid candidate data sorted: " + centroidcandidatesdatasorted);
  				
  				
  				
  				*/


						List[] hits = calculateHITS(termlist, cooccmatrix);

						for (int j = 0; j < hits[0].size(); j++) {

							Word curWordauth = (Word) hits[0].get(j);
							String entry = curWordauth.getWordStr();
							String tag = "";
							int pos = curWordauth.getWordStr().indexOf("|");
							if (pos != -1) {
								entry = curWordauth.getWordStr().substring(0, pos);

								tag = curWordauth.getWordStr().substring(pos + 1, curWordauth.getWordStr().length());

							}


							System.out.println("Authority score of " + entry + ": " + curWordauth.getSig());


							writeLinesToFile(outputDirPath + "" + curFile.getName() + "_HITS.txt", new String[]{"" + entry + ";" + tag + ";" + curWordauth.getSig() + ";A"}, true);


						}

						for (int j = 0; j < hits[1].size(); j++) {


							Word curWordhubs = (Word) hits[1].get(j);
							String entry = curWordhubs.getWordStr();
							String tag = "";
							int pos = curWordhubs.getWordStr().indexOf("|");
							if (pos != -1) {
								entry = curWordhubs.getWordStr().substring(0, pos);
								tag = curWordhubs.getWordStr().substring(pos + 1, curWordhubs.getWordStr().length());
							}


							System.out.println("Hub score of " + entry + ": " + curWordhubs.getSig());

							writeLinesToFile(outputDirPath + "" + curFile.getName() + "_HITS.txt", new String[]{"" + entry + ";" + tag + ";" + curWordhubs.getSig() + ";H"}, true);


						}


					} catch (Exception e) {
						System.out.println("ERROR !: " + e);
					}


				} else {
					System.out.println("It is no file!");
				}
			} //for all files

		}

	}


	//retrieves the list of a documents's most important terms (at most maxentries terms)
	List getTermList(String document, int maxentries) {

		List termlist = new Vector();

		try {

			File curFile = new File(outputDirPath + "" + document);
			String line;

			int count = 0;

			FileInputStream fin = new FileInputStream(curFile);
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));

			while ((line = myInput.readLine()) != null) {
				termlist.add(line);
				count++;

				if (count >= maxentries)
					break;
			}

			myInput.close();
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


		return termlist;
	}


	//calculates the similarity between two documents (comparison of their term vectors based on Dice coefficient)
	public double docsim(int doc1, int doc2) {

		String doc1name = "";
		String doc2name = "";

		double sim = 0.0;

		List docnames = new Vector();

		File outDir = new File(outputDirPath);


		if (outDir.isDirectory()) {


			File[] files = outDir.listFiles();

			Arrays.sort(files);

			for (int i = 0; i < files.length; i++) {


				File curFile = new File(files[i].getAbsolutePath());

				if (curFile.isFile()) {
					String docname = curFile.getName();
					if (docname.contains("termvector")) {
						docnames.add(docname);
					}
				}

			}


		}


		if (((docnames.size() - 1) >= doc1) && ((docnames.size() - 1) >= doc2)) {

			for (int i = 0; i < docnames.size(); i++) {

				if (i == doc1) {
					doc1name = (String) docnames.get(i);
				}

				if (i == doc2) {
					doc2name = (String) docnames.get(i);
				}

			}
		}

		if ((!doc1name.equals("")) && (!doc2name.equals(""))) {

			System.out.println("Comparing the following two texts: ");

			System.out.println(doc1name);
			System.out.println(doc2name);

			List doc1terms = getTermList(doc1name, 20);
			List doc2terms = getTermList(doc2name, 20);

			System.out.println("Termvector of text 1: " + doc1terms);
			System.out.println("Termvector of text 2: " + doc2terms);

			double commonterms = 0;

			if (doc1terms.size() == 0 || doc2terms.size() == 0) return sim;

			// check vectors' length and iterate over the shorter one
			if (doc1terms.size() > doc2terms.size()) {
				List temp = doc1terms;
				doc1terms = doc2terms;
				doc2terms = temp;
			}


			for (int i = 0; i < doc1terms.size(); i++) {
				String curTerm = (String) doc1terms.get(i);

				for (int j = 0; j < doc2terms.size(); j++) {

					String curTerm2 = (String) doc2terms.get(j);

					if (curTerm2.equals(curTerm)) {
						commonterms++;

					}

				}
			}


			sim = ((2 * commonterms) / (doc1terms.size() + doc2terms.size()));
		}

		return sim;
	}


	//Pagerank calculation
	List calculatePageRanks(List termlist, float[][] curgraph) {


		List pageranks_list = new Vector();


		float[] pagerank = new float[termlist.size()];
		float d = 0.85f;

		for (int i = 0; i < termlist.size(); i++) {
			pagerank[i] = (1 - d);
		}


		float[] out = new float[termlist.size()];

		for (int i = 0; i < termlist.size(); i++) {
			out[i] = 0;

			for (int j = 0; j < termlist.size(); j++) {

				if (curgraph[i][j] != 0)
					out[i] = out[i] + 1; //curgraph[i][j]; //+1

			}


		}


		for (int sz = 0; sz < 25; sz++) {

			for (int j = 0; j < termlist.size(); ++j) {


				float prj = 0;


				for (int i = 0; i < termlist.size(); i++) {

					if (curgraph[i][j] != 0) {

						prj = prj + (((pagerank[i] * curgraph[i][j]) / out[i]));

					}


				}


				pagerank[j] = (1 - d) + d * prj;


			}
			;

		}
		; //for sz


		float sum = 0;

		float maxpr = 0;
		int maxi = 0;

		for (int i = 0; i < termlist.size(); i++) {
			if (pagerank[i] > maxpr) {
				maxpr = pagerank[i];
				maxi = i;
			}

			sum = sum + pagerank[i];
		}


		for (int i = 0; i < termlist.size(); i++) {

			float value = 0;
			value = pagerank[i] / pagerank[maxi];

			Word curWord = new Word((String) termlist.get(i), 0);
			curWord.setSig(value);

			pageranks_list.add(curWord);

		}

		Collections.sort(pageranks_list);

		return pageranks_list;

	}


	//HITS calculation
	List[] calculateHITS(List termlist, float[][] curgraph) {


		List hits_hubs_list = new Vector();
		List hits_auths_list = new Vector();
		float[] hits_hubs = new float[termlist.size()];
		float[] hits_auths = new float[termlist.size()];


		for (int i = 0; i < termlist.size(); i++) {
			hits_auths[i] = 0.15f;
			hits_hubs[i] = 0.15f;
		}


		//Main loop
		for (int step = 0; step < 50; step++) {
			float norm = 0;

			//Authorities
			for (int j = 0; j < termlist.size(); ++j) {

				for (int i = 0; i < termlist.size(); i++) {

					if (curgraph[i][j] != 0) {
						hits_auths[j] = hits_auths[j] + (hits_hubs[i] * curgraph[i][j]);

					}


				}

				norm = norm + (hits_auths[j] * hits_auths[j]);

			}
			;

			norm = ((Double) Math.sqrt(norm)).floatValue();

			for (int j = 0; j < termlist.size(); ++j) {
				hits_auths[j] = hits_auths[j] / norm;

			}

			norm = 0;


			//Hubs
			for (int j = 0; j < termlist.size(); ++j) {

				for (int i = 0; i < termlist.size(); i++) {

					if (curgraph[j][i] != 0) {

						hits_hubs[j] = hits_hubs[j] + (hits_auths[i] * curgraph[j][i]);

					}


				}

				norm = norm + (hits_hubs[j] * hits_hubs[j]);

			}
			;


			norm = ((Double) Math.sqrt(norm)).floatValue();

			for (int j = 0; j < termlist.size(); ++j) {
				hits_hubs[j] = hits_hubs[j] / norm;

			}


		}
		; //for sz


		for (int i = 0; i < termlist.size(); i++) {

			float value = 0;

			value = hits_auths[i];


			Word curWord = new Word((String) termlist.get(i), 0);
			curWord.setSig(value);

			hits_auths_list.add(curWord);


		}


		for (int i = 0; i < termlist.size(); i++) {

			float value = 0;
			value = hits_hubs[i];


			Word curWord = new Word((String) termlist.get(i), 0);
			curWord.setSig(value);

			hits_hubs_list.add(curWord);

		}


		Collections.sort(hits_auths_list);
		Collections.sort(hits_hubs_list);

		List[] returnlist = new List[2];
		returnlist[0] = hits_auths_list;
		returnlist[1] = hits_hubs_list;


		return returnlist;
	}


	public int getLanguage(File satzFile) {

		String alltext = "";
		String lineorig;

		try {
			FileInputStream fin = new FileInputStream(satzFile);


			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));

			while ((lineorig = myInput.readLine()) != null) {

				alltext = alltext + lineorig;

			}

			myInput.close();
			fin.close();
		} catch (Exception ex) {
		}


		// get the lanikernel-object
		LanIKernel lk = null;
		try {
			lk = LanIKernel.getInstance();
		} catch (DataSourceException e) {

		}

		// fill a request object
		Request req = null;
		try {
			// new request object
			req = new Request();
			// setting up the sentence/data
			req.setSentence(alltext);
			// dont collect useless information about the evaluation
			req.setReduce(true);
			// evaluate only log(datalength>=30) words for better speed
			req.setWordsToCheck(Math.max((int) Math.sqrt(alltext.length()), 30));
		} catch (RequestException e1) {

		}


		// evaluate the request
		Response res = null;
		try {
			// the evaluation call itself
			res = lk.evaluate(req);
		} catch (Exception e2) {

		}

		// search the winner language
		HashMap tempmap = res.getResult();
		double val = 0.0;
		String key = null, winner = "REST";
		for (Iterator iter = tempmap.keySet().iterator(); iter.hasNext(); ) {
			key = (String) iter.next();
			if (((Double) tempmap.get(key)).doubleValue() > val) {
				winner = key;
				val = ((Double) tempmap.get(key)).doubleValue();
			}
		}


		int languagevalue = 1;
		if (winner.equalsIgnoreCase("de")) {
			languagevalue = Parameters.DE;
			System.out.println("Language is DE\n");
		} else if (winner.equalsIgnoreCase("en")) {
			languagevalue = Parameters.EN;
			System.out.println("Language is EN\n");
		} else if (winner.equalsIgnoreCase("REST")) {

			languagevalue = Parameters.EN;
			System.out.println("Cannot determine language, taking EN\n");

		}


		return languagevalue;
	}


	private void cleanDir(File d) {

		if (d.isDirectory()) {
			File[] files = d.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
	}


	public void writeLinesToFile(String filename,
								 String[] linesToWrite,
								 boolean appendToFile) {

		PrintWriter pw = null;

		try {

			if (appendToFile) {

				//If the file already exists, start writing at the end of it.
				pw = new PrintWriter(new FileWriter(filename, true));

			} else {

				pw = new PrintWriter(new FileWriter(filename));


			}

			for (int i = 0; i < linesToWrite.length; i++) {

				pw.println(linesToWrite[i]);

			}
			pw.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			//Close the PrintWriter
			if (pw != null)
				pw.close();

		}
	}


	public static void main(String[] args) {


		TextProcessing textprocessing = new TextProcessing("data"); //base directory: data or download

		int mode = 0;  // mode=0 analyse single files,  mode=1 analyse corpus (to create local co-occurrence graph db)

		if (mode == 0) { //Analyse single documents using current Neo4j graph database created using mode 1

			long start = System.currentTimeMillis();

			textprocessing.preprocessing(mode);

			textprocessing.analysis();

			//Extra: Calculate document similarity
			System.out.println("Similarity between document 1 and 2 is: " + textprocessing.docsim(0, 1));


			long end = System.currentTimeMillis();

			System.out.println("File processing took " + (end - start) / 1000 + " seconds.");

		} else if (mode == 1) {  //Create Neo4j graph database only

			long start = System.currentTimeMillis();

			textprocessing.preprocessing(mode);
			textprocessing.createDB();

			long end = System.currentTimeMillis();

			System.out.println("Processing took " + (end - start) / 1000 + " seconds.");

		} else if (mode == 2) {    //Create index files for single documents only

			long start = System.currentTimeMillis();

			textprocessing.preprocessing(0);
			textprocessing.createIndexes();


			long end = System.currentTimeMillis();

			System.out.println("Processing took " + (end - start) / 1000 + " seconds.");

		} else if (mode == 3) { //Create both: Neo4j graph database and index files for single documents

			long start = System.currentTimeMillis();

			textprocessing.preprocessing(1);
			textprocessing.createDB();

			textprocessing.preprocessing(0);
			textprocessing.createIndexes();

			long end = System.currentTimeMillis();

			System.out.println("Processing took " + (end - start) / 1000 + " seconds.");

		} else if (mode == 4) {  //Test Search functions: Centroid-based search and Boolean Search

			//manual test query
			Vector query = new Vector();
			query.add("Skandal");
			query.add("Motor");
			query.add("Abgas");

			//automatic test query
			// Vector query = textprocessing.generateQuery(3, "politik", 0, 10);
			textprocessing.searchIndex(query);


		} else if (mode == 5) {  //Test mode for various tasks

			// Cooccs mycooccs = new Cooccs();
			// String teststr = textprocessing.selectTermFromTopic("politik");
			// System.out.println("Selected term: " + teststr);

			//mycooccs.getShortestPathWeightInDatabase("Gehalt","Geld");
			//Paths (1840)--[IS_CONNECTED,161832]-->(20040)--[IS_CONNECTED,161929]-->(12164)--[IS_CONNECTED,161940]-->(2200)<--[IS_CONNECTED,10432]--(1261)--[IS_CONNECTED,8298]-->(1604) weight:48.5083663778567
			//Length of shortest path: 5
			//Weight of shortest path: 48.5083663778567
			//Betrug->Entschädigungszahlung->Kreditwirtschaft->Sparkasse->Bank->Geld

			//Paths (4210)<--[IS_CONNECTED,254325]--(2171)<--[IS_CONNECTED,179099]--(491)<--[IS_CONNECTED,58910]--(1604) weight:51.05117686804598
			//Length of shortest path: 3
			//Weight of shortest path: 51.05117686804598
			//Gehalt->Falle->Altersvorsorge->Geld
     		  
     		  
     		  /*
     		  Vector terms = mycooccs.getAllNodes();
     		  
     		  System.out.println("Terms: " + terms.toString());
     		  
     		  
     		  for (int i=0; i<terms.size(); i++) {
     		  
     			String term =  terms.get(i).toString(); 
     			  
     			System.out.println("Term: " + term);  
     			  
     		  HashMap neighbours = mycooccs.getAllNeighbours(term);
     		  
     		 // System.out.println("Neighbours: " + neighbours.toString());
     		  
     		  HashMap neighbourssorted = textprocessing.sortByValuesDec(neighbours); 
     		  
     		 // System.out.println("Neighbours sorted: " + neighbourssorted.toString());
     		  
     		  String string2print = term + " ";
     		  
     		  int count = 0;
     		  
     		  
     		  Iterator it = neighbourssorted.entrySet().iterator();
 			    while (it.hasNext()) {
 			  
 			    	Map.Entry pair = (Map.Entry)it.next();
 			        count ++;
 			    	
 			        String key = (String)pair.getKey();
 			        //System.out.println(key);
 			        
 			        if (count<21) string2print+=key+" ";
 			    }
 			    
 			    string2print = string2print.substring(0, string2print.length() - 1);
 			    
 			    string2print+=".";
 			    
 			   // System.out.println(string2print);
     		  
 			    textprocessing.writeLinesToFile(outputDirPath+"artificialsentences.txt", new String[] {""+string2print }, true);

 			    
     		  }*/


		}


	}


}

