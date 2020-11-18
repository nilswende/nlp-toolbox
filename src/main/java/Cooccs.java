
import java.util.*;
import java.io.*;
import java.lang.*;

import te.indexer.*;
import te.utils.*;
//import textMine.GraphDB.Labels;
//import textMine.GraphDB.RelationshipTypes;
//import textMine.GraphDB.Labels;
//import Labels;
//import RelationshipTypes;
import JLanI.kernel.DataSourceException;
import JLanI.kernel.LanIKernel;
import JLanI.kernel.Request;
import JLanI.kernel.RequestException;
import JLanI.kernel.Response;

import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;
import de.uni_leipzig.asv.toolbox.viterbitagger.Tagger;
import de.uni_leipzig.asv.utils.Pretree;

import org.json.JSONArray;      // JSON library from http://www.json.org/java/
import org.json.JSONObject;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.*;
//import org.neo4j.kernel.Traversal;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;


public class Cooccs{

	// this map contains a mapping term->Map::String->Double, i.e. each term has a number of co-occurring items
	// associated with it each of which is in turn associated with a co-occurrence frequency.
	private Map cooccs;
	
	// maps words to their frequency:
	private Map frequencies;
	
	// number of sentences in the corpus:
	private int n;
	
	private int run=0;

	private String initialquery = "";
	
	private Set expansionterms = null; 
	
	int language=0;
	
	protected List lemmata;
	protected int length;
	protected Map index = null;
	
	  //reduce file for baseform

    String redbase_en = "./resources/trees/en-nouns.tree";
    String redbase_de = "./resources/trees/de-nouns.tree";
    
    //de-nouns.tree   en-nouns.tree
    
    //reduce file for splitting

    String red = "./resources/trees/grfExt.tree";

    //forward file

    String forw = "./resources/trees/kompVVic.tree";

    //backward file

    String back = "./resources/trees/kompVHic.tree";
	
    String tmFile = "./resources/taggermodels/deTaggerModel.model"; /* deTaggerModel.model*/
    String tmFile2 = "./resources/taggermodels/english.model"; /* enTaggerModel.model*/
	
    
    
    int maximumaddedpaths = 0;  //50
	int maximumpathstoadd = 5; //number of query terms to consider for centroid calculation
	
	//for Evolving Centroids
	
	Vector distilledText = new Vector(); //contains the text to be parsed (only nouns probably)
	
	Vector currentEvolvingCentroid = new Vector(); //could be put into a separat class, too
													// 0. element: start node
													// 1. element: end node
													// 2. centroid's distance (offset) from start node 	
	
	Vector currentPath = new Vector();	//holds the current path of the currentEvolvingCentroid to the next word w_i+1
	
	Vector centroidTrail = new Vector(); //stores the sequence of all currentEvolvingCentroid(s)
	
	
	
	public Cooccs(){
		
		
	}
	
	

	
	/**
	* Constructor that will calculate co-occurrences from a file containing sentences.
	* @param satzFile The file that contains the initial corpus, one sentence per line
	*/
	public Cooccs(File satzFile, boolean usedb){
	

	    language = getLanguage(satzFile);
 
        Indexer ind = new Indexer();
 		ind.setLanguage(language);
		        
		cooccs = new HashMap();
		frequencies = new HashMap();


		Pretree pretree = new Pretree();
		
		if (language==0) {
      	  pretree.load(redbase_de);
			} else {
				pretree.load(redbase_en);
			}
       
       Zerleger2 zer = new Zerleger2();
       zer.init(forw, back, red);

      // File outputDir = new File ("output/");	 
      // cleanDir(outputDir);
       
       Properties props = new Properties();

       String tmDir = new File(tmFile).getParent();
       
      
		try {
			if (language==0) {
		props.load(new FileInputStream(tmFile));
		} else {
			props.load(new FileInputStream(tmFile2));

		}
		} catch (Exception filenotfound) {}
		
        
        System.out.println("Prop: " + props.getProperty("taglist"));
        
        Tagger tagger = new Tagger(tmDir+"/"+props.getProperty("taglist"), 

                   tmDir+"/"+props.getProperty("lexicon"), 

                   tmDir+"/"+props.getProperty("transitions"),null, false);
        
        
       
        tagger.setExtern(true);

        tagger.setReplaceNumbers( false /*props.getProperty("ReplaceNumbers").equals("false")*/);

        tagger.setUseInternalTok(true);
		

		
		// read satzFile sentence-wise:
		String lineorig = null;
		try {
			
			
			
			
			FileInputStream fin2 =  new FileInputStream(satzFile);
			
			String alltext="";
			
			BufferedReader myInput2 = new BufferedReader(new InputStreamReader(fin2));
						
			while ((lineorig = myInput2.readLine()) != null){
		
				//System.out.println(tagger.tagSentence(lineorig));
				alltext = alltext + lineorig;
			
			}
			
			
			boolean helpstem= ind.getParameters().getStemming();
			if (helpstem)  ind.getParameters().setStemming(false);
				
			ind.prepare(alltext);
			
			List phrases = ind.getPhrases();
			
	 		for (int i=0; i<phrases.size(); i++) {
	 			System.out.println("phrase: " +phrases.get(i));
	 			
	 			boolean lower = false;
	 			
	 			if (phrases.get(i).toString().endsWith("A N")) lower = true;
	 			
	 			if (lower) {
	 				
	 			String help = phrases.get(i).toString();
	 				
	 			help = Character.toLowerCase(help.charAt(0)) + help.substring(1);

	 			phrases.set(i, help.substring(0, help.indexOf(",") ));
	 			
	 				
	 			} else {
	 			phrases.set(i, phrases.get(i).toString().substring(0, phrases.get(i).toString().indexOf(",") ));
	 			
	 			}
	 			
	 			System.out.println("phrase 2: " +phrases.get(i));
	 		}
	 		
	 		ind.getParameters().setStemming(helpstem);
	 		
	 		phrases = new Vector();  //comment out if phrases should appear in coocc graph
	 			 		
	 		
	 	    //Term pairs that are phrases are bound together
	 		String alltext2 = "";
	 		fin2 =  new FileInputStream(satzFile);
		
			myInput2 = new BufferedReader(new InputStreamReader(fin2));
						
			while ((lineorig = myInput2.readLine()) != null){
				
				StringBuffer strbuf = new StringBuffer(lineorig);
				
				for (int i=0; i<phrases.size(); i++) {
					while (strbuf.toString()/*.toLowerCase()*/.indexOf(((String)phrases.get(i))/*.toLowerCase()*/)!=-1     ) {
						
						int k = strbuf.toString()/*.toLowerCase()*/.indexOf(((String)phrases.get(i))/*.toLowerCase()*/);
						
						String replacement= ((String)phrases.get(i))/*.toLowerCase()*/.replaceAll(" ", "##phrase");
						
						strbuf.replace(k, k+((String)phrases.get(i))/*.toLowerCase()*/.length(), replacement);
						
						
					}
					
					
		 		}
				
			 alltext2=alltext2+"\n"+strbuf;
			// System.out.println(alltext2);
			}

			
	
			
			
	
			System.out.println("CooccExtraction");
			
			//FileInputStream fin =  new FileInputStream(satzFile);
			//BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			BufferedReader myInput = new BufferedReader(new StringReader(alltext2));

			
			Vector cleanwordlist =  new Vector();
			Vector words = new Vector();
			
			while ((lineorig = myInput.readLine()) != null){
				
				cleanwordlist =  new Vector();

		
				List allwords = new Vector();

				
				lineorig = lineorig.replaceAll("\\[[0-9]+\\]", "");
				lineorig = lineorig.replaceAll("[^a-zA-Z 0-9 ä ö ü Ä Ö Ü ß | \\- ## ]", ""); //- /*{1,2}*/

				//System.out.println(lineorig);

				if (!lineorig.equals("")) {
				
				String taggedsentence = tagger.tagSentence(lineorig);

				//System.out.println(taggedsentence);
				
				String [] splittedsentence = taggedsentence.split(" ");

				for (int i=0; i<splittedsentence.length; i++) 
                {
				
					
					if ( (splittedsentence[i].indexOf("##phrase")!=-1)  ){
	              		  
						//System.out.println(splittedsentence[i]);
						
						String removefiller = ((String)splittedsentence[i]).replaceAll("##phrase", " ");
						//System.out.println(removefiller);
						
						int pos = removefiller.indexOf("|");  //start of wrong tag for the phrase
	              		  
						if (pos>0)
							if (removefiller.substring(0, pos)/*.toLowerCase()*/.length()>1)
								allwords.add(removefiller.substring(0, pos)+"|Phrase"/*.toLowerCase()*/);

	              	  } else {  //no phrase
					
					
              	  if ( (splittedsentence[i].indexOf("|NN")!=-1) || (splittedsentence[i].indexOf("|NE")!=-1) || (splittedsentence[i].indexOf("|NP")!=-1) ){

              		  
              		  int pos = splittedsentence[i].indexOf("|");
              		  
              		  if (pos>0)
              			  if (splittedsentence[i].substring(0, pos)/*.toLowerCase()*/.length()>0)
              				  if (!splittedsentence[i].substring(0, pos).toLowerCase().equals("%n%")) {
              					  
              					
              					if (splittedsentence[i].indexOf("|NP")!=-1) {
                					  
              						
              						if (!isStopWord(splittedsentence[i].substring(0, pos))) {
              						
                					  allwords.add(splittedsentence[i].substring(0, pos)+"|NP"/*.toLowerCase()*/);
              						} else {
                      				//	System.out.println("Stopword removed: "+splittedsentence[i]);
                      				}
                					  
                					} else 
              					  
              					if (splittedsentence[i].indexOf("|NE")!=-1) {
              						if (!isStopWord(splittedsentence[i].substring(0, pos))) {
                  						
              					  allwords.add(splittedsentence[i].substring(0, pos)+"|NE"/*.toLowerCase()*/);
              						} else {
                      					//System.out.println("Stopword removed: "+splittedsentence[i]);
                      				}
              						
              					} else 
              						if (splittedsentence[i].indexOf("|NN")!=-1)
              					{
              							
              						if (!isStopWord(splittedsentence[i].substring(0, pos))) {

              							 allwords.add(splittedsentence[i].substring(0, pos)+"|NN"/*.toLowerCase()*/);
                      				} else {
                      				//	System.out.println("Stopword removed: "+splittedsentence[i]);
                      				}
              						
              						
              						/*
              						String element = splittedsentence[i].substring(0, pos);	
              							
              						
              						
              						if (language == 1) {
       
              							element = element.toLowerCase();

              							if (ind.getParameters().getStemming()) {
      
              								
              									 Porter port = new Porter();
              								     
              								
              								     element = port.stem(element);
              							
              							}
              							
              							if (!isStopWord(element)) {
                      						
              							allwords.add(element+"|NN");
              							} else {
                          					System.out.println("Stopword removed: "+element);
                          				}
              							
              						} else {
              							
              							
              							if (ind.getParameters().getStemming()) {
              						      
              								element = zer.grundFormReduktion(element);
         							
              							}
              							

              							if (!isStopWord(element)) {
                      					
              							allwords.add(element+"|NN");
              							} else {
                          					System.out.println("Stopword removed: "+element);
                          				}
              							
              							
              						}*/
              						
              					}
              					
              					
              				  }
              	

              	  
              	  } //nouns 
        		/*  else*/ //find adjectives
              	/*	
                  	  if ((splittedsentence[i].indexOf("|ADJ")!=-1) || (splittedsentence[i].indexOf("|AJ")!=-1)) {
                  		  
                  		  int pos = splittedsentence[i].indexOf("|");
                  		  
                  		  if (pos>0)
                  		   if (splittedsentence[i].substring(0, pos).length()>1)
                  			 if (!splittedsentence[i].substring(0, pos).toLowerCase().equals("%n%")) {
                  				 
                  				if (!isStopWord(splittedsentence[i].substring(0, pos))) {
              						
                  				 
                  					allwords.add(splittedsentence[i].substring(0, pos).toLowerCase()+"|ADJ");
                  				} else {
                  				//	System.out.println("Stopword removed: "+splittedsentence[i]);
                  				}
                  			 }
                  	  }*//*else *///find adverbs
              		/*
                  	  if ((splittedsentence[i].indexOf("|ADV")!=-1) || (splittedsentence[i].indexOf("|AV")!=-1)) {
                  		  
                  		  int pos = splittedsentence[i].indexOf("|");
                  		  
                  		  if (pos>0)
                  		   if (splittedsentence[i].substring(0, pos).length()>1)
                  			 if (!splittedsentence[i].substring(0, pos).toLowerCase().equals("%n%")) {
                  	      
                  				if (!isStopWord(splittedsentence[i].substring(0, pos))) {
              						
                  				 allwords.add(splittedsentence[i].substring(0, pos).toLowerCase()+"|ADV");
                  	  
                  				} else {
                  					//System.out.println("Stopword removed: "+splittedsentence[i]);
                  				}
                  	      
                  			 }
                  	  }*/
   
                  	  /*else*/   //find verbs
              	  	/*
                  	  if ( (splittedsentence[i].indexOf("|VV")!=-1) || (splittedsentence[i].indexOf("|VA")!=-1) || (splittedsentence[i].indexOf("|VM")!=-1)   ){
                  		  
                  		  int pos = splittedsentence[i].indexOf("|");
                  		  
                  		  if (pos>0)
                  			  if (splittedsentence[i].substring(0, pos).length()>1)
                  				 if (!splittedsentence[i].substring(0, pos).toLowerCase().equals("%n%")) {
                  					 
                  					if (!isStopWord(splittedsentence[i].substring(0, pos))) {
                  						
                  						allwords.add(splittedsentence[i].substring(0, pos).toLowerCase()+"|V");
                  					} else {
                      				//	System.out.println("Stopword removed: "+splittedsentence[i]);
                      				}
                  					
                  				 }
                  	  
                  	  }*/
                  	  
                	}
                  
              
                } //for all terms in sentence

				
				
				
				for (int i=0; i<allwords.size(); i++) {
			
												
				String entry=(String)allwords.get(i); //curWord.getWordStr();
				int pos = entry.indexOf("|"); 
				int pos2= entry.indexOf("|Phrase");
				String entry2= entry.substring(0, pos);
				String tag = entry.substring(pos, entry.length());
				
				if ((entry2.length()>0) && (!entry2.equals(";")))
				
					if (pos2==-1) {  //no phrase
						
						if (language == 1) {
						       
							if (!tag.equals("|NP") && !tag.equals("|NE"))
							entry2 = entry2.toLowerCase();

  							if (ind.getParameters().getStemming()) {

  								
  									Porter port = new Porter();
  								     
  									if (!tag.equals("|NP") && !tag.equals("|NE"))
  									entry2 = port.stem(entry2);
  							
  							}
  							
  							if (!isStopWord(entry2)) {
          						
  								if ((entry2.length()>0) && (!entry2.equals(";")))
  								cleanwordlist.add(entry2+""+tag);
  								
              				}
  							
  						} else {
  							
  							
  							
  							if (ind.getParameters().getStemming()) {
  						      
  								if (!tag.equals("|NP") && !tag.equals("|NE"))
  								entry2 = zer.grundFormReduktion(entry2);
							
  							}
  							

  							if (!isStopWord(entry2)) {
          					
  								if ((entry2.length()>0) && (!entry2.equals(";")))
  								cleanwordlist.add(entry2+""+tag);
  								
              				}
  							
  							
  						}
						
						
						
						
					} else			
					cleanwordlist.add(entry);
	
				}
				

			//	words = adapttoStopwords(cleanwordlist);
				
			//	words = removeStopwords(words);   

				if(cleanwordlist.size() > 1){      //word
					
					for (int i=0; i<cleanwordlist.size(); i++) {
					
						String entry=(String)cleanwordlist.get(i); //curWord.getWordStr();
						
						//System.out.println("Term: " +entry);
						
						
						int pos = entry.indexOf("|"); 
						//int pos2= entry.indexOf("|Phrase");
						
						if (pos!=-1)
						entry= entry.substring(0, pos);
							
						cleanwordlist.set(i, entry);
					
					}
					
					
					
					extractCooccs(cleanwordlist);	//words
					
					if (usedb==true)
					addSentenceToCooccsDB(cleanwordlist);
					
					
					n++;
				}
				
				
				}
				
			} // for all lines
			

	 		
			
		}catch (Exception e) {e.printStackTrace();}
		

		//calcSigsLL();
		//calcSigsDICE();
		
		if (!usedb)
		calcAssociations();
		

		if (usedb==true)
		updateDiceandCosts();
		

		
	}
	
	
	
	
	
