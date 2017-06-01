package edu.umd.hcil.vader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by cbuntain on 5/19/17.
 */
public class LexicalModifiers {

    // Constants## 

    //  (empirically derived mean sentiment intensity rating increase for booster words) 
    public static final double B_INCR = 0.293d; // Line 22
    public static final double B_DECR = -0.293d; // Line 23

    //  (empirically derived mean sentiment intensity rating increase for using 
    //  ALLCAPs to emphasize a word) 
    public static final double C_INCR = 0.733d; // Line 27

    public static final double N_SCALAR = -0.74d; // Line 29

    // Pre- and Post-"But"/"but" dampener
    public static final double PRE_BUT_SCALAR = 0.5d;
    public static final double POST_BUT_SCALAR = 1.5d;

    //  (empirically derived mean sentiment intensity rating increase for
    //  exclamation points)
    public static final double EXCLAIM_POINT_SCALAR = 0.292d;

    //  (empirically derived mean sentiment intensity rating increase for
    //  question marks)
    //  For <4 ?s
    public static final double QUESTION_MARK_SCALAR_LT4 = 0.18d;
    public static final double QUESTION_MARK_SCALAR = 0.96d;

    //  booster/dampener 'intensifiers' or 'degree adverbs' 
    //  http://en.wiktionary.org/wiki/Category:English_degree_adverbs 

    public static Map<String, Double> BOOSTER_DICT;
    public static Map<String, Double> SPECIAL_CASE_IDIOMS;

    public static final List<String> NEGATE;

    static {

        BOOSTER_DICT = new HashMap<String, Double>();
        BOOSTER_DICT.put("absolutely", B_INCR);
        BOOSTER_DICT.put("amazingly", B_INCR);
        BOOSTER_DICT.put("awfully", B_INCR);
        BOOSTER_DICT.put("completely", B_INCR);
        BOOSTER_DICT.put("considerably", B_INCR);
        BOOSTER_DICT.put("decidedly", B_INCR);
        BOOSTER_DICT.put("deeply", B_INCR);
        BOOSTER_DICT.put("effing", B_INCR);
        BOOSTER_DICT.put("enormously", B_INCR);
        BOOSTER_DICT.put("entirely", B_INCR);
        BOOSTER_DICT.put("especially", B_INCR);
        BOOSTER_DICT.put("exceptionally", B_INCR);
        BOOSTER_DICT.put("extremely", B_INCR);
        BOOSTER_DICT.put("fabulously", B_INCR);
        BOOSTER_DICT.put("flipping", B_INCR);
        BOOSTER_DICT.put("flippin", B_INCR);
        BOOSTER_DICT.put("fricking", B_INCR);
        BOOSTER_DICT.put("frickin", B_INCR);
        BOOSTER_DICT.put("frigging", B_INCR);
        BOOSTER_DICT.put("friggin", B_INCR);
        BOOSTER_DICT.put("fully", B_INCR);
        BOOSTER_DICT.put("fucking", B_INCR);
        BOOSTER_DICT.put("greatly", B_INCR);
        BOOSTER_DICT.put("hella", B_INCR);
        BOOSTER_DICT.put("highly", B_INCR);
        BOOSTER_DICT.put("hugely", B_INCR);
        BOOSTER_DICT.put("incredibly", B_INCR);
        BOOSTER_DICT.put("intensely", B_INCR);
        BOOSTER_DICT.put("majorly", B_INCR);
        BOOSTER_DICT.put("more", B_INCR);
        BOOSTER_DICT.put("most", B_INCR);
        BOOSTER_DICT.put("particularly", B_INCR);
        BOOSTER_DICT.put("purely", B_INCR);
        BOOSTER_DICT.put("quite", B_INCR);
        BOOSTER_DICT.put("really", B_INCR);
        BOOSTER_DICT.put("remarkably", B_INCR);
        BOOSTER_DICT.put("so", B_INCR);
        BOOSTER_DICT.put("substantially", B_INCR);
        BOOSTER_DICT.put("thoroughly", B_INCR);
        BOOSTER_DICT.put("totally", B_INCR);
        BOOSTER_DICT.put("tremendously", B_INCR);
        BOOSTER_DICT.put("uber", B_INCR);
        BOOSTER_DICT.put("unbelievably", B_INCR);
        BOOSTER_DICT.put("unusually", B_INCR);
        BOOSTER_DICT.put("utterly", B_INCR);
        BOOSTER_DICT.put("very", B_INCR);
        BOOSTER_DICT.put("almost", B_DECR);
        BOOSTER_DICT.put("barely", B_DECR);
        BOOSTER_DICT.put("hardly", B_DECR);
        BOOSTER_DICT.put("just enough", B_DECR);
        BOOSTER_DICT.put("kind of", B_DECR);
        BOOSTER_DICT.put("kinda", B_DECR);
        BOOSTER_DICT.put("kindof", B_DECR);
        BOOSTER_DICT.put("kind-of", B_DECR);
        BOOSTER_DICT.put("less", B_DECR);
        BOOSTER_DICT.put("little", B_DECR);
        BOOSTER_DICT.put("marginally", B_DECR);
        BOOSTER_DICT.put("occasionally", B_DECR);
        BOOSTER_DICT.put("partly", B_DECR);
        BOOSTER_DICT.put("scarcely", B_DECR);
        BOOSTER_DICT.put("slightly", B_DECR);
        BOOSTER_DICT.put("somewhat", B_DECR);
        BOOSTER_DICT.put("sort of", B_DECR);
        BOOSTER_DICT.put("sorta", B_DECR);
        BOOSTER_DICT.put("sortof", B_DECR);
        BOOSTER_DICT.put("sort-of", B_DECR);

        SPECIAL_CASE_IDIOMS = new HashMap<>();
        SPECIAL_CASE_IDIOMS.put("the shit", 3d);
        SPECIAL_CASE_IDIOMS.put("the bomb", 3d);
        SPECIAL_CASE_IDIOMS.put("bad ass", 1.5d);
        SPECIAL_CASE_IDIOMS.put("yeah right", -2d);
        SPECIAL_CASE_IDIOMS.put("cut the mustard", 2d);
        SPECIAL_CASE_IDIOMS.put("kiss of death", -1.5d);
        SPECIAL_CASE_IDIOMS.put("hand to mouth", -2d);

        NEGATE = Arrays.asList(
                "aint", "arent", "cannot", "cant", "couldnt", "darent", "didnt", "doesnt",
                "ain't", "aren't", "can't", "couldn't", "daren't", "didn't", "doesn't",
                "dont", "hadnt", "hasnt", "havent", "isnt", "mightnt", "mustnt", "neither",
                "don't", "hadn't", "hasn't", "haven't", "isn't", "mightn't", "mustn't",
                "neednt", "needn't", "never", "none", "nope", "nor", "not", "nothing", "nowhere",
                "oughtnt", "shant", "shouldnt", "uhuh", "wasnt", "werent",
                "oughtn't", "shan't", "shouldn't", "uh-uh", "wasn't", "weren't",
                "without", "wont", "wouldnt", "won't", "wouldn't", "rarely", "seldom", "despite"); // Line 36

    }
    
}
