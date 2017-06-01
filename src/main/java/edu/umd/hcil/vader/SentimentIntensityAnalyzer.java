package edu.umd.hcil.vader;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.umd.hcil.vader.LexicalModifiers.SPECIAL_CASE_IDIOMS;

public class SentimentIntensityAnalyzer { // Line 191

    public static void print(String x) {
        //System.out.println(x);
    }
    
    private InputStream lexicon_input_stream;
    private Map<String, Double> lexicon;

    public static SentimentIntensityAnalyzer getDefaultAnalyzer() {
        //Get file from resources folder
        ClassLoader classLoader = SentimentIntensityAnalyzer.class.getClassLoader();

        String lexFile = "lexica/vader_lexicon.txt";
        URL lexResourse = classLoader.getResource(lexFile);
        System.out.println("Using Lexicon: " + lexResourse.toString());

        return new SentimentIntensityAnalyzer(classLoader.getResourceAsStream(lexFile)); // Line 464
    }

    /*
     * Give a sentiment intensity score to sentences.
    */
    public SentimentIntensityAnalyzer(String lexicon_file) throws java.io.FileNotFoundException { // Line 195
        setLexiconSource(new FileInputStream(new File(lexicon_file)));
        buildLexicon();
    }

    /*
     * Give a sentiment intensity score to sentences.
    */
    public SentimentIntensityAnalyzer(InputStream source) { // Line 195
        setLexiconSource(source);
        buildLexicon();
    }

    public void setLexiconSource(InputStream source) {

        this.lexicon_input_stream = source;
    }
    
    private List<String> readLexiconFile() {

        BufferedReader inReader = new BufferedReader(new InputStreamReader(this.lexicon_input_stream));
        Stream<String> lexLines = inReader.lines();

        return lexLines.collect(Collectors.toList());
    }

    public void buildLexicon() {
        List<String> lexicon_file_lines = readLexiconFile();
        this.lexicon = this.make_lex_dict(lexicon_file_lines); // Line 200
    }


    private Map<String, Double> make_lex_dict(List<String> lexicon_file_lines) {
        
        //Convert lexicon file to a dictionary
        
        Map<String, Double> lex_dict = new HashMap<>(); // Line 206
        
        for (String line : lexicon_file_lines) { // Line 207
            String[] lineArray = line.trim().split("\t");
            String word = lineArray[0];
            Double measure = Double.parseDouble(lineArray[1]);

            lex_dict.put(word, measure); // Line 209
        };
        
        return lex_dict;
    }
    
    public Map<String, Double> polarity_scores(String text) { // Line 212
        /*
         Return a float for sentiment strength based on the input text.
         Positive values are positive valence, negative value are negative
         valence.
         */
        SentiText sentitext = new SentiText(text); // Line 218
        print(sentitext._words_and_emoticons().toString());

        // text, words_and_emoticons, is_cap_diff = self.preprocess(text)

        List<Double> sentiments = new ArrayList<>(); // Line 221
        List<String> words_and_emoticons = sentitext._words_and_emoticons(); // Line 222

        for (int i=0; i< words_and_emoticons.size(); i++) { // Line 223
            String item = words_and_emoticons.get(i);

            // Check if in phrase "kind of" and skip if so
            if (i < words_and_emoticons.size() - 1 && item.equalsIgnoreCase("kind")) {
                if ( words_and_emoticons.get(i+1).equalsIgnoreCase("of") ) {
                    sentiments.add(0d);
                    continue;
                }
            }

            // Check if current word is a booster word and skip if so
            if ( LexicalModifiers.BOOSTER_DICT.containsKey(item.toLowerCase()) ) {
                sentiments.add(0d);
                continue;
            }

            // Else, calculate valence of this item(?)
            sentiments = this.sentiment_valence(sentitext, item, i, sentiments); // Line 232
        };

        _but_check(words_and_emoticons, sentiments); // Line 234

        Map<String, Double> valence_dict = this.score_valence(sentiments, text); // Line 236

        return valence_dict;
    }