	public static void addSentenceToCooccsDB(Vector words)   //adding data to DB from a sentence
	{
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		int count,f_flag=0;
		
      //creating a database
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
        System.out.println("database opened/created");
      	try (Transaction tx = graphDB.beginTx()) 
      	{
      		System.out.println("in transaction of database");
      		
           
            	    List<String> tempS = new ArrayList<>();
            	    
            	    for(Iterator i=words.iterator(); i.hasNext(); ){
            			            			
                    	String word = (String)i.next();
                    	tempS.add(word);
                    	f_flag=0;
                    	
                    	//checking if the node is already present
                    	ResourceIterator<Node> nodelist = graphDB.findNodes( Labels.SINGLE_NODE );
            			while( nodelist.hasNext() )
            			{
            				Node wordnode = nodelist.next();
            				if(wordnode.getProperty("name").equals(word))	
            				{
            					f_flag=1;
            					count=(int)wordnode.getProperty("occur");
            					count=count+1;
            					wordnode.setProperty("occur",count);
            					System.out.println(word +" node was already present. Count updated.");
            				}
            				
            			}
            			
            			if(f_flag!=1)
            			{
            				Node wordnode = graphDB.createNode(Labels.SINGLE_NODE);
                    		wordnode.setProperty("name", word);
                    		wordnode.setProperty("occur", 1);        //no of occurrences in database
                        	System.out.println(word +" node added.");
                    	}
                    	
                    	
                    }
                   // System.out.println("Sentence OVER");
                    
                    
                    
                    
                    boolean rel_found;
                    //CONNECTING NODES_CREATING RELATIONSHIPS	
                    
                    for (int p = 0; p < tempS.size(); p++)
                    {
                    	for (int q = p+1; q < tempS.size(); q++)
                    	{
                    		
                    	if (!((String) tempS.get(p)).equals((String) tempS.get(q))) {	
                    		
                    		rel_found=false;
                    		Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", tempS.get(p));
                			Node n2 = graphDB.findNode(Labels.SINGLE_NODE,"name", tempS.get(q));
                			
                			//checking if relationship already exists
                			Iterable<Relationship> allRelationships = n1.getRelationships();
                		    for (Relationship relationship : allRelationships) 
                		    {
                		        if(n2.equals(relationship.getOtherNode(n1)))
                		        {
                		        	count=(int)relationship.getProperty("count");
                		        	count=count+1;
                		        	relationship.setProperty("count", count);
                		        	System.out.println("Relation already existed between nodes "+tempS.get(p)+" and "+tempS.get(q)+". Count updated.");
                		        	rel_found=true;
                		        	break;
                		        }
                		        	
                		    }
                			
                    		
                    		//creating new relationship
                    		if(!rel_found)
                    		{
                    	        Relationship relationship = n1.createRelationshipTo(n2, RelationshipTypes.IS_CONNECTED );
                    	        relationship.setProperty("count", 1 );
                            	relationship.setProperty("dice", 0);  //for calculating Dice ration
                              	relationship.setProperty("cost", 0);   //for Dijkstra
                    	        System.out.println("Relation inserted with nodes "+tempS.get(p)+" and "+tempS.get(q));
                    		}
                    		
                    		
                    	}
                    		
                    	}
            		}
                    
                    
            
            tx.success();
      	}
      	

      	graphDB.shutdown();
      
	}
	
	
	//this function when called, updates the Dice ratio and costs for all the relationships present in the database
		public void updateDiceandCosts()
		{
			int countA, countB, countAB;
			double dice;
			System.out.println("in update properties of relationship");
			

			String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
			File database = new File(db_path);
			
			GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
			try (Transaction tx5 = graphDB.beginTx()) 
		    {  	
				ResourceIterator<Node> nodelist = graphDB.findNodes( Labels.SINGLE_NODE );
				while(nodelist.hasNext())
				{
					Node wordnode = nodelist.next();
					//al.add((String)user.getProperty("name"));
					String node1=(String)wordnode.getProperty("name");
					countA=(int)wordnode.getProperty("occur");
					Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", node1);
					Iterable<Relationship> allRelationships = temp.getRelationships();
				    for (Relationship relationship : allRelationships) 
				    {
				       Node n2=relationship.getOtherNode(temp);
				        countB=(int)n2.getProperty("occur");
				        countAB=(int)relationship.getProperty("count");
				        
				        
				        /***********  ********/
				        
				        int helpk=0;
				        
				        if (countB<=countA) { helpk=countB; } else
							helpk=countA;
						
						if (countAB>=helpk)
							countAB=helpk;
				        
						/***************************************/			
						
						
						
				        
				        dice=(double)(2*countAB)/(countA+countB);
				        
				        if (dice>1) dice=1.0;
				        
				        relationship.setProperty("dice", dice);
				        relationship.setProperty("cost", 1/(dice+0.01));
				        
				    }
				}
				System.out.println("Update of Dice finished.");
				tx5.success();
		    }
			graphDB.shutdown();
		}
	
	
	
	//returns all nodes' details present the database
	public Vector getAllNodes()
	{
		
		Vector result = new Vector();
		ArrayList<String> key=new ArrayList<String>();
		ArrayList<Integer> value=new ArrayList<Integer>();
		System.out.println("in getallnodes");
		
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
		System.out.println("database opened for querying");
		
		
		
		LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
		try (Transaction tx2 = graphDB.beginTx()) 
	    {  	
			ResourceIterator<Node> terms = graphDB.findNodes( Labels.SINGLE_NODE );
			while(terms.hasNext())
			{
				Node term = terms.next();
				lhm.put((String)term.getProperty("name"),(int)term.getProperty("occur"));
				result.add(term.getProperty("name").toString());
			}
			tx2.success();
	    }
		//System.out.println(lhm);
		
		
		
		
		graphDB.shutdown();
		
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public boolean testQuery(Vector query, double minpathlength, double maxpathlength)
	{
		boolean result = false;
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		if (!query.isEmpty()) {
			
			if (query.size()>1) {	
				
				GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
				System.out.println("database opened for querying");
				try (Transaction tx2 = graphDB.beginTx()) 
			    {  
					
					//Check which terms are in the graph database
					Vector helpquery = new Vector();
										
					for (int i=0; i<query.size(); i++) {
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
						
						if (temp!=null) {
							helpquery.add(query.get(i).toString());
						}
						
					}
					
					if (helpquery.size()==query.size()) {
						
						
						int helpquerysize = query.size();
						
						HashSet helpqueryset = new HashSet();
						
						
						
						for (int i=0; i<query.size(); i++) {
							
							helpqueryset.add(query.get(i).toString());
							
						}
						
						
						HashMap numberofreachednodes = new HashMap();
						
							Iterator iteratorq1 = helpqueryset.iterator(); 

							   while (iteratorq1.hasNext()){
								   
								   String queryterm = iteratorq1.next().toString();
								   
								   Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm);
								   
								   
								   Iterator iteratorq2 = helpqueryset.iterator(); 

									   while (iteratorq2.hasNext()){
										   String queryterm2 = iteratorq2.next().toString(); 
										   
										   if (!queryterm.equals(queryterm2)) {
											   Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm2);
											   
											   PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), 100, 1 );

												Path p = finder.findSinglePath( temp, temp2 );
											
												if (p!=null) {
												
													if (numberofreachednodes.containsKey(queryterm)) {
														
														HashSet helpset = (HashSet)numberofreachednodes.get(queryterm);
														helpset.add(queryterm2);
														numberofreachednodes.put(queryterm, helpset);
														
													} else {
														
														HashSet helpset= new HashSet();
														helpset.add(queryterm2);
														numberofreachednodes.put(queryterm, helpset);
													}
													
													
													
												}
											   
										   }
										   
									   }
								   }
							   
							   System.out.println("Number of reached nodes: " +numberofreachednodes.toString());;
							   
							   String mostreachableterm = "";
							   int numberofneighbours = 0;
							   
							   Iterator iteratorq3 = numberofreachednodes.keySet().iterator();
							   while (iteratorq3.hasNext()){
								   String queryterm = iteratorq3.next().toString(); 
								   
								   HashSet helphashset = (HashSet) numberofreachednodes.get(queryterm);
								   
								   if (helphashset.size()>numberofneighbours) {
									   
									   numberofneighbours = helphashset.size();
									   mostreachableterm = queryterm;
								   }
							   }
							
							   
							   
							   HashSet helphashset = (HashSet) numberofreachednodes.get(mostreachableterm);
							   
							   if (helphashset!=null) {
								   helphashset.add(mostreachableterm);
	
								   helpqueryset = helphashset;
							
								   if (helpqueryset.size()==query.size()) {
									   //result = true;
									   
									   
									   
									   //longest shortest path between the terms
									   
									   
									   double largestdistanceofqueryterms = 0;
										double largestpathlength = 0;
										
										for (int i=0; i<query.size(); i++) {
											
											for (int j=i+1; j<query.size(); j++) {
												
												Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
												Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(j).toString());
												

										    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


													WeightedPath p = finder.findSinglePath( temp, temp2 );
											
													if (p!=null)
														if (p.weight()>largestdistanceofqueryterms) {
															largestpathlength = p.length();
															largestdistanceofqueryterms=p.weight();
														}
												
											}
											
										}
										
										
										System.out.println("largestdistanceofqueryterms: " +largestdistanceofqueryterms + "  " +largestpathlength);
										
									   
									   if ((largestdistanceofqueryterms>=minpathlength) && (largestdistanceofqueryterms<maxpathlength)) {
										   
										   result = true;
										   
									   }
									   
									   
									   
									   
									   
									   
								   }
								   

								   
								   
							   }
												   
					} 
					
	
					
