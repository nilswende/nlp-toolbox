package te.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import te.indexer.Word;

public class PhraseExtractor{

	protected Map index;
	private static PhraseExtractor instance;
	Set candidates;
	
	public static final int TYPES = 0;
	public static final int TOKENS = 1;
	
	/**
	* PhraseExtractor is a singleton: this method can be used to retrieve the one and only instance of it
	* @param i an index mapping words to their positions in the text
	* @param lemmata a List of stemmed Words with POS tags
	* @param patterns a list of POS patterns to extract from the list of lemmata.
	*/
	public static PhraseExtractor getInstance(Map i, List lemmata, List patterns, Parameters parameters){
		//TODO does it really has to be a singleton?
		if(instance == null) instance = new PhraseExtractor(i, lemmata, patterns, parameters);
		return instance;
	}
	
	/**
	* This method retrieves all phrase candidates from this instance (i.e. phrases that match patterns
	* that have previously been extracted when calling getInstance()
	* @return a Set of candidate phrases (Words)
	*/
	public Set getCandidates(){
		return candidates;
	}
	
	/**
	* This method determines the frequency of a given phrase within the current text using a word index
	* @param curPhrase the phrase of which to determine the frequency
	* @return the frequency as an int
	*/
	public int getFreq(Word curPhrase){
	
		int count = 0;
		
		try {
		// get the parts of the phrase:
		Vector parts = curPhrase.getParts();
		
		// now get the positions of the first element of the phrase:
		Set pos1 = (Set)index.get(parts.elementAt(0));
		
		if(parts.size() == 1) return pos1.size();

		// determine frequency by chekcing whether the other parts occur at subsequent positions:
		for(Iterator i=pos1.iterator(); i.hasNext(); ){
			int curPos = ((Integer)i.next()).intValue();
			
			// check whether subsequent positions are OK:
			boolean allThere = true;
			for(int j=1; j<parts.size(); j++){
				Integer check = new Integer(curPos + j);
				
				// get the set of positions the j-th part of the candidate occurs at
				Set posJ = (Set)index.get(parts.elementAt(j));
				if(!posJ.contains(check))allThere = false;
			}
			if(allThere)count++;
		}
		
		} catch (Exception np ) { System.out.println("Getphrasefreq nullpointer abgefangen"); }
		
		
		return count;
	}
	
	/**
	* Method that can be used to reset PhraseExtractor (for starting a new text)
	* PhraseExtractor should only be used as a singleton within one text, i.e. when starting with a new
	* input text, this method should be called!
	*/
	public static void reset(){
		instance = null;
	}
	
	/**
	* Method that retrieves words that are part of many different phrase candidates
	* @param minFreq the minimum number of phrases a word must be part of in order to be returned
	* @param typesOrTokens indicates whether phrases should be counted as types or tokens
	* @return a List of Words that match these criteria
	*/
	public List getPhraseParts(int minFreq, int typesOrTokens){
		
		List ret = new Vector();
		Map counts = new HashMap();
		for(Iterator i=candidates.iterator(); i.hasNext(); ){
			
			// iterate over all parts of the current phrase candidates and count them:
			Word curPhrase = (Word)i.next();
			int curFreq = getFreq(curPhrase);
			if(curFreq > 0){
				Vector curParts = (curPhrase).getParts();
				for(Iterator j=curParts.iterator(); j.hasNext(); ){
					String curPart = (String)j.next();
					
					if(!counts.containsKey(curPart)){
						
						// different behaviour for types and tokens:
						if(typesOrTokens == TYPES){
							counts.put(curPart, new Integer(1));
						}
						if(typesOrTokens == TOKENS){
							counts.put(curPart, new Integer(curFreq));
						}
						
					}
					else{
						Integer oldValue = (Integer)counts.get(curPart);
						Integer newValue;
						if(typesOrTokens == TYPES){
							newValue = new Integer(oldValue.intValue() + 1);
						}
						else{
							newValue = new Integer(oldValue.intValue() + curFreq);
						}
						counts.put(curPart, newValue);
					}
				}
			}
		}
		
		// now return all words with the minimum frequency:
		Set keys = counts.keySet();
		for(Iterator k=keys.iterator(); k.hasNext(); ){
			String curStr = (String)k.next();
			int curFreq = ((Integer)counts.get(curStr)).intValue();
			if(curFreq > minFreq){
				Word w = new Word(curStr, curFreq);
				w.setSig(curFreq);
				ret.add(w);
			}
		}
		
		Collections.sort(ret);
		return ret;
	}
	
	
	// ---------------------------------- private methods: -----------------------------------
	