    /*
     * Convenience method for checking if the given string is all uppercase
     */
    public boolean isUpperCase(String testStr) {
        return testStr.equals(testStr.toUpperCase());
    }
    
    public List<Double> sentiment_valence(SentiText sentitext, String item, int i, List<Double> sentiments) {

        // Does the string contain both ALL CAPS and not-All-caps tokens?
        boolean is_cap_diff = sentitext.containsAllCapDifferential(); // Line 241

        // Get the words and emoticons in the text
        List<String> words_and_emoticons = sentitext._words_and_emoticons(); // Line 242

        String item_lowercase = item.toLowerCase(); // Line 243

        // Default valence value = 0
        Double valence = 0d;

        // Check if the Vader lexicon contains the current item
        if (this.lexicon.containsKey(item_lowercase)) { // Line 244

            // get the sentiment valence
            valence = this.lexicon.get(item_lowercase); // Line 246

            // check if sentiment laden word is in ALL CAPS (while others aren't)
            if ( isUpperCase(item) && is_cap_diff ) { // Line 249
                if (valence>0) { // Line 250
                    valence += LexicalModifiers.C_INCR; // Line 251
                } else {
                    valence -= LexicalModifiers.C_INCR; // Line 253
                }
            }

            for (int start_i=0; start_i<3; start_i++) { // Line 255

                // Skip if sliding start_i spaces before i would be before the start of the string
                if ( i<=start_i ) {
                    continue;
                }

                // Get the prior word
                String priorWord = words_and_emoticons.get((i-(start_i+1)));

                if ( !this.lexicon.containsKey(priorWord.toLowerCase()) ) { // Line 256

                    //  dampen the scalar modifier of preceding words and emoticons
                    //  (excluding the ones that immediately preceed the item) based
                    //  on their distance from the current item.
                    Double s = scalar_inc_dec(priorWord, valence, is_cap_diff); // Line 260

                    if ((start_i == 1)&&(s != 0)) { // Line 261
                        s = (s*0.95); // Line 262
                    }
                    if ((start_i == 2)&&(s != 0)) { // Line 263
                        s = (s*0.9); // Line 264
                    }

                    // Mod valence with dampened modifier
                    valence = valence+s; // Line 265

                    valence = this._never_check(valence, words_and_emoticons, start_i, i); // Line 266

                    if (start_i == 2) { // Line 267
                        valence = this._idioms_check(valence, words_and_emoticons, i); // Line 268

                        //  future work: consider other sentiment-laden idioms
                        //  other_idioms =
                        //  {"back handed": -2, "blow smoke": -2, "blowing smoke": -2,
                        //   "upper hand": 1, "break a leg": 2,
                        //   "cooking with gas": 2, "in the black": 2, "in the red": -2,
                        //   "on the ball": 2,"under the weather": -2}
                    }
                }
            };

            valence = this._least_check(valence, words_and_emoticons, i); // Line 277
        }

        sentiments.add(valence);
        return sentiments;
    }
    
    public double _least_check(double valence, List<String> words_and_emoticons, int i) {

        //  check for negation case using "least"
        if ( i > 0 ) {
            String priorWord = words_and_emoticons.get((i-1)).toLowerCase();

            if ( i > 1 ) {
                String prePriorWord = words_and_emoticons.get((i - 2)).toLowerCase();

                if (!this.lexicon.containsKey(priorWord) && priorWord.equals("least")) {
                    if (!prePriorWord.equals("at") && !prePriorWord.equals("very")) {
                        valence = (valence * LexicalModifiers.N_SCALAR); // Line 287
                    }
                }
            } else {
                if (!this.lexicon.containsKey(priorWord) && priorWord.equals("least")) {
                    valence = (valence * LexicalModifiers.N_SCALAR); // Line 290
                }
            }
        }

        return valence;
    }
    