				tx2.success();
			    }
				graphDB.shutdown();
			}
		}
		
		return result;
	}
	
	
	
	public Vector singleSpreadingActivation(Vector query, int maxactivationsteps)
	{
		
		Vector result = new Vector();
		
		System.out.println("singleSpreadingActivation " +query.size());
		
		
		int originalquerysize = query.size();
		String centroid = "";
		double shortestaveragepathlength = Double.MAX_VALUE;
		double timeelapsed = 0.0;	
		
		
		Vector termcolors = new Vector();
		HashMap node2colors = new HashMap();
		
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		if (!query.isEmpty()) {
			
			if (query.size()==1) {	
				
				GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
				System.out.println("database opened for querying");
				try (Transaction tx2 = graphDB.beginTx()) 
			    {  
									
					//Check which terms are in the graph database
					Vector helpquery = new Vector();
										
					for (int i=0; i<query.size(); i++) {
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
						
						if (temp!=null) {
							helpquery.add(query.get(i).toString());
						}
						
					}
					
					query = helpquery;
					System.out.println("Helpquery size: " +query.size() + "  "+query);;
					
					
					if ((query.size()==1)) {
					int count = 1;
					
					
					System.out.println("count: " +count );
					
					
					
					termcolors = new Vector();
					node2colors = new HashMap();	
					
					
					int color = 0;
					
					for(Iterator i=query.iterator(); i.hasNext(); ){
						
						color++;
						termcolors.add(color);
											
						String curQueryTerm = i.next().toString();
						
						HashSet helpset = new HashSet();
						helpset.add(color);
						
						node2colors.put(curQueryTerm, helpset);
						//System.out.println("Query Term: " + curQueryTerm + " Color: " + color);
						
					}
				
				
				
				
				
				
				for(Iterator i=query.iterator(); i.hasNext(); ){
					
					
					String curQueryTerm = i.next().toString();
					
					HashSet visited = new HashSet();
					LinkedList<String> queue = new LinkedList<String>();
					
					visited.add(curQueryTerm);
					queue.add(curQueryTerm);
					
					Node firstsourcenode = graphDB.findNode(Labels.SINGLE_NODE,"name", curQueryTerm);
					//System.out.println("firstsourcenode: "+ curQueryTerm);

					int steps=0;

					
					HashMap node2step = new HashMap();
					node2step.put(curQueryTerm, steps);
					
					
					while ((queue.size()!=0)) {

						//steps++;

						String sourcenodename = queue.poll();
						System.out.println("polling " + sourcenodename +  " step: " + node2step.get(sourcenodename));
						
						
						int step = (int)node2step.get(sourcenodename);
						
			
						if (step<maxactivationsteps) {
						//System.out.println("Activating: "+ sourcenodename);
												
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", sourcenodename);
						Iterable<Relationship> allRelationships = temp.getRelationships();
					    for (Relationship relationship : allRelationships) 
					    {
					       Node destinationnode=relationship.getOtherNode(temp);
					       
					       String destinationnodename =  destinationnode.getProperty("name").toString();
					       
					       if (!visited.contains(destinationnodename)) {
					    	   
					    	   
					    	  // PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


							//	WeightedPath p = finder.findSinglePath( firstsourcenode, destinationnode );
				
					    	  // if (p!=null)
					    	   //if ( p.weight() < (maxpathlength+1/* /2.0*/)  /* /2 evtl. */) {
					    		//   if ( steps < 4)  {
					    		   
					    		   visited.add(destinationnodename);
					    		   queue.add(destinationnodename);
					    		   node2step.put(destinationnodename, step+1);
					    	  // }
					    	   
					    	   /*visited.add(destinationnodename);
				    		   queue.add(destinationnodename);
					    	   */
				    		   
					    	   
					    	   
					    	   
					    	   
					       }
					       
					       
					       
					    } //for Relationships
						
						}
						
					
						
						
					} //while queue
					
				
					
					
					//HashSet help = (HashSet)node2colors.get(curQueryTerm);
					
					Iterator iterator = visited.iterator(); 
				      
					   // check values
					   while (iterator.hasNext()){
						   
						   String nodename = iterator.next().toString();
						  // System.out.println("Value: "+ nodename+ " ");  
						   
						   if (node2colors.containsKey(nodename)) {
							   
							   HashSet helpset =  (HashSet)node2colors.get(nodename);
							   
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
							   
							   node2colors.put(nodename, helpset);
							   
						   } else {
							   
							   HashSet helpset = new HashSet();
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
	
							   node2colors.put(nodename, helpset);
							   
							   
						   }
					   
					   
					   }
					
					  // System.out.println("Value: "+ node2colors.get(curQueryTerm).toString()); 
					  // System.out.println("Value: "+ node2colors.get("Grenzwert").toString());
					
					
					
					
				} //for all query terms
				
				
					
					
					
			    } // if querysize==
				   
				tx2.success();
			    }
				graphDB.shutdown();
				
				
			}
		}
		
		System.out.println("node2colors: " + node2colors.size() );
		
		Iterator nodeiterator = node2colors.keySet().iterator();
		
		while (nodeiterator.hasNext()) {
		   String curterm = nodeiterator.next().toString();
		    
		   if(!curterm.equals(query.get(0).toString())) {
			   result.add(curterm);
			   
		   }
			   
		   
		}
		
		
		return result;
	}
	
	
	
	
	public Vector singleSpreadingActivationSlow(Vector query, double minpathlength, double maxpathlength)
	{
		
		Vector result = new Vector();
		
		System.out.println("singleSpreadingActivationSlow " +query.size());
		
		
		int originalquerysize = query.size();
		String centroid = "";
		double shortestaveragepathlength = Double.MAX_VALUE;
		double timeelapsed = 0.0;	
		
		
		Vector termcolors = new Vector();
		HashMap node2colors = new HashMap();
		
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		if (!query.isEmpty()) {
			
			if (query.size()==1) {	
				
				GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
				System.out.println("database opened for querying");
				try (Transaction tx2 = graphDB.beginTx()) 
			    {  
				
				
				//query cleaning !!!!
					
					
					//Check which terms are in the graph database
					Vector helpquery = new Vector();
										
					for (int i=0; i<query.size(); i++) {
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
						
						if (temp!=null) {
							helpquery.add(query.get(i).toString());
						}
						
					}
					
					query = helpquery;
					System.out.println("Helpquery size: " +query.size() + "  "+query);;
					
					
					if ((query.size()==1)) {
					int count = 1;
					
					
					System.out.println("count: " +count );
					
					
					
					termcolors = new Vector();
					node2colors = new HashMap();	
					
					
					int color = 0;
					
					for(Iterator i=query.iterator(); i.hasNext(); ){
						
						color++;
						termcolors.add(color);
											
						String curQueryTerm = i.next().toString();
						
						HashSet helpset = new HashSet();
						helpset.add(color);
						
						node2colors.put(curQueryTerm, helpset);
						//System.out.println("Query Term: " + curQueryTerm + " Color: " + color);
						
					}
				
				
				
				
				
				
				for(Iterator i=query.iterator(); i.hasNext(); ){
					
					
					String curQueryTerm = i.next().toString();
					
					HashSet visited = new HashSet();
					HashSet visitedinrange = new HashSet();
					LinkedList<String> queue = new LinkedList<String>();
					
					visited.add(curQueryTerm);
					visitedinrange.add(curQueryTerm);
					queue.add(curQueryTerm);
					
					Node firstsourcenode = graphDB.findNode(Labels.SINGLE_NODE,"name", curQueryTerm);
					//System.out.println("firstsourcenode: "+ curQueryTerm);

					int steps=0;
					
					while ((queue.size()!=0)) {
						
						steps++;
						
						String sourcenodename = queue.poll();
						//System.out.println("Activating: "+ sourcenodename);
												
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", sourcenodename);
						Iterable<Relationship> allRelationships = temp.getRelationships();
					    for (Relationship relationship : allRelationships) 
					    {
					       Node destinationnode=relationship.getOtherNode(temp);
					       
					       String destinationnodename =  destinationnode.getProperty("name").toString();
					       
					       if (!visited.contains(destinationnodename)) {
					    	   
					    	   
					    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


								WeightedPath p = finder.findSinglePath( firstsourcenode, destinationnode );
				
					    	   if (p!=null)
					    	   if ( p.weight() < (maxpathlength/* /2.0*/)  /* /2 evtl. */) {
					    		  
					    		   
					    		   visited.add(destinationnodename);
					    		   
					    		   if ( p.weight() >= (minpathlength/* /2.0*/)  /* /2 evtl. */)
					    		   visitedinrange.add(destinationnodename);
					    		   
					    		   queue.add(destinationnodename);
					    		   
					    	   }
   
					    	   
					       }
					       
					       
					       
					    }
						
						
						
					
						
						
					} //while queue
					
				
					
					
					//HashSet help = (HashSet)node2colors.get(curQueryTerm);
					
					Iterator iterator = visited.iterator(); //visitedinrange
				      
					   // check values
					   while (iterator.hasNext()){
						   
						   String nodename = iterator.next().toString();
						  // System.out.println("Value: "+ nodename+ " ");  
						   
						   if (node2colors.containsKey(nodename)) {
							   
							   HashSet helpset =  (HashSet)node2colors.get(nodename);
							   
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
							   
							   node2colors.put(nodename, helpset);
							   
						   } else {
							   
							   HashSet helpset = new HashSet();
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
	
							   node2colors.put(nodename, helpset);
							   
							   
						   }
					   
					   
					   }
					
					  // System.out.println("Value: "+ node2colors.get(curQueryTerm).toString()); 
					  // System.out.println("Value: "+ node2colors.get("Grenzwert").toString());
	
					
				} //for all query terms
				
	
					
			    } // if querysize==
				   
				tx2.success();
			    }
				graphDB.shutdown();
				
				
			}
		}
		
		System.out.println("node2colors: " + node2colors.size() );
		
		Iterator nodeiterator = node2colors.keySet().iterator();
		
		while (nodeiterator.hasNext()) {
		   String curterm = nodeiterator.next().toString();
		    
		   if(!curterm.equals(query.get(0).toString())) {
			   result.add(curterm);
			   
		   }
			   
		   
		}
		
		
		return result;
	}
	
	public Vector getEvolvingCentroidTrail() {
		
		return centroidTrail;
	}
	
	
	public Vector getEvolvingCentroid(HashSet mostfrequentterms) {
		
		System.out.println("\n\nIn getEvolvingCentroid");
		
		
		currentEvolvingCentroid = new Vector();
		/*
		distilledText.clear();
		distilledText.add("####");
		distilledText.add("Manipulation");
		distilledText.add("Abgas-Wert");
		distilledText.add("Merkel");
		distilledText.add("Merkel");
		distilledText.add("Merkel");*/
		
		
		/*distilledText.clear();
		distilledText.add("####");
		distilledText.add("Merkel");
		distilledText.add("Merkel");
		distilledText.add("Pimpiglove");
		*/
		int existingwords = 0;
		
		for (int i = 1; i< distilledText.size(); i++) {
			
			System.out.println("\n\nReading next word: " +distilledText.get(i).toString() );
		
			if (existingwords<25)
			if (mostfrequentterms.contains(distilledText.get(i).toString())) {
				System.out.println("This is a frequent word." );
				
			
			if (checkNodeExists(distilledText.get(i).toString())) {
				existingwords++;
				System.out.println("Existing words read: " +existingwords );
			
			if (existingwords==1) { //first real word read is first centroid
				currentEvolvingCentroid.add(distilledText.get(i).toString());
				currentEvolvingCentroid.add(distilledText.get(i).toString());
				currentEvolvingCentroid.add(new Double(0.0).doubleValue());
				
				centroidTrail.add(currentEvolvingCentroid);
				
				//getPositionDistance(currentEvolvingCentroid, currentEvolvingCentroid);
				
			} else {
		
			
			if ((i)<distilledText.size()) {
				
			Vector nextWord = new Vector();
			nextWord.add(distilledText.get(i).toString());
			nextWord.add(distilledText.get(i).toString());
			nextWord.add(new Double(0.0).doubleValue());
			
			System.out.println("Calculate shortest path: " +currentEvolvingCentroid.toString() + " " + nextWord.toString() );;
			
			double currentdistance = getPositionDistance(currentEvolvingCentroid, nextWord);
			
			if (currentdistance<Double.MAX_VALUE) {
			
			//double delta = (currentdistance/(i));
			double delta = (currentdistance/(Math.sqrt(existingwords))); //existing relations?
			
			
			System.out.println("Delta " +delta  + "		s: " + (currentPath.size()-1));;
			
			
			int k=-1;
			int s=currentPath.size()-1;
			
			
			System.out.println("Current Path " +currentPath.toString());;
			
			//current centroid is on a specific node e.g. the first word read
			if (currentEvolvingCentroid.get(0).toString().equals(currentEvolvingCentroid.get(1)) && (((double)currentEvolvingCentroid.get(2))==0.0) ) {
				
				System.out.println("current centroid is on a specific node (no offset)");;
				
				if (nextWord.get(0).toString().equals(currentEvolvingCentroid.get(0).toString())) {
					
					// new word read is the same as the current centroid term (position)
					// nothing to do, new centroid is current centroid
					centroidTrail.add(currentEvolvingCentroid);
					
					System.out.println("new word read is the same as the current centroid term (position)");;
					
				} else {
					
					// new word read is not the current centroid term
					
					// determine, if these two nodes are direct neighbours
					if (currentPath.get(1).toString().equals(nextWord.get(0).toString())) {
						
						// yes, new word read is current centroid term's neighbour
						
						System.out.println("word read is current centroid term's neighbour");;
					
						
						double newoffset = ((double)currentEvolvingCentroid.get(2)) + delta;
						
						Vector nextEvolvingCentroid = new Vector();
						nextEvolvingCentroid.add(currentEvolvingCentroid.get(0).toString());
						nextEvolvingCentroid.add(nextWord.get(0).toString());		
						nextEvolvingCentroid.add(newoffset);
						
						
						currentEvolvingCentroid=nextEvolvingCentroid;
						centroidTrail.add(currentEvolvingCentroid);
						
						
						
					} else {
						
						// no, new word read is not current centroid term's neighbour
						
						// intermediate nodes are between the current centroid term and the new word read 
						
						System.out.println("word read is NOT current centroid term's neighbour");;
						
						double helpdelta = delta;
						boolean limitkfound = false;
						
						
						for (k=1; k<currentPath.size(); k++) {
							
							System.out.println("reading next word from current path " + currentPath.get(k).toString());;
							
											
							if (limitkfound == false) {
								
								Vector helpvec = new Vector();
								helpvec.add(currentPath.get(k).toString());
								helpvec.add(currentPath.get(k).toString());
								helpvec.add(new Double(0.0).doubleValue());
								
								double helpdist = getPositionDistance2(currentEvolvingCentroid, helpvec);
								System.out.println("distance from current centroid to next word from current path: " +helpdist);;
									
								
							if (helpdist >= delta) {
								
								System.out.println("helpdist >= delta");;
								
								
								limitkfound=true;
								
								
									if (k>0) {
										
										System.out.println("k>0");;
										
										System.out.println("k>0");;
										
										Vector helpvec2 = new Vector();
										helpvec2.add(currentPath.get(k-1).toString());
										helpvec2.add(currentPath.get(k-1).toString());
										helpvec2.add(new Double(0.0).doubleValue());
										
										double helpdist2 = getPositionDistance2(currentEvolvingCentroid, helpvec2);
										
										
										double newoffset = delta-helpdist2;
										
										Vector nextEvolvingCentroid = new Vector();
										nextEvolvingCentroid.add(currentPath.get(k-1).toString());
										nextEvolvingCentroid.add(currentPath.get(k).toString());		
										nextEvolvingCentroid.add(newoffset);
										
										
										currentEvolvingCentroid=nextEvolvingCentroid;
										centroidTrail.add(currentEvolvingCentroid);
										
										
										
										
									} // k>0
								
								
							} // helpdist >= delta
							
						} // limitkfound == false
							
							
						} //parse current path
						
						

					}
					
					
				}
					
				
			} else {
				
				//current centroid is not on a specific node (with offset)
				
				System.out.println("current centroid is NOT on a specific node (with offset)");;
				
				//check distance from current centroid position to next node on shortest path to next read word
				
				double helpdelta = delta;
				boolean limitkfound = false;
				
				
				for (k=0; k<currentPath.size(); k++) {
					
					System.out.println("reading next word from current path " + currentPath.get(k).toString());;
					
									
					if (limitkfound == false) {
						
						Vector helpvec = new Vector();
						helpvec.add(currentPath.get(k).toString());
						helpvec.add(currentPath.get(k).toString());
						helpvec.add(new Double(0.0).doubleValue());
						
						double helpdist = getPositionDistance2(currentEvolvingCentroid, helpvec);
						System.out.println("distance from current centroid to next word from current path: " +helpdist);;
							
						
					if (helpdist >= delta) {
						
						System.out.println("helpdist >= delta");;
						
						
						limitkfound=true;
						
						if (k==0) {
							System.out.println("k==0");;
							//case a)
							if (currentPath.get(k).toString().equals(currentEvolvingCentroid.get(0).toString())) {
								System.out.println("case a)");;
								
								double newoffset = ((double)currentEvolvingCentroid.get(2))-delta;
								
								Vector nextEvolvingCentroid = new Vector();
								nextEvolvingCentroid.add(currentEvolvingCentroid.get(0).toString());
								nextEvolvingCentroid.add(currentEvolvingCentroid.get(1).toString());		
								nextEvolvingCentroid.add(newoffset);
								
								
								currentEvolvingCentroid=nextEvolvingCentroid;
								centroidTrail.add(currentEvolvingCentroid);
	
							} else  //case b)
								if (currentPath.get(k).toString().equals(currentEvolvingCentroid.get(1).toString())) {
									System.out.println("case b)");;
									
									double newoffset = ((double)currentEvolvingCentroid.get(2))+delta;
									
									Vector nextEvolvingCentroid = new Vector();
									nextEvolvingCentroid.add(currentEvolvingCentroid.get(0).toString());
									nextEvolvingCentroid.add(currentEvolvingCentroid.get(1).toString());		
									nextEvolvingCentroid.add(newoffset);
									
									
									currentEvolvingCentroid=nextEvolvingCentroid;
									centroidTrail.add(currentEvolvingCentroid);
									
								}
							
							
							
							
						} //k==0
						else
							if (k>0) {
								
								System.out.println("k>0");;
						
								Vector helpvec2 = new Vector();
								helpvec2.add(currentPath.get(k-1).toString());
								helpvec2.add(currentPath.get(k-1).toString());
								helpvec2.add(new Double(0.0).doubleValue());
								
								double helpdist2 = getPositionDistance2(currentEvolvingCentroid, helpvec2);
								
								
								double newoffset = delta-helpdist2;
								
								Vector nextEvolvingCentroid = new Vector();
								nextEvolvingCentroid.add(currentPath.get(k-1).toString());
								nextEvolvingCentroid.add(currentPath.get(k).toString());		
								nextEvolvingCentroid.add(newoffset);
								
								
								currentEvolvingCentroid=nextEvolvingCentroid;
								centroidTrail.add(currentEvolvingCentroid);
								
								
								
								
							} // k>0
						
						
					} // helpdist >= delta
					
				} // limitkfound == false
					
					
				} //parse current path
				

				
			} // current centroid is not on a specific node (with offset)
			

			} // node distance < Double.Max
			
			}  // read distilled Text
			
			
		} // read distilled Text
			
			
		} // node exists	
			
			
			
		}	
			
			
		}  //for  read distilled Text
		
		/*
		Vector test1 =  new Vector();
		test1.add("Manipulation");
		test1.add("Abgas-Wert");
		test1.add(0.3);
		
		
		Vector test2 =  new Vector();
		test2.add("Merkel");
		test2.add("CDU");
		test2.add(0.7);
		
		Vector test2 =  new Vector();
		test2.add("Merkel");
		test2.add("Merkel");
		test2.add(0.0);
		
		
		System.out.println("Position Distance : " + getPositionDistance(test1, test2)); //Abgas-Wert
		*/
		
		//System.out.println("Distance : " + getNodeDistance("Manipulation", "Merkel")); //Abgas-Wert
		
		
		
		
		System.out.println("currentEvolvingCentroid: " + currentEvolvingCentroid.toString());
		

		System.out.println("CentroidTrail: " + centroidTrail.toString());
		
		
		return currentEvolvingCentroid;
	}
	
	
	public double getNodeDistance(String term1, String term2) {
		
		currentPath.clear();
		
		double distance=Double.MAX_VALUE;
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
		System.out.println("database opened for querying");
		try (Transaction tx = graphDB.beginTx()) 
	    {  
			
			Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", term1);
			
			if (temp!=null) {
				
				Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", term2);
				
				if (temp2!=null) {
		    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


					WeightedPath p = finder.findSinglePath( temp, temp2 );
			
					if (p!=null) {
						
						distance = p.weight();
						//p.length()
						
				//		System.out.println("Path weight: " + p.weight());;
					//	System.out.println("Path length: " + p.length());;
						
						//System.out.print("Path: ");;
						
						 for (Node n:p.nodes())
					        {
							 
							 currentPath.add(n.getProperty("name").toString()); 
							/* 
					        	if (n.getProperty("name").equals(term2)) {
					        	System.out.println(n.getProperty("name")+"");
					        	} else
					        	System.out.print(n.getProperty("name")+"->");
					       */ }
						
						
						
					}
					
						
				}
			}
			
			
			tx.success();
	    }
		graphDB.shutdown();
		
		
		return distance;
	}
	
	
	
