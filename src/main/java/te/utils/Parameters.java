package te.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import te.indexer.Indexer;

public class Parameters {

    // ---------------------------------- constants

    /**
     * constant for likelihood ratio
     */
    public static final int LR = 0;

    /**
     * constant for frequency ratio
     */
    public static final int HQ = 1;

    /**
     * constant for language modeling
     */
    public static final int TFIDF = 2;

    /**
     * constant for language German
     */
    public static final int DE = 0;

    /**
     * constant for language English
     */
    public static final int EN = 1;

    /**
     * constant for TNT-Tagger
     */
    public static final int TNT = 0;

    /**
     * constant for BaseTagger
     */
    public static final int BT = 1;

    /**
     * constant for QTag-Tagger
     */
    public static final int QT = 2;

    /**
     * constant for not using a tagger
     */
    public static final int NOTAGGER = 3;

    /**
     * constant for reference access file
     */
    public static final int FILE = 0;

    /**
     * constant for reference access RAM:
     */
    public static final int RAM = 1;

    /**
     * constant for config file name:
     */
    public static String configFile = "config/config.xml";

    // ---------------------------------- attributes

 private int language = DE;

    protected int tagger = TNT;

    protected int minFreq = 2;

    protected float minSig = 60;

    protected int corpusMinFreq = 1;

    protected int minFreqMorphemes = 3;

    protected int minFreqPhrases = 2;

    protected int minFreqCategories = 3;

    protected int sigFormula = LR;

    protected int referenceAccess = FILE;

    protected boolean stemming = true;

    protected boolean fullText = false;

    protected boolean rescale = true;

    private List posPatterns = new Vector();

    /**
     * prefix to find the qtag resources
     */
    protected  String QTAGDATA  = null;

    private static final String qTagPath = "resources/qtag";
    private static final String qTagPrefix = "qtag-";

   	private static String tagMapFileTNTDE = "resources/mappings/TNTDE.map";
   	private static String tagMapFileTNTEN = "resources/mappings/TNTEN.map";
   	private static String tagMapFileBTDE = "resources/mappings/BTDE.map";
   	private static String tagMapFileBTEN = "resources/mappings/BTEN.map";
   	private static String tagMapFileQTDE = "resources/mappings/QTDE.map";
   	private static String tagMapFileQTEN = "resources/mappings/QTEN.map";


    /**
     * Constructor that reads parameters from a config file
     *
     * @param options
     * @throws Exception
     */
    public Parameters( boolean readConfig ) {

        if ( readConfig ) {
            // open config file:
            SAXBuilder saxBuilder = null;
            Document configDoc = null;
            try {
                // build XML tree:
                saxBuilder = new SAXBuilder();
                configDoc = saxBuilder.build( configFile );
            } catch ( Exception e ) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // save values in a Map:
            Map tagNamesMap = new HashMap();
            List confElementList = configDoc.getRootElement().getChildren();
            Iterator iterator = confElementList.iterator();
            while ( iterator.hasNext() ) {
                Element child = (Element)iterator.next();
                tagNamesMap.put( child.getName(), child.getTextTrim() );
            }

            // now set required values:
	    // language:
            String languageStr = (String)tagNamesMap.get( "language" );
            if ( languageStr.equals( "de" ) )
                language = DE;
            else if ( languageStr.equals( "en" ) ) language = EN;

	    // tagger:
            String taggerStr = (String)tagNamesMap.get( "tagger" );
            if ( taggerStr == null || taggerStr.equals( "tnt" ) )
                tagger = TNT;
            else if ( taggerStr.equals( "basetagger" ) )
                tagger = BT;
            else if ( taggerStr.equals( "qtag" ) )
		tagger = QT;
            else if ( taggerStr.toLowerCase().startsWith ( "notag" ) )
                tagger = NOTAGGER;

            String qtagprefix = (String)tagNamesMap.get("qtagdata");
            if (qtagprefix != null && ! qtagprefix.equals("")) QTAGDATA = qtagprefix;

	    // thresholds:
            minFreq = Integer.parseInt( (String)tagNamesMap.get( "minFreq" ) );
            minFreqMorphemes = Integer.parseInt( (String)tagNamesMap
                    .get( "minFreqMorphemes" ) );
            minFreqPhrases = Integer.parseInt( (String)tagNamesMap
                    .get( "minFreqPhrases" ) );
            minSig = Float.parseFloat( (String)tagNamesMap.get( "minSig" ) );
            corpusMinFreq = Integer.parseInt( (String)tagNamesMap
                    .get( "corpusMinFreq" ) );
            minFreqCategories = Integer.parseInt( (String)tagNamesMap
                    .get( "minFreqCategories" ) );
            if ( ((String)tagNamesMap.get( "stemming" )).equals( "no" ) )
                stemming = false;
            else if ( ((String)tagNamesMap.get( "fullText" )).equals( "yes" ) )
                fullText = true;
            else if ( ((String)tagNamesMap.get( "rescale" )).equals( "no" ) )
                rescale = false;

	    // formula:
            String sigFormulaStr = (String)tagNamesMap.get( "sigFormula" );
            if ( sigFormulaStr.equals( "LR" ) )
                sigFormula = LR;
            else if ( sigFormulaStr.equals( "HQ" ) ) sigFormula = HQ;
	    else if ( sigFormulaStr.equals( "TFIDF" ) ) sigFormula = TFIDF;

            // access to data:
	    String referenceAccessStr = (String)tagNamesMap
                    .get( "referenceAccess" );
            if ( referenceAccessStr.equals( "file" ) )
                referenceAccess = FILE;
            else if ( referenceAccessStr.equals( "ram" ) )
                referenceAccess = RAM;

            // POS patterns for multiword detection
	    Element pos = configDoc.getRootElement().getChild( "posPatterns" );
            List patterns = pos.getChildren();
            posPatterns = new Vector();
            for ( Iterator i = patterns.iterator(); i.hasNext(); ) {
                posPatterns.add( ((Element)i.next()).getTextTrim() );
            }
        }

    }