    public List<Double> _but_check(List<String> words_and_emoticons, List<Double> sentiments) {

        // Check for modification in sentiment due to contrastive conjunction
        for ( int i=0; i<words_and_emoticons.size(); i++ ) {
            String currentToken = words_and_emoticons.get(i);

            if ( currentToken.equals("But") || currentToken.equals("but") ) {

                // Modify the sentiment values before the "But" instance
                for ( int j=0; j<i; j++ ) {
                    Double dampened = sentiments.get(j) * LexicalModifiers.PRE_BUT_SCALAR;
                    sentiments.set(j, dampened);
                }

                // Modify the sentiment values after the "But" instance
                for ( int j=i; j<sentiments.size(); j++ ) {
                    Double dampened = sentiments.get(j) * LexicalModifiers.POST_BUT_SCALAR;
                    sentiments.set(j, dampened);
                }
            }
        }

        return sentiments;
    }
    
    public double _idioms_check(double valence, List<String> words_and_emoticons, int i) {
        String onezero = String.format("%s %s",
                words_and_emoticons.get((i-1)),
                words_and_emoticons.get(i)).toLowerCase(); // Line 311

        String twoonezero = String.format("%s %s %s",
                words_and_emoticons.get((i-2)),
                words_and_emoticons.get((i-1)),
                words_and_emoticons.get(i).toLowerCase()); // Line 313

        String twoone = String.format("%s %s",
                words_and_emoticons.get((i-2)),
                words_and_emoticons.get((i-1))).toLowerCase(); // Line 316

        String threetwoone = String.format("%s %s %s",
                words_and_emoticons.get((i-3)),
                words_and_emoticons.get((i-2)),
                words_and_emoticons.get((i-1))).toLowerCase(); // Line 318

        String threetwo = String.format("%s %s",
                words_and_emoticons.get((i-3)),
                words_and_emoticons.get((i-2))).toLowerCase(); // Line 321

        List<String> sequences = Arrays.asList(new String[] {onezero, twoonezero, twoone, threetwoone, threetwo}); // Line 323

        for (String seq : sequences) { // Line 325
            if (SPECIAL_CASE_IDIOMS.containsKey(seq)) { // Line 326
                valence = SPECIAL_CASE_IDIOMS.get(seq); // Line 327
                break;
            }
        };


        if ((words_and_emoticons.size()-1)>i) { // Line 330
            String zeroone = String.format("%s %s",
                    words_and_emoticons.get(i),
                    words_and_emoticons.get((i+1))).toLowerCase(); // Line 331
            if (SPECIAL_CASE_IDIOMS.containsKey(zeroone)) { // Line 332
                valence = SPECIAL_CASE_IDIOMS.get(zeroone); // Line 333
            }
        }
        if ((words_and_emoticons.size()-1)>(i+1)) { // Line 334
            String zeroonetwo = String.format("%s %s %s",
                    words_and_emoticons.get(i),
                    words_and_emoticons.get((i+1)),
                    words_and_emoticons.get((i+2))).toLowerCase(); // Line 335
            if (SPECIAL_CASE_IDIOMS.containsKey(zeroonetwo)) { // Line 336
                valence = SPECIAL_CASE_IDIOMS.get(zeroonetwo); // Line 337
            }
        }

        //  check for booster/dampener bi-grams such as 'sort of' or 'kind of'
        if ((LexicalModifiers.BOOSTER_DICT.containsKey(threetwo))
                || (LexicalModifiers.BOOSTER_DICT.containsKey(twoone))) { // Line 340
            valence = (valence+LexicalModifiers.B_DECR); // Line 341
        }
        return valence;
    }