public double getPositionDistance(Vector pos1, Vector pos2) {
		
		System.out.println("In getPositionDistance:");
	
		double distance=Double.MAX_VALUE;
		
		String termstart_pos1 = (String)pos1.get(0);
		String termend_pos1 = (String)pos1.get(1);
		double offset_pos1_start = (double)pos1.get(2);
		
		double distance_pos1 = Double.MAX_VALUE; 
		double offset_pos1_end = Double.MAX_VALUE; 
		
		if (!termend_pos1.equals("")) {
			distance_pos1 = getNodeDistance(termstart_pos1,termend_pos1);
			offset_pos1_end = distance_pos1-offset_pos1_start;
		}
		
		System.out.println("Start:	'"+ termstart_pos1 + "' '" +termend_pos1 + "' " + offset_pos1_start + " " + offset_pos1_end);
		
		
		String termstart_pos2 = (String)pos2.get(0);
		String termend_pos2 = (String)pos2.get(1);
		double offset_pos2_start = (double)pos2.get(2);
		
		double distance_pos2 = Double.MAX_VALUE; 
		double offset_pos2_end = Double.MAX_VALUE;

		if (!termend_pos2.equals("")) {
		 distance_pos2 = getNodeDistance(termstart_pos2,termend_pos2);
		 offset_pos2_end = distance_pos2-offset_pos2_start;
		}
		
		System.out.println("Start:	'"+ termstart_pos2 + "' " +termend_pos2 + "' " + offset_pos2_start + " " + offset_pos2_end);
		
		
		if ( termstart_pos1.equals(termend_pos1) &&  termstart_pos2.equals(termend_pos2) &&  (offset_pos1_start == 0.0)  && (offset_pos2_start==0.0)) {  //shortest node distance
		
			System.out.println("Case 1:	");
			
			double nodedistance = getNodeDistance(termstart_pos1, termstart_pos2);
			
			if (nodedistance<Double.MAX_VALUE) {
			
			distance = nodedistance;
			}
			
			System.out.println("CurrentPath	" + currentPath.toString());
			
			
		} else 
			if (termstart_pos1.equals(termstart_pos2) && termend_pos1.equals(termend_pos2)) {
				//this case should not occur for the evolving centroid calculations
				
				System.out.println("Case 2:	");
				
				distance = Math.abs(offset_pos1_start-offset_pos2_start);
				
				/* currentPath needed?
				currentPath.clear();
				
				currentPath.add(termstart_pos1);
				currentPath.add(termend_pos1);
				
				*/	
				
			} else 
				if (termstart_pos1.equals(termend_pos2) && termend_pos1.equals(termstart_pos2)) {
				//this case should not occur for the evolving centroid calculations
					
				System.out.println("Case 3:	");
				
				double nodedistance = getNodeDistance(termstart_pos1, termstart_pos2);
				
				if (nodedistance<Double.MAX_VALUE) {
					distance =  Math.abs(nodedistance - offset_pos1_start - offset_pos2_start);	
				}
				
				/* currentPath needed?
				currentPath.clear();
				
				currentPath.add(termstart_pos1);
				currentPath.add(termend_pos1);
				*/
				
				} else 
				{
					
					System.out.println("Case 4:	");
					
					double lowestdistance = Double.MAX_VALUE;
					int subcase = -1;
					
					double currentdistance = getNodeDistance(termstart_pos1, termstart_pos2);
					
					if (currentdistance<Double.MAX_VALUE) {
						double distancehelp1 = currentdistance + offset_pos1_start + offset_pos2_start;
						
						if (distancehelp1<lowestdistance) {
							
							lowestdistance=distancehelp1;
							subcase=1;
						}
					}
					
					
					currentdistance =  getNodeDistance(termstart_pos1, termend_pos2);
					
					if (currentdistance<Double.MAX_VALUE) {
						double distancehelp2 = currentdistance + offset_pos1_start + offset_pos2_end;
						
						if (distancehelp2<lowestdistance) {
							
							lowestdistance=distancehelp2;
							subcase=2;
						}
					}
					
					
					currentdistance = getNodeDistance(termend_pos1, termstart_pos2);
					
					if (currentdistance<Double.MAX_VALUE) {
						double distancehelp3 = currentdistance + offset_pos1_end + offset_pos2_start;
						
						if (distancehelp3<lowestdistance) {
							
							lowestdistance=distancehelp3;
							subcase=3;
						}	
					}
					
					
					currentdistance = getNodeDistance(termend_pos1, termend_pos2);
					
					if (currentdistance<Double.MAX_VALUE) {
						double distancehelp4 = currentdistance + offset_pos1_end + offset_pos2_end;
						
						if (distancehelp4<lowestdistance) {
							
							lowestdistance=distancehelp4;
							subcase=4;
						}	
					}
					
					
					//setting the shortest current Path correctly
					if (subcase==1)
						getNodeDistance(termstart_pos1, termstart_pos2);
					
					if (subcase==2)
						getNodeDistance(termstart_pos1, termend_pos2);
						
					if (subcase==3)
						getNodeDistance(termend_pos1, termstart_pos2);
					
					if (subcase==4)
						getNodeDistance(termend_pos1, termend_pos2);
					
					System.out.println("CurrentPath	" + currentPath.toString());
					
					
					distance = lowestdistance;
					System.out.println("Subcase: " + subcase);
					
					
				} // Case 4
					
		
		
		
		//letzter Fall noch
		
		
		return distance;
	}
	
	
	
	//the following 2 methods copy the code from the both methods above, but no currentPath is set

