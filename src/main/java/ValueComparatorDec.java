import java.io.*;
import java.util.*;

class ValueComparatorDec implements Comparator<String>{
    	  
    		HashMap<String, Double> hmap = new HashMap<String, Double>();
    	 
    		public ValueComparatorDec(HashMap<String, Double> map){
    			
    			for (String key : map.keySet()) {
    				this.hmap.put(key, map.get(key));
    			}
    			
    			//this.map.putAll(map);
    		}
    	 
    		@Override
    		public int compare(String s1, String s2) {
    			if(hmap.get(s1).doubleValue() >= hmap.get(s2).doubleValue()){
    				return -1;
    			}else{
    				return 1;
    			}	
    		}
    	}