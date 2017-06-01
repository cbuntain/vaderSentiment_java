package edu.umd.hcil.vader;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Default {
    public static void main(String[] argv) throws Exception {
        new Default().runTest();
    }

    public Default() {

    }

    public void runTest() throws FileNotFoundException {
        //  --- examples -------
        List<String> sentences = Arrays.asList(new String[] {
                "VADER is smart, handsome, and funny.",  // positive sentence example // Line 448
                "VADER is not smart, handsome, nor funny.",  // negation sentence example // Line 449
                "VADER is smart, handsome, and funny!",  // punctuation emphasis handled correctly (sentiment intensity adjusted) // Line 450
                "VADER is very smart, handsome, and funny.",  // booster words handled correctly (sentiment intensity adjusted) // Line 451
                "VADER is VERY SMART, handsome, and FUNNY.",  // emphasis for ALLCAPS handled // Line 452
                "VADER is VERY SMART, handsome, and FUNNY!!!",  // combination of signals - VADER appropriately adjusts intensity // Line 453
                "VADER is VERY SMART, uber handsome, and FRIGGIN FUNNY!!!",  // booster words & punctuation make this close to ceiling for score // Line 454
                "The book was good.",  // positive sentence // Line 455
                "The book was kind of good.",  // qualified positive sentence is handled correctly (intensity adjusted) // Line 456
                "The plot was good, but the characters are uncompelling and the dialog is not great.",  // mixed negation sentence // Line 457
                "At least it isn't a horrible book.",  // negated negative sentence with contraction // Line 458
                "Make sure you :) or :D today",  // emoticons handled // Line 459
                "Make sure you :) or :D today!",  // emoticons handled // Line 459
                "Today SUX!",  //  negative slang with capitalization emphasis // Line 460
                "Today only kinda sux! But I'll get by, lol"
        }); // Line 448 // mixed sentiment example with slang and constrastive conjunction "but" // Line 461

        //Get file from resources folder
//        ClassLoader classLoader = this.getClass().getClassLoader();
//
//        String lexFile = "lexica/vader_lexicon.txt";
//        URL lexResourse = classLoader.getResource(lexFile);
//        System.out.println("Using Lexicon: " + lexResourse.toString());

//        SentimentIntensityAnalyzer analyzer = new SentimentIntensityAnalyzer(classLoader.getResourceAsStream(lexFile)); // Line 464

        SentimentIntensityAnalyzer analyzer = SentimentIntensityAnalyzer.getDefaultAnalyzer();

        System.out.println("----------------------------------------------------");
        System.out.println(" - Analyze typical example cases, including handling of:");
        System.out.println("  -- negations");
        System.out.println("  -- punctuation emphasis & punctuation flooding");
        System.out.println("  -- word-shape as emphasis (capitalization difference)");
        System.out.println("  -- degree modifiers (intensifiers such as 'very' and dampeners such as 'kind of')");
        System.out.println("  -- slang words as modifiers such as 'uber' or 'friggin' or 'kinda'");
        System.out.println("  -- contrastive conjunction 'but' indicating a shift in sentiment; sentiment of later text is dominant");
        System.out.println("  -- use of contractions as negations");
        System.out.println("  -- sentiment laden emoticons such as :) and :D");
        System.out.println("  -- sentiment laden slang words (e.g., 'sux')");
        System.out.println("  -- sentiment laden initialisms and acronyms (for example: 'lol')");

        for (String sentence:sentences) { // Line 478
            Map<String, Double> vs = analyzer.polarity_scores(sentence); // Line 479
            System.out.println(String.format("[%s]\n\t%s", sentence, vs.toString()));
        }

        System.out.println("----------------------------------------------------");
        System.out.println(" - About the scoring: ");
        System.out.println("  -- The 'compound' score is computed by summing the valence scores of each " +
                "word in the lexicon, adjusted according to the rules, and then normalized to be between " +
                "-1 (most extreme negative) and +1 (most extreme positive). This is the most useful " +
                "metric if you want a single unidimensional measure of sentiment for a given sentence. " +
                "Calling it a 'normalized, weighted composite score' is accurate.");

        System.out.println("  -- The 'pos', 'neu', and 'neg' scores are ratios for proportions of text " +
                "that fall in each category (so these should all add up to be 1... or close to it with " +
                "float operation).  These are the most useful metrics if you want multidimensional measures " +
                "of sentiment for a given sentence.");
        System.out.println("----------------------------------------------------");

        System.out.println("Press Enter to continue the demo...\n");
        try {
            int x = System.in.read();
        } catch (Exception e) {

        }

        List<String> tricky_sentences = Arrays.asList(new String[]{
                "Sentiment analysis has never been good.",
                "Sentiment analysis has never been this good!",
                "Most automated sentiment analysis tools are shit.",
                "With VADER, sentiment analysis is the shit!",
                "Other sentiment analysis tools can be quite bad.",
                "On the other hand, VADER is quite bad ass!",
                "Roger Dodger is one of the most compelling variations on this theme.",
                "Roger Dodger is one of the least compelling variations on this theme.",
                "Roger Dodger is at least compelling as a variation on the theme."
        }); // Line 494

        System.out.println("----------------------------------------------------");
        System.out.println(" - Analyze examples of tricky sentences that cause trouble to other sentiment analysis tools.");
        System.out.println("  -- special case idioms - e.g., 'never good' vs 'never this good', or 'bad' vs 'bad ass'.");
        System.out.println("  -- special uses of 'least' as negation versus comparison");
        for (String sentence:tricky_sentences) { // Line 508
            Map<String, Double> vs = analyzer.polarity_scores(sentence); // Line 509
            System.out.println(String.format("[%s]\n\t%s", sentence, vs.toString()));
        }
        System.out.println("----------------------------------------------------");

//        System.out.println("Press Enter to continue the demo...\n");
//        try {
//            int x = System.in.read();
//        } catch (Exception e) {
//
//        }
//
//        System.out.println("----------------------------------------------------");
//        System.out.println(" - VADER works best when analysis is done at the sentence level " +
//                "(but it can work on single words or entire novels).");
//
//        String paragraph = "It was one of the worst movies I've seen, despite good reviews. Unbelievably bad acting!! Poor direction. VERY poor production. The movie was bad. Very bad movie. VERY BAD movie!"; // Line 517
//        System.out.println("  -- For example, given the following paragraph text from a hypothetical movie review:" +
//            "'{}'".format(paragraph));
//        System.out.println("  -- You could use NLTK to break the paragraph into sentence tokens for VADER, " +
//                "then average the results for the paragraph like this:");
        //  simple example to tokenize paragraph into sentences for VADER

//        sentence_list = tokenize.sent_tokenize(paragraph); // Line 522
//        paragraphSentiments = 0.0; // Line 523
//        for (sentence:sentence_list) { // Line 524
//            vs = analyzer.polarity_scores(sentence); // Line 525
//            System.out.println("{:-<69} {}".format(sentence, str(vs.get("compound"))));
//            paragraphSentiments+= vs.get("compound"); // Line 527
//        };
//        System.out.println(("AVERAGE SENTIMENT FOR PARAGRAPH: 	"+str(round((paragraphSentiments/len(sentence_list)), 4))));
//        System.out.println("----------------------------------------------------");
//
//        raw_input(" // for DEMO purposes... // Line 531
//        Press Enter to continue the demo...
//        ");
//
//        System.out.println("----------------------------------------------------");
//        System.out.println(" - Analyze sentiment of IMAGES/VIDEO data based on annotation 'tags' or image labels.
//        ");
//        conceptList = Arrays.asList({"balloons", "cake", "candles", "happy birthday", "friends", "laughing", "smiling", "party"}); // Line 535
//        conceptSentiments = 0.0; // Line 536
//        for (concept:conceptList) { // Line 537
//            vs = analyzer.polarity_scores(concept); // Line 538
//            System.out.println("{:-<15} {}".format(concept, str(vs.get("compound"))));
//            conceptSentiments+= vs.get("compound"); // Line 540
//        };
//        System.out.println(("AVERAGE SENTIMENT OF TAGS/LABELS: 	"+str(round((conceptSentiments/len(conceptList)), 4))));
//        System.out.println("	");
//        conceptList = Arrays.asList({"riot", "fire", "fight", "blood", "mob", "war", "police", "tear gas"}); // Line 543
//        conceptSentiments = 0.0; // Line 544
//        for (concept:conceptList) { // Line 545
//            vs = analyzer.polarity_scores(concept); // Line 546
//            System.out.println("{:-<15} {}".format(concept, str(vs.get("compound"))));
//            conceptSentiments+= vs.get("compound"); // Line 548
//        };
//        System.out.println(("AVERAGE SENTIMENT OF TAGS/LABELS: 	"+str(round((conceptSentiments/len(conceptList)), 4))));
//        System.out.println("----------------------------------------------------");
//        /*
//        Press Enter to continue the demo...*/ // for DEMO purposes... // Line 552
//
//        do_translate = raw_input("
//        Would you like to run VADER demo examples with NON-ENGLISH text? (Note: requires Internet access)
//         Type 'y' or 'n', then press Enter: "); // Line 554
//        if (do_translate.lower().lstrip().equals("y")) { // Line 555
//            System.out.println("/n----------------------------------------------------");
//            System.out.println(" - Analyze sentiment of NON ENGLISH text...for example:");
//            System.out.println("  -- French, German, Spanish, Italian, Russian, Japanese, Arabic, Chinese");
//            System.out.println("  -- many other languages supported.
//            ");
//            languages = Arrays.asList({"English", "French", "German", "Spanish", "Italian", "Russian", "Japanese", "Arabic", "Chinese"}); // Line 560
//            language_codes = Arrays.asList({"en", "fr", "de", "es", "it", "ru", "ja", "ar", "zh"}); // Line 561
//            nonEnglish_sentences = Arrays.asList({"I'm surprised to see just how amazingly helpful VADER is!",
//            "Je suis surpris de voir juste comment incroyablement utile VADER est!",
//            "Ich bin überrascht zu sehen, nur wie erstaunlich nützlich VADER!",
//            "Me sorprende ver sólo cómo increíblemente útil VADER!",
//            "Sono sorpreso di vedere solo come incredibilmente utile VADER è!",
//            "Я удивлен увидеть, как раз как удивительно полезно ВЕЙДЕРА!",
//            "私はちょうどどのように驚くほど役に立つベイダーを見て驚いています!",
//            "أنا مندهش لرؤية فقط كيف مثير للدهشة فيدر فائدة!",
//            "惊讶地看到有用维德是的只是如何令人惊讶了 ！"}); // Line 562
//
//            for (sentence:nonEnglish_sentences) { // Line 572
//                to_lang = "en"; // Line 573
//                from_lang = language_codes.get(nonEnglish_sentences.index(sentence)); // Line 574
//                if ((from_lang.equals("en"))||(from_lang.equals("en-US"))) { // Line 575
//                    translation = sentence; // Line 576
//                    translator_name = "No translation needed"; // Line 577
//                } else {
//                     // please note usage limits for My Memory Translation Service:   http://mymemory.translated.net/doc/usagelimits.php // Line 578
//                    //  using   MY MEMORY NET   http://mymemory.translated.net
//                    api_url = "http://mymemory.translated.net/api/get?q={}&langpair={}|{}".format(sentence, from_lang, to_lang); // Line 580
//                    hdrs;
//                    //User-Agent
//                    //Accept
//                    //Accept-Charset
//                    //Accept-Encoding
//                    //Accept-Language
//                    //Connection
//                    //Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11
//                    //text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//                    //ISO-8859-1,utf-8;q=0.7,*;q=0.3
//                    //none
//                    "en-US,en;q=0.8" = "keep-alive"; // Line 581
//                    response = requests.get(api_url); // Line 587
//                    response_json = json.loads(response.text); // Line 588
//                    translation = response_json.get("responseData").get("translatedText"); // Line 589
//                    translator_name = "MemoryNet Translation Service"; // Line 590
//                }
//                vs = analyzer.polarity_scores(translation); // Line 591
//                System.out.println("- {: <8}: {: <69}	 {} ({})".format(languages.get(nonEnglish_sentences.index(sentence)), sentence, str(vs.get("compound")), translator_name));
//            };
//            System.out.println("----------------------------------------------------");
//        }

        System.out.println("Demo Done!");
    }
}