public double getNodeDistance2(String term1, String term2) {
	
	//currentPath.clear();
	
	double distance=Double.MAX_VALUE;
	
	String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
	File database = new File(db_path);
	
	GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
	System.out.println("database opened for querying");
	try (Transaction tx = graphDB.beginTx()) 
    {  
		
		Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", term1);
		
		if (temp!=null) {
			
			Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", term2);
			
			if (temp2!=null) {
	    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


				WeightedPath p = finder.findSinglePath( temp, temp2 );
		
				if (p!=null) {
					
					distance = p.weight();
					//p.length()
					
		//			System.out.println("Path weight: " + p.weight());;
			//		System.out.println("Path length: " + p.length());;
					
				//	System.out.print("Path: ");;
					
					 for (Node n:p.nodes())
				        {
						 
		//				 currentPath.add(n.getProperty("name").toString()); 
					/*	 
				        	if (n.getProperty("name").equals(term2)) {
				        	System.out.println(n.getProperty("name")+"");
				        	} else
				        	System.out.print(n.getProperty("name")+"->");
				      */  }
					
					
					
				}
				
					
			}
		}
		
		
		tx.success();
    }
	graphDB.shutdown();
	
	
	return distance;
}



public double getPositionDistance2(Vector pos1, Vector pos2) {
	
	System.out.println("In getPositionDistance2:");

	double distance=Double.MAX_VALUE;
	
	String termstart_pos1 = (String)pos1.get(0);
	String termend_pos1 = (String)pos1.get(1);
	double offset_pos1_start = (double)pos1.get(2);
	
	double distance_pos1 = Double.MAX_VALUE; 
	double offset_pos1_end = Double.MAX_VALUE; 
	
	if (!termend_pos1.equals("")) {
		distance_pos1 = getNodeDistance2(termstart_pos1,termend_pos1);
		offset_pos1_end = distance_pos1-offset_pos1_start;
	}
	
	System.out.println("Start:	'"+ termstart_pos1 + "' '" +termend_pos1 + "' " + offset_pos1_start + " " + offset_pos1_end);
	
	
	
	String termstart_pos2 = (String)pos2.get(0);
	String termend_pos2 = (String)pos2.get(1);
	double offset_pos2_start = (double)pos2.get(2);
	
	double distance_pos2 = Double.MAX_VALUE; 
	double offset_pos2_end = Double.MAX_VALUE;

	if (!termend_pos2.equals("")) {
	 distance_pos2 = getNodeDistance2(termstart_pos2,termend_pos2);
	 offset_pos2_end = distance_pos2-offset_pos2_start;
	}
	
	System.out.println("Start:	'"+ termstart_pos2 + "' " +termend_pos2 + "' " + offset_pos2_start + " " + offset_pos2_end);
	
	
	if ( termstart_pos1.equals(termend_pos1) &&  termstart_pos2.equals(termend_pos2) &&  (offset_pos1_start == 0.0)  && (offset_pos2_start==0.0)) {  //shortest node distance
	
		System.out.println("Case 1:	");
		
		distance = getNodeDistance2(termstart_pos1, termstart_pos2);
		
		System.out.println("CurrentPath	" + currentPath.toString());
		
		
	} else 
		if (termstart_pos1.equals(termstart_pos2) && termend_pos1.equals(termend_pos2)) {
			System.out.println("Case 2:	");
			
			distance = Math.abs(offset_pos1_start-offset_pos2_start);
		
			/*
			currentPath.clear();
			
			currentPath.add(termstart_pos1);
			currentPath.add(termend_pos1);
			*/
				
			
		} else 
			if (termstart_pos1.equals(termend_pos2) && termend_pos1.equals(termstart_pos2)) {
				
			System.out.println("Case 3:	");
			
			distance =  Math.abs(getNodeDistance2(termstart_pos1, termstart_pos2)- offset_pos1_start - offset_pos2_start);	
			
			/*
			currentPath.clear();
			
			currentPath.add(termstart_pos1);
			currentPath.add(termend_pos1);
			*/
			
			} else 
			{
				
				System.out.println("Case 4:	");
				
				double lowestdistance = Double.MAX_VALUE;
				int subcase = -1;
				
				double distancehelp1 = getNodeDistance2(termstart_pos1, termstart_pos2) + offset_pos1_start + offset_pos2_start;
				
				if (distancehelp1<lowestdistance) {
					
					lowestdistance=distancehelp1;
					subcase=1;
				}
				
				
				double distancehelp2 = getNodeDistance2(termstart_pos1, termend_pos2) + offset_pos1_start + offset_pos2_end;
				
				if (distancehelp2<lowestdistance) {
					
					lowestdistance=distancehelp2;
					subcase=2;
				}

				
				double distancehelp3 = getNodeDistance2(termend_pos1, termstart_pos2) + offset_pos1_end + offset_pos2_start;
				
				if (distancehelp3<lowestdistance) {
					
					lowestdistance=distancehelp3;
					subcase=3;
				}	
				
				
				
				double distancehelp4 = getNodeDistance2(termend_pos1, termend_pos2) + offset_pos1_end + offset_pos2_end;
				
				
				if (distancehelp4<lowestdistance) {
					
					lowestdistance=distancehelp4;
					subcase=4;
				}	
				
				/*
				if (subcase==1)
					getNodeDistance(termstart_pos1, termstart_pos2);
				
				if (subcase==2)
					getNodeDistance(termstart_pos1, termend_pos2);
					
				if (subcase==3)
					getNodeDistance(termend_pos1, termstart_pos2);
				
				if (subcase==4)
					getNodeDistance(termend_pos1, termend_pos2);
				
				System.out.println("CurrentPath	" + currentPath.toString());
				*/
				
				distance = lowestdistance;
				System.out.println("Subcase: " + subcase);
				
				
			} // Case 4
				
	
	
	
	//letzter Fall noch
	
	
	
	
	return distance;
}

	
	
	public HashMap getCentroidbySpreadingActivation(Vector query)
	{
		
		HashMap result = new HashMap();
		
		
		int originalquerysize = query.size();
		String centroid = "";
		double shortestaveragepathlength = Double.MAX_VALUE;
		double timeelapsed = 0.0;	
		
		HashMap centroidcandidatesdata = new HashMap();
		
		
		Vector termcolors = new Vector();
		HashMap node2colors = new HashMap();
		
		double arearadius = 10.0;
		
		Vector centroidcandidates = new Vector();
		
		String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
		File database = new File(db_path);
		
		if (!query.isEmpty()) {
			
			if (query.size()>1) {	
				
				GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
				System.out.println("database opened for querying");
				try (Transaction tx2 = graphDB.beginTx()) 
			    {  
				
				
				//query cleaning !!!!
					
					
					//Check which terms are in the graph database
					Vector helpquery = new Vector();
										
					for (int i=0; i<query.size(); i++) {
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
						
						if (temp!=null) {
							helpquery.add(query.get(i).toString());
						}
						
					}
					
					query = helpquery;
					System.out.println("Helpquery size: " +query.size() + "  "+query);;
					
					
					//Check if all query terms can be reached in the graph database from one another 
					//(remove one that cannot be reached by one or all of the other terms)
					//
					
					int helpquerysize = query.size();
					
					HashSet helpqueryset = new HashSet();
					HashSet helpqueryset2bremoved = new HashSet();
					
					Vector helpquery2 = new Vector();
					
					for (int i=0; i<query.size(); i++) {
						
						helpqueryset.add(query.get(i).toString());
						
					}
					
					
					HashMap numberofreachednodes = new HashMap();
					
						Iterator iteratorq1 = helpqueryset.iterator(); 

						   while (iteratorq1.hasNext()){
							   
							   String queryterm = iteratorq1.next().toString();
							   
							   Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm);
							   
							   
							   Iterator iteratorq2 = helpqueryset.iterator(); 

								   while (iteratorq2.hasNext()){
									   String queryterm2 = iteratorq2.next().toString(); 
									   
									   if (!queryterm.equals(queryterm2)) {
										   Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm2);
										   
										   PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), 100, 1 );

											Path p = finder.findSinglePath( temp, temp2 );
										
											if (p!=null) {
											
												if (numberofreachednodes.containsKey(queryterm)) {
													
													HashSet helpset = (HashSet)numberofreachednodes.get(queryterm);
													helpset.add(queryterm2);
													numberofreachednodes.put(queryterm, helpset);
													
												} else {
													
													HashSet helpset= new HashSet();
													helpset.add(queryterm2);
													numberofreachednodes.put(queryterm, helpset);
												}
												
												
												
											}
										   
									   }
									   
								   }
							   }
						   
						   System.out.println("Number of reached nodes: " +numberofreachednodes.toString());;
						   
						   String mostreachableterm = "";
						   int numberofneighbours = 0;
						   
						   Iterator iteratorq3 = numberofreachednodes.keySet().iterator();
						   while (iteratorq3.hasNext()){
							   String queryterm = iteratorq3.next().toString(); 
							   
							   HashSet helphashset = (HashSet) numberofreachednodes.get(queryterm);
							   
							   if (helphashset.size()>numberofneighbours) {
								   
								   numberofneighbours = helphashset.size();
								   mostreachableterm = queryterm;
							   }
						   }
						
						   HashSet helphashset = (HashSet) numberofreachednodes.get(mostreachableterm);
						   helphashset.add(mostreachableterm);
						   
						   
						   
						   
					
					/*
					String term2bremoved = "";
					
					for (int k=0; k<helpquerysize; k++) {

						if (!term2bremoved.equals("")) {
						
							helpqueryset.remove(term2bremoved);
							term2bremoved = "";
							
						}
						
						if (helpqueryset.size()>0) {
						
						Iterator iterator = helpqueryset.iterator(); 

						   while (iterator.hasNext()){
							   
							   String queryterm = iterator.next().toString();
							   
							   Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm);
							   
							   
							   Iterator iterator2 = helpqueryset.iterator(); 

								   while (iterator2.hasNext()){
									   String queryterm2 = iterator2.next().toString(); 
									   
									   if (!queryterm.equals(queryterm2)) {
										   Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", queryterm2);
										   
										   PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), 100, 1 );


											Path p = finder.findSinglePath( temp, temp2 );
										
											if (p==null) {
											
												term2bremoved=queryterm;
											}
										   
									   }
									   
								   }
							   }
							   
						   
							}
						   
						   
						   }*/
						   helpqueryset = helphashset;
						
					for (int i=0; i<query.size(); i++) {
						
						String helpterm = query.get(i).toString();
						
						if (helpqueryset.contains(helpterm)) {
							
							helpquery2.add(helpterm);
							
						}
						
					}
					
					
					query = helpquery2;
					System.out.println("Helpquery2 size: " +query.size() + "  "+query);;
					
				
					//hierher2
					if ((query.size()==query.size())  && (query.size()>1)) {
					
					
					double largestdistanceofqueryterms = 0;
					double largestpathlength = 0;
					
					for (int i=0; i<query.size(); i++) {
						
						for (int j=i+1; j<query.size(); j++) {
							
							Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(i).toString());
							Node temp2 = graphDB.findNode(Labels.SINGLE_NODE,"name", query.get(j).toString());
							

					    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


								WeightedPath p = finder.findSinglePath( temp, temp2 );
						
								if (p!=null)
									if (p.weight()>largestdistanceofqueryterms) {
										largestpathlength = p.length();
										largestdistanceofqueryterms=p.weight();
									}
							
						}
						
					}
					
					
					System.out.println("largestdistanceofqueryterms: " +largestdistanceofqueryterms + "  " +largestpathlength);
					
					arearadius = Math.ceil(largestdistanceofqueryterms / 2.0)+1; //
					
					
					double count=0; 
					
					
					long start = System.currentTimeMillis();	
					
					while ((centroidcandidates.size()<5) && (count<10)) {

					count++;
					System.out.println("Activation rounds to execute: " +count );
					
					
					if (count>2) 
					arearadius = arearadius + (arearadius / 2.0); 
					
					termcolors = new Vector();
					node2colors = new HashMap();	
					centroidcandidates = new Vector();
						
					int color = 0;
					
					for(Iterator i=query.iterator(); i.hasNext(); ){
						
						color++;
						termcolors.add(color);
											
						String curQueryTerm = i.next().toString();
						
						HashSet helpset = new HashSet();
						helpset.add(color);
						
						node2colors.put(curQueryTerm, helpset);
						//System.out.println("Query Term: " + curQueryTerm + " Color: " + color);
						
					}
				
				
				
				
				
				
				for(Iterator i=query.iterator(); i.hasNext(); ){
					
					
					String curQueryTerm = i.next().toString();
					
					HashSet visited = new HashSet();
					LinkedList<String> queue = new LinkedList<String>();
					
					visited.add(curQueryTerm);
					queue.add(curQueryTerm);
					
					Node firstsourcenode = graphDB.findNode(Labels.SINGLE_NODE,"name", curQueryTerm);
				
					int steps=0;
					
					while ((queue.size()!=0) && (steps<count)) {
						
						steps++;
						
						String sourcenodename = queue.poll();
					//	System.out.println("Activating: "+ sourcenodename);
												
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", sourcenodename);
						Iterable<Relationship> allRelationships = temp.getRelationships();
					    for (Relationship relationship : allRelationships) 
					    {
					       Node destinationnode=relationship.getOtherNode(temp);
					       
					       String destinationnodename =  destinationnode.getProperty("name").toString();
					       
					       if (!visited.contains(destinationnodename)) {
					    	   
					    	   /*
					    	   PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );


								WeightedPath p = finder.findSinglePath( firstsourcenode, destinationnode );
				
					    	   if (p!=null)
					    	   if ( p.weight() < arearadius) {
					    		   
					    		   visited.add(destinationnodename);
					    		   queue.add(destinationnodename);
					    		   
					    	   }*/
					    	   
					    	   visited.add(destinationnodename);
				    		   queue.add(destinationnodename);
					    	   
				    		   /*
					    	   int contains = 0;
					    	   
					    	   for (int querylauf = 0; querylauf<query.size(); querylauf++) {
					    		   
					    		   if (visited.contains(query.get(querylauf).toString())) {
					    			   
					    			   contains++;
					    			   
					    		   }
					    		   
					    	   }
					    	   
					    	   if (contains==query.size()) {
					    		   queue = new LinkedList<String>();					    		   
					    	   }*/
					    	   
					    	   
					    	   
					    	   
					       }
					       
					       
					       
					    }
						
						
						
					
						
						
					} //while queue
					
				
					
					
					//HashSet help = (HashSet)node2colors.get(curQueryTerm);
					
					Iterator iterator = visited.iterator(); 
				      
					   // check values
					   while (iterator.hasNext()){
						   
						   String nodename = iterator.next().toString();
						  // System.out.println("Value: "+ nodename+ " ");  
						   
						   if (node2colors.containsKey(nodename)) {
							   
							   HashSet helpset =  (HashSet)node2colors.get(nodename);
							   
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
							   
							   node2colors.put(nodename, helpset);
							   
						   } else {
							   
							   HashSet helpset = new HashSet();
							   helpset.add(termcolors.get(query.indexOf(curQueryTerm)));
	
							   node2colors.put(nodename, helpset);
							   
							   
						   }
					   
					   
					   }
					
					  // System.out.println("Value: "+ node2colors.get(curQueryTerm).toString()); 
					  // System.out.println("Value: "+ node2colors.get("Grenzwert").toString());
					
					
					
					
				} //for all query terms
				
				
				
				
				
				
				Iterator iterator = node2colors.keySet().iterator(); 
			      
				   // check values
				   while (iterator.hasNext()){
					   
					   String nodename = iterator.next().toString();
					   
					   if ((((HashSet)node2colors.get(nodename)).size()==termcolors.size()) /*&& (!helpqueryset.contains(nodename))*/) {
						   centroidcandidates.add(nodename);
					   }
					   
					   
				   }
				
				   System.out.println("Centroid candidates: " +centroidcandidates.size() + "  " + centroidcandidates.toString());
				

				} // centroidcandidates.size()<5
					
					long stop = System.currentTimeMillis();
					timeelapsed = ((double)(stop-start)/(double)1000);
							
					System.out.println("Centroid determination took "+timeelapsed+" seconds.");
					
					
					double averagepathlength = 0;
					
					
					
					
					
					for (int i=0; i<centroidcandidates.size(); i++) {
						
						String candidate = centroidcandidates.get(i).toString();
						averagepathlength=0;
						
						Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", candidate);
						
						for (int j=0; j<query.size(); j++) {
							
							String curQueryTerm = query.get(j).toString();
							
							Node n2 = graphDB.findNode(Labels.SINGLE_NODE,"name", curQueryTerm);
							
							if ((n1!=null) && (n2!=null)) {
								
								PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );
								   
								WeightedPath p = finder.findSinglePath(n1, n2);
								
								if (p!=null) {
									
									averagepathlength+=p.weight();
									
								}
							
							}
					
						}
						
						averagepathlength = averagepathlength / query.size();
						
						centroidcandidatesdata.put(candidate, averagepathlength);
						
						if (averagepathlength<shortestaveragepathlength) 
						{
							shortestaveragepathlength = averagepathlength;
							centroid = candidate;
						}
					
					} //for centroidcandidates
					
					
					
					
			    } // if querysize==
				   
				tx2.success();
			    }
				graphDB.shutdown();
				
				
			}
		}
		
		System.out.println("node2colors: " + node2colors.size() + "   " +centroid +  "   " + shortestaveragepathlength);
		
		result.put("centroid", centroid);
		result.put("shortestaveragepathlength", shortestaveragepathlength);
		result.put("activatednodes", node2colors.size());
		result.put("timeelapsed", timeelapsed);
		result.put("centroidcandidatesdata", centroidcandidatesdata);

		return result;
	}
	
	
	
	
	//returns the centroid of a query from the database
		public String findCentroid(Vector query)
		{
			
			String centroid = "";
			
			if (!query.isEmpty()) {
				
				if (query.size()>1) {	
					
					
				ArrayList<String> key=new ArrayList<String>();
				ArrayList<Integer> value=new ArrayList<Integer>();
				System.out.println("in findCentroid "  +query.toString());
				
				
				String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
				File database = new File(db_path);
				
				GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
				System.out.println("database opened for querying");
				
				double minavpathweight = Double.MAX_VALUE;
				maximumaddedpaths = 0;
				
				try (Transaction tx2 = graphDB.beginTx()) 
			    {  	

					long start = System.currentTimeMillis();	
					
					int nodenumber=0;
					ResourceIterator<Node> term1iterator = graphDB.findNodes( Labels.SINGLE_NODE );
					while(term1iterator.hasNext())
					{
						nodenumber++;
						//System.out.println("Current node " +nodenumber);
						
						Node term1 = term1iterator.next();
						String term1str = term1.getProperty("name").toString();
						
						//System.out.println("Node found: "+term1str);
						
						if (!query.contains(term1str)) {
						
							//System.out.println("term1str " +term1str);
							
						double curavpathweight=0;
						int curnumberofaddedpaths = 0;
						
						for(Iterator i=query.iterator(); i.hasNext(); ){
							
							String curQueryTerm = i.next().toString();
							
							if (curnumberofaddedpaths<maximumpathstoadd) {
							

								double pathweight = -1;
								//double pathweight = getShortestPathWeightInDatabase (term1str, curQueryTerm);
								
								//if (curQueryTerm.equals("Wasser")) term1str="Testterm";
								
								Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", term1str);
								Node n2 = graphDB.findNode(Labels.SINGLE_NODE,"name", curQueryTerm);
								
								//System.out.println("curQueryTerm  " +curQueryTerm);
								
								if ((n1!=null) && (n2!=null)) {
																
								//PathFinder<Path> finder = GraphAlgoFactory.shortestPath( Traversal.expanderForTypes( RelationshipTypes.IS_CONNECTED ), 20 );
							   
								PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );
								   
								WeightedPath p = finder.findSinglePath(n1, n2);
								
								if (p!=null) {
									
									pathweight=p.weight();
									
									//System.out.println("Pathweight " +pathweight);
								}
								
								/*								
								Iterable<WeightedPath> paths = finder.findAllPaths( n1, n2 );

							    for (WeightedPath p: paths) 
							    {
							    	pathweight=p.weight();
							    }*/
								
								//System.out.println("hier1");
								
								if (pathweight!=-1) { 
									curavpathweight +=pathweight; 
									curnumberofaddedpaths++;
								}
								//System.out.println("hier2");
							
							} //nodes exist
							
							} else {//curnumberofaddedpaths<maximumpathstoadd	
							
								//System.out.println("Fehler " + curnumberofaddedpaths +  "  " + maximumpathstoadd);
								
							}
								
						} //queryiterator
						
						if (curnumberofaddedpaths!=0) {
						curavpathweight /= curnumberofaddedpaths;
					
						
						if (maximumaddedpaths==curnumberofaddedpaths){
							maximumaddedpaths=curnumberofaddedpaths;

							if (curavpathweight<minavpathweight) {
								
								minavpathweight = curavpathweight;
								centroid = term1str;
								
							}
				      		  
				      	  } else

				      	 if (maximumaddedpaths<curnumberofaddedpaths){
				      	 
				      		maximumaddedpaths=curnumberofaddedpaths;
				      		 
				      		minavpathweight = curavpathweight;
							centroid = term1str;
							  
				      		  
				      	 }
						}
						
												
						
					}
						
					}
					System.out.println("Node number " + nodenumber + " Centroid: " +centroid);
					
					tx2.success();
					
					
//hierher3
					long stop = System.currentTimeMillis();	
					double timeelapsed = ((double)(stop-start)/(double)1000);
					
					System.out.println("Centroid determination took "+timeelapsed+" seconds.");
			    }
				
				
					
				graphDB.shutdown();
				} else {
					
					centroid = query.get(0).toString();
					
				}
			
			
			}
		
			return centroid;
			
		}
		
		//check if node exists
		public boolean checkNodeExists(String node) 
		{
			System.out.println("check if node exists");
			boolean nodeexists = false;
			
			String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
			File database = new File(db_path);
			
			GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
			System.out.println("database opened for querying");
			
			try (Transaction tx2 = graphDB.beginTx()) 
		    {  	
				//rel_found=false;
	    		Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", node);
				
				if ((n1!=null) ) {
				
				nodeexists=true;
				
		    	}
				
	    		tx2.success();
		    }
			graphDB.shutdown();
			return nodeexists;
		}
	
	
	// for getting and displaying all the properties of a specific node
		public static void getAllProperties(String n1)
		{
						
			boolean flag=false;
			int count;
			
			String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
			File database = new File(db_path);
			
			GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
			System.out.println("database opened for querying");
			try (Transaction tx2 = graphDB.beginTx()) 
		    {  	
				ResourceIterator<Node> nodelist = graphDB.findNodes( Labels.SINGLE_NODE );
				while( nodelist.hasNext() )
				{
					Node wordnode = nodelist.next();
					if(wordnode.getProperty( "name" ).equals(n1))
					{
						System.out.println("No. of occurrences of node " + wordnode.getProperty( "name" )+ " are " + wordnode.getProperty( "occur" ));
						flag=true;
						break;
					}
					
				}
				
				if(!flag)
				{
					System.out.println("Node with name "+n1+" does not exist.");
				}
				
				else
				{
				System.out.println("It is connected to all these nodes: ");
				Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", n1);
				Iterable<Relationship> allRelationships = temp.getRelationships();
				System.out.println("NAME"+"\t"+"COUNT"+"\t"+"DICE RATIO"+"\t"+"COST OF EDGE (DISTANCE)");
			    for (Relationship relationship : allRelationships) 
			    {
			        Node n2=relationship.getOtherNode(temp);
			        String dice=relationship.getProperty("dice").toString();
			        String cost=relationship.getProperty("cost").toString();
			       // System.out.println(n2.getProperty("name")+"\t"+relationship.getProperty("count")+"\t"+dice+"\t"+cost);
			        
			        System.out.println("Neighbour: " + n2.getProperty("name")+"	\t Co-occurrences: "+relationship.getProperty("count")+" \t Dice: "+dice);
			        
			        
			        
	   	
			    }
				}

				tx2.success();
		    }
			graphDB.shutdown();
		}
		
	
		
		// for getting neighbours of a specific node
				public HashMap getAllNeighbours(String n1)
				{
					HashMap result = new HashMap();
					
					boolean flag=false;
					int count;
					
					String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
					File database = new File(db_path);
					
					GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
					System.out.println("database opened for querying");
					try (Transaction tx2 = graphDB.beginTx()) 
				    {  	
						ResourceIterator<Node> nodelist = graphDB.findNodes( Labels.SINGLE_NODE );
						while( nodelist.hasNext() )
						{
							Node wordnode = nodelist.next();
							if(wordnode.getProperty( "name" ).equals(n1))
							{
								System.out.println("No. of occurrences of node " + wordnode.getProperty( "name" )+ ": " + wordnode.getProperty( "occur" ));
								flag=true;
								break;
							}
							
						}
						
						if(!flag)
						{
							System.out.println("Node with name "+n1+" does not exist.");
						}
						
						else
						{
						System.out.println("It is connected to all of these nodes: ");
						Node temp = graphDB.findNode(Labels.SINGLE_NODE,"name", n1);
						Iterable<Relationship> allRelationships = temp.getRelationships();
						System.out.println("NAME"+"\t"+"COUNT"+"\t"+"DICE RATIO"+"\t"+"COST OF EDGE (DISTANCE)");
					    for (Relationship relationship : allRelationships) 
					    {
					        Node n2=relationship.getOtherNode(temp);
					        String dice=relationship.getProperty("dice").toString();
					        String cost=relationship.getProperty("cost").toString();
					       System.out.println(n2.getProperty("name")+"\t"+relationship.getProperty("count")+"\t"+dice+"\t"+cost);
					        
					        Node startnode=relationship.getStartNode();
					        Node endnode = relationship.getEndNode();
					        
					        if (!startnode.getProperty("name").toString().equals(n1)) {
					        	
					        	//System.out.println("Variante 1");
						        
					        	
					        	int count2 = Integer.parseInt(relationship.getProperty("count").toString());		
					        	
					        	result.put(startnode.getProperty("name").toString(), count2);
					        	
					        } else {
					        	
					        	//System.out.println("Variante 2");
					        	
					        	int count2 = Integer.parseInt(relationship.getProperty("count").toString());		
					        	
					        	result.put(endnode.getProperty("name").toString(), count2);
					        	
					        	
					        }
					        
					        
					      //  System.out.println("Startnode: " +startnode.getProperty("name") + " ---> Endnode: " + endnode.getProperty("name"));
					        
					        
			   	
					    }
						}

						tx2.success();
				    }
					graphDB.shutdown();
					
					return result;
				}
		
		
		
		//implementation of dijkstra algorithm
		//takes 2 node names as input and finds the shortest path(s) between them
		//returns weight of shortest path (if there exists one)
		public double getShortestPathWeightInDatabase(String node1,String node2) 
		{
			
			double pathweight = -1;
			
			String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
			File database = new File(db_path);
			
			GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
			System.out.println("database opened for querying");
			
			try (Transaction tx2 = graphDB.beginTx()) 
		    {  	
				//rel_found=false;
	    		Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", node1);
				Node n2 = graphDB.findNode(Labels.SINGLE_NODE,"name", node2);
				
				System.out.println("N1 "+n1);
				System.out.println("N2 "+n2);
				
				if ((n1!=null) && (n2!=null)) {
				
				//PathFinder<Path> finder = GraphAlgoFactory.shortestPath( Traversal.expanderForTypes( RelationshipTypes.IS_CONNECTED ), 20 );
			   
				PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );

				//Iterable<WeightedPath> paths = finder.findAllPaths( n1, n2 );

				WeightedPath p = finder.findSinglePath( n1, n2 );
				
				System.out.println("Paths " +p);
				
			//	Iterator pathsiterator=paths.iterator(); 
					
			//	if (pathsiterator.hasNext())
			  //  for (WeightedPath p: paths) 
				if (p!=null)
			    {
			        System.out.println("Length of shortest path: "+p.length());
			        
			        System.out.println("Weight of shortest path: "+p.weight());
			       
			        pathweight=p.weight();
			        
			        for (Node n:p.nodes())
			        {
			        	if (n.getProperty("name").equals(node2)) {
			        	System.out.print(n.getProperty("name")+"");
			        	} else
			        	System.out.print(n.getProperty("name")+"->");
			        }
			        System.out.println();
			    }
				
		    	}
				
	    		tx2.success();
		    }
			graphDB.shutdown();
			return pathweight;
		}
		
	
		
				//implementation of dijkstra algorithm
				//takes 2 node names as input and finds the shortest path(s) between them
				//returns shortest path (if there exists one)
				public Vector getShortestPathInDatabase(String node1,String node2) 
				{
					
					Vector path = new Vector();
					double pathweight = -1;
					
					String db_path=System.getProperty("user.dir")+"/cooccsdatabase";
					File database = new File(db_path);
					
					GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(database);
					System.out.println("database opened for querying");
					
					try (Transaction tx2 = graphDB.beginTx()) 
				    {  	
						//rel_found=false;
			    		Node n1 = graphDB.findNode(Labels.SINGLE_NODE,"name", node1);
						Node n2 = graphDB.findNode(Labels.SINGLE_NODE,"name", node2);
						
						System.out.println("N1 "+n1);
						System.out.println("N2 "+n2);
						
						if ((n1!=null) && (n2!=null)) {
						
						//PathFinder<Path> finder = GraphAlgoFactory.shortestPath( Traversal.expanderForTypes( RelationshipTypes.IS_CONNECTED ), 20 );
					   
						PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra( PathExpanders.forTypeAndDirection( RelationshipTypes.IS_CONNECTED, Direction.BOTH ), "cost", 1 );

						//Iterable<WeightedPath> paths = finder.findAllPaths( n1, n2 );

						WeightedPath p = finder.findSinglePath( n1, n2 );
						
						System.out.println("Paths " +p);
						
					//	Iterator pathsiterator=paths.iterator(); 
							
					//	if (pathsiterator.hasNext())
					  //  for (WeightedPath p: paths) 
						if (p!=null)
					    {
					        System.out.println("Length of shortest path: "+p.length());
					        
					        System.out.println("Weight of shortest path: "+p.weight());
					       
					        pathweight=p.weight();
					        
					        for (Node n:p.nodes())
					        {
					        	if (n.getProperty("name").equals(node2)) {
					        	System.out.print(n.getProperty("name")+"");
					        	} else
					        	System.out.print(n.getProperty("name")+"->");
					        	
					        	path.add(n.getProperty("name"));
					        	
					        }
					        System.out.println();
					    }
						
				    	}
						
			    		tx2.success();
				    }
					graphDB.shutdown();
					return path;
				}
		
		
	
	
	public Map getCooccMap(){
		
		return cooccs;
		
	}
	
	public Map getFrequencies(){
		
		return frequencies;
		
	}
	
	public int getTermFrequency(String term){
		
		return ((Integer)frequencies.get(term)).intValue();	

	}
	
	
	
	
	// splits a query String by spaces and returns the items in a vector
		private static Set decompose(String query){
		
			Set ret = new HashSet();
			StringTokenizer st = new StringTokenizer(query, " ");
			while(st.hasMoreTokens()){
				ret.add(st.nextToken());
			}
			
			return ret;
		}
	
	
	public List getcombinedCooccsList(String query){
		
		List ret = new Vector();
		List sorted = new Vector();
		List retrieved = new Vector();
		List retrievedcombined = new Vector();
		HashSet wordseen = new HashSet();
		
		Set curTerms = decompose(query);
		int querysize = curTerms.size(); 
		
		for(Iterator j=curTerms.iterator(); j.hasNext(); ){
			
			try {
			String curTerm = (String)j.next();
			
			wordseen.add(curTerm);
			
			
			Map termCooccs = (Map)cooccs.get(curTerm);
			
			Set keys = termCooccs.keySet();
			for(Iterator i=keys.iterator(); i.hasNext(); ){
				String curStr = (String)i.next();
				
				float curSig = ((Float)termCooccs.get(curStr)).floatValue();
				int curFreq = ((Integer)frequencies.get(curStr)).intValue();
				
				
				Word curWord = new Word(curStr, curFreq);
				curWord.setSig(curSig);
				
				if (curSig>0)
				retrieved.add(curWord);
			} 
			
			} catch (Exception nullpo) {}

		}
		
		
		Collections.sort(retrieved);
		
		
		
		for (int i=0; i<retrieved.size(); i++) {
			
			
			Word curWord = (Word)retrieved.get(i);
			String curStr = curWord.getWordStr();
			float curWordSigCumul = 0; 
			int curWordcount = 0;
			
			if (!wordseen.contains(curStr)) {
			
			wordseen.add(curStr);
				
			for (int j=0; j<retrieved.size(); j++) {
				
				Word curWord2 = (Word)retrieved.get(j);
				String curStr2 = curWord2.getWordStr();
				
				if (curStr2.equals(curStr)) {
					curWordSigCumul+=curWord2.getSig();
					curWordcount++;
				}
				
				
			}
			

			curWord.setSig(curWordSigCumul/curWordcount);
			retrievedcombined.add(curWord);
			} 

		}
		

		Collections.sort(retrievedcombined);
		
	/*	for (int i=0; i<retrievedcombined.size(); i++)
			System.out.println(retrievedcombined.get(i).toString());
		*/
		
		
		int endIndex = 20;
		if(retrievedcombined.size() < (endIndex))endIndex = retrievedcombined.size();
		sorted.addAll(retrievedcombined.subList(0, endIndex));
		
		/*for(Iterator i=sorted.iterator(); i.hasNext(); ){
			Word curWord = (Word)i.next();
			String curStr = curWord.getWordStr();
			ret.add(curStr);
			
		}*/
		
		return sorted;
	}

	
	//expands a query
	public List expand(String query, int maxcandidates){
		
		List expanded = new Vector();
		
		List expansioncandidates = getcombinedCooccsList(query);
		
		
		if (expansioncandidates.size()<maxcandidates) maxcandidates=expansioncandidates.size();
		
		for (int i=0; i<maxcandidates; i++) {
			
			Word curWord = (Word)expansioncandidates.get(i);
			String curStr = curWord.getWordStr();
			
			expanded.add(query + " " +curStr);
			
		}
		
		return expanded;
	}
	
	
	//calculates the similarity between two terms based on their global contexts via DICE coefficient
	public double sim(String term1, String term2){
	
		
		List term1cooccs = getCooccsList(term1,10);
		List term2cooccs = getCooccsList(term2,10);
		
		double sim = 0.0;
		double commonterms = 0;
		
		if( term1cooccs.size() == 0 || term2cooccs.size() == 0 ) return sim;

        // prüfe die Länge der beiden Vektoren. iteriere über den Kürzeren der beiden
        if( term1cooccs.size() > term2cooccs.size() ) {
            List temp = term1cooccs;
            term1cooccs = term2cooccs;
            term2cooccs = temp;
        }
		
		
        for(int i=0; i<term1cooccs.size(); i++){
            String curTerm = ((Word) term1cooccs.get(i)).getWordStr();
            
            for(int j=0; j<term2cooccs.size(); j++){
            
            	String curTerm2 = ((Word) term2cooccs.get(j)).getWordStr();
            	
            	if(curTerm2.equals(curTerm))  {
            	commonterms++;
            	
            	}
            	
            }
        }
        
		
        sim =  ((2*commonterms)/(term1cooccs.size()+term2cooccs.size()));
        
		return sim;
	}
	
	
	
	