    // --------------------------------- set-methods
    /**
     * Set method for the language. Example call: setLanguage(Parameters.EN)
     *
     * @param lang
     *            an integer constant (appropiate constants are defined in the
     *            Parameters class)
     */
    public void setLanguage( int lang ) {
    	
    	//System.out.println("in Parameters Language auf " + lang);
    	
        language = lang;
    }


    /**
     * Set method for the tagger. Example call: setTagger(Parameters.BT)
     *
     * @param tagg
     *            an integer constant (appropriate constants are defined in the
     *            Parameters class)
     */
    public void setTagger( int tagg ) {
        tagger = tagg;
    }


    /**
     * Set method for frequency threshold
     *
     * @param mf
     *            the frequency threshold
     */
    public void setMinFreq( int mf ) {
        minFreq = mf;
    }


    /**
     * Set method for significance threshold
     *
     * @param ms
     *            the significance threshold
     */
    public void setMinSig( float ms ) {
        minSig = ms;
    }


    /**
     * Set method for corpus frequency threshold
     *
     * @param cmf
     *            the corpus freq. threshold
     */
    public void setCorpusMinFreq( int cmf ) {
        corpusMinFreq = cmf;
    }


    /**
     * Set method for reference access. Example call:
     * setReferenceAccess(Parameters.FILE)
     *
     * @param ra
     *            the access mode to be set (appropiate constants are defined in
     *            the Parameters class)
     */
    public void setReferenceAccess( int ra ) {
        referenceAccess = ra;
    }


    /**
     * Set method for the significance measure to use. Example call:
     * setSigFormula(Parameters.LR)
     *
     * @param sf
     *            an integer constant for the sig measure to use. (appropiate
     *            constants are defined in the Parameters class)
     */
    public void setSigFormula( int sf ) {
        sigFormula = sf;
    }


    /**
     * Set method for the freqeuncy threshold for lexical morphemes.
     *
     * @param mfm
     *            an integer describing the threshold
     */
    public void setMinFreqMorphemes( int mfm ) {
        minFreqMorphemes = mfm;
    }


    /**
     * Set method for the freqeuncy threshold for (Dornseiff) categories.
     *
     * @param mfc
     *            an integer describing the threshold
     */
    public void setMinFreqCategories( int mfc ) {
        minFreqCategories = mfc;
    }


    /**
     * Set method for the freqeuncy threshold for phrases.
     *
     * @param mfp
     *            an integer describing the threshold
     */
    public void setMinFreqPhrases( int mfp ) {
        minFreqPhrases = mfp;
    }


    /**
     * Set method for the POS patterns for phrase extraction.
     *
     * @param patterns
     *            a List of Strings describing the patterns to search for
     */
    public void setPOSPatterns( List patterns ) {
        posPatterns = patterns;
    }


    /**
     * Set method for the stemming parameter
     *
     * @param s
     *            a boolean indicating whether stemming should be performed or
     *            not
     */
    public void setStemming( boolean s ) {
        stemming = s;
    }


    /**
     * Set method for the fullText parameter
     *
     * @param f
     *            a boolean indicating whether sigs should be calculated also
     *            for words that do NOT surpass the frequency threshold
     */
    public void setFullText( boolean f ) {
        fullText = f;
    }


    /**
     * Set method for the rescale parameter
     *
     * @param r
     *            a boolean indicating whether sig values of unknown words
     *            should be rescaled
     */
    public void setRescale( boolean r ) {
        rescale = r;
    }