    public double _never_check(double valence, List<String> words_and_emoticons, int start_i, int i) { // Line 344
            if (start_i == 0) { // Line 345
                if (negated(words_and_emoticons.get((i-1)), false)) { // Line 346
                    valence = (valence*LexicalModifiers.N_SCALAR); // Line 347
                }
            }

            if (start_i == 1) { // Line 348
                if ( (words_and_emoticons.get((i-2)).equalsIgnoreCase("never") &&
                      words_and_emoticons.get((i-1)).equalsIgnoreCase("so"))

                        ||

                     (words_and_emoticons.get((i-1)).equalsIgnoreCase("this"))
                   ) {

                    valence = (valence*1.5); // Line 352

                } else {
                    if (negated(words_and_emoticons.get((i-(start_i+1))), false)) { // Line 353
                        valence = (valence*LexicalModifiers.N_SCALAR); // Line 354
                    }
                }
            }
            if (start_i == 2) { // Line 355
                if (
                    ((words_and_emoticons.get((i-3)).equals("never"))&&
                        ((words_and_emoticons.get((i-2)).equals("so")) ||
                         (words_and_emoticons.get((i-2)).equals("this")))
                    )
                        ||
                    ((words_and_emoticons.get((i-1)).equals("so")) ||
                            (words_and_emoticons.get((i-1)).equals("this")))
                   ) { // Line 356
                    valence = (valence*1.25); // Line 359
                } else {
                    if (negated(words_and_emoticons.get((i-(start_i+1))), false)) { // Line 360
                        valence = (valence*LexicalModifiers.N_SCALAR); // Line 361
                    }
                }
            }
            return valence;
        }
    
    public double _punctuation_emphasis(double sum_s, String text) {

        //  add emphasis from exclamation points and question marks
        Double ep_amplifier = this._amplify_ep(text); // Line 366
        Double qm_amplifier = this._amplify_qm(text); // Line 367
        Double punct_emph_amplifier = (ep_amplifier+qm_amplifier); // Line 368

        return punct_emph_amplifier;
    }

    public Double _amplify_ep(String text) {

        //  check for added emphasis resulting from exclamation points (up to 4 of them)
        int ep_count = text.replaceAll("[^!]", "").length();
        if (ep_count>4) { // Line 374
            ep_count = 4; // Line 375
        }

        Double ep_amplifier = (ep_count*LexicalModifiers.EXCLAIM_POINT_SCALAR); // Line 378
        return ep_amplifier;
    }

    public Double _amplify_qm(String text) {
        //  check for added emphasis resulting from question marks (2 or 3+)
        int qm_count = text.replaceAll("[^?]", "").length();

        Double qm_amplifier = 0d; // Line 384
        if (qm_count>1) { // Line 385
            if (qm_count<=3) { // Line 386

                qm_amplifier = (qm_count*LexicalModifiers.QUESTION_MARK_SCALAR_LT4); // Line 389
            } else {
                qm_amplifier = LexicalModifiers.QUESTION_MARK_SCALAR; // Line 391
            }
        }
        return qm_amplifier;
    }

    public class SiftScores {
        Double posSum;
        Double negSum;
        Integer neuCount;

        public SiftScores(Double pos, Double neg, Integer neu) {
            posSum = pos;
            negSum = neg;
            neuCount = neu;
        }

        public Double getPositiveSum() { return posSum; }

        public Double getNegativeSum() { return negSum; }

        public Integer getNeutralCount() { return neuCount; }
    }
    
    public SiftScores _sift_sentiment_scores(List<Double> sentiments) {

        //  want separate positive versus negative sentiment scores
        Double pos_sum = 0.0; // Line 396
        Double neg_sum = 0.0; // Line 397
        Integer neu_count = 0; // Line 398

        for (Double sentiment_score : sentiments) { // Line 399
            if (sentiment_score>0) { // Line 400
                pos_sum+= (sentiment_score+1); // compensates for neutral words that are counted as 1 // Line 401
            }
            if (sentiment_score<0) { // Line 402
                neg_sum+= (sentiment_score-1); // when used with math.fabs(), compensates for neutrals // Line 403
            }
            if (sentiment_score == 0) { // Line 404
                neu_count+= 1; // Line 405
            }
        };
        return new SiftScores(pos_sum, neg_sum, neu_count);
    }
    