//	 gets co-occurrences for a given term as a List
	public List getCooccsList(String term, int demand){
	
		
		List sorted = new Vector();
		List retrieved = new Vector();
		
		try {
		Map termCooccs = (Map)cooccs.get(term);
		
		
		// iterate over all the keys and put the co-occurrences into a List:
		Set keys = termCooccs.keySet();
		for(Iterator i=keys.iterator(); i.hasNext(); ){

			String curStr = (String)i.next();
			float curSig = ((Float)termCooccs.get(curStr)).floatValue();
			int curFreq = ((Integer)frequencies.get(curStr)).intValue();
			Word curWord = new Word(curStr, curFreq);
			
			curWord.setSig(curSig);
			retrieved.add(curWord);
						
		} 
		
		} catch (Exception nullpo) { }
		
		Collections.sort(retrieved);
		
		
		int endIndex = demand;
		if(retrieved.size() < endIndex)endIndex = retrieved.size();
		sorted.addAll(retrieved.subList(0, endIndex));
		

		return sorted;
	}
	

	
	public void setLanguage(int languagevalue){	
		language=languagevalue;
	}

	
	
	private boolean isStopWord(String word){
		boolean isstopword = false;
		
		Parameters p = new Parameters(true); //Parameters.getInstance();
		ExternalData ed = ExternalData.getInstance( language  ); //p.getLanguage());
		Set stopWords = ed.getStopWordMap();
		
		char firstcharacter = word.charAt(0);
				
		
		if (Character.isUpperCase(firstcharacter)) {
			
			if(stopWords.contains(word)) {
				
				isstopword=true;
				
			} else {
				
				int length = word.length();
				String s2 = word.substring(0,1).toLowerCase().concat(word.substring(1, length));
				
				if(stopWords.contains(s2)) {

					isstopword=true;
				}
				
			}
			
		} else {
			
			if(stopWords.contains(word)) {
				
				isstopword=true;
			}
			
		}
	
				
		return isstopword;
	}
	
	
	// scans a list of words for stopwords and removes them. Returns the "cleansed" list...
	private Vector removeStopwords(List words){
	
		Vector ret = new Vector();
	
		Parameters p = new Parameters(true); //Parameters.getInstance();
		ExternalData ed = ExternalData.getInstance( language  ); //p.getLanguage());
		Set stopWords = ed.getStopWordMap();
		
		for(Iterator i=words.iterator(); i.hasNext(); ){
			String curWord = (String)i.next();
			if(!stopWords.contains(curWord)){ ret.add(curWord); }// else 
				//System.out.println("Wort removed");
		}
		
		return ret;
	}
	
	
	
	private Vector adapttoStopwords(List words){
		
		Vector ret = new Vector();
	
		Parameters p = new Parameters(true); //Parameters.getInstance();
		ExternalData ed = ExternalData.getInstance( language  ); //p.getLanguage());
		Set stopWords = ed.getStopWordMap();
		
		for(Iterator i=words.iterator(); i.hasNext(); ){
			String curWord = (String)i.next();
			
			
			char firstcharacter = curWord.charAt(0);
			boolean added=false;
			
			
			if (Character.isUpperCase(firstcharacter)) {
				
				if(stopWords.contains(curWord)) {
					ret.add(curWord);
					added=true;
					
				} else {
					
					int length = curWord.length();
					String s2 = curWord.substring(0,1).toLowerCase().concat(curWord.substring(1, length));
					
					if(stopWords.contains(s2)) {

						ret.add(s2);
						added=true;
					}
					
				}
				
			} else {
				
				if(stopWords.contains(curWord)) {
					ret.add(curWord);
					added=true;
				}
				
			}
		
			if (added==false) {
				ret.add(curWord);
			}
			
		}
		
		return ret;
	}
	
	
	// for each word of the given sentence, this method adds all the other words as
	// co-occurrences...
	// it also updates the frequency information for each word
	private void extractCooccs(Vector words){
	
		for(Iterator i=words.iterator(); i.hasNext(); ){
			
			String curStr = (String)i.next();
						
			// first update frequency:
			if(!frequencies.containsKey(curStr)){
				frequencies.put(curStr, new Integer(1));
			}
			else{
				int oldFreq = ((Integer)frequencies.get(curStr)).intValue();
				frequencies.put(curStr, new Integer(oldFreq+1));
			}
			
			// now update co-occurrence info:
			// first, we have to see whether this word has already occurred
			Map curCooccs;
			if(cooccs.containsKey(curStr)){
				curCooccs = (Map)cooccs.get(curStr);
			}
			
			else{
				curCooccs = new HashMap();
			}
			
			// now, we have a Map that contains other words and their
			// frequency of co-occurrence with curStr
			// we will try to update this map with all the other words occurring in the current sentence:
			for(Iterator j=words.iterator(); j.hasNext(); ){
					String curCoocc = (String)j.next();
					
					if(!curCoocc.equals(curStr)){
						if(!curCooccs.containsKey(curCoocc)){
							curCooccs.put(curCoocc, new Float(1.0));
						}
						else{
							float oldFreq = ((Float)curCooccs.get(curCoocc)).floatValue();
							curCooccs.put(curCoocc, new Float(oldFreq+1));
						}
					}
					
			}
			cooccs.put(curStr, curCooccs);
		}
		
		
		//System.out.println("Freq: " +frequencies.toString());
		//System.out.println("Cooccs: " +cooccs.toString());
		
	}
	
	
	
	// calculates the LL co-occurrence significances for each pair of words:
	private void calcSigsLL(){
	
		Set allTerms = cooccs.keySet();
		Parameters p2 = new Parameters(true); //Parameters.getInstance();
		
		float maxtermfrequency=0;
		float sum=0;
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
			
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			sum+=na;
			
			if (na>maxtermfrequency)
				maxtermfrequency=na;
			
		}
		
		System.out.println("Maxtermfreq: " + maxtermfrequency);
		
		float mittelwert = sum/allTerms.size();
		
		
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
		
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			Map curCooccs = (Map)cooccs.get(curTerm);
			Set curTerms = curCooccs.keySet();
			Map newCurCooccs = new HashMap();
			
			for(Iterator j=curTerms.iterator(); j.hasNext(); ){
				String curCoocc = (String)j.next();
				int nb = ((Integer)frequencies.get(curCoocc)).intValue();
				float k = ((Float)curCooccs.get(curCoocc)).floatValue();
			
				float sig = 0;

				sig=sig(k,na,nb);
			
					newCurCooccs.put(curCoocc, new Float(sig));
			
			}
			
			cooccs.put(curTerm, newCurCooccs);
		}
	}
	
	// calculates the co-occurrence significances (DICE) for each pair of words:
	private void calcSigsDICE(){
		
		Set allTerms = cooccs.keySet();
		Parameters p2 = new Parameters(true); //Parameters.getInstance();
		
		float maxtermfrequency=0;
		float sum=0;
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
			
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			sum+=na;
			
			if (na>maxtermfrequency)
				maxtermfrequency=na;
			
		}
		
		System.out.println("Maxtermfreq: " + maxtermfrequency);
		
		float mittelwert = sum/allTerms.size();
		
		
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
		
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			Map curCooccs = (Map)cooccs.get(curTerm);
			Set curTerms = curCooccs.keySet();
			Map newCurCooccs = new HashMap();
			
			for(Iterator j=curTerms.iterator(); j.hasNext(); ){
				String curCoocc = (String)j.next();
				int nb = ((Integer)frequencies.get(curCoocc)).intValue();
				float k = ((Float)curCooccs.get(curCoocc)).floatValue();
			
				float helpk=0;
				if (nb<=na) { helpk=nb; } else
					helpk=na;
				
				if (k>=helpk)
				k=helpk;
				
				float sig = sig=(2*k)/(na+nb);
			
					newCurCooccs.put(curCoocc, new Float(sig));
			
			}
			
			cooccs.put(curTerm, newCurCooccs);
		}
	}
	
	// calculates the association significances for each pair of words:
	private void calcAssociations(){
	
		Set allTerms = cooccs.keySet();
		Parameters p2 = new Parameters(true); //Parameters.getInstance();
		
		float maxtermfrequency=0;
		float sum=0;
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
			
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			sum+=na;
			
			if (na>maxtermfrequency)
				maxtermfrequency=na;
			
		}
		
		System.out.println("Maxtermfreq: " + maxtermfrequency);
		
		float mittelwert = sum/allTerms.size();
		
		
		for(Iterator i=allTerms.iterator(); i.hasNext(); ){
		
			// for term i, get frequency and then all co-occurring terms:	
			String curTerm = (String)i.next();
			int na = ((Integer)frequencies.get(curTerm)).intValue();
			
			Map curCooccs = (Map)cooccs.get(curTerm);
			Set curTerms = curCooccs.keySet();
			Map newCurCooccs = new HashMap();
			
			for(Iterator j=curTerms.iterator(); j.hasNext(); ){
				String curCoocc = (String)j.next();
				int nb = ((Integer)frequencies.get(curCoocc)).intValue();
				float k = ((Float)curCooccs.get(curCoocc)).floatValue();
			
				float helpk=0;
				if (nb<=na) { helpk=nb; } else
					helpk=na;
				
				if (k>=helpk)
				k=helpk;
				
				float sig=0;

				float helpfrequa = ((float)na/(float)maxtermfrequency);
				float siga = ((float)k/(float)na);
				
				float helpfrequb = ((float)nb/(float)maxtermfrequency);
				float sigb = ((float)k/(float)nb);
			
				if ((na>1) || (nb>1)) 	
				if (siga>=sigb) 	
					sig=((float)k/(float)na)* helpfrequa;
						
	
					newCurCooccs.put(curCoocc, new Float(sig));
			
			}
			
			cooccs.put(curTerm, newCurCooccs);
		}
	}

	// calculates the actual significance of the co-occurrences...
	private float sig(float k, int na, int nb){
  		float sig;

		sig = n_log_n(k) - n_log_n(na) - n_log_n(nb) + n_log_n(n)
		+ n_log_n(n - nb - na + k)
		+ n_log_n(na - k) + n_log_n(nb - k)
		- n_log_n(n - na) - n_log_n(n - nb);

  
  		return((n*k>na*nb) ? sig : 0.0f);
	}
	
	
	private float n_log_n(float n){
		if(n<=0) return 0;
		return ((Double)(n*Math.log(n))).floatValue();
	}
	
	private int getLanguage(File satzFile) {

  	  String alltext=""; String lineorig;
  	  
  	  try {
  	  FileInputStream fin =  new FileInputStream(satzFile);

			
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
						
			while ((lineorig = myInput.readLine()) != null){
		
				alltext = alltext + lineorig;
			
			}    	  
  	  
  	   myInput.close();
  	   fin.close();
  	  } catch (Exception ex) {}
			
			
		// get the lanikernel-object
		LanIKernel lk=null;
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
			req.setWordsToCheck(Math.max( (int) Math.sqrt(alltext.length()), 30));
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
		for (Iterator iter = tempmap.keySet().iterator(); iter.hasNext();) {
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

	
	private void writeLineToFile(String filename,
            String linetoWrite,
            boolean appendToFile) {

  	  		PrintWriter pw = null;

  	  		try {

  	  			if (appendToFile) {

  	  				//If the file already exists, start writing at the end of it.
  	  				pw = new PrintWriter(new FileWriter(filename, true));

  	  			}
  	  			else {

  	  				pw = new PrintWriter(new FileWriter(filename));
  	  				

  	  			}

  	  				pw.println(linetoWrite);

  	  			
  	  			pw.flush();

  	  		}
  	  		catch (IOException e) {
  	  			e.printStackTrace();
  	  		}	
  	  		finally {

  	  			//Close the PrintWriter
  	  			if (pw != null)
  	  				pw.close();

  	  		}
    }      
	
	private void cleanDir(File d){
  		
  		if(d.isDirectory()){
  			File[] files = d.listFiles();
  			for(int i=0; i<files.length; i++){
  				files[i].delete();
  			}
  		}
  	 }
	
}