    // ------------------------------------ get-methods
    public int getLanguage() {
        return language;
    }


    public int getTagger() {
        return tagger;
    }


    public int getMinFreq() {
        return minFreq;
    }


    public float getMinSig() {
        return minSig;
    }


    public int getReferenceAccess() {
        return referenceAccess;
    }


    public int getSigFormula() {
        return sigFormula;
    }


    public int getMinFreqMorphemes() {
        return minFreqMorphemes;
    }


    public int getMinFreqCategories() {
        return minFreqCategories;
    }


    public int getMinFreqPhrases() {
        return minFreqPhrases;
    }


    public List getPOSPatterns() {
        return posPatterns;
    }


    public int getCorpusMinFreq() {
        return corpusMinFreq;
    }


    public boolean getStemming() {
        return stemming;
    }


    public boolean getFullText() {
        return fullText;
    }


    public boolean getRescale() {
        return rescale;
    }

    public String getQtagPrefix(){
    	//use user setting, if available
    	if (this.QTAGDATA != null)
    		return QTAGDATA;

    	//if there is no user setting, create the it from base dir, qTag path and prefix
    	File f = new File(Indexer.getBaseDir(), qTagPath);

    	StringBuffer buffer = new StringBuffer();
    	buffer.append(f.toString());
    	buffer.append("/");
    	buffer.append(qTagPrefix);
    	return buffer.toString();
    }

    /**
     * For a given tagger, returns the  tag map file
     */
    public File getTagmapfile() {
    	return getTagmapfile(getTagger(), getLanguage());
    }

    /**
     * For a given tagger, returns the  tag map file
     */
    public static File getTagmapfile(int tagger, int language){
    	String file;

    	if (tagger == TNT && language == DE) file = tagMapFileTNTDE;
    	else if (tagger == TNT && language == EN) file = tagMapFileTNTEN;
    	else if (tagger == BT  && language == DE) file = tagMapFileBTDE;
    	else if (tagger == BT  && language == EN) file = tagMapFileBTEN;
    	else if (tagger == QT  && language == DE) file = tagMapFileQTDE;
    	else if (tagger == QT  && language == EN) file = tagMapFileQTEN;
        else if (tagger == NOTAGGER  && language == DE) file = tagMapFileQTDE;
        else if (tagger == NOTAGGER  && language == EN) file = tagMapFileQTEN;
    	else file = tagMapFileTNTDE;

    	return new File(Indexer.getBaseDir(), file);
    }

    public String toString(){

	StringBuffer nl = new StringBuffer("\n");
	StringBuffer retSB = new StringBuffer();

	retSB.append("language=");
	switch( language ){
	case DE:{ retSB.append("DE");break; }
	case EN:{ retSB.append("EN");break; }
	}
	retSB.append(nl);

	retSB.append("tagger=");
	switch( tagger ){
	case TNT:{ retSB.append("TNT");break; }
	case BT:{ retSB.append("BT");break; }
	case QT:{ retSB.append("QT");break; }
	}
	retSB.append(nl);

	retSB.append("sigFormula=");
	switch( sigFormula ){
	case LR:{ retSB.append("LR");break; }
	case HQ:{ retSB.append("HQ");break; }
	}
	retSB.append(nl);

	retSB.append("referenceAccess=");
	switch( referenceAccess ){
	case FILE:{ retSB.append("FILE");break; }
	case RAM:{ retSB.append("RAM");break; }
	}
	retSB.append(nl);

	retSB.append("minFreq=");
	retSB.append(minFreq);
	retSB.append(nl);

	retSB.append("minSig=");
	retSB.append(minSig);
	retSB.append(nl);

	retSB.append("corpusMinFreq=");
	retSB.append(corpusMinFreq);
	retSB.append(nl);

	retSB.append("minFreqMorphemes=");
	retSB.append(minFreqMorphemes);
	retSB.append(nl);

	retSB.append("minFreqPhrases=");
	retSB.append(minFreqPhrases);
	retSB.append(nl);

	retSB.append("minFreqCategories=");
	retSB.append(minFreqCategories);
	retSB.append(nl);

	retSB.append("stemming=");
	retSB.append(stemming);
	retSB.append(nl);

	retSB.append("fullText=");
	retSB.append(fullText);
	retSB.append(nl);

	retSB.append("rescale=");
	retSB.append(rescale);
	retSB.append(nl);

	retSB.append("posPatterns=");
	retSB.append(posPatterns.toString());
	retSB.append(nl);

	retSB.append("QTAGDATA=");
	retSB.append(getQtagPrefix());
	retSB.append(nl);

	return retSB.toString();
    }
}