	/**
	* Constructor: it is private because this class is a singleton
	* @param i an index mapping words to their positions in the text
	* @param lemmata a List of stemmed Words with POS tags
	* @param patterns a list of POS patterns to extract from the list of lemmata.
	*/
	private PhraseExtractor(Map i, List lemmata, List patterns, Parameters parameters){
		index = i;
		
		candidates = new HashSet();
		for(Iterator j=patterns.iterator(); j.hasNext(); ){
			String pattern = (String)j.next();
			candidates.addAll(getCandidates(lemmata, pattern, parameters));
		}
	}
	
	/**
	* This method returns a set of candidates that match a certain POS pattern
	* @param lemmata a list of tagged and lemmatized words
	* @param pattern a String that describes the POS pattern to search for
	* @return a Set of phrases, in the form of Word objects
	*/
	private Set getCandidates(List lemmata, String pattern, Parameters parameters) throws IllegalArgumentException{
		HashSet ret = new HashSet();
		
		// initialise the POS converter machine...
		POSTagConverter converter = null;
		try{
			POSTagConverterManager posConvMan = POSTagConverterManager.getInstance();
			converter = posConvMan.getConverter(parameters.getTagger());
		}
		catch(POSTagConverterException e){e.printStackTrace();}
		
		StringTokenizer st = new StringTokenizer(pattern, " ");
		
		// put parts of the pattern into an array:
		Vector parts = new Vector();
		pattern = "";
		while(st.hasMoreTokens()){
			String curTag = converter.getConversion(st.nextToken());
			//String curTag = mapTag(st.nextToken());
			parts.add(curTag);
			pattern += curTag + " ";
		}
		pattern = pattern.trim();
		
		
		// now we have to build an automata for our pattern: 
		int state = 0;
		int pos = 0;
		String curCandidate = "";
		
		for(Iterator i=lemmata.iterator(); i.hasNext(); ){
		
			Word curWord = (Word)i.next();
			String curPOS = curWord.getPos();
			
			// state 1: we have found the first part of the pattern:
			if(state == 1){
				
				// if we find next parts of the phrase:
				if(curPOS.startsWith((String)parts.elementAt(pos))){
				
					// collect all the information for this phrase:
					curCandidate += " " + curWord.getWordStr();
					
					// if we have found the last part: store phrase candidate:
					if(pos == parts.size()-1){
						Word phrase = new Word(curCandidate, pattern);
						Vector p = new Vector();
						StringTokenizer s = new StringTokenizer(curCandidate, " ");
						while(s.hasMoreTokens()){
							p.add(s.nextToken());
						}
						phrase.setParts(p);
					
						
						// return this phrase as a candidate
						ret.add(phrase);
					
						// finally determine which state to jump to:
						if(curPOS.startsWith((String)parts.elementAt(0))){
							state=1;
							curCandidate = curWord.getWordStr();
							pos = 1;
						}
						else {
							state = 0;
							pos = 0;
							curCandidate = "";
						}
					}
					
					else{
						pos++;
					}
				}
				else{
					if(curPOS.startsWith((String)parts.elementAt(0))){
						state = 1;
						curCandidate = curWord.getWordStr();
						pos = 1;
					}
					else{
						state = 0;
						pos = 0;
						curCandidate = "";
					}
				}
			}
			
			// state 0: we have not found anything so far (starting state):
			if(state == 0){
				if(curPOS.startsWith((String)parts.elementAt(0))){
					state = 1;
					curCandidate = curWord.getWordStr();
					pos = 1;
					
					// exception for patterns of length 1:
					if(parts.size() < 2){
						Word w = new Word(curCandidate, pattern);
						Vector v = new Vector();
						v.add(curCandidate);
						w.setParts(v);
						ret.add(w);
						state = 0;
					}
				}
			}
		}
		
		
		return ret;	
		
	}
	
//	private String mapTag(String tag){
//		
//		if(tag.equals("N"))return "NN";
//		if(tag.equals("A")){
//			if(p.getLanguage() == Parameters.EN){
//				return "JJ";
//			}
//			if(p.getLanguage() == Parameters.DE){
//				return "ADJA";
//			}
//		}
//		
//		return tag;
//	}
}