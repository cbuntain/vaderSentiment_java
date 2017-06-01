package edu.umd.hcil.vader;

//  coding: utf-8
//  Author: C.J. Hutto 
//  Thanks to George Berry for reducing the time complexity from something like O(N^4) to O(N). 
//  Thanks to Ewan Klein and Pierpaolo Pantone for bringing VADER into NLTK. Those modifications were awesome. 
//  For license information, see LICENSE.TXT 
/*
If you use the VADER sentiment analysis tools, please cite:
Hutto, C.J. & Gilbert, E.E. (2014). VADER: A Parsimonious Rule-based Model for
Sentiment Analysis of Social Media Text. Eighth International Conference on
Weblogs and Social Media (ICWSM-14). Ann Arbor, MI, June 2014.
*/


import java.util.*;
import java.util.regex.*;

// Static methods## 



public class SentiText { // Line 144

	// Constants##

	//  for removing punctuation 
	private static Pattern REGEX_REMOVE_PUNCTUATION; // Line 32

    public static final List<String> PUNC_LIST;

	static {
        
        REGEX_REMOVE_PUNCTUATION = Pattern.compile("\\p{Punct}");

		PUNC_LIST = Arrays.asList(".", "!", "?", ",", ";", ":", "-", "'", "\"",
	"!!", "!!!", "??", "???", "?!?", "!?!", "?!?!", "!?!?"); // Line 34

	}
    
    public static boolean allcap_differential(List<String> words) { // Line 109
        /*
         Check whether just some words in the input are ALL CAPS
         :param list words: The words to inspect
         :returns: `True` if some but not all items in `words` are ALL CAPS
         */
        boolean is_different = false; // Line 115
        int allcap_words = 0; // Line 116
        
        for (String word : words) { // Line 117
            if (word.toUpperCase() == word) { // Line 118
                allcap_words+= 1; // Line 119
            }
        };
        
        int cap_differential = (words.size()-allcap_words); // Line 120
        
        if ((cap_differential>0)&&(cap_differential<words.size())) { // Line 121
            is_different = true; // Line 122
        }
        return is_different;
    }

    
    private String text;
    private boolean is_cap_diff;
    private List<String> words_and_emoticons;
    
    /*
	    Identify sentiment-relevant string-level properties of input text.
	    */
    public SentiText(String text) { // Line 148
        this.text = text; // Line 151
        this.words_and_emoticons = this._words_and_emoticons(); // Line 152
        //  doesn't separate words from\
        //  adjacent punctuation (keeps emoticons & contractions)
        this.is_cap_diff = allcap_differential(this.words_and_emoticons); // Line 155
    }

    /*
     * Contains at least one fully capitalized token (e.g., "TOKEN"), but
     * not all tokens are fully capitalized.
     */
    public boolean containsAllCapDifferential() {
        return is_cap_diff;
    }
    
    public List<String> _words_and_emoticons() { // Line 177
        /*
         Removes leading and trailing puncutation
         Leaves contractions and most emoticons
         Does not preserve punc-plus-letter emoticons (e.g. :D)
         */
        String[] wes = this.text.split(" "); // Line 183
        Map<String,String> words_punc_dict = this._words_plus_punc(); // Line 184
        
        for ( int i=0; i<wes.length; i++ ) {
            if ( words_punc_dict.containsKey(wes[i]) ) {
                wes[i] = words_punc_dict.get(wes[i]);
            }
        }

        return Arrays.asList(wes);
    }
    
    public Map<String,String> _words_plus_punc() { // Line 157
        /*
         Returns mapping of form:
         {
         'cat,': 'cat',
         ',cat': 'cat',
         }
         */
        String no_punc_text = REGEX_REMOVE_PUNCTUATION.matcher(this.text).replaceAll(""); // Line 165

        //  removes punctuation (but loses emoticons & contractions)
        String[] words_only = no_punc_text.split(" "); // Line 167
        //  remove singletons
        Set<String> words_only_set = new HashSet<>();
        for ( String word : words_only ) {
            if ( word.length() > 1 ) {
                words_only_set.add(word);
            }
        }
        
        Map<String, String> punctWords = new HashMap<>();
        for ( String word : words_only_set ) {
            for ( String punct : PUNC_LIST ) {
                punctWords.put(String.format("%s%s", punct, word), word);
                punctWords.put(String.format("%s%s", word, punct), word);
            }
        }
        
        return punctWords;
    }

}
