package te.indexer;

import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;
import te.utils.BaseTaggerConvertIterator;
import te.utils.Dekompost;
import te.utils.DornseiffLookup;
import te.utils.ExternalData;
import te.utils.KnowledgeBase;
import te.utils.NoTagger;
import te.utils.POSTagConverterException;
import te.utils.POSTagConverterManager;
import te.utils.Parameters;
import te.utils.PhraseExtractor;
import te.utils.Porter;
import te.utils.QTagIterator;
import te.utils.Stemmer;
import te.utils.TNTInterface;
import te.utils.TNTIteratorException;
import te.utils.TNTRunTimeException;
import te.utils.TermString;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Text
{

    protected List lemmata;
    protected List types;
    protected int length;
    protected Parameters parameters;
    protected Map index;
    protected Iterator tagged;
    
    
    String red = "./resources/trees/grfExt.tree";

    String forw = "./resources/trees/kompVVic.tree";

    String back = "./resources/trees/kompVHic.tree";
    
    

    public Text(String t, Parameters p)
        throws TNTRunTimeException, TNTIteratorException, POSTagConverterException
    {
    	
        index = null;
        parameters = p;
        tagged = getTagIterator(t);
        PhraseExtractor.reset();
        lemmata = lemmatize(p.getLanguage());
        types = countLemmata();
    }

    public List getWords()
    {
        return differentialAnalysis(); 
    }

    public List getTypes()
    {
        return types;
    }

    public int getDocLength()
    {
        return length;
    }

    public List getLexicalMorphemes()
    {
        if(parameters.getLanguage() == 0)
        {
            Map morphemes = getMorphemes(0);
            return getMorphemeCounts(morphemes);
        } else
        {
            return getPhraseParts(0);
        }
    }

    public List getFrequentMorphemes()
    {
        if(parameters.getLanguage() == 0)
        {
            Map morphemes = getMorphemes(1);
            return getMorphemeCounts(morphemes);
        } else
        {
            return getPhraseParts(1);
        }
    }

    public Set getPhrases()
    {
        Set ret = new HashSet();
        PhraseExtractor.reset();
        PhraseExtractor pe = PhraseExtractor.getInstance(index, lemmata, parameters.getPOSPatterns(), parameters);
        Set candidates = pe.getCandidates();
        for(Iterator j = candidates.iterator(); j.hasNext();)
        {
            Word curPhrase = (Word)j.next();
            int curFreq = pe.getFreq(curPhrase);
            curPhrase.setFreq(curFreq);
            curPhrase.setSig(curFreq);
            if(curFreq >= parameters.getMinFreqPhrases())
            {
                ret.add(curPhrase);
            }
        }

        return ret;
    }

    public List getCategories()
    {
        if(parameters.getLanguage() == 0)
        {
            DornseiffLookup dl = DornseiffLookup.getInstance();
            return dl.getCategories(types, parameters);
        } else
        {
            return new Vector();
        }
    }

    private List lemmatize(int lang)
    {
    	
    	Zerleger2 zer = new Zerleger2();
		   zer.init(forw, back, red);
    	
        List l = new Vector();
        ExternalData ed = ExternalData.getInstance(lang);
        Set st = ed.getStopWordMap();
        int j = 0;
        index = new HashMap();
        String lastFour[] = new String[4];
        if(tagged != null)
        {
            while(tagged.hasNext()) 
            {
                Word curWord = (Word)tagged.next();
                String curString = curWord.getWordStr();
                lastFour[0] = lastFour[1];
                lastFour[1] = lastFour[2];
                lastFour[2] = lastFour[3];
                lastFour[3] = curString;
                String lFour = (new StringBuilder(String.valueOf(lastFour[0]))).append(lastFour[1]).append(lastFour[2]).append(lastFour[3]).toString();
                if(lFour.equalsIgnoreCase("u.s."))
                {
                    Word us = new Word(lFour.toLowerCase(), "NN");
                    l.add(us);
                }
                String curPOS = curWord.getPos();
                String lemma = "";
                if(!parameters.getStemming())
                {
                    lemma = curString;
                } else
                {
                    if(lang == 1)
                    {
                        Porter port = new Porter();
                        if(parameters.getStemming())
                        {
                            lemma = port.stem(curString.toLowerCase());
                        } else
                        {
                            lemma = curString.toLowerCase();
                        }
                    }
                    if(lang == 0)
                    {
                        Stemmer stemmer = Stemmer.getInstance();
                        if(parameters.getStemming())
                        {
                            lemma = zer.grundFormReduktion(curString);  //stemmer.lemmatize(curString, curPOS);
                        } else
                        {
                            lemma = curString;
                        }
                    }
                }
                List curLemmas = TermString.crackInWords(lemma);   //   .getTerms(lemma);
                for(Iterator i = curLemmas.iterator(); i.hasNext();)
                {
                    String curLemma = (String)i.next();
                    Set curPositions;
                    if(index.containsKey(curLemma))
                    {
                        curPositions = (Set)(Set)index.get(curLemma);
                    } else
                    {
                        curPositions = new HashSet();
                    }
                    curPositions.add(new Integer(j));
                    index.put(curLemma, curPositions);
                    j++;
                    if(curLemma != null)
                    {
                        length++;
                        if(!st.contains(curLemma.toLowerCase()))
                        {
                            Word newWord = new Word(curLemma, curPOS);
                            l.add(newWord);
                        }
                    }
                }

            }
        }
        return l;
    }

    private List countLemmata()
    {
        HashMap counts = new HashMap();
        HashMap pos = new HashMap();
        for(Iterator i = lemmata.iterator(); i.hasNext();)
        {
            Word curWord = (Word)i.next();
            String curPOS = curWord.getPos();
            String curLemma = curWord.getWordStr();
            if(!counts.containsKey(curLemma))
            {
                counts.put(curLemma, new Integer(1));
                List allPOS = new Vector();
                allPOS.add(curPOS);
                pos.put(curLemma, allPOS);
            } else
            {
                Integer curValue = (Integer)counts.get(curLemma);
                Integer newValue = new Integer(1 + curValue.intValue());
                counts.put(curLemma, newValue);
                List allPOS = (List)(List)pos.get(curLemma);
                allPOS.add(curPOS);
                pos.put(curLemma, allPOS);
            }
        }

        List counted = new Vector();
        Word w;
        for(Iterator j = counts.keySet().iterator(); j.hasNext(); counted.add(w))
        {
            String lemma = (String)j.next();
            int freq = ((Integer)counts.get(lemma)).intValue();
            String curPOS = getMostFreq((List)(List)pos.get(lemma));
            w = new Word(lemma, freq);
            w.setPOS(curPOS);
        }

        return counted;
    }

    private List differentialAnalysis()
    {
        List ret = new Vector();
        KnowledgeBase kb = KnowledgeBase.getInstance(parameters.getLanguage(), parameters.getReferenceAccess(), parameters.getStemming());
        int minFreq = parameters.getMinFreq();
        float minSig = parameters.getMinSig();
        int sigFormula = parameters.getSigFormula();
        for(int i = 0; i < types.size(); i++)
        {
            Word curWord = (Word)types.get(i);
            int curFreq = curWord.getFreq();
            if(curFreq >= minFreq || parameters.getFullText())
            {
                double relFreq = (double)curFreq / (double)length;
                double corpusRelFreq = 0.0D;
                int corpusLength = 0;
                int corpusFreq = 0;
                if(!parameters.getStemming() && parameters.getLanguage() == 0)
                {
                    corpusFreq = kb.getCorpusFreq(curWord.getWordStr());
                } else
                {
                    corpusFreq = kb.getCorpusFreq(curWord.getWordStr().toLowerCase());
                }
                if(corpusFreq == -1)
                {
                    corpusFreq = 1;
                }
                if(parameters.getLanguage() == 1)
                {
                    corpusLength = KnowledgeBase.EN_LENGTH;
                } else
                if(parameters.getLanguage() == 0)
                {
                    corpusLength = KnowledgeBase.DE_LENGTH;
                }
                corpusRelFreq = (double)corpusFreq / (double)corpusLength;
                if(corpusFreq >= parameters.getCorpusMinFreq() || corpusFreq == -1)
                {
                    double sig = 0.0D;
                    if(corpusFreq == -1)
                    {
                        sig = -1D;
                    } else
                    {
                        if(sigFormula == 0)
                        {
                            sig = lr(curFreq, corpusFreq, length, corpusLength);
                        }
                        if(sigFormula == 1)
                        	
                        	
                        {
                        	
                        	
                        
                        	
                        	
                            sig = relFreq / corpusRelFreq; //curFreq
                        }
                        if(sigFormula == 2)
                        {
                            sig = tfidf(curFreq, corpusFreq, length, corpusLength);
                        }
                    }
                    curWord.setSig(sig);
                    if((sig >= (double)minSig || sig == -1D) && curFreq >= minFreq)
                    {
                        ret.add(curWord);
                    }
                }
            }
        }

        Collections.sort(ret);
        return ret;
    }


    
    private List differentialAnalysisOhneCorpora()
    {
        List ret = new Vector();
      //  KnowledgeBase kb = KnowledgeBase.getInstance(parameters.getLanguage(), parameters.getReferenceAccess(), parameters.getStemming());
        int minFreq = parameters.getMinFreq();
        float minSig = parameters.getMinSig();
        int sigFormula = parameters.getSigFormula();
        for(int i = 0; i < types.size(); i++)
        {
            Word curWord = (Word)types.get(i);

                    curWord.setSig(60.0);
                   
                        ret.add(curWord);
        
            }        

        Collections.sort(ret);
        return ret;
    }

    
    
    
    
    private void rescaleUnknowns(List terms)
    {
        Map sigForFreq = new HashMap();
        TreeSet freqs = new TreeSet();
        for(Iterator i = terms.iterator(); i.hasNext();)
        {
            Word curWord = (Word)i.next();
            double curSig = curWord.getSig();
            Integer curFreq = new Integer(curWord.getFreq());
            if(curSig > 0.0D)
            {
                if(sigForFreq.containsKey(curFreq))
                {
                    double oldSig = ((Double)sigForFreq.get(curFreq)).doubleValue();
                    if(curSig > oldSig)
                    {
                        sigForFreq.put(curFreq, new Double(curSig));
                    }
                } else
                {
                    sigForFreq.put(curFreq, new Double(curSig));
                }
                freqs.add(curFreq);
            } else
            {
                double sig = 0.0D;
                if(freqs.contains(curFreq))
                {
                    sig = ((Double)sigForFreq.get(curFreq)).doubleValue();
                } else
                {
                    Set tail = (Set)freqs.tailSet(curFreq);
                    Iterator j = tail.iterator();
                    Iterator k = freqs.iterator();
                    Integer freq = new Integer(0);
                    if(k.hasNext())
                    {
                        freq = (Integer)k.next();
                    }
                    if(j.hasNext())
                    {
                        freq = (Integer)j.next();
                    }
                    if(sigForFreq.containsKey(freq))
                    {
                        sig = ((Double)sigForFreq.get(freq)).doubleValue();
                    }
                }
                curWord.setSig(sig);
            }
        }

    }

    private List getPhraseParts(int typesOrTokens)
    {
        PhraseExtractor pe = PhraseExtractor.getInstance(index, lemmata, parameters.getPOSPatterns(), parameters);
        List phraseParts = pe.getPhraseParts(parameters.getMinFreqMorphemes(), typesOrTokens);
        return phraseParts;
    }

    private Map getMorphemes(int typesOrTokens)
    {
        Map morphemes = new HashMap();
        List nounPat = new Vector();
        nounPat.add("N");
        PhraseExtractor.reset();
        PhraseExtractor pe = PhraseExtractor.getInstance(index, lemmata, nounPat, parameters);
        Set nouns = pe.getCandidates();
        Dekompost dekompost = Dekompost.getInstance();
        for(Iterator i = nouns.iterator(); i.hasNext();)
        {
            Word curWord = (Word)i.next();
            String curStr = curWord.getWordStr();
            int curFreq = pe.getFreq(curWord);
            Vector parts = dekompost.dekompost(curStr);
            if(parts.size() > 1)
            {
                for(Iterator j = parts.iterator(); j.hasNext();)
                {
                    String curPart = ((String)j.next()).toLowerCase();
                    if(!morphemes.containsKey(curPart))
                    {
                        if(typesOrTokens == 0)
                        {
                            morphemes.put(curPart, new Integer(1));
                        } else
                        {
                            morphemes.put(curPart, new Integer(curFreq));
                        }
                    } else
                    {
                        Integer curValue = (Integer)morphemes.get(curPart);
                        Integer newValue;
                        if(typesOrTokens == 0)
                        {
                            newValue = new Integer(1 + curValue.intValue());
                        } else
                        {
                            newValue = new Integer(curFreq + curValue.intValue());
                        }
                        morphemes.put(curPart, newValue);
                    }
                }

            }
        }

        return morphemes;
    }

    private List getMorphemeCounts(Map morphemes)
    {
        List counted = new Vector();
        ExternalData ed = ExternalData.getInstance(0);
        Set stopMorphemes = ed.getStopMorphemes();
        for(Iterator k = morphemes.keySet().iterator(); k.hasNext();)
        {
            String morpheme = (String)k.next();
            int freq = ((Integer)morphemes.get(morpheme)).intValue();
            if(freq >= parameters.getMinFreqMorphemes() && !stopMorphemes.contains(morpheme))
            {
                Word w = new Word(morpheme, freq);
                w.setSig(freq);
                counted.add(w);
            }
        }

        Collections.sort(counted);
        return counted;
    }

    private double lr(int textFreq, int corpusFreq, int docLength, int corpusLength)
    {
        double k1 = textFreq;
        double k2 = corpusFreq;
        double n1 = docLength;
        double n2 = corpusLength;
        double p = (k1 + k2) / (n1 + n2);
        double p1 = k1 / n1;
        double p2 = k2 / n2;
        double ret = 0.0D;
        if(p1 > p2)
        {
            ret = 2D * ((log_L(p1, k1, n1) + log_L(p2, k2, n2)) - log_L(p, k1, n1) - log_L(p, k2, n2));
        }
        return ret;
    }

    private double log_L(double p, double k, double n)
    {
        double ret = k * Math.log(p) + (n - k) * Math.log(1.0D - p);
        return ret;
    }

    private double lm(int textFreq, int corpusFreq, int docLength, int corpusLength)
    {
        double tf = textFreq;
        double dl = docLength;
        double pc = (double)corpusFreq / (double)corpusLength;
        double mu = 2000D;
        double ret = Math.log((mu / (dl + mu)) * (1.0D + tf / (mu * pc)));

        
        //double ret=((double)textFreq/(double)docLength)*Math.log(corpusLength/(1+corpusFreq));
        

        return ret; //ret;
    }

    
    private double tfidf(int textFreq, int corpusFreq, int docLength, int corpusLength)
    {
        

        
        double ret=((double)textFreq/(double)docLength)*Math.log(corpusLength/(1+corpusFreq));
        

        return ret; //ret;
    }
    
    
    private Iterator getTagIterator(String text)
        throws POSTagConverterException
    {
        if(text == null || text.equals(""))
        {
            return null;
        }
        POSTagConverterManager posConvMan = POSTagConverterManager.getInstance();
        Iterator it = null;
        switch(parameters.getTagger())
        {
        case 0: // '\0'
            posConvMan.setTagfileForTagger("TNT", parameters.getTagmapfile());
            it = (new TNTInterface(text, parameters.getLanguage())).getIter();
            break;

        case 1: // '\001'
            posConvMan.setTagfileForTagger("BT", parameters.getTagmapfile());
            it = new BaseTaggerConvertIterator(text, parameters.getLanguage());
            break;

        case 2: // '\002'
            posConvMan.setTagfileForTagger("QT", parameters.getTagmapfile());
            it = new QTagIterator(text, parameters);
            break;

        case 3: // '\003'
            posConvMan.setTagfileForTagger("QT", parameters.getTagmapfile());
            it = new NoTagger(text, parameters);
            break;
        }
        return it;
    }

    private String getMostFreq(List allPOS)
    {
        Map tmp = new HashMap();
        for(Iterator i = allPOS.iterator(); i.hasNext();)
        {
            String curPOS = (String)i.next();
            if(!tmp.containsKey(curPOS))
            {
                tmp.put(curPOS, new Integer(1));
            } else
            {
                int oldFreq = ((Integer)tmp.get(curPOS)).intValue();
                tmp.put(curPOS, new Integer(oldFreq + 1));
            }
        }

        List tmp2 = new Vector();
        Set keys = tmp.keySet();
        Word w;
        for(Iterator j = keys.iterator(); j.hasNext(); tmp2.add(w))
        {
            String curPOS = (String)j.next();
            int curFreq = ((Integer)tmp.get(curPOS)).intValue();
            w = new Word(curPOS, 0);
            w.setSig(curFreq);
        }

        Collections.sort(tmp2);
        return ((Word)tmp2.get(0)).getWordStr();
    }
}