    public Map<String, Double> score_valence(List<Double> sentiments, String text) { // Line 408

        Map<String, Double> sentiment_dict = new HashMap<>();
        sentiment_dict.put("neg", 0d);
        sentiment_dict.put("neu", 0d);
        sentiment_dict.put("pos", 0d);
        sentiment_dict.put("compound", 0d);

        if (sentiments.size() > 0) { // Line 409
            Double sum_s = sentiments.stream().reduce((l, r) -> l+r).orElse(0d); // Line 410

            //  compute and add emphasis from punctuation in text
            Double punct_emph_amplifier = this._punctuation_emphasis(sum_s, text); // Line 412
            if (sum_s>0) { // Line 413
                sum_s += punct_emph_amplifier; // Line 414
            } else if (sum_s<0) { // Line 415
                sum_s -= punct_emph_amplifier; // Line 416
            }

            Double compound = normalize(sum_s, 15); // Line 418

            //  discriminate between positive, negative and neutral sentiment scores
            SiftScores scores = this._sift_sentiment_scores(sentiments); // Line 420
            Double pos_sum = scores.getPositiveSum();
            Double neg_sum = scores.getNegativeSum();
            Integer neu_count = scores.getNeutralCount();

            if (pos_sum>Math.abs(neg_sum)) { // Line 422
                pos_sum+= punct_emph_amplifier; // Line 423
            } else {
                if (pos_sum<Math.abs(neg_sum)) { // Line 424
                    neg_sum-= punct_emph_amplifier; // Line 425
                }
            }

            Double total = ((pos_sum+Math.abs(neg_sum))+neu_count); // Line 427
            Double pos = Math.abs((pos_sum/total)); // Line 428
            Double neg = Math.abs((neg_sum/total)); // Line 429
            Double neu = Math.abs((neu_count/total)); // Line 430

            sentiment_dict.put("neg", neg);
            sentiment_dict.put("neu", neu);
            sentiment_dict.put("pos", pos);
            sentiment_dict.put("compound", compound);
        }

        return sentiment_dict;
    }



    public static boolean negated(String input_word, boolean include_nt) { // Line 75
		/*
	    Determine if input contains negation words
	    */
        for (String negWord : LexicalModifiers.NEGATE) { // Line 81
            if (input_word.equalsIgnoreCase(negWord)) { // Line 82
                return true;
            }
        };

        if (include_nt) { // Line 84
            if (input_word.toLowerCase().contains("n't")) { // Line 86
                return true;
            }
        }

        return false;
    }

    public static double normalize(double score, int alpha) { // Line 95
        /*
         Normalize the score to be between -1 and 1 using an alpha that
         approximates the max expected value
         */
        double norm_score = (score/Math.sqrt(((score*score)+alpha))); // Line 100
        if (norm_score<-1.0) { // Line 101
            return -1.0d;
        } else {
            if (norm_score>1.0) { // Line 103
                return 1.0d;
            } else {
                return norm_score;
            }
        }
    }


    public static double scalar_inc_dec(String word, double valence, boolean is_cap_diff) { // Line 126
        /*
         Check if the preceding words increase, decrease, or negate/nullify the
         valence
         */
        double scalar = 0.0d; // Line 131
        String word_lower = word.toLowerCase(); // Line 132

        if (LexicalModifiers.BOOSTER_DICT.containsKey(word_lower)) { // Line 133
            scalar = LexicalModifiers.BOOSTER_DICT.get(word_lower); // Line 134
            if (valence<0) { // Line 135
                scalar*= -1; // Line 136
            }
            // check if booster/dampener word is in ALLCAPS (while others aren't)
            if ((word.toUpperCase() == word)&&(is_cap_diff)) { // Line 138
                if (valence>0) { // Line 139
                    scalar+= LexicalModifiers.C_INCR; // Line 140
                } else {
                    scalar-= LexicalModifiers.C_INCR; // Line 141
                }
            }
        }
        return scalar;
    }
}
